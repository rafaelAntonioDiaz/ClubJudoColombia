package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento; // --- NUEVO IMPORT ---
import com.RafaelDiaz.ClubJudoColombia.repositorio.MetricaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional; // --- NUEVO IMPORT ---
import java.util.stream.Collectors;

@Route("registrar-resultados")
@RolesAllowed("ROLE_SENSEI")
public class SenseiResultadosView extends VerticalLayout {

    // --- Servicios y Repositorios ---
    private final JudokaService judokaService;
    private final PlanEntrenamientoService planEntrenamientoService;
    private final TraduccionService traduccionService;
    private final MetricaRepository metricaRepository;
    private final ResultadoPruebaService resultadoPruebaService;

    // ... (Componentes de UI) ...
    private ComboBox<Judoka> judokaComboBox;
    private Grid<PlanEntrenamiento> planesGrid;
    private Grid<EjercicioPlanificado> pruebasGrid;
    private ResultadoPruebaForm resultadoForm;
    private Judoka judokaSeleccionado;

    public SenseiResultadosView(JudokaService judokaService,
                                PlanEntrenamientoService planEntrenamientoService,
                                TraduccionService traduccionService,
                                MetricaRepository metricaRepository,
                                ResultadoPruebaService resultadoPruebaService) {
        this.judokaService = judokaService;
        this.planEntrenamientoService = planEntrenamientoService;
        this.traduccionService = traduccionService;
        this.metricaRepository = metricaRepository;
        this.resultadoPruebaService = resultadoPruebaService;

        setSizeFull();
        add(new H1("Registro de Resultados de Pruebas Estándar"));

        // 1. Crear el formulario (oculto)
        resultadoForm = new ResultadoPruebaForm(traduccionService, metricaRepository);
        resultadoForm.setVisible(false);
        resultadoForm.addSaveListener(this::guardarResultados);
        resultadoForm.addCancelListener(e -> cerrarEditorResultados());

        // 2. Configurar el ComboBox de Judoka
        configurarJudokaComboBox();

        // 3. Configurar los Grids
        configurarGridPlanes();
        configurarGridPruebas();

        // 4. Layout
        HorizontalLayout gridsLayout = new HorizontalLayout(planesGrid, pruebasGrid);
        gridsLayout.setSizeFull();
        VerticalLayout panelDerecho = new VerticalLayout(gridsLayout, resultadoForm);
        panelDerecho.setHeightFull();
        HorizontalLayout contenido = new HorizontalLayout(judokaComboBox, panelDerecho);
        contenido.setSizeFull();

        add(contenido);
    }

    private void configurarJudokaComboBox() {
        judokaComboBox = new ComboBox<>("Seleccionar Judoka");
        judokaComboBox.setItems(judokaService.findAllJudokasWithUsuario());
        judokaComboBox.setItemLabelGenerator(judoka ->
                judoka.getUsuario().getNombre() + " " + judoka.getUsuario().getApellido()
        );
        judokaComboBox.setWidth("300px");

        judokaComboBox.addValueChangeListener(event -> {
            this.judokaSeleccionado = event.getValue();
            cargarPlanesDeEvaluacion(judokaSeleccionado);
            cerrarEditorResultados();
            pruebasGrid.setItems(Collections.emptyList());
        });
    }

    private void configurarGridPlanes() {
        planesGrid = new Grid<>(PlanEntrenamiento.class);
        planesGrid.setWidth("50%");
        planesGrid.setVisible(false);
        planesGrid.removeAllColumns();

        planesGrid.addColumn(PlanEntrenamiento::getNombre).setHeader("Planes de Evaluación");
        planesGrid.asSingleSelect().addValueChangeListener(event -> {
            cargarPruebasDelPlan(event.getValue());
            cerrarEditorResultados();
        });
    }

    private void configurarGridPruebas() {
        pruebasGrid = new Grid<>(EjercicioPlanificado.class);
        pruebasGrid.setWidth("50%");
        pruebasGrid.setVisible(false);
        pruebasGrid.removeAllColumns();

        pruebasGrid.addColumn(ep -> traduccionService.get(ep.getPruebaEstandar().getNombreKey()))
                .setHeader("Pruebas del Plan (Clic para registrar)");

        pruebasGrid.asSingleSelect().addValueChangeListener(event ->
                mostrarFormularioRegistro(event.getValue())
        );
    }

    private void cargarPlanesDeEvaluacion(Judoka judoka) {
        if (judoka == null) {
            planesGrid.setVisible(false);
            return;
        }

        List<PlanEntrenamiento> planes = planEntrenamientoService.buscarPlanesPorJudoka(judoka);

        List<PlanEntrenamiento> planesDeEvaluacion = planes.stream()
                .filter(plan -> plan.getEjerciciosPlanificados().stream()
                        .anyMatch(ep -> ep.getPruebaEstandar() != null))
                .collect(Collectors.toList());

        planesGrid.setItems(planesDeEvaluacion);
        planesGrid.setVisible(true);
    }

    private void cargarPruebasDelPlan(PlanEntrenamiento plan) {
        if (plan == null) {
            pruebasGrid.setVisible(false);
            return;
        }

        List<EjercicioPlanificado> pruebas = plan.getEjerciciosPlanificados().stream()
                .filter(ep -> ep.getPruebaEstandar() != null)
                .collect(Collectors.toList());

        pruebasGrid.setItems(pruebas);
        pruebasGrid.setVisible(true);
    }

