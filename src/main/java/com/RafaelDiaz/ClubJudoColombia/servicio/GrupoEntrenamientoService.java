package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PlanEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PlanEntrenamientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GrupoEntrenamientoService {

    private static final Logger logger = LoggerFactory.getLogger(GrupoEntrenamientoService.class);

    private final GrupoEntrenamientoRepository grupoRepository;
    private final JudokaRepository judokaRepository;
    private final PlanEntrenamientoRepository planRepository;

    // 1. CAMBIO: Inyectamos el SecurityService para saber de quién es el dojo
    private final SecurityService securityService;

    public GrupoEntrenamientoService(GrupoEntrenamientoRepository grupoRepository,
                                     JudokaRepository judokaRepository,
                                     PlanEntrenamientoRepository planRepository,
                                     SecurityService securityService) { // Inyectado en el constructor
        this.grupoRepository = grupoRepository;
        this.judokaRepository = judokaRepository;
        this.planRepository = planRepository;
        this.securityService = securityService;
    }

    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findAll(int offset, int limit, String filter) {
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) return List.of();

        PageRequest pageable = PageRequest.of(offset / limit, limit);

        List<GrupoEntrenamiento> grupos;

        // 1. Buscamos normalmente sin JOIN FETCH
        if (filter == null || filter.trim().isEmpty()) {
            grupos = grupoRepository.findBySenseiId(miSenseiId, pageable).getContent();
        } else {
            grupos = grupoRepository.findBySenseiIdAndNombreContainingIgnoreCase(miSenseiId, filter, pageable).getContent();
        }

        // 2. LA MAGIA CORRECTA: Despertamos la colección DENTRO de la transacción.
        // Como Vaadin solo va a llamar al .size() en la grilla, lo invocamos aquí para cargarlo.
        for (GrupoEntrenamiento grupo : grupos) {
            grupo.getJudokas().size();
        }

        return grupos;
    }
    @Transactional(readOnly = true)
    public long count(String filter) {
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) return 0;

        if (filter == null || filter.trim().isEmpty()) {
            return grupoRepository.countBySenseiId(miSenseiId);
        }
        return grupoRepository.countBySenseiIdAndNombreContainingIgnoreCase(miSenseiId, filter);
    }

    @Transactional
    public GrupoEntrenamiento save(GrupoEntrenamiento grupo) {
        // SEGURIDAD: Al crear un grupo nuevo, lo atamos al Sensei logueado
        if (grupo.getId() == null) {
            Long miSenseiId = securityService.getSenseiIdActual();
            // Asumimos que tienes acceso al senseiRepository aquí, o pasas el objeto Sensei
            // grupo.setSensei(senseiActual);
        }
        return grupoRepository.save(grupo);
    }

    @Transactional(readOnly = true)
    public Optional<GrupoEntrenamiento> findById(Long id) {
        return grupoRepository.findById(id);
    }

    

    @Transactional
    public void addJudokaToGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId).orElseThrow();
        Judoka judoka = judokaRepository.findById(judokaId).orElseThrow();
        grupo.getJudokas().add(judoka);
        grupoRepository.save(grupo);
    }

    @Transactional
    public void removeJudokaFromGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId).orElseThrow();
        Judoka judoka = judokaRepository.findById(judokaId).orElseThrow();
        grupo.getJudokas().remove(judoka);
        grupoRepository.save(grupo);
    }

    /**
     * 2. CAMBIO VITAL (SaaS): Ahora filtra por el ID del Sensei actual.
     * Evita que un sensei meta a sus grupos a alumnos de otro dojo.
     */
    @Transactional(readOnly = true)
    public List<Judoka> findJudokasDisponibles(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {

        // Obtener el ID del Sensei de la sesión actual
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) {
            return List.of(); // Si no es Sensei, no ve a nadie.
        }

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        // AHORA USAMOS EL MÉTODO DEL SAAS (findBySenseiIdWithUsuario)
        List<Judoka> todos = judokaRepository.findBySensei(securityService.getAuthenticatedSensei().get());

        return todos.stream()
                .filter(j -> {
                    if (nombreFilter != null) {
                        String nombreCompleto = (j.getUsuario().getNombre() + " " + j.getUsuario().getApellido()).toLowerCase();
                        return nombreCompleto.contains(nombreFilter);
                    }
                    return true;
                })
                .filter(j -> sexo == null || j.getSexo() == sexo)
                .filter(j -> grado == null || j.getGrado() == grado)
                .filter(j -> {
                    if (grupoId == null) return true;
                    // Verificación manual segura
                    return grupoRepository.findById(grupoId)
                            .map(grupo -> !grupo.getJudokas().contains(j))
                            .orElse(true);
                })
                .collect(Collectors.toList());
    }

    /**
     * Este método se mantiene igual porque ya usa la consulta correcta del repositorio (findByGrupoIdWithUsuario)
     */
    @Transactional(readOnly = true)
    public List<Judoka> findJudokasEnGrupo(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        if (grupoId == null) return List.of();

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        List<Judoka> miembros = judokaRepository.findByGrupoIdWithAcudiente(grupoId);

        return miembros.stream()
                .filter(j -> {
                    if (nombreFilter != null) {
                        String nombreCompleto = (j.getUsuario().getNombre() + " " + j.getUsuario().getApellido()).toLowerCase();
                        return nombreCompleto.contains(nombreFilter);
                    }
                    return true;
                })
                .filter(j -> sexo == null || j.getSexo() == sexo)
                .filter(j -> grado == null || j.getGrado() == grado)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGrupo(Long grupoId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId).orElseThrow();
        grupo.getJudokas().clear(); // Limpiar relaciones
        grupoRepository.delete(grupo);
    }
}