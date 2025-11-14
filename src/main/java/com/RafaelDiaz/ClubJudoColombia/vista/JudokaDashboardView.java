package com.RafaelDiaz.ClubJudoColombia.vista;

// --- IMPORTS CORRECTOS PARA APEXCHARTS (Limpios y Específicos) ---
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.RadarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.radar.builder.PolygonsBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder;
import com.github.appreciated.apexcharts.helper.Series;
// (Quitamos 'Fill' que no se usa)

// --- IMPORTS REFACTORIZADOS ---
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ResultadoPrueba;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
// --- FIN DE IMPORTS REFACTORIZADOS ---

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("dashboard-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaDashboardView extends VerticalLayout {

    // --- Servicios (Actualizados) ---
    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;

    // --- Componentes de UI ---
    private H3 poderDeCombateLabel;
    private VerticalLayout panelGraficos;

    // --- Estado ---
    private Judoka judokaActual;

    public JudokaDashboardView(SecurityService securityService,
                               TraduccionService traduccionService,
                               ResultadoPruebaService resultadoPruebaService,
                               PruebaEstandarRepository pruebaEstandarRepository) {
        this.securityService = securityService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.traduccionService = traduccionService;
        this.pruebaEstandarRepository = pruebaEstandarRepository;

        setSizeFull();

        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error de seguridad: No se pudo encontrar el perfil del Judoka logueado."));

        add(new H1("Mi Dashboard - " + judokaActual.getUsuario().getNombre()));

        poderDeCombateLabel = new H3();
        add(poderDeCombateLabel);

        Button btnCargarTareas = new Button("Ir a Mis Tareas Diarias",
                e -> UI.getCurrent().navigate("mis-planes"));
        btnCargarTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        add(btnCargarTareas);

        panelGraficos = new VerticalLayout();
        panelGraficos.setWidthFull();
        add(panelGraficos);

        // Cargar los gráficos (o el estado vacío)
        mostrarGraficosDelDashboard();
    }

    private void actualizarPoderDeCombate() {
        Double pcScore = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        if (poderDeCombateLabel == null) {
            poderDeCombateLabel = new H3();
        }
        poderDeCombateLabel.setText(String.format("Poder de Combate: %.0f PC", pcScore));
        poderDeCombateLabel.getStyle().set("color", "#1E90FF");
    }

    /**
     * --- MÉTODO CON LÓGICA DE ESTADO VACÍO CORREGIDA ---
     */
    private void mostrarGraficosDelDashboard() {
        panelGraficos.removeAll();

        // 1. Obtenemos los componentes del PC.
        Map<String, Double> componentesPC = resultadoPruebaService.getPoderDeCombateComponentes(judokaActual);

        // 2. Verificamos si TODOS los puntajes son 1.0 (el puntaje base por defecto)
        // Si todos son 1.0, significa que NO HAY DATOS en la BD.
        boolean sinDatos = componentesPC.values().stream().allMatch(puntaje -> puntaje == 1.0);

        // --- LÓGICA DE ESTADO VACÍO CORREGIDA ---
        if (sinDatos) {
            // El Judoka es nuevo Y el DataInitializer falló o fue comentado
            panelGraficos.add(crearComponenteEstadoVacio());
        } else {
            // El Judoka tiene datos (ficticios o reales), mostramos los gráficos

            // --- 1. Gráfico de Radar (Poder de Combate) ---
            panelGraficos.add(crearGraficoRadar(componentesPC));

            // --- 2. Curva de Mejora (Salto Horizontal) ---
            PruebaEstandar pruebaBase = pruebaEstandarRepository
                    .findByNombreKey("ejercicio.salto_horizontal_proesp.nombre")
                    .orElse(null);
            if (pruebaBase != null) {
                panelGraficos.add(crearGraficoHistorial(
                        pruebaBase,
                        judokaActual,
                        "Historial: Salto Horizontal"
                ));
            }

            // --- 3. Curva de Mejora (SJFT) ---
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
        }

        // Actualizamos el PC (mostrará el valor base o el real)
        actualizarPoderDeCombate();
    }

    /**
     * --- MÉTODO DE ESTADO VACÍO (CORREGIDO) ---
     */
    private VerticalLayout crearComponenteEstadoVacio() {
        H4 titulo = new H4("¡Bienvenido a tu Dashboard de Rendimiento!");
        Paragraph explicacion = new Paragraph(
                "Tu 'Poder de Combate' y tus gráficos de rendimiento aparecerán aquí " +
                        "automáticamente tan pronto como tu Sensei registre los resultados de " +
                        "tu primera Evaluación Estándar (Flujo 1)."
        );

        // (Enlace ficticio - reemplaza con tu video tutorial)
        Anchor videoLink = new Anchor("https://www.youtube.com/watch?v=0yKhlncICFs",
                "Ver video-tutorial (1 min) sobre cómo funciona el Poder de Combate");
        videoLink.setTarget("_blank"); // <-- Usamos el método setTarget()
        videoLink.getStyle().set("font-style", "italic");

        Button irATareas = new Button("Mientras tanto, ve a tus Tareas Diarias",
                new Icon(VaadinIcon.ARROW_RIGHT),
                e -> UI.getCurrent().navigate("mis-planes"));
        irATareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        irATareas.getStyle().set("margin-top", "15px");

        VerticalLayout layout = new VerticalLayout(titulo, explicacion, videoLink, irATareas);
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle().set("border", "1px dashed #9E9E9E").set("border-radius", "5px");
        layout.setPadding(true);
        return layout;
    }

    /**
     * --- GRÁFICO DE RADAR (SINTAXIS 100% CORREGIDA) ---
     */
    private ApexCharts crearGraficoRadar(Map<String, Double> data) {

        String[] etiquetas = data.keySet().toArray(new String[0]);
        Double[] puntajes = data.values().toArray(new Double[0]);
        Series<Double> series = new Series<>("Puntaje (1-5)", puntajes);

        ApexCharts radarChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.RADAR)
                        .withHeight("400px")
                        .build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Perfil de Poder de Combate")
                        .build())
                .withSeries(series)
                .withXaxis(XAxisBuilder.get()
                        .withCategories(etiquetas)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withMin(0.0)
                        .withMax(5.0)
                        .withTickAmount(5.0)  // Usa 5 (Integer); si da error, cambia a 5.0 (Double)
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withRadar(RadarBuilder.get()
                                .build())  // Configuración básica de radar (sin polygons aquí)
                        .build())
                .build();

        // Workaround para configurar polygons en versión 24.0.1 (usando updateOptions vía JS)
        radarChart.addAttachListener(event -> {
            radarChart.getElement().executeJs(
                    "this.apexchart.updateOptions({" +
                            "plotOptions: {" +
                            "radar: {" +
                            "polygons: {" +
                            "strokeColors: ['grey']," +
                            "fill: {" +
                            "colors: ['#FAFAFA']" +
                            "}" +
                            "}" +
                            "}" +
                            "}" +
                            "}, false, true);"
            );
        });

        return radarChart;
    }

    /**
     * --- GRÁFICO DE LÍNEA (LÓGICA CORREGIDA) ---
     */
    private ApexCharts crearGraficoHistorial(PruebaEstandar prueba, Judoka judoka, String titulo) {
        List<ResultadoPrueba> historial = resultadoPruebaService.getHistorialDeResultados(judoka, prueba);

        // Si el historial está vacío, muestra el mensaje "Sin Datos".
        if (historial.isEmpty()) {
            return ApexChartsBuilder.get()
                    .withChart(ChartBuilder.get().withHeight("100px").build())
                    .withTitle(TitleSubtitleBuilder.get().withText(titulo + " (Sin Datos)").build())
                    .build();
        }

        // NO filtramos los datos ficticios, los mostramos.
        Map<String, List<ResultadoPrueba>> resultadosPorMetrica = historial.stream()
                .collect(Collectors.groupingBy(res -> traduccionService.get(res.getMetrica().getNombreKey())));

        if (prueba.getNombreKey().equals("ejercicio.sjft.nombre")) {
            resultadosPorMetrica.keySet().removeIf(key -> !key.contains("Índice"));
        }

        List<Series<Double>> series = new ArrayList<>();
        List<String> fechas = new ArrayList<>();

        if (!resultadosPorMetrica.isEmpty()) {
            List<ResultadoPrueba> primeraSerieDatos = resultadosPorMetrica.values().stream().findFirst().orElse(Collections.emptyList());

            fechas = primeraSerieDatos.stream()
                    .map(res -> res.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .collect(Collectors.toList());

            series = resultadosPorMetrica.entrySet().stream()
                    .map(entry -> {
                        String nombreMetrica = entry.getKey();
                        List<Double> valores = entry.getValue().stream()
                                .map(ResultadoPrueba::getValor)
                                .collect(Collectors.toList());
                        return new Series<>(nombreMetrica, valores.toArray(new Double[0]));
                    })
                    .collect(Collectors.toList());
        }

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
                .withSeries(series.toArray(new Series[0]))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.CATEGORIES)
                        .withCategories(fechas)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withTitle(TitleBuilder.get()
                                .withText("Valor")
                                .build())
                        .build())
                .build();

        return lineChart;
    }
}