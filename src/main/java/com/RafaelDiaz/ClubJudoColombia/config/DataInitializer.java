package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.ChatService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner { // 1. Implementamos la interfaz

    private final UsuarioService usuarioService;
    private final PlanEntrenamientoService planService;
    private final RolRepository rolRepository;
    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final TareaDiariaRepository tareaDiariaRepository;
    private final TraduccionRepository traduccionRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final MetricaRepository metricaRepository;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;
    private final SesionProgramadaRepository sesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final InsigniaRepository insigniaRepository;      // <--- NUEVO
    private final JudokaInsigniaRepository judokaInsigniaRepository;
    private final EjercicioPlanificadoRepository ejercicioRepository;
    private MensajeChatRepository mensajeChatRepository;
    private ChatService chatService;

    public DataInitializer(UsuarioService usuarioService,
                           PlanEntrenamientoService planService,
                           RolRepository rolRepository,
                           SenseiRepository senseiRepository,
                           JudokaRepository judokaRepository,
                           GrupoEntrenamientoRepository grupoRepository,
                           TareaDiariaRepository tareaDiariaRepository,
                           TraduccionRepository traduccionRepository,
                           PruebaEstandarRepository pruebaEstandarRepository,
                           MetricaRepository metricaRepository,
                           ResultadoPruebaRepository resultadoPruebaRepository,
                           EjecucionTareaRepository ejecucionTareaRepository,
                           SesionProgramadaRepository sesionRepository,
                           AsistenciaRepository asistenciaRepository,
                           EjercicioPlanificadoRepository ejercicioRepository,
                           MensajeChatRepository mensajeChatRepository,
                           ChatService chatService,
                           JudokaInsigniaRepository judokaInsigniaRepository,
                           InsigniaRepository insigniaRepository) {
        this.usuarioService = usuarioService;
        this.planService = planService;
        this.rolRepository = rolRepository;
        this.senseiRepository = senseiRepository;
        this.judokaRepository = judokaRepository;
        this.grupoRepository = grupoRepository;
        this.tareaDiariaRepository = tareaDiariaRepository;
        this.traduccionRepository = traduccionRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.metricaRepository = metricaRepository;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
        this.sesionRepository = sesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.mensajeChatRepository = mensajeChatRepository;
        this.chatService = chatService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
    }

    // 2. Método run transaccional: Mantiene la sesión abierta todo el tiempo
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Verificación simple para no duplicar datos
        if (judokaRepository.count() > 0) {
            System.out.println(">>> BASE DE DATOS YA POBLADA. SALTANDO INITIALIZER.");
            return;
        }

        System.out.println(">>> INICIANDO CARGA DE DATOS MAESTROS - CLUB JUDO COLOMBIA");
// DIAGNÓSTICO DE PRUEBAS EXISTENTES
        System.out.println("--- LISTADO DE PRUEBAS EN BD ---");
        pruebaEstandarRepository.findAll().forEach(p ->
                System.out.println("ID: " + p.getId() + " | Key: " + p.getNombreKey())
        );
        System.out.println("--------------------------------");
        // 1. Validar Roles
        validarRolesExistentes();

        // 2. Traducciones
        crearTraduccionesFestivos();
        crearTraduccionesDias();
        crearTraduccionesDashboard();

        // 3. Usuarios
        Sensei kiuzo = crearSensei("kiuzo", "Kiuzo", "Mifune", "123456", GradoCinturon.NEGRO_5_DAN);
        crearSensei("toshiro", "Toshiro", "Diago", "123456", GradoCinturon.NEGRO_5_DAN);

        List<Judoka> judokas = crearJudokas();

        // 4. Grupos
        crearGruposYAsignar(judokas);

        // 5. Tareas
        crearTareasAcondicionamiento(kiuzo);

        // 6. PLAN 1
        PlanEntrenamiento planFisico = crearPlanAcondicionamiento(kiuzo);

        // 7. PLAN 2
        PlanEntrenamiento planEvaluacion = crearPlanEvaluacion(kiuzo);

        // 8. Sesiones
        programarSesiones(kiuzo);

        // 9. Historial
        generarResultadosEvaluacion(judokas, planEvaluacion);
        generarEjecucionesTareas(judokas, planFisico);
        generarAsistencias(judokas);

        // Generar historial completo para gráficos
        generarDatosHistoricosCompletos(judokas);
        crearChatInicial();
        otorgarInsigniasDemo(judokas);
        crearTraduccionesDashboard();
        System.out.println(">>> CARGA DE DATOS COMPLETADA CON ÉXITO.");
    }

    // --- MÉTODOS PRIVADOS (Igual que antes, con pequeñas mejoras de seguridad) ---

    private void validarRolesExistentes() {
        if (rolRepository.findByNombre("ROLE_SENSEI").isEmpty()) {
            throw new RuntimeException("ERROR CRÍTICO: Roles no encontrados.");
        }
    }
    private void crearTraduccionesDias() {
        if (traduccionRepository.findByClaveAndIdioma("MONDAY", "es").isPresent()) return;
        traduccionRepository.saveAll(List.of(
                new Traduccion("MONDAY", "es", "Lunes"),
                new Traduccion("TUESDAY", "es", "Martes"),
                new Traduccion("WEDNESDAY", "es", "Miércoles"),
                new Traduccion("THURSDAY", "es", "Jueves"),
                new Traduccion("FRIDAY", "es", "Viernes"),
                new Traduccion("SATURDAY", "es", "Sábado"),
                new Traduccion("SUNDAY", "es", "Domingo")
        ));
    }
    private void crearTraduccionesFestivos() {
        if (traduccionRepository.findByClaveAndIdioma("festivo.navidad", "es").isPresent()) return;

        System.out.println(">>> CARGANDO FESTIVOS...");
        traduccionRepository.saveAll(List.of(
                new Traduccion("festivo.ano_nuevo", "es", "Año Nuevo"),
                new Traduccion("festivo.reyes_magos", "es", "Epifanía del Señor (Reyes Magos)"),
                new Traduccion("festivo.san_jose", "es", "Día de San José"),
                new Traduccion("festivo.jueves_santo", "es", "Jueves Santo"),
                new Traduccion("festivo.viernes_santo", "es", "Viernes Santo"),
                new Traduccion("festivo.dia_trabajo", "es", "Día del Trabajo"),
                new Traduccion("festivo.ascension", "es", "Ascensión del Señor"),
                new Traduccion("festivo.corpus_christi", "es", "Corpus Christi"),
                new Traduccion("festivo.sagrado_corazon", "es", "Sagrado Corazón de Jesús"),
                new Traduccion("festivo.san_pedro", "es", "San Pedro y San Pablo"),
                new Traduccion("festivo.independencia", "es", "Independencia de Colombia"),
                new Traduccion("festivo.batalla_boyaca", "es", "Batalla de Boyacá"),
                new Traduccion("festivo.asuncion", "es", "Asunción de la Virgen"),
                new Traduccion("festivo.dia_raza", "es", "Día de la Raza"),
                new Traduccion("festivo.todos_santos", "es", "Todos los Santos"),
                new Traduccion("festivo.independencia_cartagena", "es", "Independencia de Cartagena"),
                new Traduccion("festivo.inmaculada", "es", "Inmaculada Concepción"),
                new Traduccion("festivo.navidad", "es", "Navidad")
        ));
    }
    private void crearTraduccionesDashboard() {
        // Validación para no duplicar datos
        if (traduccionRepository.findByClaveAndIdioma("dashboard.welcome", "es").isPresent()) return;

        System.out.println(">>> CARGANDO TODAS LAS TRADUCCIONES DEL DASHBOARD (ES/EN)...");
        List<Traduccion> lista = new ArrayList<>();

        // --- 1. DASHBOARD GENERAL (Bienvenida y Botones) ---
        agregarTraduccion(lista, "dashboard.welcome", "Hola, {0}", "Hello, {0}");
        agregarTraduccion(lista, "dashboard.btn.tareas", "Ir a Mis Tareas", "Go to My Tasks");

        // --- 2. KPIs (Indicadores Superiores) ---
        agregarTraduccion(lista, "kpi.poder_combate", "Poder de Combate", "Combat Power");
        agregarTraduccion(lista, "kpi.planes_activos", "Planes Activos", "Active Plans");
        agregarTraduccion(lista, "kpi.tareas_hoy", "Tareas Hoy", "Tasks Today");
        agregarTraduccion(lista, "kpi.proxima_eval", "Próxima Eval.", "Next Eval.");
        agregarTraduccion(lista, "kpi.hoy", "¡Hoy!", "Today!");
        agregarTraduccion(lista, "kpi.dias", "días", "days");

        // --- 3. GRÁFICOS Y LEYENDAS ---
        agregarTraduccion(lista, "chart.radar.serie", "Nivel Actual", "Current Level");
        agregarTraduccion(lista, "legend.progreso", "Mi Progreso", "My Progress");
        agregarTraduccion(lista, "legend.meta", "Meta a Batir", "Goal");
        agregarTraduccion(lista, "chart.sin_datos", "Sin datos", "No Data");

        // --- 4. ESTADOS VACÍOS ---
        agregarTraduccion(lista, "empty.title", "Aún no tienes estadísticas", "No stats yet");
        agregarTraduccion(lista, "empty.desc", "Completa tu primera evaluación para desbloquear tu Perfil de Combate.", "Complete your first evaluation to unlock your Combat Profile.");

        // --- 5. GAMIFICACIÓN: WIDGET 'MI DO' ---
        // Títulos de Columnas
        agregarTraduccion(lista, "widget.mido.titulo", "Mi Do (El Camino)", "My Do (The Way)");
        agregarTraduccion(lista, "widget.mido.shin", "SHIN (Mente)", "SHIN (Mind)");
        agregarTraduccion(lista, "widget.mido.gi", "GI (Técnica)", "GI (Technique)");
        agregarTraduccion(lista, "widget.mido.tai", "TAI (Cuerpo)", "TAI (Body)");

        // Estados del Diálogo de Insignia
        agregarTraduccion(lista, "badge.estado.desbloqueada", "¡Insignia Desbloqueada!", "Badge Unlocked!");
        agregarTraduccion(lista, "badge.estado.bloqueada", "Insignia Bloqueada", "Badge Locked");
        agregarTraduccion(lista, "badge.label.obtenida", "Obtenida el", "Obtained on");
        agregarTraduccion(lista, "badge.label.pendiente", "Aún no la tienes. ¡Sigue entrenando!", "Not earned yet. Keep training!");
        agregarTraduccion(lista, "btn.cerrar", "Entendido", "Got it");

        // --- 6. INSIGNIAS ESPECÍFICAS (SHIN - GI - TAI) ---
        // SHIN (Mente)
        agregarTraduccion(lista, "badge.shin_inicio.nombre", "Primer Paso", "First Step");
        agregarTraduccion(lista, "badge.shin_inicio.desc", "Completaste tu primer entrenamiento. El viaje comienza.", "You completed your first training. The journey begins.");

        agregarTraduccion(lista, "badge.shin_constancia.nombre", "Espíritu Indomable", "Indomitable Spirit");
        agregarTraduccion(lista, "badge.shin_constancia.desc", "10 Asistencias consecutivas sin faltar.", "10 consecutive attendances without missing.");

        agregarTraduccion(lista, "badge.shin_compromiso.nombre", "Guardián del Dojo", "Dojo Guardian");
        agregarTraduccion(lista, "badge.shin_compromiso.desc", "50 Asistencias totales acumuladas.", "50 total accumulated attendances.");

        // GI (Técnica)
        agregarTraduccion(lista, "badge.gi_cinturon.nombre", "Nuevo Horizonte", "New Horizon");
        agregarTraduccion(lista, "badge.gi_cinturon.desc", "Has ascendido de grado (Cinturón).", "You have advanced in rank (Belt).");

        agregarTraduccion(lista, "badge.gi_tecnico.nombre", "Técnica Pura", "Pure Technique");
        agregarTraduccion(lista, "badge.gi_tecnico.desc", "Evaluación técnica sobresaliente.", "Outstanding technical evaluation.");

        // TAI (Cuerpo)
        agregarTraduccion(lista, "badge.tai_hercules.nombre", "Hércules", "Hercules");
        agregarTraduccion(lista, "badge.tai_hercules.desc", "Superaste 40 flexiones en un minuto.", "You exceeded 40 push-ups in a minute.");

        agregarTraduccion(lista, "badge.tai_velocidad.nombre", "Relámpago", "Lightning");
        agregarTraduccion(lista, "badge.tai_velocidad.desc", "Corriste 20m en menos de 3.5 segundos.", "You ran 20m in less than 3.5 seconds.");

        agregarTraduccion(lista, "badge.tai_resistencia.nombre", "Pulmones de Acero", "Steel Lungs");
        agregarTraduccion(lista, "badge.tai_resistencia.desc", "Índice SJFT Excelente.", "Excellent SJFT Index.");

        traduccionRepository.saveAll(lista);
    }

    // --- MÉTODO HELPER (Asegúrate de tener este también en la clase) ---
    private void agregarTraduccion(List<Traduccion> lista, String clave, String textoEs, String textoEn) {
        lista.add(new Traduccion(clave, "es", textoEs));
        lista.add(new Traduccion(clave, "en", textoEn));
    }
    private Sensei crearSensei(String user, String nom, String ape, String pass, GradoCinturon grado) {
        Usuario u = new Usuario(user, "HASH_PENDIENTE", nom, ape);
        u.setActivo(true);
        u.getRoles().add(rolRepository.findByNombre("ROLE_SENSEI").orElseThrow());
        u.getRoles().add(rolRepository.findByNombre("ROLE_ADMIN").orElseThrow());
        u = usuarioService.saveUsuario(u, pass);

        Sensei s = new Sensei();
        s.setUsuario(u);
        s.setGrado(grado);
        s.setAnosPractica(25);
        return senseiRepository.save(s);
    }
    private List<Judoka> crearJudokas() {
        List<Judoka> lista = new ArrayList<>();
        lista.add(crearJudokaIndividual("maria.lopez", "María", "López", 2010, 3, 15, Sexo.FEMENINO, GradoCinturon.AMARILLO, true, "Jorge López"));
        lista.add(crearJudokaIndividual("juan.gomez", "Juan Camilo", "Gómez", 2008, 7, 22, Sexo.MASCULINO, GradoCinturon.NARANJA, true, "Camilo Gómez"));
        lista.add(crearJudokaIndividual("laura.ramirez", "Laura", "Ramírez", 2006, 4, 10, Sexo.FEMENINO, GradoCinturon.VERDE, false, null));
        lista.add(crearJudokaIndividual("daniel.diaz", "Daniel", "Díaz", 2003, 1, 30, Sexo.MASCULINO, GradoCinturon.NEGRO_1_DAN, true, null));
        return judokaRepository.saveAll(lista);
    }
    private Judoka crearJudokaIndividual(String user, String nom, String ape, int anio, int mes, int dia, Sexo sexo, GradoCinturon grado, boolean competidor, String acudiente) {
        Usuario u = new Usuario(user, "HASH_PENDIENTE", nom, ape);
        u.setActivo(true);
        u.getRoles().add(rolRepository.findByNombre("ROLE_JUDOKA").orElseThrow());
        if (competidor) u.getRoles().add(rolRepository.findByNombre("ROLE_COMPETIDOR").orElseThrow());
        u = usuarioService.saveUsuario(u, "123456");

        Judoka j = new Judoka();
        j.setUsuario(u);
        j.setFechaNacimiento(LocalDate.of(anio, mes, dia));
        j.setSexo(sexo);
        j.setGrado(grado);
        j.setEsCompetidorActivo(competidor);
        j.setNombreAcudiente(acudiente);
        j.setPeso(sexo == Sexo.MASCULINO ? 73.0 : 57.0);
        j.setEstatura(sexo == Sexo.MASCULINO ? 175.0 : 160.0);
        return j; // El usuario ya se guarda por cascada/servicio
    }
    private void crearGruposYAsignar(List<Judoka> judokas) {
        GrupoEntrenamiento cadetes = new GrupoEntrenamiento();
        cadetes.setNombre("Judokas Cadetes");
        cadetes.setDescripcion("Grupo enfocado en desarrollo técnico sub-18");
        cadetes.setJudokas(new HashSet<>());
        cadetes.getJudokas().add(judokas.get(0));
        cadetes.getJudokas().add(judokas.get(1));

        GrupoEntrenamiento mayores = new GrupoEntrenamiento();
        mayores.setNombre("Selección Mayores");
        mayores.setDescripcion("Equipo de competencia Élite");
        mayores.setJudokas(new HashSet<>());
        mayores.getJudokas().add(judokas.get(3));

        grupoRepository.saveAll(List.of(cadetes, mayores));
    }

    private void crearTareasAcondicionamiento(Sensei sensei) {
        if(tareaDiariaRepository.count() > 0) return;

        // Creamos un "Menú" variado de ejercicios
        tareaDiariaRepository.saveAll(List.of(
                new TareaDiaria("Calentamiento Articular", "10 min movilidad", sensei),
                new TareaDiaria("Trote Suave", "15 min zona 2", sensei),
                new TareaDiaria("Uchikomi Sombra", "50 entradas (Der/Izq)", sensei),
                new TareaDiaria("Flexiones de Pecho", "4 series x 15 reps", sensei),
                new TareaDiaria("Sentadillas con Salto", "4 series x 20 reps", sensei),
                new TareaDiaria("Uchikomi Gomas", "100 repeticiones velocidad", sensei),
                new TareaDiaria("Burpees", "3 series al fallo", sensei),
                new TareaDiaria("Abdominales en V", "3 series x 30", sensei),
                new TareaDiaria("Estiramiento Final", "15 min estático", sensei)
        ));
    }

    private PlanEntrenamiento crearPlanAcondicionamiento(Sensei sensei) {
        GrupoEntrenamiento grupo = grupoRepository.findByNombre("Judokas Cadetes").orElseThrow();

        PlanEntrenamiento plan = new PlanEntrenamiento();
        plan.setNombre("Programa Intensivo - Pretemporada 2025");
        plan.setSensei(sensei);
        plan.setFechaAsignacion(LocalDate.now());
        plan.setEstado(EstadoPlan.ACTIVO);
        plan.setTipoSesion(TipoSesion.ACONDICIONAMIENTO);
        plan.getGruposAsignados().add(grupo);

        // Guardamos el plan primero para tener ID
        plan = planService.guardarPlan(plan);

        // Recuperamos todas las tareas creadas arriba
        List<TareaDiaria> tareasDisponibles = tareaDiariaRepository.findAll();

        int orden = 1;
        for (TareaDiaria tarea : tareasDisponibles) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setPlanEntrenamiento(plan);
            ej.setTareaDiaria(tarea);
            ej.setOrden(orden++);
            ej.setNotasSensei("¡Enfócate en la técnica, no solo velocidad!");

            // --- TRUCO PARA DEMO ---
            // Asignamos la tarea a TODOS los días de la semana.
            // Así, entres el día que entres, verás el dashboard lleno.
            ej.getDiasAsignados().addAll(Arrays.asList(DayOfWeek.values()));

            plan.addEjercicio(ej);
        }

        return planService.guardarPlan(plan);
    }
    private PlanEntrenamiento crearPlanEvaluacion(Sensei sensei) {
        GrupoEntrenamiento grupo = grupoRepository.findByNombre("Judokas Cadetes").orElseThrow();
        PlanEntrenamiento plan = new PlanEntrenamiento();
        plan.setNombre("Test SJFT - Trimestre 1");
        plan.setSensei(sensei);
        plan.setFechaAsignacion(LocalDate.now());
        plan.setEstado(EstadoPlan.ACTIVO);
        plan.setTipoSesion(TipoSesion.EVALUACION);
        plan.getGruposAsignados().add(grupo);
        plan = planService.guardarPlan(plan);

        PruebaEstandar sjft = pruebaEstandarRepository.findByNombreKey("ejercicio.sjft.nombre")
                .or(() -> pruebaEstandarRepository.findByNombreKey("ejercicio.sjft"))
                .orElseThrow(() -> new RuntimeException("Error: Datos V2 no encontrados (SJFT)"));

        EjercicioPlanificado ej = new EjercicioPlanificado();
        ej.setPlanEntrenamiento(plan);
        ej.setPruebaEstandar(sjft);
        ej.setOrden(1);
        ej.setNotasSensei("Máximo esfuerzo requerido");
        ej.getDiasAsignados().add(DayOfWeek.SATURDAY);

        plan.addEjercicio(ej);
        return planService.guardarPlan(plan);
    }
    private void programarSesiones(Sensei sensei) {
        GrupoEntrenamiento grupo = grupoRepository.findByNombre("Judokas Cadetes").orElseThrow();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        List<SesionProgramada> sesiones = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SesionProgramada s = new SesionProgramada();
            s.setNombre("Clase Técnica #" + (i + 1));
            s.setTipoSesion(TipoSesion.TECNICA);
            s.setFechaHoraInicio(base.plusDays(i * 7));
            s.setFechaHoraFin(base.plusDays(i * 7).plusHours(2));
            s.setGrupo(grupo);
            s.setSensei(sensei);
            sesiones.add(s);
        }
        sesionRepository.saveAll(sesiones);
    }

    private void generarResultadosEvaluacion(List<Judoka> judokas, PlanEntrenamiento planEval) {
        if (planEval.getEjerciciosPlanificados().isEmpty()) return;
        EjercicioPlanificado ejSjft = planEval.getEjerciciosPlanificados().get(0);

        Metrica metricaTotal = metricaRepository.findByNombreKey("metrica.sjft_proyecciones_total.nombre")
                .or(() -> metricaRepository.findByNombreKey("metrica.sjft_proyecciones_total"))
                .orElse(null);

        if (metricaTotal == null) return;

        List<ResultadoPrueba> resultados = new ArrayList<>();
        LocalDateTime fechaEval = LocalDateTime.now().minusDays(5);

        for (Judoka j : judokas) {
            if (j.getUsuario().getUsername().equals("maria.lopez") || j.getUsuario().getUsername().equals("juan.gomez")) {
                ResultadoPrueba res = new ResultadoPrueba();
                res.setJudoka(j);
                res.setEjercicioPlanificado(ejSjft);
                res.setMetrica(metricaTotal);
                res.setValor(j.getSexo() == Sexo.FEMENINO ? 26.0 : 29.0);
                res.setFechaRegistro(fechaEval);
                res.setNumeroIntento(1);
                res.setNotasJudoka("Me sentí bien de aire");
                resultados.add(res);
            }
        }
        resultadoPruebaRepository.saveAll(resultados);
    }

    private void generarEjecucionesTareas(List<Judoka> judokas, PlanEntrenamiento planFisico) {
        if (planFisico.getEjerciciosPlanificados().isEmpty()) return;
        EjercicioPlanificado tareaBurpees = planFisico.getEjerciciosPlanificados().get(0);
        Judoka maria = judokas.get(0);

        EjecucionTarea ejecucion = new EjecucionTarea();
        ejecucion.setJudoka(maria);
        ejecucion.setEjercicioPlanificado(tareaBurpees);
        ejecucion.setCompletado(true);
        ejecucion.setFechaRegistro(LocalDateTime.now().minusDays(1));
        ejecucionTareaRepository.save(ejecucion);
    }

    private void generarAsistencias(List<Judoka> judokas) {
        List<SesionProgramada> sesiones = sesionRepository.findAll();
        if (sesiones.isEmpty()) return;
        SesionProgramada sesion = sesiones.get(0);
        Judoka maria = judokas.get(0);

        Asistencia asistencia = new Asistencia();
        asistencia.setJudoka(maria);
        asistencia.setSesion(sesion);
        asistencia.setPresente(true);
        asistencia.setFechaHoraMarcacion(sesion.getFechaHoraInicio().plusMinutes(5));
        asistenciaRepository.save(asistencia);
    }

    // --- EL MÉTODO QUE DA VIDA AL DASHBOARD ---
    private void generarDatosHistoricosCompletos(List<Judoka> judokas) {
        System.out.println("--- INICIANDO GENERACIÓN DE HISTÓRICO (CORREGIDO) ---");

        Judoka maria = judokas.stream()
                .filter(j -> j.getUsuario().getUsername().equals("maria.lopez"))
                .findFirst().orElse(null);

        if (maria == null) {
            System.err.println("ERROR CRÍTICO: No encontré a 'maria.lopez'.");
            return;
        }

        // Validar edad de María para tu tranquilidad
        int edadMaria = Period.between(maria.getFechaNacimiento(), LocalDate.now()).getYears();
        System.out.println("-> Judoka: " + maria.getUsuario().getNombre() + " | Edad calculada: " + edadMaria + " años.");

        // --- MAPEO EXPLÍCITO: PRUEBA -> MÉTRICA ---
        // Esto conecta exactamente con lo que definiste en V2__Poblar_Datos...sql
        Map<String, String> mapaPruebaMetrica = Map.of(
                "ejercicio.salto_horizontal_proesp", "metrica.distancia.nombre",
                "ejercicio.lanzamiento_balon",       "metrica.lanzamiento_balon.nombre",
                "ejercicio.abdominales_1min",        "metrica.abdominales_1min.nombre",
                "ejercicio.carrera_6min",            "metrica.distancia_6min.nombre",
                "ejercicio.agilidad_4x4",            "metrica.agilidad_4x4.nombre",
                "ejercicio.carrera_20m",             "metrica.velocidad_20m.nombre",
                "ejercicio.sjft",                    "metrica.sjft_indice.nombre" // Ojo: SJFT tiene muchas, usamos Indice para el radar
        );

        List<LocalDateTime> fechas = List.of(
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now()
        );

        List<ResultadoPrueba> resultadosParaGuardar = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<String, String> entry : mapaPruebaMetrica.entrySet()) {
            String clavePrueba = entry.getKey();
            String claveMetrica = entry.getValue();

            // 1. Buscar Prueba
            PruebaEstandar prueba = pruebaEstandarRepository.findByNombreKey(clavePrueba)
                    .or(() -> pruebaEstandarRepository.findByNombreKey(clavePrueba + ".nombre"))
                    .orElse(null);

            if (prueba == null) {
                System.err.println("   [X] Prueba NO encontrada: " + clavePrueba);
                continue;
            }

            // 2. Buscar Métrica EXACTA (Ya no adivinamos)
            Metrica metrica = metricaRepository.findByNombreKey(claveMetrica)
                    .orElse(null);

            if (metrica == null) {
                System.err.println("   [X] Métrica NO encontrada: " + claveMetrica);
                continue;
            }

            System.out.println("   [OK] Generando datos para: " + clavePrueba + " usando métrica: " + claveMetrica);

            // 3. Obtener ejercicio dummy
            EjercicioPlanificado ejercicioDummy = obtenerOCrearEjercicioDummy(prueba);

            // 4. Generar valores simulados (ajustados para que den 'BUENO' o 'EXCELENTE')
            // Salto ~150cm, Abdominales ~30, Carrera ~1000m, etc.
            double valorBase = calcularValorBaseLogico(clavePrueba);

            for (int i = 0; i < fechas.size(); i++) {
                ResultadoPrueba res = new ResultadoPrueba();
                res.setJudoka(maria);
                res.setMetrica(metrica);
                res.setEjercicioPlanificado(ejercicioDummy);
                res.setFechaRegistro(fechas.get(i));
                res.setNumeroIntento(1);

                // Simulamos mejora: valor base + un poquito
                // Ojo: En carrera 20m y agilidad, MENOS es MEJOR.
                boolean esTiempo = clavePrueba.contains("carrera_20m") || clavePrueba.contains("agilidad");
                double mejora = (i * 1.5);
                res.setValor(esTiempo ? valorBase - (mejora/10.0) : valorBase + mejora);

                res.setNotasJudoka("Carga inicial automática");
                resultadosParaGuardar.add(res);
            }
        }
        resultadoPruebaRepository.saveAll(resultadosParaGuardar);
        System.out.println(">>> HISTORIAL GENERADO CORRECTAMENTE (" + resultadosParaGuardar.size() + " registros).");
    }
    // Helper para dar valores que tengan sentido en la gráfica
    private double calcularValorBaseLogico(String clave) {
        if (clave.contains("salto")) return 160.0; // cm
        if (clave.contains("lanzamiento")) return 300.0; // cm
        if (clave.contains("abdominales")) return 35.0; // reps
        if (clave.contains("carrera_6min")) return 1100.0; // metros
        if (clave.contains("agilidad")) return 6.0; // segundos (menos es mejor)
        if (clave.contains("carrera_20m")) return 3.5; // segundos (menos es mejor)
        if (clave.contains("sjft")) return 12.0; // indice
        return 10.0;
    }    private EjercicioPlanificado obtenerOCrearEjercicioDummy(PruebaEstandar prueba) {
        // 1. Buscamos al Sensei Kiuzo (Usando repo directo o servicio de usuario)
        Usuario usuarioKiuzo = usuarioService.findByUsername("kiuzo").orElseThrow();
        Sensei kiuzo = senseiRepository.findByUsuario(usuarioKiuzo).orElseThrow();

        // 2. Buscamos planes
        List<PlanEntrenamiento> planes = planService.buscarPlanesPorSensei(kiuzo);
        if (planes.isEmpty()) throw new RuntimeException("No hay planes de Kiuzo");
        PlanEntrenamiento plan = planes.get(0);

        // 3. Buscar o crear ejercicio (usando comparación por ID para evitar LazyInit en equals)
        return plan.getEjerciciosPlanificados().stream()
                .filter(e -> e.getPruebaEstandar() != null &&
                        e.getPruebaEstandar().getId().equals(prueba.getId()))
                .findFirst()
                .orElseGet(() -> {
                    EjercicioPlanificado nuevo = new EjercicioPlanificado();
                    nuevo.setPlanEntrenamiento(plan);
                    nuevo.setPruebaEstandar(prueba);
                    nuevo.setOrden(99);
                    nuevo.getDiasAsignados().add(DayOfWeek.SATURDAY);

                    // Guardado directo y robusto
                    return ejercicioRepository.save(nuevo);
                });
    }
    private void crearChatInicial() {
        if (mensajeChatRepository.count() > 0) return;

        System.out.println(">>> INICIALIZANDO CHAT...");
        Usuario kiuzo = usuarioService.findByUsername("kiuzo").orElseThrow();
        Usuario maria = usuarioService.findByUsername("maria.lopez").orElseThrow();

        chatService.enviarMensaje(kiuzo, "¡Bienvenidos al Chat Oficial del Club!");
        chatService.enviarMensaje(kiuzo, "Aquí publicaremos anuncios importantes sobre los torneos.");
        chatService.enviarMensaje(maria, "¡Entendido Sensei! ¿A qué hora es el pesaje el sábado?");
    }
    private void otorgarInsigniasDemo(List<Judoka> judokas) {
        // 1. Buscar a María
        Judoka maria = judokas.stream()
                .filter(j -> j.getUsuario().getUsername().equals("maria.lopez"))
                .findFirst().orElse(null);

        if (maria == null) return;

        // 2. Definir qué medallas le damos (Una de cada categoría para que se vea equilibrado)
        List<String> clavesGanadas = List.of(
                "SHIN_INICIO",     // "Primer Paso" (Mente)
                "TAI_HERCULES",    // "Hércules" (Cuerpo - tiene sentido con sus stats físicos)
                "GI_TECNICO"       // "Técnica Pura" (Técnica)
        );

        System.out.println(">>> OTORGANDO INSIGNIAS A MARÍA...");

        for (String clave : clavesGanadas) {
            // Buscamos la insignia en el catálogo
            Insignia insignia = insigniaRepository.findByClave(clave).orElse(null);

            if (insignia != null) {
                // Creamos la relación (El logro)
                JudokaInsignia logro = new JudokaInsignia();
                logro.setInsignia(insignia);
                logro.setJudoka(maria);

                logro.setFechaObtencion(LocalDateTime.now().minusDays(new Random().nextInt(30))); // Ganada hace días

                judokaInsigniaRepository.save(logro);
                System.out.println("   -> Ganó: " + insignia.getNombre());
            }
        }
    }

}