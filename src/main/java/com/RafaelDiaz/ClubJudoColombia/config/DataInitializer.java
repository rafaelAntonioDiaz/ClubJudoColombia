package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.ChatService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PublicacionService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner { // 1. Implementamos la interfaz

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
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
    private final MensajeChatRepository mensajeChatRepository;
    private final ParticipacionCompetenciaRepository palmaresRepo;
    private ChatService chatService;
    private final PublicacionService publicacionService;
    private final PublicacionRepository publicacionRepository;

    public DataInitializer(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, UsuarioService usuarioService,
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
                           InsigniaRepository insigniaRepository, ParticipacionCompetenciaRepository palmaresRepo,
                           PublicacionService publicacionService, PublicacionRepository publicacionRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
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
        this.palmaresRepo = palmaresRepo;
        this.publicacionService = publicacionService;
        this.publicacionRepository = publicacionRepository;
    }

    // 2. Método run transaccional: Mantiene la sesión abierta todo el tiempo
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> INICIANDO VERIFICACIÓN DE ARQUITECTURA SaaS (MULTI-TENANT)");

        // --- 1. Validar Roles (Asegúrate de que ROLE_MASTER y ROLE_SENSEI estén en este método) ---
        validarRolesExistentes();

        // --- CHEQUEO 1: EL DOBLE SOMBRERO (CREACIÓN DEL MASTER / CLIENTE #0) ---
        // Esto se ejecuta siempre, garantizando que el dueño de la plataforma exista y tenga su Sensei ID.
        Sensei masterSensei = configurarUsuarioMaster();

        // --- CHEQUEO 2: VERIFICACIÓN DE DATOS DEMOSTRATIVOS ---
        // Verificación simple para no duplicar datos (Tu código original)
        if (judokaRepository.count() > 0) {
            System.out.println(">>> BASE DE DATOS YA POBLADA. SALTANDO INITIALIZER DE DATOS DEMO.");

            // Mantenemos la carga de Julián y María porque tu comentario dice que es segura.
            cargarDatosJulianYMaria(judokaRepository.findAll(), masterSensei);
            return;
        }

        System.out.println(">>> INICIANDO CARGA DE DATOS MAESTROS - CLUB JUDO COLOMBIA");

        // DIAGNÓSTICO DE PRUEBAS EXISTENTES
        System.out.println("--- LISTADO DE PRUEBAS EN BD ---");
        pruebaEstandarRepository.findAll().forEach(p ->
                System.out.println("ID: " + p.getId() + " | Key: " + p.getNombreKey())
        );
        System.out.println("--------------------------------");

        // 2. Traducciones
        cargarTraducciones(traduccionRepository);

        // 3. Usuarios (Tus "Otros Clientes" del SaaS)
        Sensei kiuzo = crearSensei("kiuzo", "Kiuzo", "Mifune",
                "123456", GradoCinturon.NEGRO_5_DAN);
        Sensei toshiro = crearSensei("toshiro", "Toshiro", "Diago", // Corregí para guardar la variable
                "123456", GradoCinturon.NEGRO_5_DAN);

        List<Judoka> judokas = crearJudokas(kiuzo);
        // 4. Grupos
        crearGruposYAsignar(judokas, kiuzo);
        // 5. Tareas
        crearTareasAcondicionamiento(kiuzo);
        cargarPruebasYMetricas();
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
        crearTraduccionesComunidad();
        crearBibliotecaSabiduria();
        judokas = judokaRepository.findAll();

        // 3. Generar Historia (Julián y actualización de María)
        // Este método YA TIENE su propio chequeo (if exists julian return)
        cargarDatosJulianYMaria(judokas, kiuzo);
        System.out.println(">>> CARGA DE DATOS COMPLETADA CON ÉXITO.");
    }
    /**
     * CHEQUEO 1: Asegura que el Master/Dueño siempre exista en el sistema.
     * Retorna el Perfil Sensei del Master (El Cliente #0).
     */
    private Sensei configurarUsuarioMaster() {
        String masterUsername = "master_admin";
        Optional<Usuario> masterExistente = usuarioRepository.findByUsername(masterUsername);

        if (masterExistente.isPresent()) {
            System.out.println(">>> [OK] Usuario Master ya configurado.");
            // Si ya existe, retornamos su perfil de Sensei.
            return senseiRepository.findByUsuario(masterExistente.get()).orElseThrow();
        }

        System.out.println("⚙️ Creando Usuario Máster (Cliente #0) con doble sombrero...");

        // Traemos los roles (ya asegurados por validarRolesExistentes)
        Rol rolMaster = rolRepository.findByNombre("ROLE_MASTER").orElseThrow();
        Rol rolSensei = rolRepository.findByNombre("ROLE_SENSEI").orElseThrow();

        // 1. Crear Usuario (Credenciales)
        Usuario masterUser = new Usuario(
                masterUsername,
                passwordEncoder.encode("contraseña"), // Credencial temporal
                "Rafael",
                "Díaz"
        );

        // Le asignamos AMBOS sombreros
        masterUser.getRoles().add(rolMaster);
        masterUser.getRoles().add(rolSensei);
        masterUser = usuarioRepository.save(masterUser);

        // 2. Crear Perfil de Tatami (Aquí nace tu Sensei ID)
        Sensei masterSenseiProfile = new Sensei();
        masterSenseiProfile.setUsuario(masterUser);
        masterSenseiProfile.setGrado(GradoCinturon.NEGRO_4_DAN);
        masterSenseiProfile.setAnosPractica(25);
        masterSenseiProfile.setBiografia("Director de la Plataforma SaaS y Sensei Titular del Club Matriz.");

        return senseiRepository.save(masterSenseiProfile);
    }

    private void crearBibliotecaSabiduria() {
        if (traduccionRepository.findByClaveAndIdioma("sabiduria.suntzu.1", "es").isPresent()) return;

        System.out.println(">>> CARGANDO BIBLIOTECA DE SABIDURÍA (ES/EN)...");
        List<Traduccion> lista = new ArrayList<>();

        // Textos de la UI del Perfil
        agregarTraduccion(lista, "perfil.titulo", "Mi Santuario", "My Sanctuary");
        agregarTraduccion(lista, "perfil.notas.titulo", "Bitácora de Reflexión", "Reflection Journal");
        agregarTraduccion(lista, "perfil.notas.placeholder", "Escribe aquí tus pensamientos, metas o correcciones...", "Write your thoughts, goals, or corrections here...");
        agregarTraduccion(lista, "perfil.btn.guardar", "Guardar Reflexión", "Save Reflection");
        agregarTraduccion(lista, "perfil.msg.guardado", "Reflexión guardada en tu mente.", "Reflection saved in your mind.");

        // --- SUN TZU (El Estratega del Tatami) ---
        agregarTraduccion(lista, "sabiduria.suntzu.1",
                "Los competidores victoriosos ganan primero en su tatami y luego van a la competencia; los derrotados van a la competencia primero y luego buscan cómo ganar.",
                "Victorious competitors win first on their tatami and then go to competition; the defeated go to competition first and then seek how to win.");

        agregarTraduccion(lista, "sabiduria.suntzu.2",
                "La excelencia suprema consiste en romper el equilibrio del oponente sin usar la fuerza bruta.",
                "Supreme excellence consists in breaking the opponent's balance without using brute force.");

        agregarTraduccion(lista, "sabiduria.suntzu.3",
                "En medio del caos del combate, siempre puedes marcar Ippon.",
                "In the midst of combat chaos, you can always score Ippon.");

        agregarTraduccion(lista, "sabiduria.suntzu.4",
                "Conoce a tu oponente y conócete a ti mismo; en cien combates, nunca perderás.",
                "Know your opponent and know yourself; in a hundred battles, you will never lose.");

        agregarTraduccion(lista, "sabiduria.suntzu.5",
                "La invencibilidad reside en la defensa; la posibilidad de ganar, en el ataque.",
                "Invincibility lies in the defense; the possibility of victory, in the attack.");

        agregarTraduccion(lista, "sabiduria.suntzu.6",
                "El agua determina su curso según el suelo; el Judoka consigue la victoria adaptándose a su oponente.",
                "Water determines its course according to the ground; the Judoka achieves victory by adapting to their opponent.");

        agregarTraduccion(lista, "sabiduria.suntzu.7",
                "Aparenta debilidad cuando seas fuerte, y fuerza cuando estés cansado.",
                "Appear weak when you are strong, and strong when you are tired.");

        agregarTraduccion(lista, "sabiduria.suntzu.8",
                "La rapidez es la esencia del Judo.",
                "Speed is the essence of Judo.");

        agregarTraduccion(lista, "sabiduria.suntzu.9",
                "La invencibilidad depende de mí, la derrota de mi oponente.",
                "Invincibility depends on me, the opponent's defeat depends on them.");

        // --- MIYAMOTO MUSASHI (Mentalidad de Acero) ---
        agregarTraduccion(lista, "sabiduria.musashi.1",
                "No hagas ningún movimiento en el tatami que no sea de utilidad.",
                "Do not make any movement on the tatami that is not useful.");

        agregarTraduccion(lista, "sabiduria.musashi.2",
                "Percibe la intención de tu oponente antes de que se mueva.",
                "Perceive your opponent's intention before they move.");

        agregarTraduccion(lista, "sabiduria.musashi.3",
                "Hoy es la victoria sobre tu yo de ayer; mañana será tu victoria en el campeonato.",
                "Today is victory over your self of yesterday; tomorrow is your victory in the championship.");

        agregarTraduccion(lista, "sabiduria.musashi.4",
                "Debes entender que hay más de un camino para lograr el Ippon.",
                "You must understand that there is more than one way to achieve Ippon.");

        agregarTraduccion(lista, "sabiduria.musashi.5",
                "En el combate, mira lo distante como si estuviera cerca y lo cercano con perspectiva.",
                "In combat, look at distant things as if they were close and close things with perspective.");

        agregarTraduccion(lista, "sabiduria.musashi.6",
                "El ritmo existe en todo. Si no entiendes el ritmo del combate, serás proyectado.",
                "Rhythm exists in everything. If you don't understand the combat rhythm, you will be thrown.");

        agregarTraduccion(lista, "sabiduria.musashi.7",
                "Si conoces el Camino ampliamente, verás el Judo en todas las cosas.",
                "If you know the Way broadly, you will see Judo in everything.");

        agregarTraduccion(lista, "sabiduria.musashi.8",
                "La verdadera técnica significa practicar de tal forma que sea útil aún en la calle.",
                "True technique means practicing in such a way that it is useful even on the street.");

        // --- JIGORO KANO (Principios) ---
        agregarTraduccion(lista, "sabiduria.kano.1", "El Judo no es solo deporte, es el principio básico de la conducta humana.", "Judo is not just a sport, it is the basic principle of human conduct.");
        agregarTraduccion(lista, "sabiduria.kano.2", "Camina por un solo camino. No te vuelvas engreído por el Oro, ni roto por la derrota.", "Walk a single path. Do not become conceited by Gold, nor broken by defeat.");
        agregarTraduccion(lista, "sabiduria.kano.3", "Lo importante no es ser mejor que otros competidores, sino ser mejor que ayer.", "The important thing is not to be better than other competitors, but to be better than yesterday.");
        agregarTraduccion(lista, "sabiduria.kano.4", "Máxima eficiencia con el mínimo esfuerzo.", "Maximum efficiency with minimum effort.");
        agregarTraduccion(lista, "sabiduria.kano.5", "Prosperidad y beneficio mutuo dentro y fuera del tatami.", "Mutual welfare and benefit inside and outside the tatami.");
        agregarTraduccion(lista, "sabiduria.kano.6", "Ser proyectado es temporal; rendirse es lo que lo hace permanente.", "Being thrown is temporary; giving up is what makes it permanent.");
        agregarTraduccion(lista, "sabiduria.kano.7", "Antes y después del Randori, inclínate ante tu compañero.", "Before and after Randori, bow to your partner.");
        agregarTraduccion(lista, "sabiduria.kano.8", "La delicadeza controla la fuerza. Cede para vencer.", "Gentleness controls strength. Yield to win.");

        traduccionRepository.saveAll(lista);
    }

    // --- MÉTODOS PRIVADOS  ---

