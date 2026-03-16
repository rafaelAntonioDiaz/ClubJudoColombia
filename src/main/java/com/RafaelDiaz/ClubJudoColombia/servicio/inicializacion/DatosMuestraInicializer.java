package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.ChatService;
import com.RafaelDiaz.ClubJudoColombia.servicio.MicrocicloService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatosMuestraInicializer {

    private final UsuarioRepository usuarioRepo;
    private final SenseiRepository senseiRepo;
    private final JudokaRepository judokaRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final SesionService sesionService;
    private final PruebaEstandarRepository pruebaRepo;
    private final MetricaRepository metricaRepo;
    private final ResultadoPruebaRepository resultadoRepo;
    private final EjercicioPlanificadoRepository ejercicioRepo;
    private final MacrocicloRepository macrocicloRepository;
    private final MicrocicloRepository microcicloRepo;
    private final ChatService chatService;
    private final InsigniaRepository insigniaRepo;
    private final JudokaInsigniaRepository judokaInsigniaRepo;
    private final PalmaresRepository palmaresRepo;
    private final TareaDiariaRepository tareaDiariaRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;
    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final MicrocicloService microcicloService;

    public DatosMuestraInicializer(UsuarioRepository usuarioRepo,
                                   SenseiRepository senseiRepo,
                                   JudokaRepository judokaRepo,
                                   RolRepository rolRepo,
                                   PasswordEncoder passwordEncoder,
                                   UsuarioService usuarioService,
                                   SesionService sesionService,
                                   PruebaEstandarRepository pruebaRepo,
                                   MetricaRepository metricaRepo,
                                   ResultadoPruebaRepository resultadoRepo,
                                   EjercicioPlanificadoRepository ejercicioRepo,
                                   MacrocicloRepository macrocicloRepository,
                                   MicrocicloRepository microcicloRepo,
                                   ChatService chatService,
                                   InsigniaRepository insigniaRepo,
                                   JudokaInsigniaRepository judokaInsigniaRepo,
                                   PalmaresRepository palmaresRepo,
                                   TareaDiariaRepository tareaDiariaRepository,
                                   GrupoEntrenamientoRepository grupoRepository,
                                   EjecucionTareaRepository ejecucionTareaRepository, SesionProgramadaRepository sesionProgramadaRepository,
                                   MicrocicloService microcicloService) {
        this.usuarioRepo = usuarioRepo;
        this.senseiRepo = senseiRepo;
        this.judokaRepo = judokaRepo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;
        this.pruebaRepo = pruebaRepo;
        this.metricaRepo = metricaRepo;
        this.resultadoRepo = resultadoRepo;
        this.ejercicioRepo = ejercicioRepo;
        this.macrocicloRepository = macrocicloRepository;
        this.microcicloRepo = microcicloRepo;
        this.chatService = chatService;
        this.insigniaRepo = insigniaRepo;
        this.judokaInsigniaRepo = judokaInsigniaRepo;
        this.palmaresRepo = palmaresRepo;
        this.tareaDiariaRepository = tareaDiariaRepository;
        this.grupoRepository = grupoRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.microcicloService = microcicloService;
    }

    @Transactional
    public void inicializar(Sensei masterSensei) {
        // Si ya existe algún sensei de muestra, asumimos que los datos ya están cargados
       // if (usuarioRepo.findByUsername("kiuzo").isPresent()) {
     //       System.out.println(">>> Datos de muestra ya existen. Omitiendo.");
     //       return;
    //    }

        System.out.println(">>> Creando senseis y judokas de muestra...");

        // 1. Crear senseis
        Sensei kiuzo = crearSensei("kiuzo", "Kiuzo", "Mifune", "123456", GradoCinturon.NEGRO_5_DAN);
        Sensei toshiro = crearSensei("toshiro", "Toshiro", "Diago", "123456", GradoCinturon.NEGRO_5_DAN);

        // 2. Crear judokas de muestra
        Judoka maria = crearMaria(kiuzo);
        Judoka julian = crearJulian(kiuzo);

        // 3. Crear un grupo unificado y asignar ambos judokas
        GrupoEntrenamiento grupoDemo = crearGrupoUnificado(kiuzo, List.of(maria, julian));

        // 4. Crear biblioteca de tareas para Kiuzo
        crearBibliotecaTareas(kiuzo);

        // 5. Crear microciclos (plan de acondicionamiento y plan de evaluación) y asignarlos al grupo
        Microciclo planAcond = crearPlanAcondicionamiento(kiuzo, grupoDemo);
        Microciclo planEval = crearPlanEvaluacion(kiuzo, grupoDemo);
        crearMicrociclosParaSensei(kiuzo);
        // 6. Generar resultados de pruebas históricos (varias fechas)
        generarResultadosFisicosConHistorial(maria, "EXPERTO");
        generarResultadosFisicosConHistorial(julian, "NOVATO");

        // 7. Generar ejecuciones de tareas diarias (simulando actividad reciente)
        generarEjecucionesTareas(maria, planAcond);
        generarEjecucionesTareas(julian, planAcond);

        // 8. Registrar antropometría (peso, estatura, etc.) como resultados de prueba
        generarAntropometriaHistorica(maria, julian);

        // 9. Cargar insignias ganadas
        cargarInsigniasGanadas(maria, julian, kiuzo);

        // 10. Crear palmarés (competiciones)
        crearPalmares(maria, julian);

        // 11. Inicializar chat
        inicializarChat(kiuzo, maria, julian);

        // 12. Crear macrociclos y microciclos con fechas
        crearMacrociclosConMicros(kiuzo, grupoDemo, maria, julian);

        // 13. Crear sesiones programadas con GPS
        crearSesionesGPS(kiuzo, grupoDemo);


        // 15. Generar más ejecuciones de tareas en diferentes meses
        generarEjecucionesTareasHistoricas(maria, julian);

        // 16. Generar resultados de pruebas en más fechas
        generarResultadosPruebasAdicionales(maria, julian);

        // 17. Crear sesiones en meses anteriores y futuros
        crearSesionesAdicionales(kiuzo, grupoDemo);
        crearFamiliaJaimes(masterSensei);

        System.out.println(">>> Datos de muestra cargados exitosamente.");
    }

    // ------------------------------------------------------------------------
    // Métodos existentes (se mantienen igual)
    // ------------------------------------------------------------------------

    private GrupoEntrenamiento crearGrupoUnificado(Sensei sensei, List<Judoka> judokas) {
        String nombreGrupo = "Grupo de Demostración";
        Optional<GrupoEntrenamiento> existente = grupoRepository.findBySenseiAndNombre(sensei, nombreGrupo);
        if (existente.isPresent()) {
            System.out.println(">>> Grupo '" + nombreGrupo + "' ya existe. Reutilizando.");
            return existente.get();
        }

        GrupoEntrenamiento grupo = new GrupoEntrenamiento();
        grupo.setNombre(nombreGrupo);
        grupo.setDescripcion("Grupo unificado para pruebas (incluye María y Julián)");
        grupo.setSensei(sensei);
        grupo = grupoRepository.save(grupo);

        for (Judoka j : judokas) {
            j.setGrupo(grupo);
            judokaRepo.save(j);
        }
        return grupo;
    }
    private Microciclo crearPlanAcondicionamiento(Sensei sensei, GrupoEntrenamiento grupo) {
        Microciclo plan = new Microciclo();
        plan.setNombre("Plan Base de Acondicionamiento");
        plan.setSensei(sensei);
        plan.setEstado(EstadoMicrociclo.ACTIVO);
        plan.setTipoMicrociclo(TipoMicrociclo.AJUSTE);
        plan.setFechaInicio(LocalDate.now().minusWeeks(1));
        plan.setFechaFin(LocalDate.now().plusWeeks(3));
        plan.getGruposAsignados().add(grupo);
        plan = microcicloRepo.save(plan);

        List<TareaDiaria> tareas = tareaDiariaRepository.findAll();
        int orden = 1;
        for (int i = 0; i < tareas.size(); i++) {
            TareaDiaria t = tareas.get(i);
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(plan);
            ej.setTareaDiaria(t);
            ej.setOrden(orden++);
            ej.setNotasSensei("Enfocar en técnica");
            ej.getDiasAsignados().addAll(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
            ej.setDuracionMinutos(15 + i * 5);

            // Alternar entre supervisado y autónomo
            if (i % 2 == 0) {
                ej.setRequiereSupervision(true); // tarea de clase
                ej.setJudokaAsignado(null);
            } else {
                ej.setRequiereSupervision(false); // tarea para casa
                if (i == 1) {
                    ej.setJudokaAsignado(judokaRepo.findByAcudiente_Username("maria.lopez").orElse(null));
                } else if (i == 3) {
                    ej.setJudokaAsignado(judokaRepo.findByAcudiente_Username("julian.bohorquez").orElse(null));
                } else {
                    ej.setJudokaAsignado(null); // grupal para casa
                }
            }
            plan.addEjercicio(ej);
        }
        return microcicloRepo.save(plan);
    }

    private Microciclo crearPlanEvaluacion(Sensei sensei, GrupoEntrenamiento grupo) {
        Microciclo plan = new Microciclo();
        plan.setNombre("Evaluación Trimestral");
        plan.setSensei(sensei);
        plan.setEstado(EstadoMicrociclo.ACTIVO);
        plan.setTipoMicrociclo(TipoMicrociclo.CONTROL);
        plan.setFechaInicio(LocalDate.now().withDayOfMonth(1));
        plan.setFechaFin(LocalDate.now().withDayOfMonth(1).plusWeeks(1));
        plan.getGruposAsignados().add(grupo);
        plan = microcicloRepo.save(plan);

        List<PruebaEstandar> pruebas = pruebaRepo.findAll();
        int orden = 1;
        for (PruebaEstandar p : pruebas) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(plan);
            ej.setPruebaEstandar(p);
            ej.setOrden(orden++);
            ej.setNotasSensei("Registrar resultado");
            ej.getDiasAsignados().add(DayOfWeek.SATURDAY);
            ej.setRequiereSupervision(true); // las pruebas siempre son supervisadas
            ej.setJudokaAsignado(null);
            plan.addEjercicio(ej);
        }
        return microcicloRepo.save(plan);
    }

    private Sensei crearSensei(String username, String nombre, String apellido, String pass, GradoCinturon grado) {
        Usuario u = usuarioRepo.findByUsername(username).orElse(null);
        if (u == null) {
            u = new Usuario(username, passwordEncoder.encode(pass), nombre, apellido);
            u.setEmail(username + "@judocolombia.com");
            u.setActivo(true);
            u.setRoles(Set.of(
                    rolRepo.findByNombre("ROLE_SENSEI").orElseThrow(),
                    rolRepo.findByNombre("ROLE_ADMIN").orElseThrow()
            ));
            u = usuarioService.saveUsuario(u, pass);
        }

        Optional<Sensei> senseiOpt = senseiRepo.findByUsuario(u);
        if (senseiOpt.isPresent()) {
            return senseiOpt.get();
        }

        Sensei s = new Sensei();
        s.setUsuario(u);
        s.setGrado(grado);
        s.setAnosPractica(25);
        return senseiRepo.save(s);
    }

    private Judoka crearMaria(Sensei sensei) {
        String username = "maria.lopez";
        Usuario u = usuarioRepo.findByUsername(username).orElse(null);
        if (u == null) {
            u = new Usuario(username, passwordEncoder.encode("1234"), "María", "López");
            u.setEmail(username + "@judocolombia.com");
            u.setActivo(true);
            u.setRoles(Set.of(
                    rolRepo.findByNombre("ROLE_JUDOKA").orElseThrow(),
                    rolRepo.findByNombre("ROLE_COMPETIDOR").orElseThrow()
            ));
            u = usuarioService.saveUsuario(u, "1234");
        }

        Optional<Judoka> judokaOpt = judokaRepo.findByAcudiente_Username(username);
        if (judokaOpt.isPresent()) {
            return judokaOpt.get();
        }

        Judoka j = new Judoka();
        j.setAcudiente(u);
        j.setNombre("María");
        j.setApellido("López");
        j.setSensei(sensei);
        j.setFechaNacimiento(LocalDate.of(2008, 5, 15));
        j.setSexo(Sexo.FEMENINO);
        j.setGrado(GradoCinturon.VERDE);
        j.setEsCompetidorActivo(true);
        j.setPeso(57.0);
        j.setEstatura(160.0);
        j.setEps("Sura");
        j.setNombreAcudiente("Carlos López");
        j.setTelefonoAcudiente("300 111 2233");
        j.setEstado(EstadoJudoka.ACTIVO);
        return judokaRepo.save(j);
    }

    private Judoka crearJulian(Sensei sensei) {
        String username = "julian.bohorquez";
        Usuario u = usuarioRepo.findByUsername(username).orElse(null);
        if (u == null) {
            u = new Usuario(username, passwordEncoder.encode("1234"), "Julián", "Bohórquez");
            u.setEmail(username + "@judocolombia.com");
            u.setActivo(true);
            u.setRoles(Set.of(rolRepo.findByNombre("ROLE_JUDOKA").orElseThrow()));
            u = usuarioService.saveUsuario(u, "1234");
        }

        Optional<Judoka> judokaOpt = judokaRepo.findByAcudiente_Username(username);
        if (judokaOpt.isPresent()) {
            return judokaOpt.get();
        }

        Judoka j = new Judoka();
        j.setAcudiente(u);
        j.setNombre("Julián");
        j.setApellido("Bohórquez");
        j.setSensei(sensei);
        j.setFechaNacimiento(LocalDate.now().minusYears(10));
        j.setSexo(Sexo.MASCULINO);
        j.setGrado(GradoCinturon.BLANCO);
        j.setEsCompetidorActivo(false);
        j.setPeso(34.0);
        j.setEstatura(138.0);
        j.setEps("Sanitas");
        j.setNombreAcudiente("Carlos Bohórquez");
        j.setTelefonoAcudiente("300 123 4567");
        j.setEstado(EstadoJudoka.ACTIVO);
        return judokaRepo.save(j);
    }

    private void registrarAntropometria(Judoka judoka, double peso, double estatura,
                                        double cintura, double envergadura, LocalDateTime fecha) {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.medicion_antropo.nombre").orElseThrow();
        Metrica mPeso = metricaRepo.findByNombreKey("metrica.masa_corporal.nombre").orElseThrow();
        Metrica mEstatura = metricaRepo.findByNombreKey("metrica.estatura.nombre").orElseThrow();
        Metrica mCintura = metricaRepo.findByNombreKey("metrica.cintura.nombre").orElseThrow();
        Metrica mEnvergadura = metricaRepo.findByNombreKey("metrica.envergadura.nombre").orElseThrow();

        EjercicioPlanificado dummy = obtenerEjercicioDummy(prueba);
        resultadoRepo.save(crearResultado(judoka, dummy, mPeso, peso, fecha));
        resultadoRepo.save(crearResultado(judoka, dummy, mEstatura, estatura, fecha));
        resultadoRepo.save(crearResultado(judoka, dummy, mCintura, cintura, fecha));
        resultadoRepo.save(crearResultado(judoka, dummy, mEnvergadura, envergadura, fecha));
    }
    private ResultadoPrueba crearResultado(Judoka j, EjercicioPlanificado ej, Metrica m, double valor, LocalDateTime fecha) {
        ResultadoPrueba r = new ResultadoPrueba();
        r.setJudoka(j);
        r.setEjercicioPlanificado(ej);
        r.setMetrica(m);
        r.setValor(valor);
        r.setFechaRegistro(fecha);
        r.setNumeroIntento(1);
        return r;
    }

    private EjercicioPlanificado obtenerEjercicioDummy(PruebaEstandar prueba) {
        return ejercicioRepo.findByPruebaEstandar(prueba).stream().findFirst()
                .orElseGet(() -> {
                    Microciclo dummy = microcicloRepo.findAll().stream().findFirst()
                            .orElseGet(() -> {
                                Microciclo m = new Microciclo();
                                m.setNombre("Dummy");
                                m.setSensei(senseiRepo.findAll().get(0));
                                return microcicloRepo.save(m);
                            });
                    EjercicioPlanificado ej = new EjercicioPlanificado();
                    ej.setMicrociclo(dummy);
                    ej.setPruebaEstandar(prueba);
                    ej.setOrden(999);
                    return ejercicioRepo.save(ej);
                });
    }

    private void generarResultadosFisicosConHistorial(Judoka judoka, String perfil) {
        Map<String, List<Double>> valoresPorPrueba = perfil.equals("EXPERTO")
                ? Map.of(
                "ejercicio.salto_horizontal_proesp.nombre", List.of(195.0, 205.0, 210.0, 215.0),
                "ejercicio.lanzamiento_balon.nombre", List.of(420.0, 440.0, 450.0, 460.0),
                "ejercicio.abdominales_1min.nombre", List.of(40.0, 43.0, 45.0, 47.0),
                "ejercicio.carrera_6min.nombre", List.of(1350.0, 1380.0, 1400.0, 1420.0),
                "ejercicio.agilidad_4x4.nombre", List.of(5.8, 5.6, 5.5, 5.4),
                "ejercicio.carrera_20m.nombre", List.of(3.4, 3.3, 3.2, 3.1),
                "ejercicio.sjft.nombre", List.of(11.2, 10.8, 10.5, 10.3)
        )
                : Map.of(
                "ejercicio.salto_horizontal_proesp.nombre", List.of(100.0, 110.0, 120.0, 115.0),
                "ejercicio.lanzamiento_balon.nombre", List.of(180.0, 190.0, 200.0, 195.0),
                "ejercicio.abdominales_1min.nombre", List.of(15.0, 18.0, 20.0, 19.0),
                "ejercicio.carrera_6min.nombre", List.of(750.0, 780.0, 800.0, 790.0),
                "ejercicio.agilidad_4x4.nombre", List.of(8.0, 7.8, 7.5, 7.6),
                "ejercicio.carrera_20m.nombre", List.of(4.8, 4.7, 4.5, 4.6),
                "ejercicio.sjft.nombre", List.of(19.0, 18.5, 18.0, 18.2)
        );

        LocalDateTime base = LocalDateTime.now().minusMonths(3);
        for (int i = 0; i < 4; i++) {
            LocalDateTime fecha = base.plusMonths(i);
            for (Map.Entry<String, List<Double>> entry : valoresPorPrueba.entrySet()) {
                String clavePrueba = entry.getKey();
                Double valor = entry.getValue().get(i);
                pruebaRepo.findByNombreKey(clavePrueba).ifPresent(prueba -> {
                    Metrica metrica = prueba.getMetricas().stream().findFirst().orElse(null);
                    if (metrica != null) {
                        EjercicioPlanificado dummy = obtenerEjercicioDummy(prueba);
                        resultadoRepo.save(crearResultado(judoka, dummy, metrica, valor, fecha));
                    }
                });
            }
        }
    }

    private void generarEjecucionesTareas(Judoka judoka, Microciclo plan) {
        List<EjercicioPlanificado> ejercicios = plan.getEjerciciosPlanificados().stream()
                .filter(ej -> ej.getTareaDiaria() != null)
                .collect(Collectors.toList());
        if (ejercicios.isEmpty()) return;

        Random rand = new Random();
        LocalDateTime hoy = LocalDateTime.now().withHour(18).withMinute(0);
        for (int i = 0; i < 10; i++) {
            LocalDateTime fecha = hoy.minusDays(i);
            for (EjercicioPlanificado ej : ejercicios) {
                if (rand.nextDouble() > 0.3) {
                    EjecucionTarea ejec = new EjecucionTarea();
                    ejec.setJudoka(judoka);
                    ejec.setEjercicioPlanificado(ej);
                    ejec.setCompletado(true);
                    ejec.setFechaRegistro(fecha);
                    ejecucionTareaRepository.save(ejec);
                }
            }
        }
    }

    private void cargarInsigniasGanadas(Judoka maria, Judoka julian, Sensei sensei) {
        asignarLogro(maria, "SHIN_CONSTANCIA", sensei, 60);
        asignarLogro(maria, "SHIN_COMPROMISO", sensei, 30);
        asignarLogro(maria, "GI_CINTURON", sensei, 90);
        asignarLogro(maria, "TAI_HERCULES", sensei, 15);
        asignarLogro(maria, "COMP_ORO", sensei, 45);

        asignarLogro(julian, "SHIN_INICIO", sensei, 2);
        asignarLogro(julian, "TAI_VELOCIDAD", sensei, 1);
    }

    private void asignarLogro(Judoka j, String clave, Sensei s, int diasAtras) {
        if (j == null) return;
        insigniaRepo.findByClave(clave).ifPresent(ins -> {
            if (!judokaInsigniaRepo.existsByJudokaAndInsignia_Clave(j, clave)) {
                JudokaInsignia li = new JudokaInsignia();
                li.setJudoka(j);
                li.setInsignia(ins);
                li.setSenseiOtorgante(s);
                li.setFechaObtencion(LocalDateTime.now().minusDays(diasAtras));
                judokaInsigniaRepo.save(li);
            }
        });
    }

    private void crearPalmares(Judoka maria, Judoka julian) {
        if (maria != null && palmaresRepo.findByJudokaOrderByFechaDesc(maria).isEmpty()) {
            palmaresRepo.save(new ParticipacionCompetencia(maria, "Nacional Mayores", "Bogotá",
                    LocalDate.now().minusMonths(2), NivelCompetencia.NACIONAL, ResultadoCompetencia.ORO, null));
            palmaresRepo.save(new ParticipacionCompetencia(maria, "Departamental Valle", "Cali",
                    LocalDate.now().minusMonths(5), NivelCompetencia.DEPARTAMENTAL, ResultadoCompetencia.PLATA, null));
        }
        if (julian != null && palmaresRepo.findByJudokaOrderByFechaDesc(julian).isEmpty()) {
            palmaresRepo.save(new ParticipacionCompetencia(julian, "Festival Infantil", "Bucaramanga",
                    LocalDate.now().minusWeeks(3), NivelCompetencia.LOCAL, ResultadoCompetencia.BRONCE, null));
        }
    }

    private void inicializarChat(Sensei kiuzo, Judoka maria, Judoka julian) {
        if (kiuzo != null) {
            chatService.enviarMensajeAlDojo(kiuzo.getUsuario(), "Bienvenidos al chat del dojo.", kiuzo.getId());
            if (maria != null) {
                chatService.enviarMensajeAlDojo(maria.getUsuario(), "Gracias Sensei!", kiuzo.getId());
            }
            if (julian != null) {
                chatService.enviarMensajeAlDojo(julian.getUsuario(), "Hola a todos!", kiuzo.getId());
            }
        }
    }

    private void crearBibliotecaTareas(Sensei sensei) {
        System.out.println(">>> Creando biblioteca de tareas para Sensei " + sensei.getUsuario().getNombre());

        List<TareaDiaria> tareasNuevas = List.of(
                new TareaDiaria("Calentamiento articular", "10 min de movilidad dinámica", sensei, CategoriaEjercicio.FLEXIBILIDAD),
                new TareaDiaria("Técnica de suelo (Ne-waza)", "15 min de inmovilizaciones y transiciones", sensei, CategoriaEjercicio.TECNICA),
                new TareaDiaria("Uchi-komi con gomas", "100 repeticiones (velocidad)", sensei, CategoriaEjercicio.VELOCIDAD),
                new TareaDiaria("Randori ligero", "3 combates de 3 min", sensei, CategoriaEjercicio.APTITUD_AEROBICA),
                new TareaDiaria("Flexiones de pecho", "4 series x 15 reps", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Abdominales en V", "3 series x 20 reps", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Sentadillas con salto", "4 series x 10 reps", sensei, CategoriaEjercicio.POTENCIA),
                new TareaDiaria("Estiramiento final", "10 min estático", sensei, CategoriaEjercicio.FLEXIBILIDAD),

                // --- BLOQUE: PRINCIPIANTES ---

                // Flexibilidad y Agilidad (Protección y Coordinación)
                new TareaDiaria("Movilidad Articular Integral", "10 min de rotaciones y movilidad dinámica", sensei, CategoriaEjercicio.FLEXIBILIDAD),
                new TareaDiaria("Ukemi (Caídas) Básicas", "10 min de rodadas y caídas laterales suaves", sensei, CategoriaEjercicio.AGILIDAD),
                new TareaDiaria("Puente de cuello asistido", "3 series x 30 seg - Soporte con manos", sensei, CategoriaEjercicio.FLEXIBILIDAD),

                // Técnica y Anticipación
                new TareaDiaria("Uchi-komi en sombra", "4 series x 15 reps - Enfoque en postura y desequilibrio (Kuzushi)", sensei, CategoriaEjercicio.TECNICA),
                new TareaDiaria("Juego de toques (Hombros/Rodillas)", "3 rondas x 2 min - Reacción al movimiento del compañero", sensei, CategoriaEjercicio.ANTICIPACION),

                // Resistencia Muscular y Dinámica
                new TareaDiaria("Flexiones de pecho regulares", "4 series x 12 reps - Rango completo", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Sentadillas al aire (Bodyweight)", "4 series x 15 reps - Espalda recta", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Desplazamientos Ebi (Camarón)", "4 series x 10 metros - Movilidad de cadera en suelo", sensei, CategoriaEjercicio.RESISTENCIA_DINAMICA),
                new TareaDiaria("Caminata de oso (Bear Crawl)", "3 series x 15 metros - Estabilidad de hombros", sensei, CategoriaEjercicio.RESISTENCIA_DINAMICA),

                // Isometría y Base Aeróbica
                new TareaDiaria("Plancha frontal (Plank)", "3 series x 45 seg - Core estable", sensei, CategoriaEjercicio.RESISTENCIA_ISOMETRICA),
                new TareaDiaria("Ne-waza ligero (Suelo)", "4 rondas x 3 min - Combate fluido sin fuerza excesiva", sensei, CategoriaEjercicio.APTITUD_AEROBICA),

                // --- BLOQUE: AVANZADOS ---

                // Potencia y Velocidad
                new TareaDiaria("Flexiones Pliométricas (Con aplauso)", "4 series x 8 reps - Explosividad de empuje", sensei, CategoriaEjercicio.POTENCIA),
                new TareaDiaria("Saltos al cajón o Jump Squats altos", "4 series x 6 reps - Triple extensión rápida", sensei, CategoriaEjercicio.POTENCIA),
                new TareaDiaria("Uchi-komi con bandas elásticas", "100 repeticiones cronometradas - Máxima velocidad", sensei, CategoriaEjercicio.VELOCIDAD),
                new TareaDiaria("Entradas a Seoi-Nage relámpago", "10 repeticiones en menos de 15 segundos", sensei, CategoriaEjercicio.VELOCIDAD),

                // Resistencia Específica de Judo
                new TareaDiaria("Dominadas agarrando Judogi", "4 series al fallo - Tracción y fuerza de agarre", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Sentadillas a una pierna (Pistol Squats)", "3 series x 6 reps por pierna - Equilibrio y fuerza", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Suspensión isométrica en barra (con Gi)", "3 series x máximo tiempo - Resistencia de agarre", sensei, CategoriaEjercicio.RESISTENCIA_ISOMETRICA),
                new TareaDiaria("Sentadilla isométrica en pared (Wall Sit)", "3 series x 90 seg - Posición de defensa baja", sensei, CategoriaEjercicio.RESISTENCIA_ISOMETRICA),

                // Aptitud Anaeróbica y Aeróbica (Sistemas de Energía)
                new TareaDiaria("Sprawls reactivos (Tabata)", "8 rondas: 20s trabajo / 10s descanso - Defensa de derribo", sensei, CategoriaEjercicio.APTITUD_ANAEROBICA),
                new TareaDiaria("Nage-komi con fatiga", "Lanzamientos continuos durante 2 minutos a máxima intensidad", sensei, CategoriaEjercicio.APTITUD_ANAEROBICA),
                new TareaDiaria("Randori de pie y suelo continuo", "6 rondas x 5 min - Simulación de competencia", sensei, CategoriaEjercicio.APTITUD_AEROBICA),

                // Anticipación y Agilidad avanzada
                new TareaDiaria("Práctica de caída reactiva (Ukemi ciego)", "Compañero empuja sorpresivamente desde varios ángulos", sensei, CategoriaEjercicio.ANTICIPACION),
                new TareaDiaria("Transiciones rápidas de inmovilización (Osaekomi)", "Cambiar de Hon-Kesa-Gatame a Yoko-Shiho en < 2 seg", sensei, CategoriaEjercicio.AGILIDAD)
        );
        List<TareaDiaria> aGuardar = new ArrayList<>();
        for (TareaDiaria t : tareasNuevas) {
            if (!tareaDiariaRepository.existsByNombreAndSenseiCreador(t.getNombre(), sensei)) {
                t.setSenseiCreador(sensei); // Asegurar el sensei
                aGuardar.add(t);
            }
        }
        if (!aGuardar.isEmpty()) {
            tareaDiariaRepository.saveAll(aGuardar);
            System.out.println(">>> Se agregaron " + aGuardar.size() + " nuevas tareas para " + sensei.getUsuario().getNombre());
        }
    }

    private void crearMacrociclosParaSensei(Sensei sensei) {
        Macrociclo macro = new Macrociclo();
        macro.setNombre("Preparación Juegos Nacionales 2026");
        macro.setObjetivoPrincipal("Alcanzar el pico de forma en agosto");
        macro.setFechaInicio(LocalDate.now().minusMonths(2));
        macro.setFechaFin(LocalDate.now().plusMonths(6));
        macro.setSensei(sensei);
        macro = macrocicloRepository.save(macro);
    }

    private void crearMicrociclosParaSensei(Sensei sensei) {
        if (microcicloRepo.count() > 0) return;

        System.out.println(">>> Creando microciclos para Sensei " + sensei.getUsuario().getNombre());

        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(sensei, "Selección Mayores")
                .orElseGet(() -> {
                    GrupoEntrenamiento g = new GrupoEntrenamiento();
                    g.setNombre("Selección Mayores");
                    g.setDescripcion("Grupo de competencia");
                    g.setSensei(sensei);
                    return grupoRepository.save(g);
                });

        Microciclo planAcond = new Microciclo();
        planAcond.setNombre("Base de pretemporada");
        planAcond.setSensei(sensei);
        planAcond.setFechaInicio(LocalDate.now().minusWeeks(1));
        planAcond.setFechaFin(LocalDate.now().plusWeeks(3));
        planAcond.setEstado(EstadoMicrociclo.ACTIVO);
        planAcond.setTipoMicrociclo(TipoMicrociclo.AJUSTE);
        planAcond.getGruposAsignados().add(grupo);
        planAcond = microcicloRepo.save(planAcond);

        List<TareaDiaria> tareas = tareaDiariaRepository.findAll();
        int orden = 1;
        for (TareaDiaria t : tareas) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(planAcond);
            ej.setTareaDiaria(t);
            ej.setOrden(orden++);
            ej.setNotasSensei("Ejecutar con buena técnica");
            ej.getDiasAsignados().addAll(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
            planAcond.addEjercicio(ej);
        }
        microcicloRepo.save(planAcond);

        Microciclo planEval = new Microciclo();
        planEval.setNombre("Evaluación trimestral");
        planEval.setSensei(sensei);
        planEval.setEstado(EstadoMicrociclo.ACTIVO);
        planEval.setTipoMicrociclo(TipoMicrociclo.CONTROL);
        planEval.getGruposAsignados().add(grupo);
        planEval = microcicloRepo.save(planEval);

        List<PruebaEstandar> pruebas = pruebaRepo.findAll();
        orden = 1;
        for (PruebaEstandar p : pruebas) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(planEval);
            ej.setPruebaEstandar(p);
            ej.setOrden(orden++);
            ej.setNotasSensei("Máximo esfuerzo");
            ej.getDiasAsignados().add(DayOfWeek.SATURDAY);
            planEval.addEjercicio(ej);
        }
        microcicloRepo.save(planEval);
    }

    private void crearMacrociclosConMicros(Sensei sensei, GrupoEntrenamiento grupo, Judoka maria, Judoka julian) {
        LocalDate hoy = LocalDate.now();
        YearMonth mesActual = YearMonth.from(hoy);
        LocalDate inicioMes = mesActual.atDay(1);
        LocalDate finMes = mesActual.atEndOfMonth();

        // Macrociclo que cubre todo el año
        Macrociclo macro = new Macrociclo();
        macro.setNombre("Preparación Juegos Nacionales 2026");
        macro.setObjetivoPrincipal("Alcanzar el pico de forma en agosto");
        macro.setFechaInicio(inicioMes.minusMonths(2));
        macro.setFechaFin(finMes.plusMonths(6));
        macro.setSensei(sensei);
        macro = macrocicloRepository.save(macro);

        // Microciclo 1: primera semana del mes
        crearMicrocicloConTareas(sensei, grupo, macro, maria, julian, "Adquisición Fuerza",
                inicioMes, inicioMes.plusDays(6),
                TipoMicrociclo.CORRIENTE, MesocicloATC.ADQUISICION,
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        // Microciclo 2: segunda semana
        crearMicrocicloConTareas(sensei, grupo, macro, maria, julian, "Adquisición Resistencia",
                inicioMes.plusDays(7), inicioMes.plusDays(13),
                TipoMicrociclo.CORRIENTE, MesocicloATC.ADQUISICION,
                List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY));

        // Microciclo 3: tercera semana (con tareas autónomas)
        crearMicrocicloConTareas(sensei, grupo, macro, maria, julian, "Transferencia Técnica",
                inicioMes.plusDays(14), inicioMes.plusDays(20),
                TipoMicrociclo.CHOQUE, MesocicloATC.TRANSFERENCIA,
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        // Microciclo 4: cuarta semana
        crearMicrocicloConTareas(sensei, grupo, macro, maria, julian, "Pre-Competitivo",
                inicioMes.plusDays(21), finMes,
                TipoMicrociclo.AJUSTE, MesocicloATC.COMPETENCIA,
                List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
    }
    private void crearSesionesGPS(Sensei sensei, GrupoEntrenamiento grupo) {
        LocalDate hoy = LocalDate.now();

        // Sesión 1: hoy a las 16:00
        LocalDateTime inicio1 = hoy.atTime(16, 0);
        LocalDateTime fin1 = hoy.atTime(17, 30);
        if (!sesionProgramadaRepository.existsByGrupoAndFechaHoraInicioAndFechaHoraFin(grupo, inicio1, fin1)) {
            SesionProgramada sesion1 = new SesionProgramada();
            sesion1.setNombre("Entrenamiento Técnico - Parque Norte");
            sesion1.setGrupo(grupo);
            sesion1.setSensei(sensei);
            sesion1.setFechaHoraInicio(inicio1);
            sesion1.setFechaHoraFin(fin1);
            sesion1.setTipoSesion(TipoSesion.TECNICA);
            sesion1.setLatitud(6.2716);
            sesion1.setLongitud(-75.5634);
            sesion1.setRadioPermitidoMetros(100);
            sesionService.guardar(sesion1);
        }

        // Sesión 2: pasado mañana a las 6:00
        LocalDateTime inicio2 = hoy.plusDays(2).atTime(6, 0);
        LocalDateTime fin2 = hoy.plusDays(2).atTime(7, 30);
        if (!sesionProgramadaRepository.existsByGrupoAndFechaHoraInicioAndFechaHoraFin(grupo, inicio2, fin2)) {
            SesionProgramada sesion2 = new SesionProgramada();
            sesion2.setNombre("Entrenamiento Físico - Estadio");
            sesion2.setGrupo(grupo);
            sesion2.setSensei(sensei);
            sesion2.setFechaHoraInicio(inicio2);
            sesion2.setFechaHoraFin(fin2);
            sesion2.setTipoSesion(TipoSesion.ACONDICIONAMIENTO);
            sesion2.setLatitud(6.2545);
            sesion2.setLongitud(-75.5916);
            sesion2.setRadioPermitidoMetros(150);
            sesionService.guardar(sesion2);
        }

        // Sesión 3: próximo miércoles a las 18:00
        LocalDateTime inicio3 = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY)).atTime(18, 0);
        LocalDateTime fin3 = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY)).atTime(19, 30);
        if (!sesionProgramadaRepository.existsByGrupoAndFechaHoraInicioAndFechaHoraFin(grupo, inicio3, fin3)) {
            SesionProgramada sesion3 = new SesionProgramada();
            sesion3.setNombre("Randori - Dojo Central");
            sesion3.setGrupo(grupo);
            sesion3.setSensei(sensei);
            sesion3.setFechaHoraInicio(inicio3);
            sesion3.setFechaHoraFin(fin3);
            sesion3.setTipoSesion(TipoSesion.RANDORI);
            sesion3.setLatitud(6.2442);
            sesion3.setLongitud(-75.5812);
            sesion3.setRadioPermitidoMetros(100);
            sesionService.guardar(sesion3);
        }
    }    // ------------------------------------------------------------------------
    // NUEVOS MÉTODOS PARA DATOS ADICIONALES
    // ------------------------------------------------------------------------


    private void crearMicrocicloConTareas(Sensei sensei, GrupoEntrenamiento grupo, Macrociclo macro,
                                          Judoka maria, Judoka julian,
                                          String nombre, LocalDate inicio, LocalDate fin,
                                          TipoMicrociclo tipo, MesocicloATC meso,
                                          List<DayOfWeek> diasTarea) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de microciclo no pueden ser nulas");
        }
        Optional<Microciclo> existente = microcicloRepo.findByNombreAndSensei(nombre, sensei);
        Microciclo micro;
        if (existente.isPresent()) {
            micro = existente.get();
            micro.setFechaInicio(inicio);
            micro.setFechaFin(fin);
            micro.setMacrociclo(macro);
            micro.setTipoMicrociclo(tipo);
            micro.setMesocicloATC(meso);
            micro.getGruposAsignados().clear();
            micro.getGruposAsignados().add(grupo);
            micro = microcicloRepo.save(micro);
        } else {
            micro = new Microciclo();
            micro.setNombre(nombre);
            micro.setSensei(sensei);
            micro.setEstado(EstadoMicrociclo.ACTIVO);
            micro.setFechaInicio(inicio);
            micro.setFechaFin(fin);
            micro.setTipoMicrociclo(tipo);
            micro.setMesocicloATC(meso);
            micro.getGruposAsignados().add(grupo);
            micro.setMacrociclo(macro);
            micro = microcicloRepo.save(micro);
        }

        List<TareaDiaria> tareas = tareaDiariaRepository.findAll();
        int maxTareas = Math.min(tareas.size(), 8);
        int orden = 1;
        for (int i = 0; i < maxTareas; i++) {
            TareaDiaria t = tareas.get(i);
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(micro);
            ej.setTareaDiaria(t);
            ej.setOrden(orden++);
            ej.setNotasSensei("Realizar con buena técnica");
            ej.getDiasAsignados().addAll(diasTarea);
            ej.setDuracionMinutos(15 + i * 5);

            // Diferenciar supervisión y asignación individual
            if (i % 3 == 0) {
                ej.setRequiereSupervision(false); // tarea para casa
                if (i == 0) {
                    ej.setJudokaAsignado(maria); // solo para María
                } else if (i == 3) {
                    ej.setJudokaAsignado(julian); // solo para Julián
                } else {
                    ej.setJudokaAsignado(null); // tarea grupal para casa
                }
            } else {
                ej.setRequiereSupervision(true); // tarea supervisada
                ej.setJudokaAsignado(null);
            }

            micro.addEjercicio(ej);
        }
        microcicloRepo.save(micro);
    }

    private void generarEjecucionesTareasHistoricas(Judoka maria, Judoka julian) {
        Random rand = new Random();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < 60; i++) {
            LocalDate fecha = hoy.minusDays(i);
            List<Microciclo> planesMaria = microcicloService.buscarPlanesPorJudoka(maria);
            List<Microciclo> planesJulian = microcicloService.buscarPlanesPorJudoka(julian);

            generarEjecucionesParaJudokaEnFecha(maria, planesMaria, fecha, rand, 0.8);
            generarEjecucionesParaJudokaEnFecha(julian, planesJulian, fecha, rand, 0.6);
        }
    }

    private void generarEjecucionesParaJudokaEnFecha(Judoka judoka, List<Microciclo> planes, LocalDate fecha, Random rand, double prob) {
        for (Microciclo plan : planes) {
            if (plan.getFechaInicio().isAfter(fecha) || plan.getFechaFin().isBefore(fecha)) continue;
            for (EjercicioPlanificado ej : plan.getEjerciciosPlanificados()) {
                if (ej.getTareaDiaria() == null) continue;
                if (!ej.getDiasAsignados().contains(fecha.getDayOfWeek())) continue;

                boolean yaExiste = ejecucionTareaRepository.existsByJudokaAndEjercicioPlanificadoAndFechaRegistroBetween(
                        judoka, ej, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
                if (!yaExiste && rand.nextDouble() < prob) {
                    EjecucionTarea ejec = new EjecucionTarea();
                    ejec.setJudoka(judoka);
                    ejec.setEjercicioPlanificado(ej);
                    ejec.setCompletado(true);
                    ejec.setFechaRegistro(fecha.atTime(18, 0).plusMinutes(rand.nextInt(60)));
                    ejec.setLatitud(6.2442 + (rand.nextDouble() - 0.5) * 0.1);
                    ejec.setLongitud(-75.5812 + (rand.nextDouble() - 0.5) * 0.1);
                    ejecucionTareaRepository.save(ejec);
                }
            }
        }
    }

    private void generarResultadosPruebasAdicionales(Judoka maria, Judoka julian) {
        List<PruebaEstandar> pruebas = pruebaRepo.findAll();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate fecha = hoy.minusDays(i * 14);
            for (PruebaEstandar prueba : pruebas) {
                Metrica metrica = prueba.getMetricas().stream().findFirst().orElse(null);
                if (metrica == null) continue;
                generarResultadoParaJudoka(maria, prueba, metrica, fecha, 0.9, 1.1);
                generarResultadoParaJudoka(julian, prueba, metrica, fecha, 0.7, 0.9);
            }
        }
    }

    private void generarResultadoParaJudoka(Judoka judoka, PruebaEstandar prueba, Metrica metrica, LocalDate fecha, double minFactor, double maxFactor) {
        double valorBase = obtenerValorBaseParaPrueba(prueba.getNombreKey(), judoka);
        double variacion = (Math.random() * (maxFactor - minFactor) + minFactor);
        double valor = valorBase * variacion;

        EjercicioPlanificado dummy = obtenerEjercicioDummy(prueba);
        ResultadoPrueba resultado = new ResultadoPrueba();
        resultado.setJudoka(judoka);
        resultado.setEjercicioPlanificado(dummy);
        resultado.setMetrica(metrica);
        resultado.setValor(valor);
        resultado.setFechaRegistro(fecha.atTime(12, 0));
        resultado.setNumeroIntento(1);
        resultadoRepo.save(resultado);
    }

    private double obtenerValorBaseParaPrueba(String nombreKey, Judoka judoka) {
        Map<String, Double> valoresMaria = Map.of(
                "ejercicio.salto_horizontal_proesp.nombre", 210.0,
                "ejercicio.lanzamiento_balon.nombre", 450.0,
                "ejercicio.abdominales_1min.nombre", 45.0,
                "ejercicio.carrera_6min.nombre", 1400.0,
                "ejercicio.agilidad_4x4.nombre", 5.4,
                "ejercicio.carrera_20m.nombre", 3.1,
                "ejercicio.sjft.nombre", 10.5
        );
        Map<String, Double> valoresJulian = Map.of(
                "ejercicio.salto_horizontal_proesp.nombre", 120.0,
                "ejercicio.lanzamiento_balon.nombre", 200.0,
                "ejercicio.abdominales_1min.nombre", 20.0,
                "ejercicio.carrera_6min.nombre", 800.0,
                "ejercicio.agilidad_4x4.nombre", 7.5,
                "ejercicio.carrera_20m.nombre", 4.5,
                "ejercicio.sjft.nombre", 18.0
        );
        if (judoka.getNombre().equals("María")) {
            return valoresMaria.getOrDefault(nombreKey, 100.0);
        } else {
            return valoresJulian.getOrDefault(nombreKey, 50.0);
        }
    }

    private void crearSesionesAdicionales(Sensei sensei, GrupoEntrenamiento grupo) {

        LocalDate hoy = LocalDate.now();
        // Horarios diferentes para cada semana (9, 10, 11, 12) para evitar solapamiento
        int[] horas = {9, 10, 11, 12};

        for (int i = -3; i <= 3; i++) {
            LocalDate fechaBase = hoy.plusMonths(i).withDayOfMonth(15);
            for (int w = 0; w < 4; w++) {
                LocalDate fecha = fechaBase.plusWeeks(w);
                if (fecha.getMonth() != fechaBase.getMonth()) continue;

                SesionProgramada sesion = new SesionProgramada();
                sesion.setNombre("Entrenamiento " + (i < 0 ? "Histórico" : "Planificado") + " - Semana " + (w + 1));
                sesion.setGrupo(grupo);
                sesion.setSensei(sensei);
                sesion.setFechaHoraInicio(fecha.atTime(horas[w], 0));
                sesion.setFechaHoraFin(fecha.atTime(horas[w] + 1, 30)); // Duración 1.5 horas
                sesion.setTipoSesion(TipoSesion.TECNICA);
                sesion.setLatitud(6.2442 + (Math.random() - 0.5) * 0.1);
                sesion.setLongitud(-75.5812 + (Math.random() - 0.5) * 0.1);
                sesion.setRadioPermitidoMetros(100);
                if (!sesionProgramadaRepository.existsByGrupoAndFechaHoraInicioAndFechaHoraFin(grupo, sesion.getFechaHoraInicio(), sesion.getFechaHoraFin())) {
                    sesionService.guardar(sesion);
                }
            }
        }
    }

    private void generarAntropometriaHistorica(Judoka maria, Judoka julian) {
        LocalDateTime hoy = LocalDateTime.now();
        // Generar mediciones cada 3 meses durante los últimos 4 años (16 trimestres)
        int totalMediciones = 16; // 4 años * 4 trimestres

        for (int i = 0; i < totalMediciones; i++) {
            // Retroceder i trimestres (cada 3 meses)
            LocalDateTime fecha = hoy.minusMonths(i * 3).withDayOfMonth(15).withHour(10).withMinute(0);

            // Factor de tiempo: a mayor i (más antiguo), menor valor
            // Usamos (totalMediciones - 1 - i) para que i=0 (hoy) tenga el factor máximo
            int factor = totalMediciones - 1 - i;

            // --- Valores para María (crecimiento progresivo) ---
            // A los 12 años (hace 4 años): peso 48 kg, estatura 150 cm
            // A los 16 años (hoy): peso 57 kg, estatura 160 cm
            double pesoMaria = 48.0 + factor * (9.0 / (totalMediciones - 1)); // +9 kg en 4 años
            double estaturaMaria = 150.0 + factor * (10.0 / (totalMediciones - 1)); // +10 cm
            double cinturaMaria = 62.0 + factor * (4.0 / (totalMediciones - 1)); // +4 cm
            double envergaduraMaria = 155.0 + factor * (8.0 / (totalMediciones - 1)); // +8 cm

            registrarAntropometria(maria, pesoMaria, estaturaMaria, cinturaMaria, envergaduraMaria, fecha);

            // --- Valores para Julián (crecimiento más rápido) ---
            // A los 6 años (hace 4 años): peso 22 kg, estatura 120 cm
            // A los 10 años (hoy): peso 34 kg, estatura 138 cm
            double pesoJulian = 22.0 + factor * (12.0 / (totalMediciones - 1)); // +12 kg
            double estaturaJulian = 120.0 + factor * (18.0 / (totalMediciones - 1)); // +18 cm
            double cinturaJulian = 52.0 + factor * (6.0 / (totalMediciones - 1)); // +6 cm
            double envergaduraJulian = 118.0 + factor * (14.0 / (totalMediciones - 1)); // +14 cm

            registrarAntropometria(julian, pesoJulian, estaturaJulian, cinturaJulian, envergaduraJulian, fecha);
        }
    }
    private void crearFamiliaJaimes(Sensei master) {
        // Evitar duplicados
        if (usuarioRepo.findByUsername("juliana_v8@test.com").isPresent()) {
            System.out.println(">>> El escenario Familia Jaimes ya existe. Omitiendo.");
            return;
        }

        System.out.println(">>> Creando escenario Familia Jaimes (V8)...");

        // 1. Crear rol acudiente si no existe
        Rol rolAcudiente = rolRepo.findByNombre("ROLE_ACUDIENTE")
                .orElseGet(() -> rolRepo.save(new Rol("ROLE_ACUDIENTE")));

        // 2. Crear usuario acudiente (Juliana)
        Usuario juliana = new Usuario();
        juliana.setUsername("juliana_v8@test.com");
        juliana.setNombre("Juliana");
        juliana.setApellido("Jaimes");
        juliana.setEmail("juliana_v8@test.com");
        juliana.setPasswordHash(passwordEncoder.encode("1234"));
        juliana.setRoles(Set.of(rolAcudiente));
        juliana.setActivo(true);
        juliana = usuarioRepo.save(juliana);

        // 3. Crear grupo
        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(master, "Jóvenes Girón - V8")
                .orElseGet(() -> {
                    GrupoEntrenamiento g = new GrupoEntrenamiento();
                    g.setNombre("Jóvenes Girón - V8");
                    g.setSensei(master);
                    return grupoRepository.save(g);
                });
        // 4. Crear judokas
        String[][] datosJudokas = {
                {"Thaliana", "Jaimes", "FEMENINO"},
                {"Nahomy", "Jaimes", "FEMENINO"},
                {"Marian", "Jaimes", "FEMENINO"},
                {"Johan", "Jaimes", "MASCULINO"}
        };
        for (String[] d : datosJudokas) {
            Judoka j = new Judoka();
            j.setNombre(d[0]);
            j.setApellido(d[1]);
            j.setAcudiente(juliana);
            j.setSensei(master);
            j.setGrupo(grupo);
            j.setFechaNacimiento(LocalDate.now().minusYears(15));
            j.setSexo(Sexo.valueOf(d[2]));
            j.setGrado(GradoCinturon.BLANCO);
            j.setEstado(EstadoJudoka.ACTIVO);
            j.setMatriculaPagada(true);
            j.setSuscripcionActiva(true);
            j.setFechaVencimientoSuscripcion(LocalDate.now().plusMonths(1));
            judokaRepo.save(j);
        }

        // 5. Crear macrociclo
        Macrociclo macro = new Macrociclo();
        macro.setNombre("Adquisición Inicial");
        macro.setObjetivoPrincipal("Adaptación base");
        macro.setSensei(master);
        macro.setFechaInicio(LocalDate.now());
        macro.setFechaFin(LocalDate.now().plusMonths(4));
        macro = macrocicloRepository.save(macro);

        // 6. Crear tareas específicas si no existen
        TareaDiaria tareaMov = tareaDiariaRepository.findByNombreAndSenseiCreador("Movilidad Articular", master)
                .orElseGet(() -> {
                    TareaDiaria t = new TareaDiaria();
                    t.setNombre("Movilidad Articular");
                    t.setSenseiCreador(master);
                    t.setCategoria(CategoriaEjercicio.AGILIDAD);
                    return tareaDiariaRepository.save(t);
                });

        TareaDiaria tareaUchi = tareaDiariaRepository.findByNombreAndSenseiCreador("Uchikomi", master)
                .orElseGet(() -> {
                    TareaDiaria t = new TareaDiaria();
                    t.setNombre("Uchikomi");
                    t.setSenseiCreador(master);
                    t.setCategoria(CategoriaEjercicio.TECNICA);
                    return tareaDiariaRepository.save(t);
                });

        // 7. Crear microciclo
        Microciclo micro = new Microciclo();
        micro.setNombre("Semana de Prueba V8");
        micro.setMacrociclo(macro);
        micro.setSensei(master);
        micro.setFechaInicio(LocalDate.now());
        micro.setFechaFin(LocalDate.now().plusDays(7));
        micro.setEstado(EstadoMicrociclo.ACTIVO);
        micro.setTipoMicrociclo(TipoMicrociclo.AJUSTE);
        micro.setMesocicloATC(MesocicloATC.ADQUISICION);
        micro.getGruposAsignados().add(grupo);
        micro = microcicloRepo.save(micro);

        // 8. Ejercicios planificados
        EjercicioPlanificado ep1 = new EjercicioPlanificado();
        ep1.setMicrociclo(micro);
        ep1.setTareaDiaria(tareaMov);
        ep1.setDuracionMinutos(15);
        ep1.setNotasSensei("Fase inicial");
        ep1.setOrden(1);
        // Opcional: días asignados (por defecto ninguno)
        micro.addEjercicio(ep1);

        EjercicioPlanificado ep2 = new EjercicioPlanificado();
        ep2.setMicrociclo(micro);
        ep2.setTareaDiaria(tareaUchi);
        ep2.setDuracionMinutos(45);
        ep2.setNotasSensei("Fase técnica");
        ep2.setOrden(2);
        micro.addEjercicio(ep2);

        microcicloRepo.save(micro);

        System.out.println(">>> Escenario Familia Jaimes creado correctamente.");
    }
}