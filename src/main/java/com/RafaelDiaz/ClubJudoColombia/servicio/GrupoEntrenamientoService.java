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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones de negocio relacionadas con Grupos de Entrenamiento.
 * Incluye CRUD, adición/remoción de Judokas, listas filtradas y eliminación con desasociación.
 *
 * @author RafaelDiaz
 * @version 3.0 (Armonizado con paginación real)
 * @since 2025-11-19
 */
@Service
@Transactional
public class GrupoEntrenamientoService {

    private static final Logger logger = LoggerFactory.getLogger(GrupoEntrenamientoService.class);

    private final GrupoEntrenamientoRepository grupoRepository;
    private final JudokaService judokaService;
    private final JudokaRepository judokaRepository;
    private final PlanEntrenamientoRepository planRepository;

    public GrupoEntrenamientoService(GrupoEntrenamientoRepository grupoRepository,
                                     JudokaService judokaService,
                                     JudokaRepository judokaRepository,
                                     PlanEntrenamientoRepository planRepository) {
        this.grupoRepository = grupoRepository;
        this.judokaService = judokaService;
        this.judokaRepository = judokaRepository;
        this.planRepository = planRepository;
    }

    /**
     * Obtiene todos los grupos con paginación y filtrado opcional.
     *
     * @param offset Desplazamiento (índice inicial)
     * @param limit Número máximo de registros
     * @param filter Filtro opcional por nombre (contains)
     * @return Lista de grupos
     */
// GrupoEntrenamientoService.java (findAll modificado)
    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findAll(int offset, int limit, String filter) {
        PageRequest pageable = PageRequest.of(offset / limit, limit);

        if (filter == null || filter.trim().isEmpty()) {
            return grupoRepository.findAll(pageable).getContent(); // Usa EntityGraph
        }
        return grupoRepository.findByNombreContainingIgnoreCase(filter, pageable).getContent(); // Usa EntityGraph
    }
    /**
     * Cuenta total de grupos con filtrado opcional.
     *
     * @param filter Filtro por nombre (contains)
     * @return Número total de grupos
     */
    @Transactional(readOnly = true)
    public long count(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return grupoRepository.count();
        }
        return grupoRepository.countByNombreContainingIgnoreCase(filter);
    }

    @Transactional(readOnly = true)
    public Optional<GrupoEntrenamiento> findById(Long id) {
        return grupoRepository.findById(id);
    }

    @Transactional
    public GrupoEntrenamiento save(GrupoEntrenamiento grupo) {
        logger.info("Guardando grupo: {}", grupo.getNombre());
        return grupoRepository.save(grupo);
    }

    @Transactional
    public void addJudokaToGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));
        Judoka judoka = judokaRepository.findById(judokaId)
                .orElseThrow(() -> new RuntimeException("Judoka no encontrado: " + judokaId));

        grupo.getJudokas().add(judoka);
        grupoRepository.save(grupo);
        logger.info("Judoka {} agregado al grupo {}", judokaId, grupoId);
    }

    @Transactional
    public void removeJudokaFromGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));
        Judoka judoka = judokaRepository.findById(judokaId)
                .orElseThrow(() -> new RuntimeException("Judoka no encontrado: " + judokaId));

        grupo.getJudokas().remove(judoka);
        grupoRepository.save(grupo);
        logger.info("Judoka {} removido del grupo {}", judokaId, grupoId);
    }


    @Transactional(readOnly = true)
    public List<Judoka> findJudokasDisponibles(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        return judokaRepository.findAll().stream()
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
                    // Excluir judokas ya en el grupo
                    if (grupoId == null) return true;
                    return grupoRepository.findById(grupoId)
                            .map(grupo -> !grupo.getJudokas().contains(j))
                            .orElse(true);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Judoka> findJudokasEnGrupo(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        if (grupoId == null) return List.of();

        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        return grupo.getJudokas().stream()
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
    /**
     * Elimina un grupo de entrenamiento de forma segura.
     */
    @Transactional
    public void deleteGrupo(Long grupoId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + grupoId));

        logger.warn("Eliminando grupo {} con {} judokas y {} planes asociados",
                grupo.getNombre(),
                grupo.getJudokas().size(),
                grupo.getPlanesAsignados().size());

        if (!grupo.getJudokas().isEmpty()) {
            grupo.getJudokas().clear();
            logger.info("Judokas desasociados del grupo {}", grupoId);
        }

        List<PlanEntrenamiento> planesConGrupo = planRepository.findAllByGruposAsignadosContains(grupo);
        for (PlanEntrenamiento plan : planesConGrupo) {
            plan.getGruposAsignados().remove(grupo);
            planRepository.save(plan);
            logger.info("Grupo {} desasociado del plan {}", grupoId, plan.getId());
        }

        grupoRepository.delete(grupo);
        logger.info("Grupo {} eliminado exitosamente", grupoId);
    }

}