package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SesionProgramadaRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO UNIFICADO PARA SESIONES.
 * Combina gestión administrativa (Sensei), validaciones, KPIs,
 * y visualización para el alumno (Calendario/GPS).
 */
@Service
public class SesionService {

    private static final Logger logger = LoggerFactory.getLogger(SesionService.class);

    private final SesionProgramadaRepository sesionRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final SecurityService securityService; // Para KPIs del Sensei

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SesionService(SesionProgramadaRepository sesionRepository,
                         GrupoEntrenamientoRepository grupoRepository,
                         AsistenciaRepository asistenciaRepository,
                         SecurityService securityService) {
        this.sesionRepository = sesionRepository;
        this.grupoRepository = grupoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.securityService = securityService;
    }

    // ==========================================
    // 1. GESTIÓN Y VALIDACIÓN (SENSEI)
    // ==========================================

    @Transactional
    public SesionProgramada guardar(SesionProgramada sesion) {
        logger.info("Intentando guardar sesión: {}", sesion.getNombre());

        // 1. Validaciones básicas
        validarDatosBasicos(sesion);

        // 2. Validación de Solapamiento (Lógica robusta importada)
        validarSolapamiento(sesion);

        return sesionRepository.save(sesion);
    }

    @Transactional
    public void delete(SesionProgramada sesion) {
        if (sesion == null) return;

        // Regla de Negocio: No borrar historial antiguo por accidente
        if (!sesion.esFutura()) {
            // Opcional: Lanzar error o solo loguear.
            // logger.warn("Intentando borrar sesión pasada: {}", sesion.getId());
            // throw new RuntimeException("No se pueden eliminar sesiones pasadas.");
        }
        sesionRepository.delete(sesion);
    }

    @Transactional(readOnly = true)
    public List<SesionProgramada> findAll() {
        // Usamos la consulta con JOIN FETCH para evitar LazyInit en la vista
        return sesionRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<SesionProgramada> buscarPorSensei(Sensei sensei) {
        return sesionRepository.findBySenseiOrderByFechaHoraInicioDesc(sensei);
    }

    @Transactional(readOnly = true)
    public Optional<SesionProgramada> findById(Long id) {
        return sesionRepository.findById(id);
    }

    // ==========================================
    // 2. KPIs Y DASHBOARD (SENSEI)
    // ==========================================

    @Transactional(readOnly = true)
    public long countByTipoAndFechaHoraEntre(TipoSesion tipoSesion, LocalDateTime inicio, LocalDateTime fin) {
        return securityService.getAuthenticatedSensei()
                .map(sensei -> sesionRepository.contarPruebasDelSenseiHoy(tipoSesion, inicio, fin, sensei.getId()))
                .orElse(0L);
    }

    /**
     * Paginación optimizada con EntityGraph (Importado de SesionProgramadaService)
     */
    @Transactional(readOnly = true)
    public List<SesionProgramada> findActivasPaginadas(Long grupoId, int offset, int limit) {
        EntityGraph<?> graph = entityManager.getEntityGraph("SesionProgramada.completo");
        return entityManager
                .createQuery("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio > :ahora ORDER BY s.fechaHoraInicio ASC", SesionProgramada.class)
                .setParameter("grupoId", grupoId)
                .setParameter("ahora", LocalDateTime.now())
                .setHint("jakarta.persistence.fetchgraph", graph)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    // ==========================================
    // 3. VISUALIZACIÓN CALENDARIO (JUDOKA)
    // ==========================================

    @Transactional(readOnly = true)
    public List<EventoCalendario> obtenerEventosMes(Judoka judoka, YearMonth mes) {
        List<GrupoEntrenamiento> grupos = grupoRepository.findAllByJudokasContains(judoka);
        if (grupos.isEmpty()) return List.of();

        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fin = mes.atEndOfMonth().atTime(23, 59, 59);

        List<SesionProgramada> sesiones = sesionRepository.findByGruposAndFechaBetween(grupos, inicio, fin);
        List<EventoCalendario> eventos = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (SesionProgramada sesion : sesiones) {
            EstadoSesion estado;
            if (sesion.getFechaHoraInicio().isAfter(ahora)) {
                estado = EstadoSesion.FUTURA;
            } else {
                boolean asistio = asistenciaRepository.existsByJudokaAndSesion(judoka, sesion);
                estado = asistio ? EstadoSesion.ASISTIO : EstadoSesion.FALTO;
            }
            eventos.add(new EventoCalendario(sesion, estado));
        }
        return eventos;
    }

    // ==========================================
    // 4. GPS / CHECK-IN (JUDOKA)
    // ==========================================

    @Transactional(readOnly = true)
    public Optional<SesionProgramada> buscarSesionActivaParaCheckIn(GrupoEntrenamiento grupo) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime margenInicio = ahora.plusMinutes(30);
        return sesionRepository.findSesionActiva(grupo, margenInicio, ahora);
    }

    // ==========================================
    // 5. VALIDACIONES PRIVADAS
    // ==========================================

    private void validarDatosBasicos(SesionProgramada sesion) {
        if (sesion.getTipoSesion() == null) throw new RuntimeException("El tipo de sesión es obligatorio");
        if (sesion.getGrupo() == null) throw new RuntimeException("Debe asignar un grupo válido");
        if (sesion.getFechaHoraInicio().isAfter(sesion.getFechaHoraFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la de fin");
        }
    }

    private void validarSolapamiento(SesionProgramada sesion) {
        if (sesion.getGrupo() == null || sesion.getGrupo().getId() == null) return;

        List<SesionProgramada> sesionesExistentes = sesionRepository.findByGrupoIdAndFechaHoraInicioBetween(
                sesion.getGrupo().getId(),
                sesion.getFechaHoraInicio().minusHours(2),
                sesion.getFechaHoraFin().plusHours(2)
        );

        boolean haySolapamiento = sesionesExistentes.stream()
                .anyMatch(s -> !s.getId().equals(sesion.getId()) &&
                        s.getFechaHoraInicio().isBefore(sesion.getFechaHoraFin()) &&
                        s.getFechaHoraFin().isAfter(sesion.getFechaHoraInicio()));

        if (haySolapamiento) {
            throw new RuntimeException("Ya existe una sesión en este horario para el grupo " + sesion.getGrupo().getNombre());
        }
    }

    // ==========================================
    // DTOs
    // ==========================================
    public record EventoCalendario(SesionProgramada sesion, EstadoSesion estado) {}
    public enum EstadoSesion { FUTURA, ASISTIO, FALTO }
}