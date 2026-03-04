package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.ChatService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PruebaEstandarRepository pruebaRepo;
    private final MetricaRepository metricaRepo;
    private final ResultadoPruebaRepository resultadoRepo;
    private final EjercicioPlanificadoRepository ejercicioRepo;
    private final MicrocicloRepository microcicloRepo;
    private final ChatService chatService;
    private final InsigniaRepository insigniaRepo;
    private final JudokaInsigniaRepository judokaInsigniaRepo;
    private final PalmaresRepository palmaresRepo;
    private final TareaDiariaRepository tareaDiariaRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;

    public DatosMuestraInicializer(UsuarioRepository usuarioRepo,
                                   SenseiRepository senseiRepo,
                                   JudokaRepository judokaRepo,
                                   RolRepository rolRepo,
                                   PasswordEncoder passwordEncoder,
                                   UsuarioService usuarioService,
                                   PruebaEstandarRepository pruebaRepo,
                                   MetricaRepository metricaRepo,
                                   ResultadoPruebaRepository resultadoRepo,
                                   EjercicioPlanificadoRepository ejercicioRepo,
                                   MicrocicloRepository microcicloRepo,
                                   ChatService chatService,
                                   InsigniaRepository insigniaRepo,
                                   JudokaInsigniaRepository judokaInsigniaRepo,
                                   PalmaresRepository palmaresRepo, TareaDiariaRepository tareaDiariaRepository, GrupoEntrenamientoRepository grupoRepository, EjecucionTareaRepository ejecucionTareaRepository) {
        this.usuarioRepo = usuarioRepo;
        this.senseiRepo = senseiRepo;
        this.judokaRepo = judokaRepo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
        this.pruebaRepo = pruebaRepo;
        this.metricaRepo = metricaRepo;
        this.resultadoRepo = resultadoRepo;
        this.ejercicioRepo = ejercicioRepo;
        this.microcicloRepo = microcicloRepo;
        this.chatService = chatService;
        this.insigniaRepo = insigniaRepo;
        this.judokaInsigniaRepo = judokaInsigniaRepo;
        this.palmaresRepo = palmaresRepo;
        this.tareaDiariaRepository = tareaDiariaRepository;
        this.grupoRepository = grupoRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
    }

    @Transactional
    public void inicializar(Sensei masterSensei) {
        // Si ya existe algún sensei de muestra, asumimos que los datos ya están cargados
        if (usuarioRepo.findByUsername("kiuzo").isPresent()) {
            System.out.println(">>> Datos de muestra ya existen. Omitiendo.");
            return;
        }

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

        // 6. Generar resultados de pruebas históricos (varias fechas)
        generarResultadosFisicosConHistorial(maria, "EXPERTO");
        generarResultadosFisicosConHistorial(julian, "NOVATO");

        // 7. Generar ejecuciones de tareas diarias (simulando actividad reciente)
        generarEjecucionesTareas(maria, planAcond);
        generarEjecucionesTareas(julian, planAcond);

        // 8. Registrar antropometría (peso, estatura, etc.) como resultados de prueba
        registrarAntropometria(maria, 57.0, 160.0, 68.0, 165.0);
        registrarAntropometria(julian, 34.0, 138.0, 60.0, 135.0);

        // 9. Cargar insignias ganadas
        cargarInsigniasGanadas(maria, julian, kiuzo);

        // 10. Crear palmarés (competiciones)
        crearPalmares(maria, julian);

        // 11. Inicializar chat
        inicializarChat(kiuzo, maria, julian);
    }
    private GrupoEntrenamiento crearGrupoUnificado(Sensei sensei, List<Judoka> judokas) {
        GrupoEntrenamiento grupo = new GrupoEntrenamiento();
        grupo.setNombre("Grupo de Demostración");
        grupo.setDescripcion("Grupo unificado para pruebas (incluye María y Julián)");
        grupo.setSensei(sensei);
        grupo = grupoRepository.save(grupo);

        // Asignar el grupo a cada judoka y guardarlos
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
        plan.getGruposAsignados().add(grupo);
        plan = microcicloRepo.save(plan);

        // Asignar todas las tareas de la biblioteca
        List<TareaDiaria> tareas = tareaDiariaRepository.findAll();
        int orden = 1;
        for (TareaDiaria t : tareas) {
            EjercicioPlanificado ej = new EjercicioPlanificado();
            ej.setMicrociclo(plan);
            ej.setTareaDiaria(t);
            ej.setOrden(orden++);
            ej.setNotasSensei("Enfocar en técnica");
            ej.getDiasAsignados().addAll(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
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
            plan.addEjercicio(ej);
        }
        return microcicloRepo.save(plan);
    }
    // ------------------------------------------------------------------------
    // Métodos idempotentes de creación
    // ------------------------------------------------------------------------

    private Sensei crearSensei(String username, String nombre, String apellido, String pass, GradoCinturon grado) {
        // Buscar usuario existente
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

        // Buscar sensei existente asociado al usuario
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
        // Buscar usuario existente
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

        // Buscar judoka existente asociado al usuario
        Optional<Judoka> judokaOpt = judokaRepo.findByAcudiente_Username(username);        if (judokaOpt.isPresent()) {
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

    // ------------------------------------------------------------------------
    // Métodos auxiliares para datos relacionados (antropometría, resultados, etc.)
    // ------------------------------------------------------------------------

    private void registrarAntropometria(Judoka judoka, double peso, double estatura, double cintura, double envergadura) {
        // Verificar si ya tiene estos registros para evitar duplicados masivos
        // (simplificamos: no verificamos, pero podríamos hacerlo)
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.medicion_antropo.nombre").orElseThrow();
        Metrica mPeso = metricaRepo.findByNombreKey("metrica.masa_corporal.nombre").orElseThrow();
        Metrica mEstatura = metricaRepo.findByNombreKey("metrica.estatura.nombre").orElseThrow();
        Metrica mCintura = metricaRepo.findByNombreKey("metrica.cintura.nombre").orElseThrow();
        Metrica mEnvergadura = metricaRepo.findByNombreKey("metrica.envergadura.nombre").orElseThrow();

        EjercicioPlanificado dummy = obtenerEjercicioDummy(prueba);
        LocalDateTime ahora = LocalDateTime.now();

        resultadoRepo.save(crearResultado(judoka, dummy, mPeso, peso, ahora));
        resultadoRepo.save(crearResultado(judoka, dummy, mEstatura, estatura, ahora));
        resultadoRepo.save(crearResultado(judoka, dummy, mCintura, cintura, ahora));
        resultadoRepo.save(crearResultado(judoka, dummy, mEnvergadura, envergadura, ahora));
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
        // Buscar si ya existe un ejercicio con esa prueba
        return ejercicioRepo.findByPruebaEstandar(prueba).stream().findFirst()
                .orElseGet(() -> {
                    // Crear un microciclo dummy si es necesario
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
        for (int i = 0; i < 10; i++) { // últimos 10 días
            LocalDateTime fecha = hoy.minusDays(i);
            for (EjercicioPlanificado ej : ejercicios) {
                if (rand.nextDouble() > 0.3) { // 70% de completar
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
        // María: varias insignias
        asignarLogro(maria, "SHIN_CONSTANCIA", sensei, 60);
        asignarLogro(maria, "SHIN_COMPROMISO", sensei, 30);
        asignarLogro(maria, "GI_CINTURON", sensei, 90);
        asignarLogro(maria, "TAI_HERCULES", sensei, 15);
        asignarLogro(maria, "COMP_ORO", sensei, 45);

        // Julián: solo las básicas
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
        // Verificar si ya existen tareas para este sensei (opcional)
        if (tareaDiariaRepository.count() > 0) return;

        System.out.println(">>> Creando biblioteca de tareas para Sensei " + sensei.getUsuario().getNombre());

        List<TareaDiaria> tareas = List.of(
                new TareaDiaria("Calentamiento articular", "10 min de movilidad dinámica", sensei, CategoriaEjercicio.FLEXIBILIDAD),
                new TareaDiaria("Técnica de suelo (Ne-waza)", "15 min de inmovilizaciones y transiciones", sensei, CategoriaEjercicio.TECNICA),
                new TareaDiaria("Uchi-komi con gomas", "100 repeticiones (velocidad)", sensei, CategoriaEjercicio.VELOCIDAD),
                new TareaDiaria("Randori ligero", "3 combates de 3 min", sensei, CategoriaEjercicio.APTITUD_AEROBICA),
                new TareaDiaria("Flexiones de pecho", "4 series x 15 reps", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Abdominales en V", "3 series x 20 reps", sensei, CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA),
                new TareaDiaria("Sentadillas con salto", "4 series x 10 reps", sensei, CategoriaEjercicio.POTENCIA),
                new TareaDiaria("Estiramiento final", "10 min estático", sensei, CategoriaEjercicio.FLEXIBILIDAD)
        );
        tareaDiariaRepository.saveAll(tareas);
    }
    private void crearMicrociclosParaSensei(Sensei sensei) {
        if (microcicloRepo.count() > 0) return;

        System.out.println(">>> Creando microciclos para Sensei " + sensei.getUsuario().getNombre());

        // Buscar un grupo existente (opcional)
        GrupoEntrenamiento grupo = grupoRepository.findBySenseiAndNombre(sensei, "Selección Mayores")
                .orElseGet(() -> {
                    GrupoEntrenamiento g = new GrupoEntrenamiento();
                    g.setNombre("Selección Mayores");
                    g.setDescripcion("Grupo de competencia");
                    g.setSensei(sensei);
                    return grupoRepository.save(g);
                });

        // Microciclo de acondicionamiento
        Microciclo planAcond = new Microciclo();
        planAcond.setNombre("Base de pretemporada");
        planAcond.setSensei(sensei);
        planAcond.setEstado(EstadoMicrociclo.ACTIVO);
        planAcond.setTipoMicrociclo(TipoMicrociclo.AJUSTE);
        planAcond.getGruposAsignados().add(grupo);
        planAcond = microcicloRepo.save(planAcond);

        // Asignar ejercicios (tareas diarias)
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

        // Microciclo de evaluación (con pruebas)
        Microciclo planEval = new Microciclo();
        planEval.setNombre("Evaluación trimestral");
        planEval.setSensei(sensei);
        planEval.setEstado(EstadoMicrociclo.ACTIVO);
        planEval.setTipoMicrociclo(TipoMicrociclo.CONTROL);
        planEval.getGruposAsignados().add(grupo);
        planEval = microcicloRepo.save(planEval);

        // Agregar pruebas (SJFT, salto, etc.)
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
}