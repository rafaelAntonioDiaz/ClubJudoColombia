package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.JudokaCalendar;
import com.RafaelDiaz.ClubJudoColombia.vista.component.KpiCard;
import com.RafaelDiaz.ClubJudoColombia.vista.component.MiDoWidget;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Legend;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.RadarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.radar.builder.PolygonsBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.xaxis.labels.builder.StyleBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route("dashboard-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Dashboard | Club Judo Colombia")
@CssImport("./styles/dashboard-judoka.css")
public class JudokaDashboardView extends JudokaLayout implements LocaleChangeObserver {

    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final SesionService sesionService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;

    private Judoka judokaActual;

    private H2 tituloBienvenida;
    private Button btnTareas;
    private JudokaCalendar calendario;
    private MiDoWidget insigniasWidget;
    private Div kpiContainer;
    private Div chartsGrid;

    @Autowired
    public JudokaDashboardView(SecurityService securityService,
                               ResultadoPruebaService resultadoPruebaService,
                               TraduccionService traduccionService,
                               PruebaEstandarRepository pruebaEstandarRepository,
                               AccessAnnotationChecker accessChecker,
                               SesionService sesionService,
                               InsigniaRepository insigniaRepository,
                               JudokaInsigniaRepository judokaInsigniaRepository) {
        super(securityService, accessChecker);
        this.securityService = securityService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.traduccionService = traduccionService;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.sesionService = sesionService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;

        initJudoka();
        buildDashboard();
        cargarDatos();
    }

    private void initJudoka() {
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error: Judoka no autenticado."));
    }

    private void buildDashboard() {
        Div mainContent = new Div();
        mainContent.addClassName("judoka-dashboard-content");

        tituloBienvenida = new H2();
        tituloBienvenida.addClassName("dashboard-page-title");

        btnTareas = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        btnTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnTareas.addClickListener(e -> UI.getCurrent().navigate("mis-planes"));

        Div headerWrapper = new Div(tituloBienvenida, btnTareas);
        headerWrapper.getStyle().set("display", "flex");
        headerWrapper.getStyle().set("justify-content", "space-between");
        headerWrapper.getStyle().set("align-items", "center");
        headerWrapper.getStyle().set("flex-wrap", "wrap");
        headerWrapper.getStyle().set("gap", "1rem");
        headerWrapper.getStyle().set("margin-bottom", "1.5rem");

        calendario = new JudokaCalendar(sesionService, traduccionService, judokaActual);

        List<Insignia> todasLasInsignias = insigniaRepository.findAll();
        List<JudokaInsignia> misLogros = judokaInsigniaRepository.findByJudoka(judokaActual);
        insigniasWidget = new MiDoWidget(todasLasInsignias, misLogros, traduccionService);

        kpiContainer = new Div();
        kpiContainer.addClassName("dashboard-kpis");

        chartsGrid = new Div();
        chartsGrid.addClassName("dashboard-charts");

        mainContent.add(headerWrapper, kpiContainer, calendario, insigniasWidget, chartsGrid);
        setContent(mainContent);
    }

    private void cargarDatos() {
        kpiContainer.removeAll();
        chartsGrid.removeAll();

        Double poderDeCombate = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        Map<String, Double> componentes = resultadoPruebaService.getPoderDeCombateComponentes(judokaActual);

        boolean sinDatos = componentes.isEmpty();

        kpiContainer.add(
                new KpiCard(getTranslation("kpi.poder_combate"), String.format("%.0f PC", poderDeCombate), new Icon(VaadinIcon.FIRE)),
                new KpiCard(getTranslation("kpi.planes_activos"), String.valueOf(resultadoPruebaService.contarPlanesActivos(judokaActual)), new Icon(VaadinIcon.CALENDAR)),
                new KpiCard(getTranslation("kpi.tareas_hoy"), String.valueOf(ejecucionesHoy()), new Icon(VaadinIcon.CHECK_CIRCLE)),
                new KpiCard(getTranslation("kpi.proxima_eval"), diasHastaProximaEvaluacion(), new Icon(VaadinIcon.CLOCK))
        );

        if (sinDatos) {
            chartsGrid.add(crearEstadoVacio());
        } else {
            // LEYENDA GLOBAL UNIFICADA
            chartsGrid.add(crearLeyendaGlobal());

            ApexCharts radarChart = crearGraficoRadar(componentes);
            chartsGrid.add(wrapInCard(radarChart, "radar-chart-container"));

            List<String> clavesPruebas = List.of(
                    "ejercicio.salto_horizontal_proesp.nombre",
                    "ejercicio.lanzamiento_balon.nombre",
                    "ejercicio.abdominales_1min.nombre",
                    "ejercicio.carrera_6min.nombre",
                    "ejercicio.agilidad_4x4.nombre",
                    "ejercicio.carrera_20m.nombre",
                    "ejercicio.sjft.nombre"
            );

            for (String clave : clavesPruebas) {
                Optional<PruebaEstandar> pruebaOpt = pruebaEstandarRepository.findByNombreKey(clave);
                if (pruebaOpt.isPresent()) {
                    String titulo = traduccionService.get(clave);
                    ApexCharts chart = crearGraficoHistorial(pruebaOpt.get(), titulo);
                    chartsGrid.add(wrapInCard(chart, ""));
                }
            }
            UI.getCurrent().getPage().executeJs("setTimeout(() => window.dispatchEvent(new Event('resize')), 500);");
        }
    }

