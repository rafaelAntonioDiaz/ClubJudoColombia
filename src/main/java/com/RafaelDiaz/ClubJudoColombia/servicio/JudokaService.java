package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JudokaService {

    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;
    private final GamificationService gamificationService;
    private final ReflexionRepository reflexionRepository;

    // --- DEPENDENCIAS ADICIONALES PARA INICIALIZACIÓN Y SEGURIDAD ---
    private final MicrocicloRepository planRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final PruebaEstandarRepository pruebaRepository;
    private final EjercicioPlanificadoRepository ejercicioPlanificadoRepository;
    private final SenseiRepository senseiRepository;

    public JudokaService(AlmacenamientoCloudService almacenamientoCloudService,
                         JudokaRepository judokaRepository,
                         GamificationService gamificationService,
                         ReflexionRepository reflexionRepository,
                         MicrocicloRepository planRepository,
                         GrupoEntrenamientoRepository grupoRepository,
                         PruebaEstandarRepository pruebaRepository,
                         EjercicioPlanificadoRepository ejercicioPlanificadoRepository,
                         SenseiRepository senseiRepository) {
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;
        this.gamificationService = gamificationService;
        this.reflexionRepository = reflexionRepository;
        this.planRepository = planRepository;
        this.grupoRepository = grupoRepository;
        this.pruebaRepository = pruebaRepository;
        this.ejercicioPlanificadoRepository = ejercicioPlanificadoRepository;
        this.senseiRepository = senseiRepository;
    }

    @Transactional
    public Judoka ascenderGrado(Judoka judoka, GradoCinturon nuevoGrado) {
        judoka.setGrado(nuevoGrado);
        Judoka guardado = judokaRepository.save(judoka);
        gamificationService.verificarLogrosGrado(guardado);
        return guardado;
    }

    @Transactional
    public Judoka save(Judoka judoka) {
        return judokaRepository.save(judoka);
    }

    public List<Judoka> findAllJudokas() {
        return judokaRepository.findAll();
    }

    // --- REFLEXIONES ---

    public List<Reflexion> obtenerHistorialReflexiones(Judoka judoka) {
        return reflexionRepository.findByJudokaOrderByFechaCreacionDesc(judoka);
    }

    @Transactional
    public void crearReflexion(Judoka judoka, String contenido) {
        Reflexion nueva = new Reflexion(judoka, contenido);
        reflexionRepository.save(nueva);
    }

    @Transactional
    public void editarReflexion(Reflexion reflexion, String nuevoContenido) {
        if (!reflexion.esEditable()) {
            throw new RuntimeException("El tiempo de edición (24h) ha expirado.");
        }
        reflexion.setContenido(nuevoContenido);
        reflexion.setFechaUltimaEdicion(LocalDateTime.now());
        reflexionRepository.save(reflexion);
    }

    // --- FOTO DE PERFIL (CLOUD) ---

    @Transactional
    public void actualizarFotoPerfil(Judoka judoka, InputStream inputStream, String filename) {
        try {
            // 1. Sube y obtén solo el nombre
            String nombreFinal = almacenamientoCloudService.subirArchivo(
                    judoka.getId(), filename, inputStream
            );
            System.out.println(">>> nombreFinal: " + nombreFinal);

            // 2. Obtén la URL completa desde el servicio
            String urlEnLaNube = almacenamientoCloudService.obtenerUrl(judoka.getId(), nombreFinal);
            System.out.println(">>> URL completa: " + urlEnLaNube);

            // 3. Guarda la URL en la entidad
            judoka.setUrlFotoPerfil(urlEnLaNube);
            judokaRepository.save(judoka);
            System.out.println(">>> Foto actualizada en BD");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar foto: " + e.getMessage());
        }
    }

    // ============================================================
    //  SEGURIDAD: AISLAMIENTO DE DATOS (CORREGIDO)
    // ============================================================

    @Transactional(readOnly = true)
    public List<Judoka> buscarPorSensei(Sensei sensei) {
        List<Judoka> todos = judokaRepository.findAll();

        return todos.stream()
                .filter(judoka -> perteneceASensei(judoka, sensei))
                .collect(Collectors.toList());
    }

    private boolean perteneceASensei(Judoka judoka, Sensei sensei) {
        // CORRECCIÓN: Usamos la relación directa que ya existe en tu entidad Judoka.
        // Si el judoka tiene asignado este Sensei, es suyo.
        return judoka.getSensei() != null &&
                judoka.getSensei().getId().equals(sensei.getId());
    }

    // ============================================================
    //  SEMBRADO AUTOMÁTICO: PLAN DE BIENVENIDA (CORREGIDO)
    // ============================================================

    @Transactional
    public void inicializarJudokaNuevo(Judoka judoka) {
        // 1. Obtener Sensei por defecto (ID 1) o usar el del propio Judoka si tiene
        Sensei senseiResponsable = (judoka.getSensei() != null)
                ? judoka.getSensei()
                : senseiRepository.findById(1L).orElse(null);

        if (senseiResponsable == null) {
            // Log warning o fallback si no hay sensei 1
            System.err.println("ADVERTENCIA: No se pudo asignar Sensei al plan inicial.");
        }

        // 2. Crear Grupo Personal
        GrupoEntrenamiento grupoPersonal = new GrupoEntrenamiento();
        grupoPersonal.setNombre("Individual - " + judoka.getUsuario().getNombre());
        grupoPersonal.setDescripcion("Grupo automático para evaluación individual.");
        grupoPersonal.setSensei(judoka.getSensei());
        grupoPersonal.getJudokas().add(judoka);


        grupoRepository.save(grupoPersonal);

        // 3. Crear Plan de Evaluación
        Microciclo micro = new Microciclo();
        micro.setNombre("Evaluación Inicial 2026");

        micro.setSensei(senseiResponsable);

        micro.setEstado(EstadoMicrociclo.ACTIVO);
        micro.setTipoMicrociclo(TipoMicrociclo.CONTROL);

        // CORRECCIÓN: Usamos getGruposAsignados en lugar de getGrupos
        micro.getGruposAsignados().add(grupoPersonal);

        micro = planRepository.save(micro);

        // 4. Agregar Pruebas
        agregarPruebaAlPlan(micro, "ejercicio.abdominales_1min.nombre", 1);
        agregarPruebaAlPlan(micro, "ejercicio.carrera_20m.nombre", 2);
        agregarPruebaAlPlan(micro, "ejercicio.agilidad_4x4.nombre", 3);
        agregarPruebaAlPlan(micro, "ejercicio.salto_horizontal_proesp.nombre", 4);
    }

    @Transactional(readOnly = true)
    public List<Judoka> findByMecenas(Mecenas mecenas) {
        return judokaRepository.findAll().stream()
                .filter(j -> j.getMecenas() != null && j.getMecenas().equals(mecenas))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<Judoka> findByAcudiente(Usuario acudiente) {
        return judokaRepository.findByAcudienteWithMecenas(acudiente);
    }

    private void agregarPruebaAlPlan(Microciclo micro, String keyPrueba, int orden) {
        Optional<PruebaEstandar> pruebaOpt = pruebaRepository.findByNombreKey(keyPrueba);
        if (pruebaOpt.isPresent()) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(micro);
            ej.setPruebaEstandar(pruebaOpt.get());
            ej.setOrden(orden);
            ejercicioPlanificadoRepository.save(ej);
        }
    }
    /**
     * Calcula el monto total mensual para el SaaS.
     * Estructura: 15.000 COP (Plataforma/SaaS) + Mensualidad pactada con el Sensei.
     */
    public BigDecimal calcularMontoMensualTotal(Judoka judoka) {
        BigDecimal tasaPlataforma = new BigDecimal("15000");
        BigDecimal mensualidadSensei = judoka.getMontoMensualidad() != null ?
                judoka.getMontoMensualidad() : BigDecimal.ZERO;
        return tasaPlataforma.add(mensualidadSensei);
    }
}