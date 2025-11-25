package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Component
public class DataInitializer {

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
                           AsistenciaRepository asistenciaRepository) {
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
    }

    @Bean
    @Transactional
    public CommandLineRunner init() {
        return args -> {
            if (judokaRepository.count() > 5) {
                System.out.println("Datos ya creados. Saltando inicialización.");
                return;
            }

            System.out.println("CREANDO DATOS REALES DEL CLUB JUDO COLOMBIA 2025");

            crearRoles();
            crearTraduccionesDias();

            Sensei kiuzo = crearSensei("kiuzo", "Kiuzo", "Mifune", "123456", GradoCinturon.NEGRO_5_DAN);
            Sensei toshiro = crearSensei("toshiro", "Toshiro", "Diago", "123456", GradoCinturon.NEGRO_5_DAN);

            List<Judoka> judokas = crearJudokas();
            crearGruposYAsignar(judokas);
            crearTareasCadetes(kiuzo);

            PlanEntrenamiento planSemanal = crearPlanSemanal(kiuzo);
            PlanEntrenamiento planEval = crearPlanEvaluacion(kiuzo);

            programarSesiones(kiuzo);
            generarHistoriaReal(judokas, planEval);
            generarEjecuciones(planSemanal);
            generarAsistencias();

            System.out.println("¡CLUB JUDO COLOMBIA 100% LISTO!");
        };
    }

    private void crearRoles() {
        List.of("ROLE_SENSEI", "ROLE_JUDOKA", "ROLE_COMPETIDOR", "ROLE_ADMIN", "ROLE_MECENAS")
                .forEach(nombre -> rolRepository.findByNombre(nombre)
                        .orElseGet(() -> rolRepository.save(new Rol(nombre))));
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

    private Sensei crearSensei(String user, String nom, String ape, String pass, GradoCinturon grado) {
        Usuario u = new Usuario();
        u.setUsername(user);
        u.setNombre(nom);
        u.setApellido(ape);
        u.setActivo(true);
        u = usuarioService.saveUsuario(u, pass);
        u.getRoles().addAll(Set.of(
                rolRepository.findByNombre("ROLE_SENSEI").orElseThrow(),
                rolRepository.findByNombre("ROLE_ADMIN").orElseThrow()
        ));

        Sensei s = new Sensei();
        s.setUsuario(u);
        s.setGrado(grado);
        s.setAnosPractica(30);
        return senseiRepository.save(s);
    }

    private List<Judoka> crearJudokas() {
        List<Judoka> lista = new ArrayList<>();
        lista.add(judoka("maria.lopez", "María", "López", 2010, 3, 15, Sexo.FEMENINO, GradoCinturon.AMARILLO, true, "Jorge López"));
        lista.add(judoka("juan.gomez", "Juan Camilo", "Gómez", 2008, 7, 22, Sexo.MASCULINO, GradoCinturon.NARANJA, true, "Camilo Gómez"));
        lista.add(judoka("laura.ramirez", "Laura", "Ramírez", 2006, 4, 10, Sexo.FEMENINO, GradoCinturon.VERDE, false, null));
        lista.add(judoka("daniel.diaz", "Daniel", "Díaz", 2003, 1, 30, Sexo.MASCULINO, GradoCinturon.NEGRO_1_DAN, true, null));
        lista.add(judoka("danna.ortega", "Danna", "Ortega", 2000, 9, 8, Sexo.FEMENINO, GradoCinturon.NEGRO_1_DAN, true, null));
        return judokaRepository.saveAll(lista);
    }

    private Judoka judoka(String user, String nom, String ape, int año, int mes, int dia, Sexo sexo, GradoCinturon grado, boolean comp, String acudiente) {
        Usuario u = new Usuario();
        u.setUsername(user);
        u.setNombre(nom);
        u.setApellido(ape);
        u.setActivo(true);
        u = usuarioService.saveUsuario(u, "123456");
        u.getRoles().add(rolRepository.findByNombre("ROLE_JUDOKA").orElseThrow());
        if (comp) u.getRoles().add(rolRepository.findByNombre("ROLE_COMPETIDOR").orElseThrow());

        Judoka j = new Judoka();
        j.setUsuario(u);
        j.setFechaNacimiento(LocalDate.of(año, mes, dia));
        j.setSexo(sexo);
        j.setGrado(grado);
        j.setEsCompetidorActivo(comp);
        j.setNombreAcudiente(acudiente);
        return j;
    }

    private void crearGruposYAsignar(List<Judoka> judokas) {
        // Creamos los grupos
        GrupoEntrenamiento cadetes = grupo("Judokas Cadetes", "15-17 años");
        GrupoEntrenamiento campeonato = grupo("Equipo Campeonato San Vicente", "Élite");

        // === AHORA SÍ: ASIGNAMOS DESDE EL LADO OWNER (GrupoEntrenamiento) ===
        cadetes.getJudokas().addAll(List.of(
                judokas.get(0), // María
                judokas.get(1), // Juan Camilo
                judokas.get(2)  // Laura
        ));

        campeonato.getJudokas().addAll(List.of(
                judokas.get(1), // Juan Camilo
                judokas.get(3), // Daniel
                judokas.get(4)  // Danna
        ));

        // Guardamos los grupos (y se persiste la tabla judoka_grupos automáticamente)
        grupoRepository.saveAll(List.of(cadetes, campeonato));
    }

    private GrupoEntrenamiento grupo(String nombre, String desc) {
        GrupoEntrenamiento g = new GrupoEntrenamiento();
        g.setNombre(nombre);
        g.setDescripcion(desc);
        g.setJudokas(new HashSet<>()); // Importante: inicializar el Set
        return g;
    }

    private void crearTareasCadetes(Sensei sensei) {
        tareaDiariaRepository.saveAll(List.of(
                t("Press Banca", "4x5 @65-70% 1RM", sensei),
                t("Power Clean", "4x3 @60-70% 1RM", sensei),
                t("Sentadilla Trasera", "4x5 @70-75% 1RM", sensei),
                t("Dominadas con Toalla / Gi", "4x6-8", sensei),
                t("Plancha Frontal", "3x40-50s", sensei)
        ));
    }

    private TareaDiaria t(String nombre, String meta, Sensei s) {
        TareaDiaria td = new TareaDiaria();
        td.setNombre(nombre);
        td.setMetaTexto(meta);
        td.setSenseiCreador(s);
        return td;
    }

    private PlanEntrenamiento crearPlanSemanal(Sensei sensei) {
        GrupoEntrenamiento cadetes = grupoRepository.findByNombre("Judokas Cadetes").orElseThrow();

        PlanEntrenamiento plan = planService.crearPlanEntrenamiento(
                "Acondicionamiento Cadetes 2025-2026",
                sensei,
                Set.of(cadetes)
        );

        plan.setEstado(EstadoPlan.EN_PROGRESO);
        plan.setFechaAsignacion(LocalDate.now());

        TareaDiaria press = tareaDiariaRepository.findAll().stream()
                .filter(t -> t.getNombre().contains("Press Banca"))
                .findFirst()
                .orElseThrow();

        EjercicioPlanificado ep = new EjercicioPlanificado();
        ep.setTareaDiaria(press);
        ep.setDiasAsignados(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

        // ESTAS DOS LÍNEAS SON LA CLAVE
        ep.setPlanEntrenamiento(plan);                    // ← ¡¡ESTO FALTABA!!
        plan.getEjerciciosPlanificados().add(ep);         // ← Esto solo mantiene el orden

        return planService.guardarPlan(plan);
    }
    private PlanEntrenamiento crearPlanEvaluacion(Sensei sensei) {
        GrupoEntrenamiento cadetes = grupoRepository.findByNombre("Judokas Cadetes")
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        PlanEntrenamiento plan = planService.crearPlanEntrenamiento(
                "Evaluación Física – Diciembre 2025",
                sensei,
                Set.of(cadetes)
        );

        plan.setTipoSesion(TipoSesion.EVALUACION);
        plan.setEstado(EstadoPlan.EN_PROGRESO);
        plan.setFechaAsignacion(LocalDate.now());

        return planService.guardarPlan(plan);
    }

    private void programarSesiones(Sensei sensei) {
        GrupoEntrenamiento g = grupoRepository.findByNombre("Judokas Cadetes").orElseThrow();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(17).withMinute(0);
        for (int i = 0; i < 10; i++) {
            SesionProgramada s = new SesionProgramada();
            s.setNombre("Entrenamiento Cadetes");
            s.setTipoSesion(TipoSesion.ENTRENAMIENTO);
            s.setFechaHoraInicio(inicio.plusDays(i * 2));
            s.setFechaHoraFin(inicio.plusDays(i * 2).plusHours(2));
            s.setGrupo(g);
            s.setSensei(sensei);
            sesionRepository.save(s);
        }
    }

    private void generarHistoriaReal(List<Judoka> judokas, PlanEntrenamiento planEval) {
        if (judokas.isEmpty()) return;

        Judoka maria = judokas.get(0);

        // Aseguramos que haya al menos un ejercicio planificado
        EjercicioPlanificado ep = planEval.getEjerciciosPlanificados().stream()
                .findFirst()
                .orElseGet(() -> {
                    EjercicioPlanificado nuevo = new EjercicioPlanificado();
                    nuevo.setPlanEntrenamiento(planEval);  // ← ¡¡AQUÍ TAMBIÉN!!
                    planEval.getEjerciciosPlanificados().add(nuevo);
                    return nuevo;
                });

        Metrica metrica = metricaRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Metrica m = new Metrica();
                    m.setNombreKey("temp.sjft_total");
                    m.setUnidad("reps");
                    return metricaRepository.save(m);
                });

        LocalDateTime base = LocalDateTime.now().minusMonths(6);
        for (int i = 0; i < 6; i++) {
            ResultadoPrueba r = new ResultadoPrueba();
            r.setJudoka(maria);
            r.setEjercicioPlanificado(ep);
            r.setMetrica(metrica);
            r.setValor(24.0 + i * 1.5);
            r.setFechaRegistro(base.plusMonths(i));
            resultadoPruebaRepository.save(r);
        }
    }

    private void generarEjecuciones(PlanEntrenamiento plan) {
        // 100% seguro y simple
        Judoka j = judokaRepository.findAll().get(0);
        EjercicioPlanificado ep = plan.getEjerciciosPlanificados().get(0);
        for (int i = 0; i < 20; i++) {
            EjecucionTarea e = new EjecucionTarea();
            e.setJudoka(j);
            e.setEjercicioPlanificado(ep);
            e.setCompletado(true);
            e.setFechaRegistro(LocalDateTime.now().minusDays(i));
            ejecucionTareaRepository.save(e);
        }
    }

    private void generarAsistencias() {
        SesionProgramada s = sesionRepository.findAll().get(0);
        Judoka j = judokaRepository.findAll().get(0);
        Asistencia a = new Asistencia();
        a.setJudoka(j);
        a.setSesion(s);
        a.setPresente(true);
        a.setFechaHoraMarcacion(s.getFechaHoraInicio().plusMinutes(5));
        asistenciaRepository.save(a);
    }
}