    // --- CORRECCIÓN EN EL GRÁFICO HISTORIAL ---
    private ApexCharts crearGraficoHistorial(PruebaEstandar prueba, String titulo) {
        // 1. Datos del Usuario
        List<Map<String, Object>> datos = resultadoPruebaService.getHistorialParaGrafico(judokaActual, prueba);
        if (datos.isEmpty()) return crearChartSinDatos(titulo);

        List<String> fechas = datos.stream()
                .map(m -> (String) m.get("fecha"))
                .distinct()
                .collect(Collectors.toList());

        // FIX: Tomar solo la PRIMERA métrica encontrada para evitar series duplicadas (ej: SJFT Pulsaciones)
        String metricaPrincipal = datos.get(0).get("metrica").toString();

        Double[] valoresUsuario = datos.stream()
                .filter(m -> m.get("metrica").equals(metricaPrincipal))
                .map(m -> Double.valueOf(m.get("valor").toString()))
                .toArray(Double[]::new);

        Series<Double> serieUsuario = new Series<>(getTranslation("legend.progreso"), valoresUsuario);

        // 2. Datos del Benchmark (Meta)
        // Llamada al método que creaste en el servicio
        List<Map<String, Object>> normas = resultadoPruebaService.getNormasParaGrafico(judokaActual, prueba);
        Series<Double> serieMeta = null;

        if (!normas.isEmpty()) {
            Double valorMeta = Double.valueOf(normas.get(0).get("valor").toString());
            // Crear línea constante
            Double[] valoresMeta = new Double[fechas.size()];
            Arrays.fill(valoresMeta, valorMeta);
            serieMeta = new Series<>(getTranslation("legend.meta"), valoresMeta);
        }

        // 3. Configurar Colores y Series
        String[] colores;
        Series[] seriesChart;

        if (serieMeta != null) {
            colores = new String[] { "#1A73E8", "#F1C40F" }; // 0=Azul (User), 1=Dorado (Meta)
            seriesChart = new Series[] { serieUsuario, serieMeta };
        } else {
            colores = new String[] { "#1A73E8" }; // Solo Azul
            seriesChart = new Series[] { serieUsuario };
        }

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.LINE).withHeight("350px").withZoom(ZoomBuilder.get().withEnabled(false).build()).withForeColor("#333").build())
                .withColors(colores) // Aplicar colores estrictos
                .withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).withWidth(3.0).build())
                .withLegend(createLegend(false)) // Ocultar leyenda individual
                .withTitle(TitleSubtitleBuilder.get().withText(titulo).withAlign(com.github.appreciated.apexcharts.config.subtitle.Align.LEFT).build())
                .withSeries(seriesChart)
                .withXaxis(XAxisBuilder.get().withType(XAxisType.CATEGORIES).withCategories(fechas).build())
                .build();
    }

    private Component crearLeyendaGlobal() {
        HorizontalLayout leyenda = new HorizontalLayout();
        leyenda.setAlignItems(FlexComponent.Alignment.CENTER);
        leyenda.getStyle().set("margin-bottom", "10px");
        leyenda.add(crearItemLeyenda("#1A73E8", getTranslation("legend.progreso"))); // "Mi Progreso"
        leyenda.add(crearItemLeyenda("#F1C40F", getTranslation("legend.meta")));     // "Meta a Batir"
        return leyenda;
    }

    private Span crearItemLeyenda(String color, String texto) {
        Span punto = new Span();
        punto.getStyle().set("background-color", color).set("width", "12px").set("height", "12px").set("border-radius", "50%").set("display", "inline-block").set("margin-right", "5px");
        Span label = new Span(texto);
        label.getStyle().set("font-size", "0.9rem").set("color", "var(--lumo-secondary-text-color)");
        Span wrapper = new Span(punto, label);
        wrapper.getStyle().set("display", "flex").set("align-items", "center").set("margin-right", "15px");
        return wrapper;
    }

    private Legend createLegend(boolean show) {
        Legend legend = new Legend();
        legend.setShow(show);
        return legend;
    }

    // ... Resto de métodos (crearGraficoRadar, localeChange, etc.) IGUALES ...
    @Override
    public void localeChange(LocaleChangeEvent event) {
        tituloBienvenida.setText(getTranslation("dashboard.welcome", judokaActual.getUsuario().getNombre()));
        btnTareas.setText(getTranslation("dashboard.btn.tareas"));
        if (calendario != null) calendario.refresh();
        if (insigniasWidget != null) insigniasWidget.refresh();
        cargarDatos();
    }

    private Div wrapInCard(Component chart, String extraClass) {
        Div card = new Div(chart);
        card.addClassName("chart-card");
        if (extraClass != null && !extraClass.isEmpty()) card.addClassName(extraClass);
        if (chart instanceof ApexCharts) {
            ((ApexCharts) chart).setWidth("100%");
            ((ApexCharts) chart).setHeight("400px");
        }
        return card;
    }

    private ApexCharts crearGraficoRadar(Map<String, Double> data) {
        String[] categorias = data.keySet().toArray(new String[0]);
        Double[] valores = data.values().toArray(new Double[0]);
        List<String> coloresEtiquetas = Collections.nCopies(categorias.length, "#000000");
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.RADAR).withHeight("500px").withForeColor("#000000").build())
                .withColors("#FF6B6B", "#2C3E50")
                .withFill(FillBuilder.get().withOpacity(0.2).build())
                .withStroke(StrokeBuilder.get().withWidth(2.0).build())
                .withPlotOptions(PlotOptionsBuilder.get().withRadar(RadarBuilder.get().withSize(150.0).withPolygons(PolygonsBuilder.get().withStrokeColor(Collections.singletonList("#90A4AE")).withConnectorColors(Collections.singletonList("#000000")).build()).build()).build())
                .withMarkers(MarkersBuilder.get().withSize(5.0, 5.0).build())
                .withSeries(new Series<>(getTranslation("chart.radar.serie"), valores))                .withXaxis(XAxisBuilder.get().withCategories(categorias).withLabels(LabelsBuilder.get().withStyle(StyleBuilder.get().withColors(coloresEtiquetas).withFontSize("13px").withFontFamily("Inter, sans-serif").build()).build()).build())
                .withYaxis(YAxisBuilder.get().withMin(0).withMax(5).withTickAmount(5.0).withShow(false).build())
                .build();
    }

    private ApexCharts crearChartSinDatos(String titulo) {
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withHeight("350px").withForeColor("#000000").build())
                .withTitle(TitleSubtitleBuilder.get().withText(titulo + " (" + getTranslation("chart.sin_datos") + ")").build())
                .build();
    }

    private long ejecucionesHoy() { return resultadoPruebaService.contarEjecucionesTareaHoy(judokaActual); }
    private String diasHastaProximaEvaluacion() {
        return resultadoPruebaService.proximaEvaluacionEnDias(judokaActual)
                .map(d -> d == 0 ? getTranslation("kpi.hoy") : d + " " + getTranslation("kpi.dias"))
                .orElse("-");
    }
    private VerticalLayout crearEstadoVacio() {
        H3 titulo = new H3(getTranslation("empty.title"));
        Paragraph p = new Paragraph(getTranslation("empty.desc"));
        VerticalLayout box = new VerticalLayout(titulo, p);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        box.addClassName("empty-state-card");
        return box;
    }
}