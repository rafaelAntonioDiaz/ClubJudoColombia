package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JudokaService {

    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;
    private final GamificationService gamificationService;
    private final GrupoEntrenamientoService grupoService;
    private final ReflexionRepository reflexionRepository;
    private final FinanzasService finanzasService;
    // --- DEPENDENCIAS ADICIONALES PARA INICIALIZACIÓN Y SEGURIDAD ---
    private final MicrocicloRepository planRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final PruebaEstandarRepository pruebaRepository;
    private final EjercicioPlanificadoRepository ejercicioPlanificadoRepository;
    private final SenseiRepository senseiRepository;

    public JudokaService(AlmacenamientoCloudService almacenamientoCloudService,
                         JudokaRepository judokaRepository,
                         GamificationService gamificationService, GrupoEntrenamientoService grupoService,
                         ReflexionRepository reflexionRepository, FinanzasService finanzasService,
                         MicrocicloRepository planRepository,
                         GrupoEntrenamientoRepository grupoRepository,
                         PruebaEstandarRepository pruebaRepository,
                         EjercicioPlanificadoRepository ejercicioPlanificadoRepository,
                         SenseiRepository senseiRepository) {
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;
        this.gamificationService = gamificationService;
        this.grupoService = grupoService;
        this.reflexionRepository = reflexionRepository;
        this.finanzasService = finanzasService;
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
            System.err.println("ADVERTENCIA: No se pudo asignar Sensei al plan inicial.");
            return;
        }

        // 2. Crear Grupo Personal
        GrupoEntrenamiento grupoPersonal = new GrupoEntrenamiento();
        grupoPersonal.setNombre("Individual - " + judoka.getUsuario().getNombre());
        grupoPersonal.setDescripcion("Grupo automático para evaluación individual.");
        grupoPersonal.setSensei(senseiResponsable); // Asignamos sensei

        // --- Asignar valores por defecto para todos los campos NOT NULL ---
        grupoPersonal.setComisionSensei(BigDecimal.ZERO);          // Comisión 0%
        grupoPersonal.setTarifaMensual(BigDecimal.ZERO);           // Tarifa 0 (no se cobrará)
        grupoPersonal.setMontoMatricula(BigDecimal.ZERO);          // Matrícula 0
        grupoPersonal.setDiasGracia(5);                            // Días de gracia por defecto
        grupoPersonal.setIncluyeMatricula(false);                  // No incluye matrícula
        grupoPersonal.setLugarPractica("Sala de evaluación");      // Texto por defecto
        grupoPersonal.setRadioPermitidoMetros(0);                  // Radio en metros
        grupoPersonal.setHoraInicio(java.time.LocalTime.of(8, 0)); // 8:00 AM
        grupoPersonal.setHoraFin(java.time.LocalTime.of(20, 0));   // 8:00 PM
        grupoPersonal.setLatitud(0.0);
        grupoPersonal.setLongitud(0.0);

        // Relación muchos-a-muchos: agregar el judoka al grupo
        grupoPersonal.getJudokas().add(judoka);
        grupoRepository.save(grupoPersonal);

        // 3. Crear Plan de Evaluación
        Microciclo micro = new Microciclo();
        micro.setNombre("Evaluación Inicial 2026");
        micro.setSensei(senseiResponsable);
        micro.setEstado(EstadoMicrociclo.ACTIVO);
        micro.setTipoMicrociclo(TipoMicrociclo.CONTROL);
        micro.getGruposAsignados().add(grupoPersonal);
        micro = planRepository.save(micro);

        // 4. Agregar Pruebas estándar al plan
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
        return judokaRepository.findByAcudienteWithDetails(acudiente);
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

    @Transactional
    public Judoka crearJudokaPorAcudiente(Usuario acudiente, String nombre, String apellido,
                                          LocalDate fechaNacimiento, GrupoEntrenamiento grupo) {
        // Validar que el usuario tenga rol de acudiente
        if (acudiente.getRoles().stream().noneMatch(r -> r.getNombre().equals("ROLE_ACUDIENTE"))) {
            throw new RuntimeException("El usuario no tiene permisos para agregar deportistas.");
        }

        // Si no se proporcionó grupo, tomar el que tenga el acudiente (si existe)
        if (grupo == null) {
            grupo = acudiente.getGrupoTarifario();
            if (grupo == null) {
                Sensei senseiInvitador = acudiente.getSenseiInvitador();
                if (senseiInvitador == null) {
                    throw new RuntimeException("No se pudo determinar el sensei responsable. Contacte al administrador.");
                }
                List<GrupoEntrenamiento> gruposSensei = grupoService.findBySensei(senseiInvitador);
                if (gruposSensei.isEmpty()) {
                    throw new RuntimeException("El sensei responsable no tiene grupos configurados.");
                }
                grupo = gruposSensei.get(0);
            }
        }

        // ✅ Inicializar el grupo (y su sensei) antes de acceder a sus propiedades perezosas
        // Si el grupo es un proxy o está detached, lo volvemos a buscar gestionado
        Long grupoId = grupo.getId();
        grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado con ID: " + grupoId));
        // Ahora grupo está gestionado por la sesión actual
        Hibernate.initialize(grupo);
        if (grupo.getSensei() != null) {
            Hibernate.initialize(grupo.getSensei());
        }

        Sensei sensei = grupo.getSensei();
        Judoka judoka = new Judoka();
        judoka.setNombre(nombre);
        judoka.setApellido(apellido);
        judoka.setAcudiente(acudiente);
        judoka.setFechaNacimiento(fechaNacimiento);
        judoka.setEstado(EstadoJudoka.EN_REVISION);
        judoka.setSensei(sensei);
        judoka.setGrupoFacturacion(grupo);
        judoka.setGrupo(grupo);
        judoka.setGrado(GradoCinturon.BLANCO);

        judoka = judokaRepository.save(judoka);

        // Generar cobros de bienvenida (matrícula + primera mensualidad)
        finanzasService.generarCobroBienvenida(judoka);

        return judoka;
    }

    @Transactional(readOnly = true)
    public Judoka findByIdWithDetails(Long id) {
        // Usa un query con JOIN FETCH para evitar proxies
        return judokaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Judoka no encontrado con id: " + id));
    }

}