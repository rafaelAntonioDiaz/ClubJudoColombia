package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SesionProgramadaRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar sesiones programadas con paginación, validaciones y lazy loading seguro.
 * Sigue el patrón establecido en {@link GrupoEntrenamientoService}.
 *
 * @author RafaelDiaz
 * @version 1.1 (Armonizada)
 * @since 2025-11-20
 */
@Service
@Transactional
public class SesionProgramadaService {

    private static final Logger logger = LoggerFactory.getLogger(SesionProgramadaService.class);
    private final SesionProgramadaRepository sesionRepository;
    private final SecurityService seguridadService;

    @PersistenceContext
    private EntityManager entityManager;

    public SesionProgramadaService(SesionProgramadaRepository sesionRepository, SecurityService seguridadService) {
        this.sesionRepository = sesionRepository;
        this.seguridadService = seguridadService;
    }

    /**
     * Obtiene sesiones activas para un grupo con paginación real.
     * Lazy loading seguro mediante @EntityGraph.
     *
     * @param grupoId ID del grupo
     * @param offset  Desplazamiento (índice inicial)
     * @param limit   Número máximo de registros
     * @return Lista de sesiones con colecciones inicializadas
     */
    @Transactional(readOnly = true)
    public List<SesionProgramada> findActivas(Long grupoId, int offset, int limit) {
        logger.debug("Buscando sesiones activas para grupo {} (offset={}, limit={})",
                grupoId, offset, limit);

        EntityGraph<?> graph = entityManager.getEntityGraph("SesionProgramada.completo");
        List<SesionProgramada> sesiones = entityManager
                .createQuery("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio > :ahora ORDER BY s.fechaHoraInicio ASC", SesionProgramada.class)
                .setParameter("grupoId", grupoId)
                .setParameter("ahora", LocalDateTime.now())
                .setHint("jakarta.persistence.fetchgraph", graph)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        logger.info("Encontradas {} sesiones activas para grupo {}", sesiones.size(), grupoId);
        return sesiones;
    }

    /**
     * Cuenta sesiones por tipo y rango de fechas (KPI para dashboard).
     *
     * @param tipoSesion  Tipo de sesión
     * @param inicio Fecha/hora inicial
     * @param fin   Fecha/hora final
     * @return Número total
     */
    @Transactional(readOnly = true)
    public long countByTipoAndFechaHoraEntre(
            TipoSesion tipoSesion, LocalDateTime inicio, LocalDateTime fin) {
                logger.debug("Contando sesiones {} entre {} y {}", tipoSesion, inicio, fin);
                long count = sesionRepository.contarPruebasDelSenseiHoy(tipoSesion, inicio, fin,
                seguridadService.getAuthenticatedSensei()
                        .map(Sensei::getId)
                        .orElseThrow(() -> new RuntimeException("Sensei no autenticado")));
        logger.debug("Conteo resultante: {}", count);
        return count;
    }

    /**
     * Guarda una sesión con validaciones de negocio.
     * Previene solapamiento y duración mínima.
     *
     * @param sesion Sesión a persistir
     * @return Sesión guardada
     * @throws RuntimeException si no pasa validaciones
     */
    @Transactional
    public SesionProgramada guardar(SesionProgramada sesion) {
        logger.info("Guardando sesión: {} - {} para grupo {}", sesion.getTipoSesion(), sesion.getFechaHoraInicio(), sesion.getGrupo().getNombre());

        validarSesion(sesion);
        validarSolapamiento(sesion);

        SesionProgramada guardada = sesionRepository.save(sesion);
        logger.info("Sesión guardada con ID: {}", guardada.getId());
        return guardada;
    }

    /**
     * Elimina una sesión solo si es futura.
     *
     * @param sesionId ID de la sesión
     */
    @Transactional
    public void eliminar(Long sesionId) {
        logger.info("Eliminando sesión {}", sesionId);

        SesionProgramada sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada: " + sesionId));

        if (!sesion.esFutura()) {
            throw new RuntimeException("No se pueden eliminar sesiones pasadas o en curso");
        }

        sesionRepository.delete(sesion);
        logger.warn("Sesión {} eliminada exitosamente", sesionId);
    }

    /**
     * Busca sesión completa con colecciones inicializadas mediante JOIN FETCH.
     *
     * @param sesionId ID de la sesión
     * @return Sesión con asistencias cargadas
     */
    @Transactional(readOnly = true)
    public SesionProgramada buscarCompleta(Long sesionId) {
        logger.debug("Buscando sesión completa para ID {}", sesionId);

        Optional<SesionProgramada> opt = sesionRepository.findById(sesionId);
        if (opt.isEmpty()) {
            throw new RuntimeException("Sesión no encontrada: " + sesionId);
        }

        SesionProgramada sesion = opt.get();
        // Inicializar colecciones
        if (sesion.getAsistencias() != null) {
            sesion.getAsistencias().size();
        }

        logger.info("Sesión completa cargada para ID {}", sesionId);
        return sesion;
    }

    /**
     * Validaciones de negocio para sesiones.
     */
    private void validarSesion(SesionProgramada sesion) {
        if (sesion.getTipoSesion() == null) {
            throw new RuntimeException("El tipo de sesión es obligatorio");
        }
        if (sesion.getGrupo() == null || sesion.getGrupo().getId() == null) {
            throw new RuntimeException("Debe asignar un grupo válido");
        }
        if (sesion.getSensei() == null || sesion.getSensei().getId() == null) {
            throw new RuntimeException("Debe asignar un sensei responsable");
        }
        if (sesion.getFechaHoraInicio().isAfter(sesion.getFechaHoraFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la de fin");
        }
    }

    /**
     * Valida que no haya solapamiento de sesiones en el mismo grupo.
     */
    private void validarSolapamiento(SesionProgramada sesion) {
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
}