    private void mostrarFormularioRegistro(EjercicioPlanificado ejercicioPlan) {
        if (ejercicioPlan == null) {
            cerrarEditorResultados();
            return;
        }

        resultadoForm.setPrueba(ejercicioPlan, judokaSeleccionado);
        resultadoForm.setVisible(true);
    }

    /**
     * --- MÉTODO ACTUALIZADO ---
     * Handler para el evento 'Save'.
     * Ahora construye un feedback detallado para el Sensei.
     */
    private void guardarResultados(ResultadoPruebaForm.SaveEvent event) {
        List<ResultadoPrueba> resultadosBrutos = event.getResultados();
        EjercicioPlanificado ejPlan = resultadosBrutos.get(0).getEjercicioPlanificado();
        String nombrePruebaKey = ejPlan.getPruebaEstandar().getNombreKey();

        // StringBuilder para construir el mensaje de feedback
        StringBuilder feedback = new StringBuilder("Resultados guardados: ");

        try {
            // --- TAREA 3: CÁLCULO DE ÍNDICES ---
            if (nombrePruebaKey.equals("ejercicio.sjft.nombre")) {
                // Flujo SJFT: Calcular índice
                ResultadoPrueba indiceGuardado = calcularYGuardarIndiceSJFT(resultadosBrutos, judokaSeleccionado, ejPlan);

                // Obtener clasificación PARA ESE ÍNDICE
                Optional<ClasificacionRendimiento> clasificacionOpt = resultadoPruebaService.getClasificacionParaResultado(indiceGuardado);
                // Usamos el traductor en el Enum (ej. EXCELENTE -> "Excelente")
                String clasificacionStr = clasificacionOpt.map(clasificacion -> traduccionService.get(clasificacion.name()))
                        .orElse("Sin clasificación");
                feedback.append(String.format("Índice SJFT: %.2f (%s). ", indiceGuardado.getValor(), clasificacionStr));

            } else {
                // Flujo Pruebas Simples: Guardar y clasificar cada una
                for (ResultadoPrueba res : resultadosBrutos) {
                    ResultadoPrueba resultadoGuardado = resultadoPruebaService.registrarResultado(res);

                    // Obtener clasificación
                    Optional<ClasificacionRendimiento> clasificacionOpt = resultadoPruebaService.getClasificacionParaResultado(resultadoGuardado);
                    String clasificacionStr = clasificacionOpt.map(clasificacion -> traduccionService.get(clasificacion.name()))
                            .orElse("Sin clasificación");
                    // Obtener nombre de la métrica (ej. "Distancia")
                    String metricaNombre = traduccionService.get(res.getMetrica().getNombreKey());

                    feedback.append(String.format("%s: %.1f -> %s. ", metricaNombre, res.getValor(), clasificacionStr));
                }
            }

            // --- MOSTRAR EL FEEDBACK CONSTRUIDO ---
            Notification.show(feedback.toString(), 5000, Notification.Position.MIDDLE);
            cerrarEditorResultados();

        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    /**
     * --- MÉTODO ACTUALIZADO ---
     * Ahora devuelve el ResultadoPrueba (el índice) que guardó.
     */
    private ResultadoPrueba calcularYGuardarIndiceSJFT(List<ResultadoPrueba> resultadosBrutos, Judoka judoka, EjercicioPlanificado ejPlan) {

        Map<String, Double> datos = resultadosBrutos.stream()
                .collect(Collectors.toMap(
                        res -> res.getMetrica().getNombreKey(),
                        ResultadoPrueba::getValor
                ));

        // 1. Extraer datos brutos
        Double s1 = datos.get("metrica.sjft_proyecciones_s1.nombre");
        Double s2 = datos.get("metrica.sjft_proyecciones_s2.nombre");
        Double s3 = datos.get("metrica.sjft_proyecciones_s3.nombre");
        Double fcFinal = datos.get("metrica.sjft_fc_final.nombre");
        Double fc1Min = datos.get("metrica.sjft_fc_1min.nombre");

        // 2. Validar
        if (s1 == null || s2 == null || s3 == null || fcFinal == null || fc1Min == null) {
            throw new IllegalStateException("Faltan datos para calcular el índice SJFT. Asegúrese de llenar todos los campos.");
        }

        // 3. Calcular
        double totalProyecciones = s1 + s2 + s3;
        if (totalProyecciones == 0) {
            throw new IllegalStateException("El total de proyecciones no puede ser cero.");
        }

        double indiceCalculado = (fcFinal + fc1Min) / totalProyecciones;

        // 4. Obtener la Métrica "Índice"
        Metrica metricaIndice = metricaRepository.findByNombreKey("metrica.sjft_indice.nombre")
                .orElseThrow(() -> new RuntimeException("Error fatal: No se encuentra la métrica 'metrica.sjft_indice.nombre'"));

        // 5. Crear y guardar el NUEVO resultado
        ResultadoPrueba resultadoIndice = new ResultadoPrueba();
        resultadoIndice.setJudoka(judoka);
        resultadoIndice.setEjercicioPlanificado(ejPlan);
        resultadoIndice.setMetrica(metricaIndice);
        resultadoIndice.setValor(indiceCalculado);
        resultadoIndice.setFechaRegistro(LocalDateTime.now());
        resultadoIndice.setNotasJudoka("Índice SJFT calculado automáticamente.");

        // --- 6. DEVOLVER EL RESULTADO GUARDADO ---
        return resultadoPruebaService.registrarResultado(resultadoIndice);
    }

    private void cerrarEditorResultados() {
        resultadoForm.setVisible(false);
        pruebasGrid.asSingleSelect().clear();
    }
}