// --- NUEVOS MÉTODOS PARA CREAR PRUEBAS MAESTRAS ---

    private void cargarPruebasYMetricas() {
        System.out.println(">>> CARGANDO PRUEBAS ESTÁNDAR Y MÉTRICAS...");

        // 1. Crear el Test SJFT
        if (pruebaEstandarRepository.findByNombreKey("ejercicio.sjft.nombre").isEmpty() &&
                pruebaEstandarRepository.findByNombreKey("ejercicio.sjft").isEmpty()) {

            PruebaEstandar sjft = new PruebaEstandar();
            sjft.setNombreKey("ejercicio.sjft.nombre");
            sjft.setDescripcionKey("ejercicio.sjft.desc");
            sjft.setObjetivoKey("ejercicio.sjft.objetivo"); // <-- OBLIGATORIO
            sjft.setCategoria(CategoriaEjercicio.RESISTENCIA_DINAMICA); // <-- USANDO TU ENUM REAL
            pruebaEstandarRepository.save(sjft);

            Metrica metricaTotal = new Metrica();
            metricaTotal.setNombreKey("metrica.sjft_proyecciones_total.nombre");
            metricaTotal.setUnidad("reps");
            metricaRepository.save(metricaTotal);

            Metrica metricaIndice = new Metrica();
            metricaIndice.setNombreKey("metrica.sjft_indice.nombre");
            metricaIndice.setUnidad("pts");
            metricaRepository.save(metricaIndice);
        }

        // 2. Crear las demás pruebas para el Radar de Combate
        // (Ajusta los nombres del Enum si en tu código son diferentes a POTENCIA, FUERZA, etc.)
        crearPruebaSimple("ejercicio.salto_horizontal_proesp", "metrica.distancia.nombre", "cm", CategoriaEjercicio.POTENCIA);
        crearPruebaSimple("ejercicio.lanzamiento_balon", "metrica.lanzamiento_balon.nombre", "cm", CategoriaEjercicio.POTENCIA);
        crearPruebaSimple("ejercicio.abdominales_1min", "metrica.abdominales_1min.nombre", "reps", CategoriaEjercicio.POTENCIA);
        crearPruebaSimple("ejercicio.carrera_6min", "metrica.distancia_6min.nombre", "m", CategoriaEjercicio.APTITUD_AEROBICA);
        crearPruebaSimple("ejercicio.agilidad_4x4", "metrica.agilidad_4x4.nombre", "s", CategoriaEjercicio.AGILIDAD);
        crearPruebaSimple("ejercicio.carrera_20m", "metrica.velocidad_20m.nombre", "s", CategoriaEjercicio.VELOCIDAD);
    }

    // Método actualizado para cumplir con todas las reglas de la base de datos
    private void crearPruebaSimple(String keyPrueba, String keyMetrica, String unidad, CategoriaEjercicio categoria) {
        if (pruebaEstandarRepository.findByNombreKey(keyPrueba).isEmpty()) {
            PruebaEstandar p = new PruebaEstandar();
            p.setNombreKey(keyPrueba);
            p.setCategoria(categoria); // <-- Enum CategoriaEjercicio

            // Evitamos el error de NULL en la base de datos autogenerando las claves
            p.setDescripcionKey(keyPrueba + ".desc");
            p.setObjetivoKey(keyPrueba + ".objetivo");

            pruebaEstandarRepository.save(p);

            Metrica m = new Metrica();
            m.setNombreKey(keyMetrica);
            m.setUnidad(unidad);
            metricaRepository.save(m);
        }
    }
    private void validarRolesExistentes() {
        System.out.println(">>> VERIFICANDO ROLES DEL SISTEMA...");
        crearRolSiNoExiste("ROLE_SENSEI");
        crearRolSiNoExiste("ROLE_JUDOKA");
        crearRolSiNoExiste("ROLE_COMPETIDOR");
        crearRolSiNoExiste("ROLE_ADMIN");
        crearRolSiNoExiste("ROLE_MASTER");

    }

    private void crearRolSiNoExiste(String nombreRol) {
        if (rolRepository.findByNombre(nombreRol).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombreRol);
            rolRepository.save(rol);
            System.out.println("   -> Rol creado exitosamente: " + nombreRol);
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
        agregarTraduccion(lista, "dashboard.welcome", "Hola, {0}",
                "Hello, {0}");
        agregarTraduccion(lista, "dashboard.btn.tareas",
                "Ir a Mis Tareas", "Go to My Tasks");
        agregarTraduccion(lista, "dashboard.titulo",
                "Panel de Control del Sensei", "Sensei Control Panel");
        agregarTraduccion(lista, "dashboard.boton.tomar_asistencia",
                "Tomar Asistencia / SOS","Take Attendance / SOS");
        // --- 2. KPIs (Indicadores Superiores) ---
        agregarTraduccion(lista, "kpi.poder_combate",
                "Poder de Combate", "Combat Power");
        agregarTraduccion(lista, "kpi.planes_activos",
                "Planes Activos", "Active Plans");
        agregarTraduccion(lista, "kpi.tareas_hoy",
                "Tareas Hoy", "Tasks Today");
        agregarTraduccion(lista, "kpi.proxima_eval",
                "Próxima Eval.", "Next Eval.");
        agregarTraduccion(lista, "kpi.hoy",
                "¡Hoy!", "Today!");
        agregarTraduccion(lista, "kpi.dias",
                "días", "days");

        agregarTraduccion(lista, "dashboard.kpi.total_judokas", "Total Judokas","Judokas Total");
        agregarTraduccion(lista, "dashboard.kpi.grupos_activos","Grupos Activos","Active Groups");
        agregarTraduccion(lista, "dashboard.kpi.pruebas_hoy","Para Hoy", "For Today");
        agregarTraduccion(lista, "dashboard.kpi.asistencia_promedio","Asistencia Promedio","Average Attendance");



        // --- 3. GRÁFICOS Y LEYENDAS ---
        agregarTraduccion(lista, "chart.radar.serie", "Nivel Actual", "Current Level");
        agregarTraduccion(lista, "legend.progreso", "Mi Progreso", "My Progress");
        agregarTraduccion(lista, "legend.meta", "Meta a Batir", "Goal");
        agregarTraduccion(lista, "chart.sin_datos", "Sin datos", "No Data");
        agregarTraduccion(lista, "dashboard.grafico.poder_combate_titulo","Poder de Combate por Grupo","Group Combat Power");
        agregarTraduccion(lista, "dashboard.grafico.asistencia_30dias_titulo","Asistencia últimos 30 días","Last 30 days attendance");
        agregarTraduccion(lista, "dashboard.grafico.promedio","Promedio","Average");
        agregarTraduccion(lista, "dashboard.grafico.asistencia_porcentaje","Asistencia %","Attendance %");

        // --- 4. ESTADOS VACÍOS ---
        agregarTraduccion(lista, "empty.title", "Aún no tienes estadísticas", "No stats yet");
        agregarTraduccion(lista, "empty.desc", "Completa tu primera evaluación para desbloquear tu Perfil de Combate.", "Complete your first evaluation to unlock your Combat Profile.");

        // --- 5. GAMIFICACIÓN: WIDGET 'MI DO' ---
        // Títulos de Columnas
        agregarTraduccion(lista, "widget.mido.titulo",
                "Mi Do (La Vía)", "My Do (The Way)");
        agregarTraduccion(lista, "widget.mido.shin",
                "SHIN (Mente Enfocada)", "SHIN (Focus Mind)");
        agregarTraduccion(lista, "widget.mido.gi",
                "GI (Técnica Impecable)", "GI (Perfect Technique)");
        agregarTraduccion(lista, "widget.mido.tai",
                "TAI (Cuerpo Poderoso)", "TAI (Strong Body)");
        agregarTraduccion(lista, "widget.mido.btn_catalogo",
                "Ver Catálogo", "View Catalog");
        agregarTraduccion(lista, "widget.mido.catalogo_titulo",
                "Salón de la Fama - Todas las Insignias", "Hall of Fame - All Badges");
        agregarTraduccion(lista, "widget.mido.msg_inicio",
                "¡Tu camino comienza! Aquí verás tus primeros objetivos.",
                "Your journey begins! Here are your first goals.");
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
// --- SenseiPlanView (Gestión de Planes) ---
        agregarTraduccion(lista, "view.sensei.plan.titulo", "Gestión de Planes de Entrenamiento", "Training Plans Management");
        agregarTraduccion(lista, "lbl.tipo.sesion", "Tipo de Sesión", "Session Type");
        agregarTraduccion(lista, "lbl.seleccionar.grupo", "Seleccionar Grupo", "Select Group");
        agregarTraduccion(lista, "btn.crear.plan", "Crear Nuevo Plan", "Create New Plan");
        agregarTraduccion(lista, "lbl.nombre.plan", "Nombre del Plan", "Plan Name");
        agregarTraduccion(lista, "lbl.asignar.dias", "Asignar para los días:", "Assign to days:");
        agregarTraduccion(lista, "header.bibliotecas", "Bibliotecas (Seleccione días arriba primero)", "Libraries (Select days above first)");
        agregarTraduccion(lista, "header.plan.actual", "Plan Actual", "Current Plan");
        agregarTraduccion(lista, "btn.guardar.cambios", "Guardar Cambios", "Save Changes");
        agregarTraduccion(lista, "btn.completar.plan", "Marcar Plan como COMPLETADO", "Mark Plan as COMPLETED");
        agregarTraduccion(lista, "col.prueba", "Prueba", "Test");
        agregarTraduccion(lista, "col.tarea", "Tarea", "Task");
        agregarTraduccion(lista, "col.meta", "Meta", "Goal");
        agregarTraduccion(lista, "col.ejercicio", "Ejercicio", "Exercise");
        agregarTraduccion(lista, "col.dias", "Días", "Days");
        agregarTraduccion(lista, "tipo.prueba", "Prueba", "Test");
        agregarTraduccion(lista, "tipo.tarea", "Tarea", "Task");
        agregarTraduccion(lista, "txt.cualquier.dia", "Cualquier día", "Any day");

// --- Mensajes y Notificaciones (SenseiPlanView) ---
        agregarTraduccion(lista, "msg.error.seleccionar.grupo", "Por favor, seleccione un grupo primero.", "Please select a group first.");
        agregarTraduccion(lista, "msg.error.nombre.vacio", "Ingrese un nombre para el plan.", "Please enter a plan name.");
        agregarTraduccion(lista, "msg.exito.plan.guardado", "Plan guardado exitosamente.", "Plan saved successfully.");
        agregarTraduccion(lista, "msg.exito.plan.completado", "Plan marcado como COMPLETADO.", "Plan marked as COMPLETED.");
        agregarTraduccion(lista, "msg.error.general", "Error", "Error");

// --- PerfilJudokaView (Perfil del Judoka) ---
        agregarTraduccion(lista, "tooltip.cambiar.foto", "Cambiar foto de perfil", "Change profile photo");
        agregarTraduccion(lista, "msg.foto.actualizada", "Foto actualizada correctamente", "Photo updated successfully");
        agregarTraduccion(lista, "alt.foto.perfil", "Foto de Perfil", "Profile Photo");
        agregarTraduccion(lista, "lbl.peso", "Peso", "Weight");
        agregarTraduccion(lista, "lbl.altura", "Altura", "Height");
        agregarTraduccion(lista, "lbl.edad", "Edad", "Age");
        agregarTraduccion(lista, "btn.registrar.pensamiento", "Registrar Pensamiento", "Log Thought");
        agregarTraduccion(lista, "msg.diario.vacio", "Tu diario está vacío. Empieza hoy.", "Your diary is empty. Start today.");
        agregarTraduccion(lista, "title.editar.reflexion", "Editar Reflexión", "Edit Reflection");
        agregarTraduccion(lista, "msg.entrada.actualizada", "Entrada actualizada.", "Entry updated.");
        agregarTraduccion(lista, "btn.cancelar", "Cancelar", "Cancel");
        agregarTraduccion(lista, "tooltip.registro.permanente", "Registro permanente (No editable)", "Permanent record (Non-editable)");

// --- SenseiPlanView (Gestión de Planes) ---
        agregarTraduccion(lista, "view.sensei.plan.titulo", "Gestión de Planes de Entrenamiento", "Training Plans Management");
        agregarTraduccion(lista, "lbl.tipo.sesion", "Tipo de Sesión", "Session Type");
        agregarTraduccion(lista, "lbl.seleccionar.grupo", "Seleccionar Grupo", "Select Group");
        agregarTraduccion(lista, "btn.crear.plan", "Crear Nuevo Plan", "Create New Plan");
        agregarTraduccion(lista, "lbl.nombre.plan", "Nombre del Plan", "Plan Name");
        agregarTraduccion(lista, "lbl.asignar.dias", "Asignar para los días:", "Assign to days:");
        agregarTraduccion(lista, "header.bibliotecas", "Bibliotecas (Seleccione días arriba primero)", "Libraries (Select days above first)");
        agregarTraduccion(lista, "header.plan.actual", "Plan Actual", "Current Plan");
        agregarTraduccion(lista, "btn.guardar.cambios", "Guardar Cambios", "Save Changes");
        agregarTraduccion(lista, "btn.completar.plan", "Marcar Plan como COMPLETADO", "Mark Plan as COMPLETED");
        agregarTraduccion(lista, "col.prueba", "Prueba", "Test");
        agregarTraduccion(lista, "col.tarea", "Tarea", "Task");
        agregarTraduccion(lista, "col.meta", "Meta", "Goal");
        agregarTraduccion(lista, "col.ejercicio", "Ejercicio", "Exercise");
        agregarTraduccion(lista, "col.dias", "Días", "Days");
        agregarTraduccion(lista, "tipo.prueba", "Prueba", "Test");
        agregarTraduccion(lista, "tipo.tarea", "Tarea", "Task");
        agregarTraduccion(lista, "txt.cualquier.dia", "Cualquier día", "Any day");

// --- Mensajes y Notificaciones (SenseiPlanView) ---
        agregarTraduccion(lista, "msg.error.seleccionar.grupo", "Por favor, seleccione un grupo primero.", "Please select a group first.");
        agregarTraduccion(lista, "msg.error.nombre.vacio", "Ingrese un nombre para el plan.", "Please enter a plan name.");
        agregarTraduccion(lista, "msg.exito.plan.guardado", "Plan guardado exitosamente.", "Plan saved successfully.");
        agregarTraduccion(lista, "msg.exito.plan.completado", "Plan marcado como COMPLETADO.", "Plan marked as COMPLETED.");
        agregarTraduccion(lista, "msg.error.general", "Error", "Error");

// --- PerfilJudokaView (Perfil del Judoka) ---
        agregarTraduccion(lista, "tooltip.cambiar.foto", "Cambiar foto de perfil", "Change profile photo");
        agregarTraduccion(lista, "msg.foto.actualizada", "Foto actualizada correctamente", "Photo updated successfully");
        agregarTraduccion(lista, "alt.foto.perfil", "Foto de Perfil", "Profile Photo");
        agregarTraduccion(lista, "lbl.peso", "Peso", "Weight");
        agregarTraduccion(lista, "lbl.altura", "Altura", "Height");
        agregarTraduccion(lista, "lbl.edad", "Edad", "Age");
        agregarTraduccion(lista, "btn.registrar.pensamiento", "Registrar Pensamiento", "Log Thought");
        agregarTraduccion(lista, "msg.diario.vacio", "Tu diario está vacío. Empieza hoy.", "Your diary is empty. Start today.");
        agregarTraduccion(lista, "title.editar.reflexion", "Editar Reflexión", "Edit Reflection");
        agregarTraduccion(lista, "msg.entrada.actualizada", "Entrada actualizada.", "Entry updated.");
        agregarTraduccion(lista, "btn.cancelar", "Cancelar", "Cancel");
        agregarTraduccion(lista, "tooltip.registro.permanente", "Registro permanente (No editable)", "Permanent record (Non-editable)");
        // -- TESORERIA  ---
        agregarTraduccion(lista, "tesoreria.titulo", "Gestión Financiera", "Financial Management");
        agregarTraduccion(lista, "tesoreria.tab.registrar_ingreso", "Registrar Ingreso (Cobro)", "Register Income (Payment)");
        agregarTraduccion(lista, "tesoreria.tab.registrar_gasto", "Registrar Gasto (Egreso)", "Register Expense (Outflow)");
        agregarTraduccion(lista, "tesoreria.tab.balance_reportes", "Balance y Reportes", "Balance and Reports");

        agregarTraduccion(lista, "tesoreria.alumno", "Alumno", "Student");
        agregarTraduccion(lista, "tesoreria.buscar_alumno", "Buscar alumno...", "Search student...");
        agregarTraduccion(lista, "tesoreria.concepto", "Concepto", "Concept");
        agregarTraduccion(lista, "tesoreria.valor", "Valor ($)", "Amount ($)");
        agregarTraduccion(lista, "tesoreria.valor_pagado", "Valor Pagado ($)", "Amount Paid ($)");
        agregarTraduccion(lista, "tesoreria.metodo_pago", "Método de Pago", "Payment Method");
        agregarTraduccion(lista, "tesoreria.observacion", "Observación (Opcional)", "Observation (Optional)");
        agregarTraduccion(lista, "tesoreria.categoria_gasto", "Categoría de Gasto", "Expense Category");
        agregarTraduccion(lista, "tesoreria.detalle_proveedor", "Detalle / Proveedor", "Detail / Supplier");
        agregarTraduccion(lista, "tesoreria.foto_factura", "Foto de Factura", "Invoice Photo");
        agregarTraduccion(lista, "tesoreria.soporte", "Soporte", "Support");

        agregarTraduccion(lista, "tesoreria.boton.registrar_generar_recibo", "Registrar y Generar Recibo", "Register and Generate Receipt");
        agregarTraduccion(lista, "tesoreria.boton.registrar_salida", "Registrar Salida", "Register Outflow");
        agregarTraduccion(lista, "tesoreria.boton.guardar", "Guardar", "Save");

        agregarTraduccion(lista, "tesoreria.kpi.ingresos_mes", "Ingresos Mes", "Monthly Income");
        agregarTraduccion(lista, "tesoreria.kpi.egresos_mes", "Egresos Mes", "Monthly Expenses");
        agregarTraduccion(lista, "tesoreria.kpi.balance", "Balance", "Balance");

        agregarTraduccion(lista, "tesoreria.grid.fecha", "Fecha", "Date");
        agregarTraduccion(lista, "tesoreria.grid.tipo", "Tipo", "Type");
        agregarTraduccion(lista, "tesoreria.grid.concepto", "Concepto", "Concept");
        agregarTraduccion(lista, "tesoreria.grid.monto", "Monto", "Amount");
        agregarTraduccion(lista, "tesoreria.grid.judoka", "Judoka", "Judoka");
        agregarTraduccion(lista, "tesoreria.grid.soporte", "Soporte", "Support");

        agregarTraduccion(lista, "tesoreria.dialog.nuevo_concepto.titulo", "Nuevo Concepto de", "New Concept for");
        agregarTraduccion(lista, "tesoreria.dialog.nuevo_concepto.nombre", "Nombre del Concepto", "Concept Name");
        agregarTraduccion(lista, "tesoreria.dialog.nuevo_concepto.valor_sugerido", "Valor Sugerido (Opcional)", "Suggested Value (Optional)");

        agregarTraduccion(lista, "tesoreria.validacion.concepto_monto", "Concepto y Monto son obligatorios", "Concept and Amount are required");
        agregarTraduccion(lista, "tesoreria.validacion.categoria_monto", "Categoría y Monto obligatorios", "Category and Amount are required");

        agregarTraduccion(lista, "tesoreria.notificacion.ingreso_exitoso", "Ingreso registrado con éxito", "Income registered successfully");
        agregarTraduccion(lista, "tesoreria.notificacion.soporte_cargado", "Soporte cargado", "Support uploaded");
        agregarTraduccion(lista, "tesoreria.notificacion.error_subir", "Error al subir: ", "Error uploading: ");
        agregarTraduccion(lista, "tesoreria.notificacion.gasto_registrado", "Gasto registrado", "Expense registered");
        agregarTraduccion(lista, "tesoreria.notificacion.concepto_creado", "Concepto Creado", "Concept Created");

        //   -- ASISTENCIA   ---
        agregarTraduccion(lista, "asistencia.boton.cerrar_clase", "Cerrar Clase y Guardar", "Close Class and Save");
        agregarTraduccion(lista, "asistencia.boton.cerrar", "Cerrar", "Close");
        agregarTraduccion(lista, "asistencia.selector.grupo", "Selecciona el Grupo", "Select Group");
        agregarTraduccion(lista, "asistencia.placeholder.grupo", "Ej: Infantiles Martes", "Ex: Kids Tuesday");
        agregarTraduccion(lista, "asistencia.fecha", "Fecha", "Date");
        agregarTraduccion(lista, "asistencia.mensaje.sin_alumnos", "Este grupo no tiene alumnos asignados.", "This group has no assigned students.");
        agregarTraduccion(lista, "asistencia.estado.ausente", "AUSENTE", "ABSENT");
        agregarTraduccion(lista, "asistencia.estado.presente", "PRESENTE", "PRESENT");
        agregarTraduccion(lista, "asistencia.notificacion.cargados", "Cargados", "Loaded");
        agregarTraduccion(lista, "asistencia.notificacion.alumnos", "alumnos", "students");
        agregarTraduccion(lista, "asistencia.notificacion.registrada", "Asistencia registrada", "Attendance registered");
        agregarTraduccion(lista, "asistencia.notificacion.presentes", "Presentes", "Present");
        agregarTraduccion(lista, "asistencia.notificacion.error_guardar", "Error al guardar: ", "Error saving: ");
        agregarTraduccion(lista, "asistencia.dialog.sos.titulo", "🚨 INFORMACIÓN DE EMERGENCIA", "🚨 EMERGENCY INFORMATION");
        agregarTraduccion(lista, "asistencia.dialog.sos.acudiente_movil", "Acudiente/Móvil", "Guardian/Phone");
        agregarTraduccion(lista, "asistencia.dialog.sos.email", "Email", "Email");
        agregarTraduccion(lista, "asistencia.dialog.sos.eps", "EPS", "Health Insurance");
        agregarTraduccion(lista, "asistencia.dialog.sos.nombre_acudiente", "Nombre Acudiente", "Guardian Name");
        agregarTraduccion(lista, "asistencia.dialog.sos.llamar_ahora", "Llamar Ahora", "Call Now");
        agregarTraduccion(lista, "asistencia.dialog.sos.sin_telefono", "Sin Teléfono Registrado", "No Phone Registered");

        // --- JudokaPlanView (Vista de Planes para Alumnos) ---
        agregarTraduccion(lista, "view.judoka.plan.titulo", "Entrenamiento de Hoy", "Today's Training");
        agregarTraduccion(lista, "lbl.selecciona.plan", "Selecciona tu Plan", "Select Your Plan");
        agregarTraduccion(lista, "lbl.progreso.cero", "0% Completado", "0% Completed");
        agregarTraduccion(lista, "lbl.progreso.dia", "Completado del Día", "Completed Today");
        agregarTraduccion(lista, "msg.entrenamiento.finalizado", "¡Entrenamiento del día finalizado! 🥋🔥", "Daily training finished! 🥋🔥");
        agregarTraduccion(lista, "btn.completado", "¡Completado!", "Completed!");
        agregarTraduccion(lista, "btn.marcar.hecho", "Marcar como Hecho", "Mark as Done");
        agregarTraduccion(lista, "msg.excelente.trabajo", "¡Excelente trabajo!", "Excellent work!");
        agregarTraduccion(lista, "msg.error.guardar", "Error al guardar", "Error saving");
        agregarTraduccion(lista, "msg.dia.descanso", "Hoy es día de descanso. ¡Recupérate!", "Today is a rest day. Recover!");

        // --- JudokaDashboardView (Dashboard Principal) ---
        agregarTraduccion(lista, "dashboard.welcome", "Hola, %s", "Hello, %s");
        agregarTraduccion(lista, "tooltip.trofeos", "Mis Trofeos e Insignias", "My Trophies & Badges");
        agregarTraduccion(lista, "tooltip.palmares", "Mi Palmarés (Medallero)", "My Record (Medals)");
        agregarTraduccion(lista, "cat.fuerza", "Fuerza", "Strength");
        agregarTraduccion(lista, "cat.velocidad", "Velocidad", "Speed");
        agregarTraduccion(lista, "cat.resistencia", "Resistencia Esp.", "Spec. Endurance");
        agregarTraduccion(lista, "cat.agilidad", "Agilidad", "Agility");
        agregarTraduccion(lista, "cat.potencia", "Potencia", "Power");
        agregarTraduccion(lista, "msg.selecciona.categoria", "Selecciona una categoría arriba para ver tu evolución.", "Select a category above to see your progress.");
        agregarTraduccion(lista, "kpi.poder_combate", "Poder de Combate", "Combat Power");
        agregarTraduccion(lista, "chart.radar.serie", "Habilidades", "Skills");
        agregarTraduccion(lista, "chart.sin_datos", "Sin datos suficientes", "Not enough data");
        agregarTraduccion(lista, "legend.progreso", "Mi Progreso", "My Progress");
        agregarTraduccion(lista, "legend.meta", "Meta Esperada", "Target Goal");
        agregarTraduccion(lista, "err.config.no_encontrada", "Configuración no encontrada", "Configuration not found");
        agregarTraduccion(lista, "badge.estado.bloqueada", "(Bloqueada)", "(Locked)");
        agregarTraduccion(lista, "empty.desc", "Realiza test físicos para desbloquear esta métrica.", "Perform physical tests to unlock this metric.");
        agregarTraduccion(lista, "btn.cerrar", "Cerrar", "Close");
        agregarTraduccion(lista, "kpi.tareas_hoy", "Agenda del Día", "Today's Agenda");

        // --- GestionUsuariosView (Admin de Usuarios) ---
        agregarTraduccion(lista, "view.gestion.usuarios.titulo", "Gestión de Usuarios", "User Management");
        agregarTraduccion(lista, "btn.nuevo.usuario", "Nuevo Usuario", "New User");
        agregarTraduccion(lista, "col.username", "Usuario", "Username");
        agregarTraduccion(lista, "col.nombre", "Nombre", "First Name");
        agregarTraduccion(lista, "col.apellido", "Apellido", "Last Name");
        agregarTraduccion(lista, "col.activo", "Activo", "Active");

        //-- REVISIÓN DE TAREAS (SenseiRevisionView)
        agregarTraduccion(lista, "revision.titulo", "Revisión de Tareas Diarias Completadas", "Daily Tasks Review");
        agregarTraduccion(lista, "revision.grid.fecha_hora", "Fecha y Hora", "Date and Time");
        agregarTraduccion(lista, "revision.grid.judoka", "Judoka", "Judoka");
        agregarTraduccion(lista, "revision.grid.tarea_realizada", "Tarea Realizada", "Task Completed");
        agregarTraduccion(lista, "revision.grid.ubicacion_gps", "Ubicación (GPS)", "Location (GPS)");
        agregarTraduccion(lista, "revision.grid.sin_gps", "Sin GPS", "No GPS");
        agregarTraduccion(lista, "revision.grid.ver_mapa", "Ver Mapa", "View Map");

        // --- AsignacionJudokasView (Sensei) ---
        agregarTraduccion(lista, "view.asignacion.titulo", "Asignación de Judokas a Grupos", "Judoka Group Assignment");
        agregarTraduccion(lista, "lbl.judokas.disponibles", "Judokas Disponibles", "Available Judokas");
        agregarTraduccion(lista, "lbl.judokas.grupo", "Judokas en el Grupo", "Judokas in Group");
        agregarTraduccion(lista, "col.nombre.completo", "Nombre Completo", "Full Name");
        agregarTraduccion(lista, "col.grado", "Grado", "Rank");
        agregarTraduccion(lista, "col.sexo", "Sexo", "Sex");
        agregarTraduccion(lista, "col.edad", "Edad", "Age");
        agregarTraduccion(lista, "col.accion", "Acción", "Action");
        agregarTraduccion(lista, "lbl.anios", "años", "years");
        agregarTraduccion(lista, "tooltip.asignar.grupo", "Asignar a", "Assign to");
        agregarTraduccion(lista, "tooltip.remover.grupo", "Remover del grupo", "Remove from group");
        agregarTraduccion(lista, "msg.exito.asignacion", "Judoka asignado correctamente a", "Judoka successfully assigned to");
        agregarTraduccion(lista, "msg.error.asignacion", "Error al asignar", "Error assigning");
        agregarTraduccion(lista, "msg.exito.remocion", "Judoka removido del grupo", "Judoka removed from group");
        agregarTraduccion(lista, "msg.error.remocion", "Error al remover", "Error removing");

        //  -- BIBLIOTECA DE TAREAS (BibliotecaView)
                agregarTraduccion(lista, "biblioteca.titulo", "Biblioteca de Tareas Diarias", "Daily Tasks Library");
        agregarTraduccion(lista, "biblioteca.boton.nueva_tarea", "Añadir Nueva Tarea", "Add New Task");
        agregarTraduccion(lista, "biblioteca.grid.nombre_tarea", "Nombre Tarea", "Task Name");
        agregarTraduccion(lista, "biblioteca.grid.meta", "Meta", "Goal");
        agregarTraduccion(lista, "biblioteca.grid.descripcion", "Descripción", "Description");
        agregarTraduccion(lista, "biblioteca.grid.video", "Video", "Video");
        agregarTraduccion(lista, "biblioteca.grid.tooltip.tiene_video", "Tiene video", "Has video");
        agregarTraduccion(lista, "biblioteca.grid.tooltip.editar_tarea", "Editar tarea", "Edit task");
        agregarTraduccion(lista, "biblioteca.grid.acciones", "Acciones", "Actions");
        agregarTraduccion(lista, "biblioteca.error.sensei_no_autenticado", "Sensei no autenticado", "Sensei not authenticated");
        agregarTraduccion(lista, "biblioteca.notificacion.tarea_guardada", "Tarea guardada: %s", "Task saved: %s");
        agregarTraduccion(lista, "biblioteca.notificacion.error_guardar", "Error al guardar: ", "Error saving: ");

// --- LoginView (Pantalla de Acceso) ---
        agregarTraduccion(lista, "app.nombre", "Club de Judo Colombia", "Judo Club Colombia");
        agregarTraduccion(lista, "login.form.titulo", "Iniciar Sesión", "Sign In");
        agregarTraduccion(lista, "login.lbl.usuario", "Usuario", "Username");
        agregarTraduccion(lista, "login.lbl.password", "Contraseña", "Password");
        agregarTraduccion(lista, "login.btn.ingresar", "Entrar", "Log in");
        agregarTraduccion(lista, "login.link.olvido", "¿Olvidaste tu contraseña?", "Forgot password?");
        agregarTraduccion(lista, "login.error.titulo", "Usuario o contraseña incorrectos", "Incorrect username or password");
        agregarTraduccion(lista, "login.error.mensaje", "Por favor, verifica tus credenciales e intenta nuevamente.", "Please check your credentials and try again.");

        // --- MainView (Ruta Raíz / Redirección) ---
        agregarTraduccion(lista, "main.cargando", "Cargando...", "Loading...");
        agregarTraduccion(lista, "main.bienvenido", "Bienvenido, %s", "Welcome, %s");
        agregarTraduccion(lista, "main.error.sin_rol_1", "No tienes un rol de 'Sensei' o 'Judoka' asignado para redirigir.", "You do not have a 'Sensei' or 'Judoka' role assigned for redirection.");
        agregarTraduccion(lista, "main.error.sin_rol_2", "Contacta al administrador para verificar tus permisos.", "Contact the administrator to verify your permissions.");
        agregarTraduccion(lista, "btn.cerrar.sesion", "Cerrar Sesión", "Log Out");

        //-- RESULTADOS (SenseiResultadosView)
        agregarTraduccion(lista, "resultados.titulo", "Registro de Resultados de Pruebas Estándar", "Standard Test Results Registration");
        agregarTraduccion(lista, "resultados.selector.judoka", "Seleccionar Judoka", "Select Judoka");
        agregarTraduccion(lista, "resultados.grid.planes.header", "Planes de Evaluación", "Evaluation Plans");
        agregarTraduccion(lista, "resultados.grid.pruebas.header", "Pruebas del Plan (Clic para registrar)", "Plan Tests (Click to register)");
        agregarTraduccion(lista, "resultados.feedback.inicio", "Resultados guardados: ", "Results saved: ");
        agregarTraduccion(lista, "resultados.feedback.sjft", "Índice SJFT: %.2f (%s). ", "SJFT Index: %.2f (%s). ");
        agregarTraduccion(lista, "resultados.feedback.prueba", "%s: %.1f -> %s. ", "%s: %.1f -> %s. ");
        agregarTraduccion(lista, "resultados.error.guardar", "Error al guardar: ", "Error saving: ");
        agregarTraduccion(lista, "resultados.sin_clasificacion", "Sin clasificación", "No classification");
        agregarTraduccion(lista, "resultados.sjft.error.faltan_datos", "Faltan datos para calcular el índice SJFT. Asegúrese de llenar todos los campos.", "Missing data to calculate SJFT index. Make sure to fill all fields.");
        agregarTraduccion(lista, "resultados.sjft.error.total_cero", "El total de proyecciones no puede ser cero.", "Total projections cannot be zero.");
        agregarTraduccion(lista, "resultados.sjft.error.metrica_no_encontrada", "Error fatal: No se encuentra la métrica 'metrica.sjft_indice.nombre'", "Fatal error: Metric 'metrica.sjft_indice.nombre' not found");
        agregarTraduccion(lista, "resultados.sjft.nota_automatica", "Índice SJFT calculado automáticamente.", "SJFT index automatically calculated.");

        //-- ENUMS de ClasificacionRendimiento (necesarios para traducción de enum)
        agregarTraduccion(lista, "enum.clasificacionrendimiento.excelente", "Excelente", "Excellent");
        agregarTraduccion(lista, "enum.clasificacionrendimiento.bueno", "Bueno", "Good");
        agregarTraduccion(lista, "enum.clasificacionrendimiento.regular", "Regular", "Regular");
        agregarTraduccion(lista, "enum.clasificacionrendimiento.mejorable", "Mejorable", "Needs Improvement");

        //  --- ENUMS ---
        agregarTraduccion(lista, "enum.metodopago.efectivo", "Efectivo", "Cash");
        agregarTraduccion(lista, "enum.metodopago.transferencia", "Transferencia", "Transfer");
        agregarTraduccion(lista, "enum.metodopago.tarjeta", "Tarjeta", "Card");
        agregarTraduccion(lista, "enum.metodopago.nequi", "Nequi", "Nequi");
        agregarTraduccion(lista, "enum.metodopago.daviplata", "Daviplata", "Daviplata");

        agregarTraduccion(lista, "enum.tipotransaccion.ingreso", "Ingreso", "Income");
        agregarTraduccion(lista, "enum.tipotransaccion.egreso", "Egreso", "Expense");

        traduccionRepository.saveAll(lista);
    }

    // --- MÉTODO HELPER
    //  (Asegúrate de tener este también en la clase) ---
    private void agregarTraduccion(List<Traduccion> lista,
                                   String clave, String textoEs, String textoEn) {
        lista.add(new Traduccion(clave, "es", textoEs));
        lista.add(new Traduccion(clave, "en", textoEn));
    }
    private Sensei crearSensei(String user,
                               String nom, String ape, String pass, GradoCinturon grado) {
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

    private List<Judoka> crearJudokas(Sensei sensei) { // <-- RECIBE SENSEI
        List<Judoka> lista = new ArrayList<>();
        lista.add(crearJudokaIndividual("maria.lopez", "María", "López", 2010, 3, 15, Sexo.FEMENINO, GradoCinturon.AMARILLO, true, "Jorge López", sensei));
        lista.add(crearJudokaIndividual("juan.gomez", "Juan Camilo", "Gómez", 2008, 7, 22, Sexo.MASCULINO, GradoCinturon.NARANJA, true, "Camilo Gómez", sensei));
        lista.add(crearJudokaIndividual("laura.ramirez", "Laura", "Ramírez", 2006, 4, 10, Sexo.FEMENINO, GradoCinturon.VERDE, false, null, sensei));
        lista.add(crearJudokaIndividual("daniel.diaz", "Daniel", "Díaz", 2003, 1, 30, Sexo.MASCULINO, GradoCinturon.NEGRO_1_DAN, true, null, sensei));
        return judokaRepository.saveAll(lista);
    }

    private Judoka crearJudokaIndividual(String user, String nom, String ape,
                                         int anio, int mes, int dia,
                                         Sexo sexo, GradoCinturon grado,
                                         boolean competidor, String acudiente,
                                         Sensei sensei) { // <-- RECIBE SENSEI
        // ... (Tu código de creación de usuario que ya tienes) ...
        Usuario u = new Usuario(user, "HASH_PENDIENTE", nom, ape);
        u.setActivo(true);
        u.getRoles().add(rolRepository.findByNombre("ROLE_JUDOKA").orElseThrow());
        if (competidor) u.getRoles().add(rolRepository.findByNombre("ROLE_COMPETIDOR").orElseThrow());
        u = usuarioService.saveUsuario(u, "123456");

        Judoka j = new Judoka();
        j.setUsuario(u);
        j.setSensei(sensei); // <--- ¡EL CANDADO SAAS SE CIERRA AQUÍ!
        j.setFechaNacimiento(LocalDate.of(anio, mes, dia));
        j.setSexo(sexo);
        j.setGrado(grado);
        j.setEsCompetidorActivo(competidor);
        j.setNombreAcudiente(acudiente);
        j.setPeso(sexo == Sexo.MASCULINO ? 73.0 : 57.0);
        j.setEstatura(sexo == Sexo.MASCULINO ? 175.0 : 160.0);
        return j;
    }
    /**
     * Crea los grupos de entrenamiento de prueba y los ASIGNA A UN SENSEI (SaaS).
     */
    private void crearGruposYAsignar(List<Judoka> judokas, Sensei senseiDuenio) {
        System.out.println("--- CREANDO GRUPOS DE ENTRENAMIENTO PARA EL DOJO DE: " + senseiDuenio.getUsuario().getNombre() + " ---");

        // 1. Grupo Infantil
        GrupoEntrenamiento grupoInfantil = new GrupoEntrenamiento();
        grupoInfantil.setNombre("Infantil Novatos");
        grupoInfantil.setDescripcion("Iniciación al Judo para menores de 12 años.");
        grupoInfantil.setSensei(senseiDuenio);
        // 2. Grupo Mayores
        GrupoEntrenamiento grupoMayores = new GrupoEntrenamiento();
        grupoMayores.setNombre("Selección Mayores");
        grupoMayores.setDescripcion("Grupo de alto rendimiento y competencia.");
        grupoMayores.setSensei(senseiDuenio);

        // Asignamos algunos judokas a los grupos para la demo
        // (Asumiendo que tienes al menos 3 judokas en la lista)
        if (judokas.size() >= 3) {
            grupoInfantil.getJudokas().add(judokas.get(0));
            grupoMayores.getJudokas().add(judokas.get(1));
            grupoMayores.getJudokas().add(judokas.get(2));
        }

        // Guardamos en la base de datos
        grupoRepository.save(grupoInfantil);
        grupoRepository.save(grupoMayores);

        System.out.println(">>> Grupos creados y asignados al Sensei con éxito.");
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
        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(sensei, "Selección Mayores").orElseThrow();
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
        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(sensei, "Selección Mayores").orElseThrow();        PlanEntrenamiento plan = new PlanEntrenamiento();
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
        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(sensei, "Selección Mayores").orElseThrow();        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
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

    private void generarResultadosEvaluacion(List<Judoka> judokas,
                                             PlanEntrenamiento planEval) {
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

    private void generarEjecucionesTareas(List<Judoka> judokas,
                                          PlanEntrenamiento planFisico) {
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
    }
    private EjercicioPlanificado obtenerOCrearEjercicioDummy(PruebaEstandar prueba) {
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

        System.out.println(">>> INICIALIZANDO CHAT SAAS...");
        Usuario kiuzo = usuarioService.findByUsername("kiuzo").orElseThrow();
        Usuario maria = usuarioService.findByUsername("maria.lopez").orElseThrow();

        // Obtenemos el perfil de Sensei de Kiuzo para el ID del Dojo
        Sensei kiuzoSensei = senseiRepository.findByUsuario(kiuzo).orElseThrow();

        // MÉTODO CORREGIDO: enviarMensajeAlDojo (autor, texto, idDojo)
        chatService.enviarMensajeAlDojo(kiuzo, "¡Bienvenidos al Chat Oficial del Club!", kiuzoSensei.getId());
        chatService.enviarMensajeAlDojo(maria, "¡Entendido Sensei!", kiuzoSensei.getId());
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

    // -------------------------------------------------------------------------
    //  DATOS DEMO: MARÍA (VETERANA) Y JULIÁN (PROMESA)
    //  Refactorizado: Ambos con Acudiente/Contacto de Emergencia.
    // -------------------------------------------------------------------------
    private void cargarDatosJulianYMaria(List<Judoka> judokas, Sensei sensei) {
        // Validación para no repetir
        if (usuarioRepository.findByUsername("julian.bohorquez").isPresent()) return;

        System.out.println(">>> CREANDO HISTORIA: MARÍA VETERANA Y JULIÁN PROMESA...");

        // 1. MARÍA LÓPEZ
        Judoka maria = judokas.stream()
                .filter(j -> j.getUsuario().getUsername().equals("maria.lopez"))
                .findFirst()
                .orElse(null);

        if (maria != null) {
            // --- DATOS FÍSICOS (Aseguramos que no sean nulos para el Wizard) ---
            maria.setPeso(58.5);
            maria.setEstatura(1.65);
            maria.setFechaNacimiento(LocalDate.of(2010, 3, 15));
            maria.setSexo(Sexo.FEMENINO);

            // --- DATOS DE DOJO ---
            maria.setGradoCinturon(GradoCinturon.AZUL);
            maria.setSensei(sensei);

            // --- DATOS LEGALES Y CONTACTO ---
            maria.setEps("Sura");
            maria.setNombreAcudiente("Jorge López (Hermano)");
            maria.setTelefonoAcudiente("315 987 6543");

            // --- LEGALIZACIÓN COMPLETA (EL BYPASS DEL WIZARD) ---
            maria.setEstado(EstadoJudoka.ACTIVO); // Ya no es PENDIENTE
            maria.setMatriculaPagada(true);       // Finanzas en verde
            maria.setRutaCertificadoEps("documentos/eps/maria_eps_2025.pdf");
            maria.setRutaAutorizacionWaiver("documentos/waivers/maria_waiver_firmado.pdf");

            // Forzamos el guardado inmediato en disco
            judokaRepository.saveAndFlush(maria);

            // Insignias María...
            insigniaRepository.findByClave("GI_CINTURON").ifPresent(insignia -> {
                if (!judokaInsigniaRepository.existsByJudokaAndInsignia_Clave(
                        maria, "GI_CINTURON")) {
                    JudokaInsignia logro = new JudokaInsignia();
                    logro.setJudoka(maria);
                    logro.setInsignia(insignia);
                    logro.setFechaObtencion(LocalDateTime.now().minusMonths(6));
                    judokaInsigniaRepository.save(logro);
                }
            });

            insigniaRepository.findByClave("SHIN_CONSTANCIA").
                    ifPresent(insignia -> {
                        if (!judokaInsigniaRepository.existsByJudokaAndInsignia_Clave(maria, "SHIN_CONSTANCIA")) {
                            JudokaInsignia logro = new JudokaInsignia();
                            logro.setJudoka(maria);
                            logro.setInsignia(insignia);
                            logro.setFechaObtencion(LocalDateTime.now().minusMonths(2));
                            judokaInsigniaRepository.save(logro);
                        }
                    });
        }

        // 2. JULIÁN ANDRÉS
        Usuario userJulian = new Usuario();
        userJulian.setUsername("julian.bohorquez");
        userJulian.setPasswordHash(passwordEncoder.encode("1234"));
        userJulian.setNombre("Julián Andrés");
        userJulian.setApellido("Bohórquez Díaz");
        userJulian.setEmail("julian@judocolombia.com");
        userJulian.setActivo(true);

        Rol rolJudoka = rolRepository.findByNombre("ROLE_JUDOKA")
                .orElseThrow(() -> new RuntimeException("ERROR CRÍTICO: No existe el rol ROLE_JUDOKA en la BD"));

        userJulian.setRoles(new HashSet<>(Set.of(rolJudoka)));

        usuarioRepository.save(userJulian);

        // B. Crear Perfil Judoka
        Judoka julian = new Judoka();
        julian.setUsuario(userJulian);
        julian.setSensei(sensei);
        julian.setGradoCinturon(GradoCinturon.BLANCO);
        julian.setFechaNacimiento(LocalDate.now().minusYears(10));
        julian.setPeso(34.0);
        julian.setEstatura(1.38);
        julian.setSexo(Sexo.MASCULINO);
        julian.setEps("Sanitas");
        julian.setNombreAcudiente("Carlos Bohórquez (Padre)");
        julian.setTelefonoAcudiente("300 123 4567");
        julian.setRutaCertificadoEps("documentos/eps/julian_eps_2025.pdf");
        julian.setRutaAutorizacionWaiver("documentos/waivers/julian_waiver_firmado.pdf");

        judokaRepository.save(julian);

        // C. Gamificación Inicial
        insigniaRepository.findByClave("SHIN_INICIO").ifPresent(insignia -> {
            JudokaInsignia logro = new JudokaInsignia();
            logro.setJudoka(julian);
            logro.setInsignia(insignia);
            logro.setFechaObtencion(LocalDateTime.now().minusDays(5));
            judokaInsigniaRepository.save(logro);
        });

        insigniaRepository.findByClave("TAI_VELOCIDAD").ifPresent(insignia -> {
            JudokaInsignia logro = new JudokaInsignia();
            logro.setJudoka(julian);
            logro.setInsignia(insignia);
            logro.setFechaObtencion(LocalDateTime.now().minusDays(1));
            judokaInsigniaRepository.save(logro);
        });
        // --- CARGAR PALMARES ---
        // 1. Palmarés de María (Experimentada)
        if (maria != null && palmaresRepo.findByJudokaOrderByFechaDesc(maria).isEmpty()) {
            System.out.println(">>> GENERANDO PALMARÉS PARA MARÍA...");

            palmaresRepo.saveAll(List.of(
                    new ParticipacionCompetencia(maria, "Campeonato Nacional Mayores", "Bogotá",
                            LocalDate.now().minusMonths(2), NivelCompetencia.NACIONAL, ResultadoCompetencia.ORO,
                            "https://www.youtube.com/watch?v=dQw4w9WgXcQ"), // Link demo (cambiar por real de Judo)

                    new ParticipacionCompetencia(maria, "Departamental Open Valle", "Cali",
                            LocalDate.now().minusMonths(5), NivelCompetencia.DEPARTAMENTAL, ResultadoCompetencia.PLATA,
                            null),

                    new ParticipacionCompetencia(maria, "Copa Internacional Andina", "Quito",
                            LocalDate.now().minusYears(1), NivelCompetencia.INTERNACIONAL, ResultadoCompetencia.QUINTO,
                            "https://www.youtube.com/watch?v=VIDEO_ID_HERE")
            ));
        }

        // 2. Palmarés de Julián (Novato)
        if (julian != null && palmaresRepo.findByJudokaOrderByFechaDesc(julian).isEmpty()) {
            System.out.println(">>> GENERANDO PALMARÉS PARA JULIÁN...");

            palmaresRepo.save(new ParticipacionCompetencia(julian, "Festival Infantil Local", "Bucaramanga",
                    LocalDate.now().minusWeeks(3), NivelCompetencia.LOCAL, ResultadoCompetencia.BRONCE,
                    "https://www.youtube.com/watch?v=KID_JUDO_VIDEO"));
        }

        System.out.println("   -> Julián Andrés creado (User: julian.bohorquez / Pass: 1234).");
    }
    private void crearTraduccionesComunidad() {
        // Validación básica
        if (traduccionRepository.findByClaveAndIdioma("comunidad.tab.muro", "es").isPresent()) return;

        System.out.println(">>> CARGANDO TRADUCCIONES DE COMUNIDAD...");
        List<Traduccion> lista = new ArrayList<>();

        // --- PESTAÑAS Y TÍTULOS ---
        agregarTraduccion(lista, "comunidad.tab.muro", "Muro del Dojo", "Dojo Wall");
        agregarTraduccion(lista, "comunidad.tab.chat", "Chat Grupal", "Group Chat");

        // --- CREAR PUBLICACIÓN ---
        agregarTraduccion(lista, "comunidad.post.placeholder", "¿Qué entrenaste hoy? Comparte tu progreso...", "What did you train today? Share your progress...");
        agregarTraduccion(lista, "comunidad.btn.subir_foto", "Subir Foto/Video", "Upload Photo/Video");
        agregarTraduccion(lista, "comunidad.label.drop", "Arrastra archivos aquí...", "Drag files here...");
        agregarTraduccion(lista, "comunidad.btn.publicar", "Publicar", "Post");
        agregarTraduccion(lista, "comunidad.msg.publicado", "¡Publicado en el muro!", "Posted on the wall!");
        agregarTraduccion(lista, "comunidad.msg.archivo_listo", "Archivo listo", "File ready");
        agregarTraduccion(lista, "comunidad.warn.empty_post", "Escribe algo o sube una foto", "Write something or upload a photo");

        // --- TARJETAS Y COMENTARIOS ---
        agregarTraduccion(lista, "comunidad.btn.comentar", "Comentar", "Comment");
        agregarTraduccion(lista, "comunidad.comment.placeholder", "Escribe una respuesta...", "Write a reply...");
        agregarTraduccion(lista, "comunidad.msg.comment_sent", "Comentario enviado", "Comment sent");
        agregarTraduccion(lista, "comunidad.label.image_of", "Imagen de", "Image of");

        // --- CHAT ---
        agregarTraduccion(lista, "comunidad.chat.escribir", "Escribe un mensaje...", "Type a message...");
        agregarTraduccion(lista, "comunidad.chat.enviar", "Enviar", "Send");

        // --- ERRORES GENÉRICOS (Opcional, reutilizable) ---
        agregarTraduccion(lista, "error.generic", "Ha ocurrido un error", "An error occurred");
        agregarTraduccion(lista, "error.upload", "Error al subir archivo", "Error uploading file");

        traduccionRepository.saveAll(lista);
    }
    private void cargarTraducciones(TraduccionRepository repo) {
        System.out.println("🌍 Verificando traducciones del sistema...");
        crearTraduccionesDashboard(repo);
        crearTraduccionesDias(repo);
        crearTraduccionesFestivos(repo);
        crearTraduccionesGenerales(repo);
        // --- DASHBOARD & SALUDOS ---
        crearSiNoExiste(repo, "dashboard.welcome", "es", "Hola {0}");
        crearSiNoExiste(repo, "dashboard.welcome", "en", "Hello {0}");
        crearSiNoExiste(repo, "app.nombre", "es", "Club Judo Colombia");

        // --- KPI CARDS ---
        crearSiNoExiste(repo, "kpi.tareas_hoy", "es", "Tareas de Hoy");
        crearSiNoExiste(repo, "kpi.asistencia_mes", "es", "Asistencia Mes");
        crearSiNoExiste(repo, "kpi.nivel_tecnico", "es", "Nivel Técnico");
        crearSiNoExiste(repo, "kpi.proximo_evento", "es", "Próximo Evento");

        // --- MENÚS ---
        crearSiNoExiste(repo, "menu.dashboard", "es", "Dashboard");
        crearSiNoExiste(repo, "menu.dashboard", "en", "Dashboard");
        crearSiNoExiste(repo, "menu.mis.planes", "es", "Mis Planes");
        crearSiNoExiste(repo, "menu.mis.planes", "en", "My Plans");
        crearSiNoExiste(repo, "menu.comunidad", "es", "Comunidad");
        crearSiNoExiste(repo, "menu.comunidad", "en", "Community");
        crearSiNoExiste(repo, "menu.mi.perfil", "es", "Mi Perfil");
        crearSiNoExiste(repo, "btn.cerrar.sesion", "es", "Cerrar Sesión");
        crearSiNoExiste(repo, "btn.cerrar.sesion", "en", "Logout");


        // --- VISTAS SENSEI ---
        crearSiNoExiste(repo, "view.sensei.plan.titulo", "es", "Gestión de Planes");
        crearSiNoExiste(repo, "view.sensei.plan.nuevo", "es", "Nuevo Plan");

        // --- ADMISIONES ---
        crearSiNoExiste(repo, "admisiones.titulo", "es", "Validación de Ingresos");
        crearSiNoExiste(repo, "admisiones.descripcion", "es", "Revise los documentos y pagos de los aspirantes.");
        crearSiNoExiste(repo, "admisiones.grid.registrado", "es", "Registrado");
        crearSiNoExiste(repo, "admisiones.grid.documentos", "es", "Documentos");
        crearSiNoExiste(repo, "admisiones.grid.pago", "es", "Pago Matrícula");
        crearSiNoExiste(repo, "admisiones.btn.marcar_pago", "es", "Marcar Pago Manual");
        crearSiNoExiste(repo, "admisiones.msg.activado", "es", "¡Judoka activado con éxito!");
        crearSiNoExiste(repo, "admisiones.msg.rechazado", "es", "Aspirante rechazado.");
        crearSiNoExiste(repo, "menu.invitar", "es", "Invitar Aspirante.");

        // --- FINANZAS / TESORERÍA ---
        crearSiNoExiste(repo, "finanzas.titulo", "es", "Gestión Financiera");
        crearSiNoExiste(repo, "finanzas.tab.ingreso", "es", "Registrar Ingreso");
        crearSiNoExiste(repo, "finanzas.tab.gasto", "es", "Registrar Gasto");
        crearSiNoExiste(repo, "finanzas.tab.balance", "es", "Balance y Reportes");
        crearSiNoExiste(repo, "finanzas.label.alumno", "es", "Alumno (Opcional)");
        crearSiNoExiste(repo, "finanzas.placeholder.buscar_alumno", "es", "Buscar por nombre...");
        crearSiNoExiste(repo, "finanzas.label.concepto", "es", "Concepto");
        crearSiNoExiste(repo, "finanzas.label.valor", "es", "Valor ($)");
        crearSiNoExiste(repo, "finanzas.label.valor_sugerido", "es", "Valor Sugerido");
        crearSiNoExiste(repo, "finanzas.label.metodo_pago", "es", "Método de Pago");
        crearSiNoExiste(repo, "finanzas.label.observacion", "es", "Observación");
        crearSiNoExiste(repo, "finanzas.label.categoria_gasto", "es", "Categoría de Gasto");
        crearSiNoExiste(repo, "finanzas.label.valor_pagado", "es", "Valor Pagado ($)");
        crearSiNoExiste(repo, "finanzas.label.detalle_proveedor", "es", "Detalle / Proveedor");
        crearSiNoExiste(repo, "finanzas.label.nombre_concepto", "es", "Nombre del Concepto");
        crearSiNoExiste(repo, "finanzas.btn.registrar_ingreso", "es", "Cobrar e Imprimir Recibo");
        crearSiNoExiste(repo, "finanzas.btn.registrar_gasto", "es", "Registrar Salida");
        crearSiNoExiste(repo, "finanzas.btn.foto_factura", "es", "Foto Factura");
        crearSiNoExiste(repo, "finanzas.msg.ingreso_exito", "es", "Ingreso registrado correctamente");
        crearSiNoExiste(repo, "finanzas.msg.gasto_exito", "es", "Gasto registrado correctamente");
        crearSiNoExiste(repo, "finanzas.msg.soporte_cargado", "es", "Soporte cargado");
        crearSiNoExiste(repo, "finanzas.msg.concepto_creado", "es", "Concepto Financiero Creado");
        crearSiNoExiste(repo, "finanzas.error.campos_obligatorios", "es", "Por favor complete los campos obligatorios");
        crearSiNoExiste(repo, "finanzas.kpi.ingresos", "es", "Ingresos Mes");
        crearSiNoExiste(repo, "finanzas.kpi.egresos", "es", "Egresos Mes");
        crearSiNoExiste(repo, "finanzas.kpi.balance", "es", "Balance Neto");
        crearSiNoExiste(repo, "finanzas.grid.fecha", "es", "Fecha");
        crearSiNoExiste(repo, "finanzas.grid.tipo", "es", "Tipo");
        crearSiNoExiste(repo, "finanzas.grid.soporte", "es", "Soporte");
        crearSiNoExiste(repo, "finanzas.dialog.nuevo_concepto_titulo", "es", "Nuevo Concepto de");

        // --- AGENDA GPS ---
        crearSiNoExiste(repo, "agenda.titulo", "es", "Agenda & GPS");
        crearSiNoExiste(repo, "agenda.btn.nueva", "es", "Nueva Sesión");
        crearSiNoExiste(repo, "agenda.grid.sesion", "es", "Sesión");
        crearSiNoExiste(repo, "agenda.tooltip.gps_activo", "es", "GPS Activo");
        crearSiNoExiste(repo, "agenda.tooltip.sin_gps", "es", "Sin restricción GPS");
        crearSiNoExiste(repo, "agenda.dialog.programar", "es", "Programar Sesión");
        crearSiNoExiste(repo, "agenda.dialog.editar", "es", "Editar Sesión");
        crearSiNoExiste(repo, "agenda.field.nombre", "es", "Nombre de la Sesión");
        crearSiNoExiste(repo, "agenda.default.entrenamiento", "es", "Entrenamiento Regular");
        crearSiNoExiste(repo, "agenda.section.gps", "es", "Configuración GPS (Opcional)");
        crearSiNoExiste(repo, "agenda.field.latitud", "es", "Latitud");
        crearSiNoExiste(repo, "agenda.field.longitud", "es", "Longitud");
        crearSiNoExiste(repo, "agenda.field.radio", "es", "Radio Permitido (metros)");

        // --- CHECK-IN WIDGET ---
        crearSiNoExiste(repo, "checkin.titulo", "es", "Control de Asistencia GPS");
        crearSiNoExiste(repo, "checkin.status.ready", "es", "Listo para verificar ubicación.");
        crearSiNoExiste(repo, "checkin.btn.marcar", "es", "Marcar Asistencia");
        crearSiNoExiste(repo, "checkin.status.locating", "es", "Localizando...");
        crearSiNoExiste(repo, "checkin.status.requesting", "es", "Solicitando permiso GPS...");
        crearSiNoExiste(repo, "checkin.btn.retry", "es", "Reintentar Check-in");
        crearSiNoExiste(repo, "checkin.error.denied", "es", "Error GPS: Permiso denegado.");
        crearSiNoExiste(repo, "checkin.error.browser", "es", "ERROR: Debes habilitar el GPS y dar permiso.");
        crearSiNoExiste(repo, "checkin.status.validating", "es", "Coordenadas recibidas. Validando distancia...");
        crearSiNoExiste(repo, "checkin.btn.success", "es", "¡Asistencia Marcada!");
        crearSiNoExiste(repo, "checkin.status.registered", "es", "Te has registrado correctamente.");
        crearSiNoExiste(repo, "checkin.msg.oss", "es", "¡Asistencia registrada! Oss.");

        // --- INVENTARIO ---
        crearSiNoExiste(repo, "inventario.titulo", "es", "Tienda del Dojo");
        crearSiNoExiste(repo, "inventario.btn.nuevo", "es", "Nuevo Producto");
        crearSiNoExiste(repo, "inventario.grid.articulo", "es", "Artículo");
        crearSiNoExiste(repo, "inventario.grid.stock", "es", "Stock");
        crearSiNoExiste(repo, "inventario.grid.venta", "es", "Precio Venta");
        crearSiNoExiste(repo, "inventario.grid.costo", "es", "Costo");
        crearSiNoExiste(repo, "inventario.status.agotado", "es", "AGOTADO");
        crearSiNoExiste(repo, "inventario.tooltip.add_stock", "es", "Agregar Stock");
        crearSiNoExiste(repo, "inventario.dialog.venta", "es", "Registrar Venta");
        crearSiNoExiste(repo, "inventario.msg.venta_ok", "es", "Venta registrada y descontada del inventario");
        crearSiNoExiste(repo, "inventario.dialog.stock", "es", "Reabastecer Stock");
        crearSiNoExiste(repo, "inventario.field.cantidad_ingreso", "es", "Cantidad a Ingresar");
        crearSiNoExiste(repo, "inventario.dialog.nuevo", "es", "Nuevo Producto");
        crearSiNoExiste(repo, "inventario.dialog.editar", "es", "Editar Producto");
        crearSiNoExiste(repo, "inventario.field.costo", "es", "Costo Compra ($)");
        crearSiNoExiste(repo, "inventario.field.precio", "es", "Precio Venta ($)");
        crearSiNoExiste(repo, "inventario.field.stock_inicial", "es", "Stock Inicial");

        // --- GESTIÓN DE GRUPOS ---
        crearSiNoExiste(repo, "grupos.titulo", "es", "Gestión de Grupos");
        crearSiNoExiste(repo, "grupos.btn.nuevo", "es", "Nuevo Grupo");
        crearSiNoExiste(repo, "grupos.grid.nombre", "es", "Nombre Grupo");
        crearSiNoExiste(repo, "grupos.grid.descripcion", "es", "Descripción");
        crearSiNoExiste(repo, "grupos.grid.miembros", "es", "Miembros");
        crearSiNoExiste(repo, "grupos.label.alumnos", "es", "alumnos");
        crearSiNoExiste(repo, "grupos.tooltip.gestionar_miembros", "es", "Gestionar Miembros");
        crearSiNoExiste(repo, "grupos.dialog.miembros.titulo", "es", "Miembros de");
        crearSiNoExiste(repo, "grupos.field.buscar_alumno", "es", "Buscar alumno para agregar...");
        crearSiNoExiste(repo, "grupos.section.agregar", "es", "Agregar Nuevo Miembro");
        crearSiNoExiste(repo, "grupos.section.actuales", "es", "Miembros Actuales");

        // --- CAMPEONATOS ---
        crearSiNoExiste(repo, "campeonatos.titulo", "es", "Gestión de Campeonatos");
        crearSiNoExiste(repo, "campeonatos.btn.nueva_convocatoria", "es", "Nueva Convocatoria");
        crearSiNoExiste(repo, "campeonatos.grid.evento", "es", "Evento");
        crearSiNoExiste(repo, "campeonatos.grid.resultado", "es", "Resultado");
        crearSiNoExiste(repo, "campeonatos.dialog.convocatoria.titulo", "es", "Crear Convocatoria");
        crearSiNoExiste(repo, "campeonatos.field.nombre_evento", "es", "Nombre del Evento");
        crearSiNoExiste(repo, "campeonatos.field.lugar", "es", "Lugar/Ciudad");
        crearSiNoExiste(repo, "campeonatos.field.nivel", "es", "Nivel Competitivo");
        crearSiNoExiste(repo, "campeonatos.field.seleccionar_atletas", "es", "Seleccionar Atletas");
        crearSiNoExiste(repo, "campeonatos.msg.inscritos", "es", "atletas inscritos exitosamente.");
        crearSiNoExiste(repo, "campeonatos.dialog.resultado.titulo", "es", "Resultado:");
        crearSiNoExiste(repo, "campeonatos.field.medalla", "es", "Medalla / Puesto");
        crearSiNoExiste(repo, "campeonatos.field.link_video", "es", "Link Video (YouTube)");

        // --- CAMPOS DE ENTRENAMIENTO ---
        crearSiNoExiste(repo, "campos.titulo", "es", "Campos de Entrenamiento");
        crearSiNoExiste(repo, "campos.btn.programar", "es", "Programar Campo");
        crearSiNoExiste(repo, "campos.grid.nombre", "es", "Campo / Evento");
        crearSiNoExiste(repo, "campos.estado.en_curso", "es", "En Curso");
        crearSiNoExiste(repo, "campos.btn.certificar", "es", "Certificar Cumplimiento");
        crearSiNoExiste(repo, "campos.dialog.programar.titulo", "es", "Programar Campo");
        crearSiNoExiste(repo, "campos.field.nombre", "es", "Nombre del Campo");
        crearSiNoExiste(repo, "campos.placeholder.ej_campamento", "es", "Ej: Campamento de Altura");
        crearSiNoExiste(repo, "campos.field.lugar", "es", "Ubicación");
        crearSiNoExiste(repo, "campos.field.objetivo", "es", "Enfoque / Objetivo");
        crearSiNoExiste(repo, "campos.placeholder.ej_tactico", "es", "Ej: Táctico Competitivo");
        crearSiNoExiste(repo, "campos.field.convocados", "es", "Convocados");
        crearSiNoExiste(repo, "campos.msg.programado", "es", "Campo programado para");
        crearSiNoExiste(repo, "campos.dialog.certificar.titulo", "es", "Certificar:");
        crearSiNoExiste(repo, "campos.label.pregunta_cumplimiento", "es", "¿El judoka completó satisfactoriamente el campo?");
        crearSiNoExiste(repo, "campos.field.puntos_ascenso", "es", "Puntos de Ascenso a Otorgar");
        crearSiNoExiste(repo, "campos.btn.confirmar_puntos", "es", "Certificar y Otorgar Puntos");

        // --- COMUNIDAD ---
        crearSiNoExiste(repo, "comunidad.tab.muro", "es", "Muro del Dojo", "Dojo Wall");
        crearSiNoExiste(repo, "comunidad.tab.chat", "es", "Chat Grupal", "Group Chat");
        crearSiNoExiste(repo, "comunidad.post.placeholder", "es", "Comparte algo con el dojo...", "Share something...");
        crearSiNoExiste(repo, "comunidad.btn.subir_foto", "es", "Subir Foto/Video", "Upload Photo/Video");
        crearSiNoExiste(repo, "comunidad.label.drop", "es", "Arrastra archivos aquí...", "Drag files here...");
        crearSiNoExiste(repo, "comunidad.btn.publicar", "es", "Publicar", "Post");
        crearSiNoExiste(repo, "comunidad.msg.publicado", "es", "¡Publicado en el muro!", "Posted on the wall!");
        crearSiNoExiste(repo, "comunidad.msg.archivo_listo", "es", "Archivo listo", "File ready");
        crearSiNoExiste(repo, "comunidad.warn.empty_post", "es", "Escribe algo o sube una foto", "Write something or upload a photo");
        crearSiNoExiste(repo, "comunidad.btn.comentar", "es", "Comentar", "Comment");
        crearSiNoExiste(repo, "comunidad.comment.placeholder", "es", "Escribe una respuesta...", "Write a reply...");
        crearSiNoExiste(repo, "comunidad.msg.comment_sent", "es", "Comentario enviado", "Comment sent");
        crearSiNoExiste(repo, "comunidad.label.image_of", "es", "Imagen de", "Image of");
        crearSiNoExiste(repo, "comunidad.chat.escribir", "es", "Escribe un mensaje...", "Type a message...");
        crearSiNoExiste(repo, "comunidad.chat.enviar", "es", "Enviar", "Send");

        // --- ADMINISTRACIÓN ---
        crearSiNoExiste(repo, "admin.titulo", "es", "Configuración del Sistema");
        crearSiNoExiste(repo, "admin.descripcion", "es", "Ajuste los parámetros globales de la organización.");
        crearSiNoExiste(repo, "admin.field.nombre_org", "es", "Nombre de la Organización");
        crearSiNoExiste(repo, "admin.field.nivel", "es", "Nivel Organizacional");
        crearSiNoExiste(repo, "admin.helper.nivel", "es", "Define el alcance (Club, Liga o Federación)");
        crearSiNoExiste(repo, "admin.field.telefono", "es", "Teléfono de Contacto");
        crearSiNoExiste(repo, "admin.field.email", "es", "Email de Soporte");
        crearSiNoExiste(repo, "admin.field.moneda", "es", "Moneda (ISO 4217)");
        crearSiNoExiste(repo, "admin.note.title", "es", "Nota Importante:");
        crearSiNoExiste(repo, "admin.note.text", "es", "Los cambios en el 'Nivel Organizacional' pueden habilitar o deshabilitar módulos específicos.\nAsegúrese de guardar antes de salir.");
        crearSiNoExiste(repo, "msg.success.config_saved", "es", "Configuración guardada correctamente.");

        // --- MENSAJES DE ÉXITO Y ERROR ---
        crearSiNoExiste(repo, "msg.success.saved", "es", "Guardado exitosamente");
        crearSiNoExiste(repo, "msg.success.updated", "es", "Actualizado exitosamente");
        crearSiNoExiste(repo, "msg.success.deleted", "es", "Eliminado exitosamente");
        crearSiNoExiste(repo, "msg.success.added", "es", "Agregado exitosamente");
        crearSiNoExiste(repo, "msg.success.removed", "es", "Removido exitosamente");
        crearSiNoExiste(repo, "msg.success.payment_manual", "es", "Pago registrado manualmente.");
        crearSiNoExiste(repo, "error.generic", "es", "Ha ocurrido un error");
        crearSiNoExiste(repo, "error.upload", "es", "Error al subir archivo");
        crearSiNoExiste(repo, "error.archivo_perdido", "es", "Archivo físico no encontrado");
        crearSiNoExiste(repo, "error.grupo_no_guardado", "es", "Primero guarde el grupo antes de eliminarlo.");
        crearSiNoExiste(repo, "error.campos_obligatorios", "es", "Campos obligatorios incompletos");

        // InvitarAspiranteView
        crearSiNoExiste(repo,"vista.invitar.titulo", "es","Invitar al Club","Invite to the Club");
        crearSiNoExiste(repo,"vista.invitar.descripcion","es", "Envía un enlace a su email", "Send email link");
        crearSiNoExiste(repo,"label.nombre", "es","Nombre","First Name");
        crearSiNoExiste(repo,"label.apellido", "es","Apellido","Last Name");

        crearSiNoExiste(repo,"label.email", "es","Correo Electrónico","E mail");

        crearSiNoExiste(repo,"boton.enviar_invitacion", "es","Enviar","Send");
        crearSiNoExiste(repo,"error.campos_incompletos", "es","Por favor, llene todos los campos !","Please fill all the blanks !");
        crearSiNoExiste(repo,"exito.invitacion_enviada", "es","Invitación enviada con éxito","Invitation sent successfully");
        crearSiNoExiste(repo,"error.sistema", "es","Error del sistema, trata de nuevo por favor.","System error, please try again.");
        // --- ENUMS (Traducciones de sistema) ---
        crearSiNoExiste(repo, "enum.tipotransaccion.ingreso", "es", "Ingreso");
        crearSiNoExiste(repo, "enum.tipotransaccion.egreso", "es", "Egreso/Gasto");
        crearSiNoExiste(repo, "enum.metodopago.efectivo", "es", "Efectivo");
        crearSiNoExiste(repo, "enum.metodopago.transferencia", "es", "Transferencia");
        crearSiNoExiste(repo, "enum.metodopago.tarjeta", "es", "Tarjeta");
        crearSiNoExiste(repo, "enum.resultadocompetencia.participacion", "es", "Participación");
        crearSiNoExiste(repo, "enum.resultadocompetencia.oro", "es", "Oro 🥇");
        crearSiNoExiste(repo, "enum.resultadocompetencia.plata", "es", "Plata 🥈");
        crearSiNoExiste(repo, "enum.resultadocompetencia.bronce", "es", "Bronce 🥉");
        crearSiNoExiste(repo, "enum.nivelcompetencia.departamental", "es", "Departamental");
        crearSiNoExiste(repo, "enum.nivelcompetencia.nacional", "es", "Nacional");
        crearSiNoExiste(repo, "enum.nivelcompetencia.internacional", "es", "Internacional");
        crearSiNoExiste(repo, "enum.nivelcompetencia.club", "es", "Interno (Club)");
        crearSiNoExiste(repo, "enum.nivelorganizacional.club.nombre", "es", "Club");
        crearSiNoExiste(repo, "enum.nivelorganizacional.liga.nombre", "es", "Liga");
        crearSiNoExiste(repo, "enum.nivelorganizacional.federacion.nombre", "es", "Federación");
    }
    /**
     * Método auxiliar robusto para evitar Duplicate Entry.
     * Verifica si la clave ya existe antes de insertar.
     */
    private void crearTraduccionesDashboard(TraduccionRepository repo) {
        // Saludos y Títulos
        crearSiNoExiste(repo, "dashboard.titulo", "es","Tablero Sensei");
        crearSiNoExiste(repo, "dashboard.welcome", "es", "Hola {0}");
        crearSiNoExiste(repo, "app.nombre", "es", "Club Judo Colombia");

        // Tarjetas KPI (Indicadores Clave)
        crearSiNoExiste(repo, "kpi.tareas_hoy", "es", "Tareas de Hoy");
        crearSiNoExiste(repo, "kpi.asistencia_mes", "es", "Asistencia Mes");
        crearSiNoExiste(repo, "kpi.nivel_tecnico", "es", "Nivel Técnico");
        crearSiNoExiste(repo, "kpi.proximo_evento", "es", "Próximo Evento");
        crearSiNoExiste(repo, "kpi.estado_fisico", "es", "Estado Físico");
        crearSiNoExiste(repo, "kpi.tecnica", "es", "Técnica");

        // Gráficos
        crearSiNoExiste(repo, "chart.poder_combate", "es", "Poder de Combate");
        crearSiNoExiste(repo, "chart.asistencia", "es", "Histórico de Asistencia");
        crearSiNoExiste(repo, "chart.progreso_tecnico", "es", "Progreso Técnico");
        crearSiNoExiste(repo, "menu.sensei.dashboard", "es", "Dashboard");
        crearSiNoExiste(repo, "menu.sensei.comunidad", "es", "Dojo virtual");
        crearSiNoExiste(repo, "menu.sensei.admisiones", "es", "Admisiones");
        crearSiNoExiste(repo, "menu.sensei.grupos", "es", "Gestión de grupos");
        crearSiNoExiste(repo, "menu.sensei.asistencia", "es", "Control Asistencia");

        // Layout
        crearSiNoExiste(repo, "menu.asistencia", "es", "Control Asistencia");
        crearSiNoExiste(repo, "menu.biblioteca", "es", "Biblioteca Ejercicios");
        crearSiNoExiste(repo, "menu.resultados", "es", "Resultados Tests");
    }

    private void crearTraduccionesDias(TraduccionRepository repo) {
        // Días de la semana (Usados en Calendario y Planes)
        crearSiNoExiste(repo, "MONDAY", "es", "Lunes");
        crearSiNoExiste(repo, "TUESDAY", "es", "Martes");
        crearSiNoExiste(repo, "WEDNESDAY", "es", "Miércoles");
        crearSiNoExiste(repo, "THURSDAY", "es", "Jueves");
        crearSiNoExiste(repo, "FRIDAY", "es", "Viernes");
        crearSiNoExiste(repo, "SATURDAY", "es", "Sábado");
        crearSiNoExiste(repo, "SUNDAY", "es", "Domingo");

        // Meses (Opcional, si usas formatos personalizados)
        crearSiNoExiste(repo, "month.january", "es", "Enero");
        // ... etc
    }

    private void crearTraduccionesFestivos(TraduccionRepository repo) {
        // Festivos de Colombia (Para que el calendario los muestre bonitos)
        crearSiNoExiste(repo, "holiday.ano_nuevo", "es", "Año Nuevo");
        crearSiNoExiste(repo, "holiday.reyes_magos", "es", "Reyes Magos");
        crearSiNoExiste(repo, "holiday.san_jose", "es", "Día de San José");
        crearSiNoExiste(repo, "holiday.jueves_santo", "es", "Jueves Santo");
        crearSiNoExiste(repo, "holiday.viernes_santo", "es", "Viernes Santo");
        crearSiNoExiste(repo, "holiday.dia_trabajo", "es", "Día del Trabajo");
        crearSiNoExiste(repo, "holiday.ascension", "es", "Ascensión del Señor");
        crearSiNoExiste(repo, "holiday.corpus_christi", "es", "Corpus Christi");
        crearSiNoExiste(repo, "holiday.sagrado_corazon", "es", "Sagrado Corazón");
        crearSiNoExiste(repo, "holiday.san_pedro", "es", "San Pedro y San Pablo");
        crearSiNoExiste(repo, "holiday.independencia", "es", "Día de la Independencia");
        crearSiNoExiste(repo, "holiday.batalla_boyaca", "es", "Batalla de Boyacá");
        crearSiNoExiste(repo, "holiday.asuncion", "es", "La Asunción");
        crearSiNoExiste(repo, "holiday.raza", "es", "Día de la Raza");
        crearSiNoExiste(repo, "holiday.todos_santos", "es", "Todos los Santos");
        crearSiNoExiste(repo, "holiday.independencia_cartagena", "es", "Independencia de Cartagena");
        crearSiNoExiste(repo, "holiday.inmaculada", "es", "Inmaculada Concepción");
        crearSiNoExiste(repo, "holiday.navidad", "es", "Navidad");
    }

    private void crearTraduccionesGenerales(TraduccionRepository repo) {
        // --- BOTONES GENÉRICOS ---
        crearSiNoExiste(repo, "btn.guardar", "es", "Guardar");
        crearSiNoExiste(repo, "btn.cancelar", "es", "Cancelar");
        crearSiNoExiste(repo, "btn.editar", "es", "Editar");
        crearSiNoExiste(repo, "btn.eliminar", "es", "Eliminar");
        crearSiNoExiste(repo, "btn.crear", "es", "Crear");
        crearSiNoExiste(repo, "btn.ver_pdf", "es", "Ver PDF");
        crearSiNoExiste(repo, "btn.activar", "es", "Activar");
        crearSiNoExiste(repo, "btn.rechazar", "es", "Rechazar");
        crearSiNoExiste(repo, "btn.agregar", "es", "Agregar");
        crearSiNoExiste(repo, "btn.quitar", "es", "Quitar");
        crearSiNoExiste(repo, "btn.cerrar", "es", "Cerrar");
        crearSiNoExiste(repo, "btn.vender", "es", "Vender");
        crearSiNoExiste(repo, "btn.confirmar", "es", "Confirmar");
        crearSiNoExiste(repo, "btn.actualizar", "es", "Actualizar");
        crearSiNoExiste(repo, "btn.guardar_cambios", "es", "Guardar Cambios");
        crearSiNoExiste(repo, "registro.titulo", "es", "Registro de Aspirante");
        crearSiNoExiste(repo, "registro.subtitulo", "es", "Únete a nuestro Dojo");
        crearSiNoExiste(repo, "registro.btn.siguiente", "es", "Siguiente");
        crearSiNoExiste(repo, "registro.btn.volver", "es", "Ya tengo cuenta");
        crearSiNoExiste(repo, "registro.exito", "es", "Registro Exitoso. ¡Bienvenido!");
        crearSiNoExiste(repo, "error.usuario.existe", "es", "Este correo ya está registrado.");
        crearSiNoExiste(repo, "login.btn.registrar", "es", "¿No tienes cuenta? Regístrate aquí.");
        // --- TEXTOS GENÉRICOS ---
        crearSiNoExiste(repo, "generic.fecha", "es", "Fecha");
        crearSiNoExiste(repo, "generic.nombre", "es", "Nombre");
        crearSiNoExiste(repo, "generic.descripcion", "es", "Descripción");
        crearSiNoExiste(repo, "generic.acciones", "es", "Acciones");
        crearSiNoExiste(repo, "generic.estado", "es", "Estado");
        crearSiNoExiste(repo, "generic.cantidad", "es", "Cantidad");
        crearSiNoExiste(repo, "generic.judoka", "es", "Judoka");
        crearSiNoExiste(repo, "generic.grupo", "es", "Grupo");
        crearSiNoExiste(repo, "generic.horario", "es", "Horario");
        crearSiNoExiste(repo, "generic.tipo", "es", "Tipo");
        crearSiNoExiste(repo, "generic.inicio", "es", "Inicio");
        crearSiNoExiste(repo, "generic.fin", "es", "Fin");
        crearSiNoExiste(repo, "generic.fecha_inicio", "es", "Fecha Inicio");
        crearSiNoExiste(repo, "generic.fecha_fin", "es", "Fecha Fin");
        crearSiNoExiste(repo, "generic.pts", "es", "pts");
        crearSiNoExiste(repo, "generic.pendiente", "es", "Pendiente");
        crearSiNoExiste(repo, "generic.pagado", "es", "Pagado");
        crearSiNoExiste(repo, "generic.no_registrado", "es", "No registrado");
        crearSiNoExiste(repo, "generic.aspirante", "es", "Aspirante");
        crearSiNoExiste(repo, "generic.decision", "es", "Decisión");
        crearSiNoExiste(repo, "generic.cinturon", "es", "Cinturón");
        crearSiNoExiste(repo, "generic.placeholder.escribe_nombre", "es", "Escribe el nombre...");
        crearSiNoExiste(repo, "btn.guardar", "es", "Guardar");
        // --- ASISTENTE DE ADMISIÓN (WIZARD - ACTUALIZADO) ---
        // Paso 1: Datos Físicos
        crearSiNoExiste(repo, "vista.wizard.titulo", "es", "Asistente de Admisión");
        crearSiNoExiste(repo, "vista.wizard.paso1.titulo", "es", "Paso 1: Datos Físicos");
        crearSiNoExiste(repo, "vista.wizard.paso1.desc", "es", "Para personalizar tu entrenamiento, necesitamos conocer tu categoría.");
        crearSiNoExiste(repo, "label.fecha_nacimiento", "es", "Fecha de Nacimiento");
        crearSiNoExiste(repo, "label.peso_kg", "es", "Peso (Kg)");
        crearSiNoExiste(repo, "btn.siguiente.paso", "es", "Siguiente Paso");
        crearSiNoExiste(repo, "msg.error.campos.incompletos", "es", "Por favor, completa todos los campos requeridos.");

        // Paso 2: Documentos y Pago
        crearSiNoExiste(repo, "vista.wizard.paso2.titulo", "es", "Paso 2: Documentos Legales y Pago");
        crearSiNoExiste(repo, "vista.wizard.paso2.desc.completa", "es", "Sube tu Exoneración de Responsabilidad, Certificado de EPS y el Comprobante de Pago (Pantallazo de Nequi).");
        crearSiNoExiste(repo, "msg.waiver.instruccion", "es", "Arrastra aquí tu PDF de Exoneración (Waiver) firmado.");
        crearSiNoExiste(repo, "msg.eps.instruccion", "es", "Arrastra aquí tu Certificado de Afiliación a la EPS.");
        crearSiNoExiste(repo, "msg.pago.instruccion", "es", "Arrastra aquí el pantallazo de tu pago por Nequi o Consignación.");
        crearSiNoExiste(repo, "btn.atras", "es", "Atrás");
        crearSiNoExiste(repo, "btn.finalizar", "es", "Finalizar Registro");

        // Paso 3: Confirmación
        crearSiNoExiste(repo, "vista.wizard.paso3.titulo", "es", "¡Casi listo!");
        crearSiNoExiste(repo, "vista.wizard.paso3.mensaje", "es", "Tus documentos han sido enviados exitosamente. Tu Sensei revisará la información y activará tu cuenta pronto.");

        // Mensajes de Sistema (Uploads y UX)
        crearSiNoExiste(repo, "msg.exito.archivo_subido", "es", "¡Archivo subido y guardado con éxito!");
        crearSiNoExiste(repo, "msg.exito.puede_continuar", "es", "¡Excelente! Ya puedes finalizar el registro.");
        crearSiNoExiste(repo, "msg.error.nube", "es", "Error al conectar con la Nube");

        // Enum del nuevo tipo de documento
        crearSiNoExiste(repo, "enum.tipo_documento.comprobante_pago", "es", "Comprobante de Pago / Nequi");
// --- MENSAJES DE UPLOAD ---
        crearSiNoExiste(repo, "msg.waiver.instruccion", "es", "Arrastra aquí tu WAIVER (PDF)");
        crearSiNoExiste(repo, "msg.waiver.instruccion", "en", "Drag your WAIVER (PDF) here");
        crearSiNoExiste(repo, "msg.eps.instruccion", "es", "Arrastra aquí tu Certificado EPS (PDF)");
        crearSiNoExiste(repo, "msg.eps.instruccion", "en", "Drag your Social Security certificate (PDF) here");

        crearSiNoExiste(repo, "msg.exito.archivo_subido", "es", "Documento guardado correctamente en la nube.");
        crearSiNoExiste(repo, "msg.exito.archivo_subido", "en", "Document successfully saved to the cloud.");
    }
    private void crearSiNoExiste(TraduccionRepository repo, String clave, String idioma, String texto) {
        if (repo.findByClaveAndIdioma(clave, idioma).isEmpty()) {
            repo.save(new Traduccion(clave, idioma, texto));
        }
    }

    // Sobrecarga para soportar el caso donde pasas también texto en inglés en la misma línea (opcional)
    private void crearSiNoExiste(TraduccionRepository repo, String clave, String idioma, String texto, String textoEn) {
        crearSiNoExiste(repo, clave, idioma, texto);
        if (textoEn != null) {
            crearSiNoExiste(repo, clave, "en", textoEn);
        }
    }
    private void ejecutarTestSeveroSaaS(Sensei master, Sensei kiuzo) {
        System.out.println("\n🔥 INICIANDO TEST ÁCIDO DE SEGURIDAD SAAS 🔥");

        // 1. EL MÁSTER CREA DATOS TOP SECRET (Forzamos guardado inmediato con Flush)
        GrupoEntrenamiento grupoMaster = new GrupoEntrenamiento();
        grupoMaster.setSensei(master);
        grupoMaster.setNombre("Finanzas Master 2025");
        grupoRepository.saveAndFlush(grupoMaster); // <-- CAMBIO CLAVE

        Publicacion postMaster = new Publicacion(master.getUsuario(), "Reporte Confidencial Plataforma", null);
        postMaster.setSensei(master);
        postMaster.setFecha(LocalDateTime.now());
        publicacionRepository.saveAndFlush(postMaster); // <-- CAMBIO CLAVE

        chatService.enviarMensajeAlDojo(master.getUsuario(), "Clave de bóveda: 999", master.getId());

        // 2. AUDITORÍA DEL SENSEI KIUZO (Intento de Hackeo)
        System.out.println("   -> Simulando inicio de sesión de KIUZO...");

        boolean vioGrupoMaster = grupoRepository.findBySenseiId(kiuzo.getId(), Pageable.unpaged())
                .getContent().stream().anyMatch(g -> g.getNombre().contains("Finanzas Master"));

        boolean vioPostMaster = publicacionRepository.findBySenseiIdOrderByFechaDesc(kiuzo.getId())
                .stream().anyMatch(p -> p.getContenido().contains("Confidencial"));

        boolean vioChatMaster = chatService.obtenerHistorialDelDojo(kiuzo.getId())
                .stream().anyMatch(m -> m.getContenido().contains("bóveda"));

        // 3. EL VEREDICTO DE SEGURIDAD (IPPON SI FALLA)
        if (vioGrupoMaster || vioPostMaster || vioChatMaster) {
            System.err.println("❌ ERROR: KIUZO PUDO VER LOS DATOS DEL MASTER.");
            throw new SecurityException("LA ARQUITECTURA SAAS HA FALLADO.");
        }

        // 4. NUEVO: VERIFICACIÓN POSITIVA (El Máster DEBE ver sus datos)
        boolean masterVeSuGrupo = grupoRepository.findBySenseiId(master.getId(), Pageable.unpaged())
                .getContent().stream().anyMatch(g -> g.getNombre().contains("Finanzas"));

        if (!masterVeSuGrupo) {
            System.err.println("❌ ERROR: EL MASTER NO PUEDE VER SUS PROPIOS DATOS.");
            throw new SecurityException("FALLO EN LA PERSISTENCIA DE DATOS DEL MASTER.");
        }

        System.out.println("✅ TEST SUPERADO: Aislamiento verificado. El Master ve lo suyo, Kiuzo no puede hackearlo.");
        System.out.println("===============================================================\n");
    }
}