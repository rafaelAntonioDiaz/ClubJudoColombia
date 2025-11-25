package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.KpiCard;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.RadarBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("dashboard-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaDashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;

    private Judoka judokaActual;
    private Div panelKpis;
    private Div panelGraficos;

    @Autowired
    public JudokaDashboardView(SecurityService securityService,
                               ResultadoPruebaService resultadoPruebaService,
                               TraduccionService traduccionService,
                               PruebaEstandarRepository pruebaEstandarRepository) {
        this.securityService = securityService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.traduccionService = traduccionService;
        this.pruebaEstandarRepository = pruebaEstandarRepository;

        initJudoka();
        buildUI();
        cargarDatos();
    }

    private void initJudoka() {
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Judoka no autenticado"));
    }

    private void buildUI() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("judoka-dashboard");

        // === CABECERA ===
        H1 titulo = new H1("Mi Dashboard – " + judokaActual.getUsuario().getNombre());
        titulo.addClassName("dashboard-title");

        // === KPIS ===
        panelKpis = new Div();
        panelKpis.addClassName("kpi-container");

        // === GRÁFICOS ===
        panelGraficos = new Div();
        panelGraficos.addClassName("charts-container");

        // === BOTÓN ACCESO RÁPIDO ===
        Button btnTareas = new Button("Ir a Mis Tareas Diarias →", VaadinIcon.ARROW_RIGHT.create(),
                e -> UI.getCurrent().navigate("mis-planes"));
        btnTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnTareas.addClassName("cta-button");

        add(titulo, panelKpis, panelGraficos, btnTareas);
    }

    private void cargarDatos() {
        panelKpis.removeAll();
        panelGraficos.removeAll();

        Double poderDeCombate = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        Map<String, Double> componentes = resultadoPruebaService.getPoderDeCombateComponentes(judokaActual);
        boolean sinDatos = componentes.values().stream().allMatch(v -> Math.abs(v - 1.0) < 0.001);

        // === KPIS ===
        panelKpis.add(
                new KpiCard("Poder de Combate Total", String.format("%.0f PC", poderDeCombate), new Icon(VaadinIcon.FIRE)),
                new KpiCard("Planes Activos", resultadoPruebaService.contarPlanesActivos(judokaActual), new Icon(VaadinIcon.CALENDAR)),
                new KpiCard("Tareas Completadas Hoy", ejecucionesHoy(), new Icon(VaadinIcon.CHECK_CIRCLE)),
                new KpiCard("Próxima Evaluación", diasHastaProximaEvaluacion(), new Icon(VaadinIcon.CLOCK))
        );

        // === ESTADO VACÍO O GRÁFICOS REALES ===
        if (sinDatos) {
            panelGraficos.add(crearEstadoVacio());
        } else {
            panelGraficos.add(crearGraficoRadar(componentes));
            panelGraficos.add(crearGraficoSaltoHorizontal());
            panelGraficos.add(crearGraficoSJFT());
        }
    }

    private long ejecucionesHoy() {
        return resultadoPruebaService.contarEjecucionesTareaHoy(judokaActual);
    }

    private String diasHastaProximaEvaluacion() {
        return resultadoPruebaService.proximaEvaluacionEnDias(judokaActual)
                .map(d -> d == 0 ? "¡Hoy!" : "En " + d + " días")
                .orElse("Sin programar");
    }

    private VerticalLayout crearEstadoVacio() {
        H3 titulo = new H3("¡Bienvenido a tu Dashboard de Rendimiento!");
        Paragraph p = new Paragraph(
                "Aquí verás tu Poder de Combate y evolución tan pronto como tu Sensei registre tu primera evaluación física."
        );
        Anchor video = new Anchor("https://www.youtube.com/watch?v=0yKhlncICFs", "Ver video explicativo (1 min)");
        video.setTarget("_blank");

        Button irTareas = new Button("Mientras tanto → Mis Tareas Diarias", VaadinIcon.ARROW_RIGHT.create(),
                e -> UI.getCurrent().navigate("mis-planes"));
        irTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout box = new VerticalLayout(titulo, p, video, irTareas);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        box.addClassName("empty-state-box");
        return box;
    }

    private ApexCharts crearGraficoRadar(Map<String, Double> data) {
        String[] categorias = data.keySet().toArray(new String[0]);
        Double[] valores = data.values().toArray(new Double[0]);

        ApexCharts chart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.RADAR).withHeight("450px").build())
                .withTitle(TitleSubtitleBuilder.get().withText("Perfil de Poder de Combate").build())
                .withSeries(new Series<>("Puntaje", valores))
                .withXaxis(XAxisBuilder.get().withCategories(categorias).build())
                .withYaxis(YAxisBuilder.get().withMin(0).withMax(5).withTickAmount(5.0).build())
                .withPlotOptions(PlotOptionsBuilder.get().withRadar(RadarBuilder.get().build()).build())
                .build();

        // Estilo bonito del polígono (workaround JS)
        chart.getElement().executeJs(
                "this.apexchart.updateOptions({plotOptions:{radar:{polygons:{strokeColors:['#e0e0e0'],fill:{colors:['#fff']}}}}}, false, true);"
        );

        chart.addClassName("radar-chart");
        return chart;
    }

    private ApexCharts crearGraficoSaltoHorizontal() {
        return crearGraficoHistorial(
                "ejercicio.salto_horizontal_proesp.nombre",
                "Evolución: Salto Horizontal (cm)",
                "#d32f2f"
        );
    }

    private ApexCharts crearGraficoSJFT() {
        return crearGraficoHistorial(
                "ejercicio.sjft.nombre",
                "Special Judo Fitness Test – Índice",
                "#1976d2"
        );
    }

    private ApexCharts crearGraficoHistorial(String nombreKeyPrueba, String titulo, String color) {
        Optional<PruebaEstandar> opt = pruebaEstandarRepository.findByNombreKey(nombreKeyPrueba);
        if (opt.isEmpty()) return new ApexCharts(); // vacío

        List<Map<String, Object>> datos = resultadoPruebaService.getHistorialParaGrafico(judokaActual, opt.get());

        if (datos.isEmpty()) {
            return crearChartSinDatos(titulo);
        }

        List<String> fechas = datos.stream()
                .map(m -> (String) m.get("fecha"))
                .collect(Collectors.toList());

        List<Series<Double>> series = datos.stream()
                .filter(m -> m.containsKey("metrica") && m.containsKey("valor"))
                .collect(Collectors.groupingBy(m -> (String) m.get("metrica")))
                .entrySet().stream()
                .map(e -> new Series<>(e.getKey(), e.getValue().stream()
                        .mapToDouble(m -> (Double) m.get("valor"))
                        .boxed()
                        .toArray(Double[]::new)))
                .collect(Collectors.toList());

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withHeight("320px")
                        .withZoom(ZoomBuilder.get().withEnabled(true).build())
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).build())
                .withTitle(TitleSubtitleBuilder.get().withText(titulo).build())
                .withSeries(series.toArray(new Series[0]))
                .withXaxis(XAxisBuilder.get().withType(XAxisType.CATEGORIES).withCategories(fechas).build())
                .build();
    }

    private ApexCharts crearChartSinDatos(String titulo) {
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withHeight("150px").build())
                .withTitle(TitleSubtitleBuilder.get().withText(titulo + " – Sin datos aún").build())
                .build();
    }
}