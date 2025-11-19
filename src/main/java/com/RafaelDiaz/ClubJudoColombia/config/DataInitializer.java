package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
// --- Servicios Refactorizados ---
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService; // Refactorizado
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet; // --- Importar ---

@Component
public class DataInitializer implements CommandLineRunner {

    // --- Repositorios y Servicios Inyectados (Completos) ---
    private final RolRepository rolRepository;
    private final UsuarioService usuarioService;
    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;
    private final SesionProgramadaRepository sesionProgramadaRepository;
    private final PlanEntrenamientoService planEntrenamientoService;
    private final PruebaEstandarRepository pruebaEstandarRepository; // Refactorizado
    private final MetricaRepository metricaRepository;
    private final ResultadoPruebaService resultadoPruebaService; // Refactorizado
    private final TareaDiariaRepository tareaDiariaRepository; // Nuevo
    private final TraduccionRepository traduccionRepository;
    /**
     * --- CONSTRUCTOR ACTUALIZADO ---
     */
    public DataInitializer(RolRepository rolRepository,
                           UsuarioService usuarioService,
                           SenseiRepository senseiRepository,
                           JudokaRepository judokaRepository,
                           GrupoEntrenamientoRepository grupoEntrenamientoRepository,
                           SesionProgramadaRepository sesionProgramadaRepository,
                           PlanEntrenamientoService planEntrenamientoService,
                           PruebaEstandarRepository pruebaEstandarRepository, // Refactorizado
                           MetricaRepository metricaRepository,
                           ResultadoPruebaService resultadoPruebaService, // Refactorizado
                           TareaDiariaRepository tareaDiariaRepository, TraduccionRepository traduccionRepository // Nuevo
    ) {
        this.rolRepository = rolRepository;
        this.usuarioService = usuarioService;
        this.senseiRepository = senseiRepository;
        this.judokaRepository = judokaRepository;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
        this.sesionProgramadaRepository = sesionProgramadaRepository;
        this.planEntrenamientoService = planEntrenamientoService;
        this.pruebaEstandarRepository = pruebaEstandarRepository; // Refactorizado
        this.metricaRepository = metricaRepository;
        this.resultadoPruebaService = resultadoPruebaService; // Refactorizado
        this.tareaDiariaRepository = tareaDiariaRepository; // Nuevo
        this.traduccionRepository = traduccionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Los roles y la biblioteca de pruebas vienen de Flyway (V1-V14).

        Sensei senseiDePrueba = crearSenseiDePrueba();
        Judoka judokaDePrueba = crearJudokaYGrupoDePrueba();
        generarCalendarioSesiones(senseiDePrueba);

        crearTraduccionesDias();
        crearResultadosDePrueba(senseiDePrueba, judokaDePrueba); // Flujo 1: Evaluación (Sensei)
        crearPlanDeTareasDiarias(senseiDePrueba, judokaDePrueba); // Flujo 2: Tareas (Judoka)
    }

    private Sensei crearSenseiDePrueba() {
        // ... (Sin cambios) ...
        System.out.println("--- Verificando Sensei de prueba ---");
        if (usuarioService.findByUsername("sensei.admin").isPresent()) {
            System.out.println("... Sensei 'sensei.admin' ya existe.");
            Usuario usuario = usuarioService.findByUsername("sensei.admin").get();
            return senseiRepository.findByUsuario(usuario).orElseThrow();
        }
        System.out.println(">>> Creando Sensei 'sensei.admin'...");
        Rol rolSensei = rolRepository.findByNombre("ROLE_SENSEI").orElseThrow();
        Usuario usuarioSensei = new Usuario("sensei.admin", "password123", "Sensei", "Admin");
        usuarioSensei.setRoles(Set.of(rolSensei));
        usuarioService.saveUsuario(usuarioSensei, "password123");
        Sensei perfilSensei = new Sensei();
        perfilSensei.setUsuario(usuarioSensei);
        perfilSensei.setGrado(GradoCinturon.NEGRO_4_DAN);
        perfilSensei.setAnosPractica(45);
        perfilSensei.setBiografia("Sensei principal del club.");
        return senseiRepository.save(perfilSensei);
    }

