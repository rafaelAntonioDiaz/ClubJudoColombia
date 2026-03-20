package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka; // <-- IMPORTANTE
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MicrocicloRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GrupoEntrenamientoService {

    private static final Logger logger = LoggerFactory.getLogger(GrupoEntrenamientoService.class);

    private final GrupoEntrenamientoRepository grupoRepository;
    private final JudokaRepository judokaRepository;
    private final MicrocicloRepository planRepository;
    private final ConfiguracionService configService;
    private final SecurityService securityService;

    public GrupoEntrenamientoService(GrupoEntrenamientoRepository grupoRepository,
                                     JudokaRepository judokaRepository,
                                     MicrocicloRepository planRepository, ConfiguracionService configService,
                                     SecurityService securityService) {
        this.grupoRepository = grupoRepository;
        this.judokaRepository = judokaRepository;
        this.planRepository = planRepository;
        this.configService = configService;
        this.securityService = securityService;
    }

    // --- HELPER DE NOMBRE SEGURO ---
    private String obtenerNombreSeguro(Judoka j) {
        if (j.getNombre() != null && !j.getNombre().isEmpty()) {
            return j.getNombre() + " " + j.getApellido();
        } else if (j.getUsuario() != null) {
            return j.getUsuario().getNombre() + " " + j.getUsuario().getApellido();
        }
        return "";
    }

    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findAll(int offset, int limit, String filter) {
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) return List.of();

        PageRequest pageable = PageRequest.of(offset / limit, limit);
        List<GrupoEntrenamiento> grupos;

        if (filter == null || filter.trim().isEmpty()) {
            grupos = grupoRepository.findBySenseiId(miSenseiId, pageable).getContent();
        } else {
            grupos = grupoRepository.findBySenseiIdAndNombreContainingIgnoreCase(miSenseiId, filter, pageable).getContent();
        }

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
    public List<GrupoEntrenamiento> getGruposPorJudoka(Long judokaId) {
        return grupoRepository.findByJudokas_Id(judokaId);
    }

    @Transactional
    public GrupoEntrenamiento save(GrupoEntrenamiento grupo) {
        if (grupo.getId() == null) {
            Long miSenseiId = securityService.getSenseiIdActual();
        }
        return grupoRepository.save(grupo);
    }

    @Transactional(readOnly = true)
    public Optional<GrupoEntrenamiento> findById(Long id) {
        return grupoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Judoka> findJudokasDisponibles(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) return List.of();

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        List<Judoka> todos = judokaRepository.findBySensei(securityService.getAuthenticatedSensei().get());

        // 🛡️ ESCUDO 1: Filtrar duplicados cartesianos de Hibernate
        java.util.Map<Long, Judoka> unicos = new java.util.LinkedHashMap<>();
        for (Judoka j : todos) {
            unicos.put(j.getId(), j);
        }

        // 🛡️ ESCUDO 2 (LA CURA DEL BUG): Extraemos solo los IDs numéricos del grupo ANTES de filtrar.
        // Esto evita que Java intente comparar objetos completos y colapse la vista.
        java.util.Set<Long> idsEnGrupo = new java.util.HashSet<>();
        if (grupoId != null) {
            grupoRepository.findById(grupoId).ifPresent(g -> {
                if (g.getJudokas() != null) {
                    g.getJudokas().forEach(miembro -> idsEnGrupo.add(miembro.getId()));
                }
            });
        }

        return unicos.values().stream()
                .filter(j -> j.getEstado() == EstadoJudoka.ACTIVO)
                // Usamos la búsqueda ultra rápida y segura por ID numérico
                .filter(j -> !idsEnGrupo.contains(j.getId()))
                .filter(j -> {
                    if (nombreFilter != null) {
                        return obtenerNombreSeguro(j).toLowerCase().contains(nombreFilter);
                    }
                    return true;
                })
                .filter(j -> sexo == null || j.getSexo() == sexo)
                .filter(j -> grado == null || j.getGrado() == grado)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addJudokaToGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId).orElseThrow();
        Judoka judoka = judokaRepository.findById(judokaId).orElseThrow();

        // 1. LA REGLA DE ORO: Actualizamos al dueño de la relación (El Judoka)
        // NOTA: Si en tu clase Judoka el setter se llama distinto (ej. setGrupoEntrenamiento), cámbialo aquí.
        judoka.setGrupo(grupo);
        judokaRepository.save(judoka);

        // 2. Sincronizamos la memoria del grupo para evitar errores de caché de Hibernate
        if (grupo.getJudokas() != null) {
            boolean yaExiste = grupo.getJudokas().stream().anyMatch(j -> j.getId().equals(judokaId));
            if (!yaExiste) {
                grupo.getJudokas().add(judoka);
                grupoRepository.save(grupo);
            }
        }
    }

    @Transactional
    public void removeJudokaFromGrupo(Long grupoId, Long judokaId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId).orElseThrow();
        Judoka judoka = judokaRepository.findById(judokaId).orElseThrow();

        // 1. Rompemos la relación desde el lado dueño
        judoka.setGrupo(null);
        judokaRepository.save(judoka);

        // 2. Limpiamos la memoria del grupo
        if (grupo.getJudokas() != null) {
            grupo.getJudokas().removeIf(j -> j.getId().equals(judokaId));
            grupoRepository.save(grupo);
        }
    }

    @Transactional(readOnly = true)
    public List<Judoka> findJudokasEnGrupo(Long grupoId, String searchNombre, Sexo sexo, GradoCinturon grado) {
        if (grupoId == null) return List.of();

        String nombreFilter = (searchNombre != null && !searchNombre.trim().isEmpty()) ? searchNombre.toLowerCase() : null;

        List<Judoka> miembros = judokaRepository.findByGrupoIdWithAcudiente(grupoId);

        // 🛡️ ESCUDO ANTI-CLONES
        java.util.Map<Long, Judoka> unicos = new java.util.LinkedHashMap<>();
        for (Judoka j : miembros) {
            unicos.put(j.getId(), j);
        }

        return unicos.values().stream()
                .filter(j -> {
                    if (nombreFilter != null) {
                        return obtenerNombreSeguro(j).toLowerCase().contains(nombreFilter);
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
        grupo.getJudokas().clear();
        grupoRepository.delete(grupo);
    }

    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findAllBySenseiId(Long senseiId) {
        return grupoRepository.findBySenseiId(senseiId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }
    @Transactional(readOnly = true)
    public List<GrupoEntrenamiento> findBySensei(Sensei sensei) {
        return grupoRepository.findBySenseiId(sensei.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Transactional(readOnly = true)
    public GrupoEntrenamiento obtenerGrupoConJudokas(Long grupoId) {
        GrupoEntrenamiento grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        Hibernate.initialize(grupo.getJudokas()); // Forzar carga de la colección LAZY
        return grupo;
    }
    public GrupoEntrenamiento crearGrupo(Sensei sensei, String nombre, String descripcion,
                                         BigDecimal tarifaMensual, BigDecimal comisionSensei,
                                         boolean incluyeMatricula, BigDecimal montoMatricula,
                                         int diasGracia) {
        // Validar tarifa mínima
        BigDecimal tarifaMinima = configService.getTarifaMinimaGlobal();
        if (tarifaMensual.compareTo(tarifaMinima) < 0) {
            throw new RuntimeException("La tarifa no puede ser menor a la mínima global: " + tarifaMinima);
        }

        // Validar matrícula
        if (incluyeMatricula && (montoMatricula == null || montoMatricula.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Si incluye matrícula, debe indicar un monto válido");
        }

        GrupoEntrenamiento grupo = new GrupoEntrenamiento();
        grupo.setSensei(sensei);
        grupo.setNombre(nombre);
        grupo.setDescripcion(descripcion);
        grupo.setTarifaMensual(tarifaMensual);
        grupo.setComisionSensei(comisionSensei);
        grupo.setIncluyeMatricula(incluyeMatricula);
        grupo.setMontoMatricula(incluyeMatricula ? montoMatricula : null);
        grupo.setDiasGracia(diasGracia);

        return grupoRepository.save(grupo);
    }

    public GrupoEntrenamiento actualizarGrupo(Long id, String nombre, String descripcion,
                                              BigDecimal tarifaMensual, BigDecimal comisionSensei,
                                              boolean incluyeMatricula, BigDecimal montoMatricula,
                                              int diasGracia) {
        GrupoEntrenamiento grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        // Validar tarifa mínima (misma lógica)
        BigDecimal tarifaMinima = configService.getTarifaMinimaGlobal();
        if (tarifaMensual.compareTo(tarifaMinima) < 0) {
            throw new RuntimeException("La tarifa no puede ser menor a la mínima global: " + tarifaMinima);
        }

        if (incluyeMatricula && (montoMatricula == null || montoMatricula.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Si incluye matrícula, debe indicar un monto válido");
        }

        grupo.setNombre(nombre);
        grupo.setDescripcion(descripcion);
        grupo.setTarifaMensual(tarifaMensual);
        grupo.setComisionSensei(comisionSensei);
        grupo.setIncluyeMatricula(incluyeMatricula);
        grupo.setMontoMatricula(incluyeMatricula ? montoMatricula : null);
        grupo.setDiasGracia(diasGracia);

        return grupoRepository.save(grupo);
    }

    public void eliminarGrupo(Long id) {
        GrupoEntrenamiento grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        if (!grupo.getJudokas().isEmpty()) {
            throw new RuntimeException("No se puede eliminar un grupo con judokas asignados");
        }
        grupoRepository.delete(grupo);
    }

    public GrupoEntrenamiento crearGrupoPorDefecto(Sensei sensei) {
        // Usar valores de configuración
        ConfiguracionSistema config = configService.obtenerConfiguracion();
        return crearGrupo(sensei,
                "Grupo por defecto",
                "",
                config.getGrupoTarifaDefault(),
                config.getGrupoComisionDefault(),
                config.isGrupoIncluyeMatriculaDefault(),
                config.getGrupoMontoMatriculaDefault(),
                config.getGrupoDiasGraciaDefault());
    }
}