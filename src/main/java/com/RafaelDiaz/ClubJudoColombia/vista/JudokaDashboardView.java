package com.RafaelDiaz.ClubJudoColombia.vista;

// --- IMPORTS CORRECTOS PARA APEXCHARTS ---
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.apexcharts.config.yaxis.builder.*;
// --- FIN DE IMPORTS CORRECTOS ---

// --- IMPORTS REFACTORIZADOS ---
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar; // Refactorizado
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ResultadoPrueba; // Refactorizado
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository; // Refactorizado
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService; // Refactorizado
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
// --- FIN DE IMPORTS REFACTORIZADOS ---

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * --- VISTA REFACTORIZADA (Dashboard Gráfico) ---
 * Esta vista es ahora la página principal de estadísticas del Judoka (Flujo 1).
 */
@Route("dashboard-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaDashboardView extends VerticalLayout {

    // --- Servicios (Actualizados) ---
    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService; // Refactorizado
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;

    // --- Componentes de UI ---
    private H3 poderDeCombateLabel;
    private VerticalLayout panelGraficos;

    // --- Estado ---
    private Judoka judokaActual;

    /**
     * --- CONSTRUCTOR ACTUALIZADO ---
     */
    public JudokaDashboardView(SecurityService securityService,
                               TraduccionService traduccionService,
                               ResultadoPruebaService resultadoPruebaService, // Refactorizado
                               PruebaEstandarRepository pruebaEstandarRepository) {
        this.securityService = securityService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.traduccionService = traduccionService;
        this.pruebaEstandarRepository = pruebaEstandarRepository;

        setSizeFull();

        // 1. Obtener Judoka
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error de seguridad: No se pudo encontrar el perfil del Judoka logueado."));

        add(new H1("Mi Dashboard - " + judokaActual.getUsuario().getNombre()));

        // 2. Calcular y mostrar Poder de Combate
        poderDeCombateLabel = new H3(); // Inicializar
        add(poderDeCombateLabel);
        actualizarPoderDeCombate(); // Calcular y poner texto

        // 3. Botón para ir a las Tareas (Flujo 2)
        Button btnCargarTareas = new Button("Ir a Mis Tareas Diarias",
                e -> UI.getCurrent().navigate("mis-planes"));
        add(btnCargarTareas);

        // 4. Configurar el panel de gráficos
        panelGraficos = new VerticalLayout();
        panelGraficos.setWidthFull();
        add(panelGraficos);

        // 5. Cargar los gráficos
        mostrarGraficosDelDashboard();
    }

    /**
     * Actualiza la etiqueta del Poder de Combate.
     */
    private void actualizarPoderDeCombate() {
        Double pcScore = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        if (poderDeCombateLabel == null) {
            poderDeCombateLabel = new H3();
        }
        poderDeCombateLabel.setText(String.format("Poder de Combate: %.0f PC", pcScore));
        poderDeCombateLabel.getStyle().set("color", "#1E90FF");
    }

    /**
     * Carga todos los gráficos principales del dashboard.
     */
    private void mostrarGraficosDelDashboard() {
        panelGraficos.removeAll();

        // --- Gráfico 1: Curva de Mejora (Salto Horizontal) ---
        PruebaEstandar saltoHorizontal = pruebaEstandarRepository
                .findByNombreKey("ejercicio.salto_horizontal_proesp.nombre")
                .orElse(null);

        if (saltoHorizontal != null) {
            panelGraficos.add(crearGraficoHistorial(
                    saltoHorizontal,
                    judokaActual,
                    "Historial: Salto Horizontal"
            ));
        }

        // --- Gráfico 2: Curva de Mejora (SJFT) ---
        PruebaEstandar sjft = pruebaEstandarRepository
                .findByNombreKey("ejercicio.sjft.nombre")
                .orElse(null);

        if (sjft != null) {
            panelGraficos.add(crearGraficoHistorial(
                    sjft,
                    judokaActual,
                    "Historial: Special Judo Fitness Test (Índice)"
            ));
        }

        // --- (Aquí añadiremos el Gráfico de Radar) ---
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * Crea un gráfico (ApexCharts) para una PruebaEstandar y judoka específicos.
     */
    private ApexCharts crearGraficoHistorial(PruebaEstandar prueba, Judoka judoka, String titulo) {
        // Llama al servicio refactorizado
        List<ResultadoPrueba> historial = resultadoPruebaService.getHistorialDeResultados(judoka, prueba);

        // (No mostrar si no hay datos)
        if (historial.isEmpty()) {
            // Creamos un gráfico "vacío" con el título y una altura
            return ApexChartsBuilder.get()
                    .withChart(ChartBuilder.get()
                            .withHeight("100px") // <-- Altura (CORREGIDO)
                            .build())
                    .withTitle(TitleSubtitleBuilder.get() // Título
                            .withText(titulo + " (Sin Datos)")
                            .build())
                    .build();
        }

        // Agrupamos por métrica
        Map<String, List<ResultadoPrueba>> resultadosPorMetrica = historial.stream()
                .collect(Collectors.groupingBy(res -> traduccionService.get(res.getMetrica().getNombreKey())));

        // (Para SJFT, solo queremos graficar el "Índice")
        if (prueba.getNombreKey().equals("ejercicio.sjft.nombre")) {
            resultadosPorMetrica.keySet().removeIf(key -> !key.contains("Índice"));
        }

        // Creamos la serie de datos
        List<Series<Double>> series = resultadosPorMetrica.entrySet().stream()
                .map(entry -> {
                    String nombreMetrica = entry.getKey();
                    List<Double> valores = entry.getValue().stream()
                            .map(ResultadoPrueba::getValor)
                            .collect(Collectors.toList());
                    return new Series<>(nombreMetrica, valores.toArray(new Double[0]));
                })
                .collect(Collectors.toList());

        // Creamos las categorías del eje X (fechas)
        List<String> fechas = resultadosPorMetrica.values().stream().findFirst().orElse(Collections.emptyList())
                .stream()
                .map(res -> res.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .collect(Collectors.toList());

        // Construir el gráfico
        ApexCharts lineChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withHeight("300px")
                        .withZoom(ZoomBuilder.get().withEnabled(true).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.SMOOTH)
                        .build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(titulo)
                        .build())
                .withSeries(series.toArray(new Series[0])) // Asignar todas las series
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.CATEGORIES)
                        .withCategories(fechas)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withTitle(TitleBuilder.get()
                                .withText("Valor") // Título genérico del eje Y
                                .build())
                        .build())
                .build();

        return lineChart;
    }
}