    private Judoka crearJudokaYGrupoDePrueba() {
        // ... (Sin cambios, sigue devolviendo el Judoka) ...
        System.out.println("--- Verificando Judoka y Grupo de prueba ---");
        Judoka judokaPrueba;
        if (usuarioService.findByUsername("judoka.prueba").isEmpty()) {
            System.out.println(">>> Creando Judoka 'judoka.prueba'...");
            Rol rolJudoka = rolRepository.findByNombre("ROLE_JUDOKA").orElseThrow();
            Usuario usuarioJudoka = new Usuario("judoka.prueba", "password123", "Judoka", "De Prueba");
            usuarioJudoka.setRoles(Set.of(rolJudoka));
            usuarioService.saveUsuario(usuarioJudoka, "password123");
            Judoka perfilJudoka = new Judoka();
            perfilJudoka.setUsuario(usuarioJudoka);
            perfilJudoka.setFechaNacimiento(LocalDate.of(2010, 5, 15)); // (Edad 15)
            perfilJudoka.setSexo(Sexo.MASCULINO);
            perfilJudoka.setGrado(GradoCinturon.VERDE);
            perfilJudoka.setPeso(60.0);
            perfilJudoka.setEstatura(165.0);
            perfilJudoka.setNombreAcudiente("Acudiente de Prueba");
            perfilJudoka.setTelefonoAcudiente("3001234567");
            judokaPrueba = judokaRepository.save(perfilJudoka);
        } else {
            System.out.println("... Judoka 'judoka.prueba' ya existe.");
            Usuario usuario = usuarioService.findByUsername("judoka.prueba").get();
            judokaPrueba = judokaRepository.findByUsuario(usuario).orElseThrow();
        }
        if (grupoEntrenamientoRepository.findByNombre("Equipo Sub-15").isEmpty()) {
            System.out.println(">>> Creando 'Equipo Sub-15'...");
            GrupoEntrenamiento grupo = new GrupoEntrenamiento();
            grupo.setNombre("Equipo Sub-15");
            grupo.setDescripcion("Grupo de prueba para menores de 15 años.");
            grupo.getJudokas().add(judokaPrueba);
            grupoEntrenamientoRepository.save(grupo);
        } else {
            System.out.println("... 'Equipo Sub-15' ya existe.");
        }
        return judokaPrueba;
    }

