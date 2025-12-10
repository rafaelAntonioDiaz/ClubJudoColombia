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

    public GrupoEntrenamientoService(GrupoEntrenamientoRepository grupoRepository,
                                     JudokaRepository judokaRepository,
                                     PlanEntrenamientoRepository planRepository) {
        this.grupoRepository = grupoRepository;
        this.judokaRepository = judokaRepository;
        this.planRepository = planRepository;
    }

    // ... (Mantén findAll, count, findById, save, addJudokaToGrupo, removeJudokaFromGrupo IGUALES) ...
    // Solo pego los que cambian para brevedad, pero asegúrate de tener todo el archivo completo.

    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findAll(int offset, int limit, String filter) {
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        if (filter == null || filter.trim().isEmpty()) {
            return grupoRepository.findAll(pageable).getContent();
        }
        return grupoRepository.findByNombreContainingIgnoreCase(filter, pageable).getContent();
    }

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
        return grupoRepository.save(grupo);
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
     * CORREGIDO: Usa findAllWithUsuario para evitar LazyInitException en el ComboBox
     */
    @Transactional(readOnly = true)
    public List<Judoka> findJudokasDisponibles(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

       // Usamos el método optimizado del repositorio [cite: 1]
        List<Judoka> todos = judokaRepository.findAllWithUsuario();

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
     * CORREGIDO: Usa findByGrupoIdWithUsuario para evitar LazyInitException en la Grilla
     */
    @Transactional(readOnly = true)
    public List<Judoka> findJudokasEnGrupo(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        if (grupoId == null) return List.of();

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        List<Judoka> miembros = judokaRepository.findByGrupoIdWithUsuario(grupoId);

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