    private void generarCalendarioSesiones(Sensei sensei) {
        // ... (Sin cambios) ...
        System.out.println("--- Verificando calendario de sesiones ---");
        if (sesionProgramadaRepository.count() > 0) {
            System.out.println("... El calendario ya tiene sesiones programadas.");
            return;
        }
        System.out.println("--- Generando calendario de sesiones regulares ---");
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = LocalDate.of(2025, 12, 12);
        LocalTime inicioMayores = LocalTime.of(16, 0);
        LocalTime finMayores = LocalTime.of(17, 30);
        LocalTime inicioMenores = LocalTime.of(18, 0);
        LocalTime finMenores = LocalTime.of(19, 30);
        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia == DayOfWeek.MONDAY || dia == DayOfWeek.WEDNESDAY || dia == DayOfWeek.FRIDAY) {
                // ... (Lógica de crear sesiones) ...
            }
        }
        System.out.println("--- Generación de calendario completada ---");
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * Flujo 1: Crea un Plan de EVALUACIÓN y los RESULTADOS (del Sensei)
     * para que el "Poder de Combate" tenga datos.
     */
    private void crearResultadosDePrueba(Sensei sensei, Judoka judoka) {
        System.out.println("--- Verificando resultados de pruebas (Flujo 1) ---");

        PruebaEstandar pruebaReferencia = pruebaEstandarRepository.findByNombreKey("ejercicio.salto_horizontal_proesp.nombre").orElse(null);
        if (pruebaReferencia == null) {
            System.out.println("... Biblioteca de pruebas aún no cargada. Omitiendo resultados.");
            return;
        }
        if (!resultadoPruebaService.getHistorialDeResultados(judoka, pruebaReferencia).isEmpty()) {
            System.out.println("... El Judoka de prueba ya tiene resultados de pruebas.");
            return;
        }

        System.out.println(">>> Creando Plan de Evaluación Inicial y resultados ficticios...");

        GrupoEntrenamiento grupo = grupoEntrenamientoRepository.findByNombre("Equipo Sub-15").orElseThrow();
        Set<GrupoEntrenamiento> gruposDelPlan = new HashSet<>(Set.of(grupo));

        PlanEntrenamiento plan = planEntrenamientoService.crearPlanEntrenamiento(
                "Evaluación Inicial 2025 (Pruebas Sensei)",
                sensei,
                gruposDelPlan
        );

        List<String> clavesPruebasClave = List.of(
                "ejercicio.salto_horizontal_proesp.nombre",
                "ejercicio.lanzamiento_balon.nombre",
                "ejercicio.abdominales_1min.nombre",
                "ejercicio.carrera_6min.nombre",
                "ejercicio.agilidad_4x4.nombre",
                "ejercicio.carrera_20m.nombre",
                "ejercicio.sjft.nombre"
        );
        Map<String, Double> valoresFicticios = Map.of(
                "ejercicio.salto_horizontal_proesp.nombre", 190.0,
                "ejercicio.lanzamiento_balon.nombre", 440.0,
                "ejercicio.abdominales_1min.nombre", 45.0,
                "ejercicio.carrera_6min.nombre", 1130.0,
                "ejercicio.agilidad_4x4.nombre", 6.0,
                "ejercicio.carrera_20m.nombre", 3.5,
                "ejercicio.sjft.nombre", 14.0
        );

        // BUCLE 1: Crear Tareas (EjercicioPlanificado) vinculadas a Pruebas
        for (String clave : clavesPruebasClave) {
            PruebaEstandar prueba = pruebaEstandarRepository.findByNombreKey(clave).orElseThrow();
            EjercicioPlanificado tarea = new EjercicioPlanificado();
            tarea.setPruebaEstandar(prueba); // <-- Vincula a la PRUEBA
            plan.addEjercicio(tarea);
        }

        PlanEntrenamiento planGuardado = planEntrenamientoService.guardarPlan(plan);

        // BUCLE 2: Crear Resultados de Prueba (los datos del Sensei)
        for (EjercicioPlanificado tareaGuardada : planGuardado.getEjerciciosPlanificados()) {
            String clave = tareaGuardada.getPruebaEstandar().getNombreKey();

            Metrica metricaARegistrar;
            if (clave.equals("ejercicio.sjft.nombre")) {
                metricaARegistrar = metricaRepository.findByNombreKey("metrica.sjft_indice.nombre").orElseThrow();
            } else {
                metricaARegistrar = tareaGuardada.getPruebaEstandar().getMetricas().stream().findFirst().orElseThrow();
            }

            ResultadoPrueba resultado = new ResultadoPrueba(); // <-- Objeto ResultadoPrueba
            resultado.setJudoka(judoka);
            resultado.setEjercicioPlanificado(tareaGuardada);
            resultado.setMetrica(metricaARegistrar);
            resultado.setValor(valoresFicticios.get(clave));
            resultado.setNotasJudoka("Resultado base (Sensei) generado por el sistema.");

            resultadoPruebaService.registrarResultado(resultado); // Usa el servicio de Pruebas
        }

        System.out.println(">>> Resultados ficticios de pruebas creados.");
    }

    /**
     * --- ¡NUEVO MÉTODO AÑADIDO! ---
     * Flujo 2: Crea Tareas Diarias y un Plan de Acondicionamiento (para el Judoka).
     */
    /**
     * --- ¡MÉTODO ACTUALIZADO! ---
     * Flujo 2: Crea Tareas Diarias y un Plan de Acondicionamiento (para el Judoka).
     * (Ahora asigna los días de la semana a las tareas).
     */
    /**
     * --- ¡MÉTODO ACTUALIZADO! ---
     * Flujo 2: Crea Tareas Diarias y un Plan de Acondicionamiento (para el Judoka).
     * (Ahora asigna los días de la semana a las tareas).
     */
    private void crearPlanDeTareasDiarias(Sensei sensei, Judoka judoka) {
        System.out.println("--- Verificando tareas diarias (Flujo 2) ---");

        if (tareaDiariaRepository.count() > 0) {
            System.out.println("... Tareas diarias ya existen.");
            return;
        }

        System.out.println(">>> Creando Tareas Diarias y Plan de Acondicionamiento...");

        // 1. Crear Tareas Diarias en la biblioteca
        TareaDiaria flexiones = new TareaDiaria();
        flexiones.setNombre("Flexiones de Brazo");
        flexiones.setDescripcion("Realizar flexiones de pecho estándar.");
        flexiones.setMetaTexto("4 series x 15 repeticiones");
        flexiones.setSenseiCreador(sensei);
        tareaDiariaRepository.save(flexiones);

        TareaDiaria saltos = new TareaDiaria();
        saltos.setNombre("Saltos Largos");
        saltos.setDescripcion("Saltos horizontales con ambas piernas.");
        saltos.setMetaTexto("4 series x 10 saltos");
        saltos.setSenseiCreador(sensei);
        tareaDiariaRepository.save(saltos);

        // 2. Crear un Plan de Acondicionamiento
        GrupoEntrenamiento grupo = grupoEntrenamientoRepository.findByNombre("Equipo Sub-15").orElseThrow();
        Set<GrupoEntrenamiento> gruposDelPlan = new HashSet<>(Set.of(grupo));

        PlanEntrenamiento planAcond = planEntrenamientoService.crearPlanEntrenamiento(
                "Rutina Acondicionamiento Semana 1",
                sensei,
                gruposDelPlan
        );

        // --- ¡LÓGICA DE DÍAS AÑADIDA! ---
        Set<DayOfWeek> diasAlternos = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> diasMartesJueves = Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);

        // 3. Vincular las tareas al plan CON DÍAS
        EjercicioPlanificado tareaPlan1 = new EjercicioPlanificado();
        tareaPlan1.setTareaDiaria(flexiones); // <-- Vincula a la TAREA
        tareaPlan1.setDiasAsignados(diasAlternos); // <-- Asigna L-M-V
        planAcond.addEjercicio(tareaPlan1);

        EjercicioPlanificado tareaPlan2 = new EjercicioPlanificado();
        tareaPlan2.setTareaDiaria(saltos); // <-- Vincula a la TAREA
        tareaPlan2.setDiasAsignados(diasMartesJueves); // <-- Asigna M-J
        planAcond.addEjercicio(tareaPlan2);

        planEntrenamientoService.guardarPlan(planAcond);

        System.out.println(">>> Plan de Acondicionamiento creado.");
    }
    private void crearTraduccionesDias() {
        // Verificamos si ya existen para no duplicar
        if (traduccionRepository.findByClaveAndIdioma("MONDAY", "es").isPresent()) {
            return; // Ya existen
        }

        System.out.println(">>> Poblando traducciones de días de la semana...");

        traduccionRepository.saveAll(List.of(
                new Traduccion("MONDAY", "es", "Lunes"),
                new Traduccion("TUESDAY", "es", "Martes"),
                new Traduccion("WEDNESDAY", "es", "Miércoles"),
                new Traduccion("THURSDAY", "es", "Jueves"),
                new Traduccion("FRIDAY", "es", "Viernes"),
                new Traduccion("SATURDAY", "es", "Sábado"),
                new Traduccion("SUNDAY", "es", "Domingo"),

                // (Traducciones de Estados de Plan, por si acaso)
                new Traduccion("PENDIENTE", "es", "Pendiente"),
                new Traduccion("EN_PROGRESO", "es", "En Progreso"),
                new Traduccion("COMPLETADO", "es", "Completado")
        ));
    }
}