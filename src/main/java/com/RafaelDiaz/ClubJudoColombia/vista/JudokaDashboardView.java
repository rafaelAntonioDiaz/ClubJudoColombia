package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class JudokaDashboardView extends JudokaLayout {

    private static final Map<ClasificacionRendimiento, String> COLOR_MAP = new EnumMap<>(ClasificacionRendimiento.class);

    static {
        COLOR_MAP.put(ClasificacionRendimiento.EXCELENTE, "#00E396");
        COLOR_MAP.put(ClasificacionRendimiento.MUY_BIEN, "#775DD0");
        COLOR_MAP.put(ClasificacionRendimiento.BUENO, "#008FFB");
        COLOR_MAP.put(ClasificacionRendimiento.REGULAR, "#FEB019");
        COLOR_MAP.put(ClasificacionRendimiento.RAZONABLE, "#FF4560");
        COLOR_MAP.put(ClasificacionRendimiento.DEBIL, "#A52A2A");
        COLOR_MAP.put(ClasificacionRendimiento.MUY_DEBIL, "#333333");
    }

    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final SesionService sesionService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;

    private Judoka judokaActual;
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

        H2 titulo = new H2("Hola, " + judokaActual.getUsuario().getNombre());
        titulo.addClassName("dashboard-page-title");

        Button btnTareas = new Button("Ir a Mis Tareas", new Icon(VaadinIcon.ARROW_RIGHT));
        btnTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnTareas.addClickListener(e -> UI.getCurrent().navigate("mis-planes"));

        Div headerWrapper = new Div(titulo, btnTareas);
        headerWrapper.getStyle().set("display", "flex");
        headerWrapper.getStyle().set("justify-content", "space-between");
        headerWrapper.getStyle().set("align-items", "center");
        headerWrapper.getStyle().set("flex-wrap", "wrap");
        headerWrapper.getStyle().set("gap", "1rem");
        headerWrapper.getStyle().set("margin-bottom", "1.5rem");

        JudokaCalendar calendario = new JudokaCalendar(sesionService, traduccionService, judokaActual);
        List<Insignia> todasLasInsignias = insigniaRepository.findAll();
        List<JudokaInsignia> misLogros = judokaInsigniaRepository.findByJudoka(judokaActual);
        MiDoWidget insignias = new MiDoWidget(todasLasInsignias, misLogros, traduccionService);

        kpiContainer = new Div();
        kpiContainer.addClassName("dashboard-kpis");

        chartsGrid = new Div();
        chartsGrid.addClassName("dashboard-charts");

        mainContent.add(headerWrapper, kpiContainer, calendario, insignias, chartsGrid);
        setContent(mainContent);
    }

    private void cargarDatos() {
        kpiContainer.removeAll();
        chartsGrid.removeAll();

        Double poderDeCombate = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        Map<String, Double> componentes = resultadoPruebaService.getPoderDeCombateComponentes(judokaActual);

        boolean sinDatos = componentes.values().stream().allMatch(v -> v <= 1.0);

        kpiContainer.add(
                new KpiCard("Poder de Combate", String.format("%.0f PC", poderDeCombate), new Icon(VaadinIcon.FIRE)),
                new KpiCard("Planes Activos", String.valueOf(resultadoPruebaService.contarPlanesActivos(judokaActual)), new Icon(VaadinIcon.CALENDAR)),
                new KpiCard("Tareas Hoy", String.valueOf(ejecucionesHoy()), new Icon(VaadinIcon.CHECK_CIRCLE)),
                new KpiCard("Próxima Eval.", diasHastaProximaEvaluacion(), new Icon(VaadinIcon.CLOCK))
        );

        if (sinDatos) {
            chartsGrid.add(crearEstadoVacio());
        } else {
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

            boolean isFirstChart = true;
            for (String clave : clavesPruebas) {
                Optional<PruebaEstandar> pruebaOpt = pruebaEstandarRepository.findByNombreKey(clave);
                if (pruebaOpt.isPresent()) {
                    String titulo = traduccionService.get(clave);
                    ApexCharts chart = crearGraficoHistorial(pruebaOpt.get(), titulo, isFirstChart);
                    chartsGrid.add(wrapInCard(chart, ""));
                    isFirstChart = false;
                }
            }
            UI.getCurrent().getPage().executeJs("setTimeout(() => window.dispatchEvent(new Event('resize')), 500);");
        }
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

    private Legend createLegend(boolean show) {
        Legend legend = new Legend();
        legend.setShow(show);
        return legend;
    }

    private ApexCharts crearGraficoRadar(Map<String, Double> data) {
        String[] categorias = data.keySet().toArray(new String[0]);
        Double[] valores = data.values().toArray(new Double[0]);
        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.RADAR).withHeight("400px").withForeColor("#333").build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Perfil de Combate")
                        .withAlign(com.github.appreciated.apexcharts.config.subtitle.Align.LEFT)
                        .build())
                .withColors("#4CAF50")
                .withFill(FillBuilder.get().withOpacity(0.2).build())
                .withStroke(StrokeBuilder.get().withWidth(2.0).build())
                .withPlotOptions(PlotOptionsBuilder.get().withRadar(RadarBuilder.get()
                        .withSize(140.0)
                        .withPolygons(PolygonsBuilder.get()
                                .withStrokeColor(Collections.singletonList("#e9e9e9")) // FIX: Pass a List
                                .withConnectorColors(Collections.singletonList("#e9e9e9"))
                                .build())
                        .build()).build())
                .withMarkers(MarkersBuilder.get().withSize(4.0, 6.0).build()) // FIX: Pass two arguments
                .withSeries(new Series<>("Nivel Actual", valores))
                .withXaxis(XAxisBuilder.get()
                        .withCategories(categorias)
                        .withLabels(LabelsBuilder.get()
                                .withStyle(StyleBuilder.get()
                                        .withColors(Collections.nCopies(categorias.length, "#555"))
                                        .withFontSize("13px")
                                        .withFontFamily("Inter, sans-serif")
                                        .build())
                                .build())
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withMin(0)
                        .withMax(5)
                        .withTickAmount(5.0)
                        .withShow(true)
                        .build())
                .build();
    }

    private ApexCharts crearGraficoHistorial(PruebaEstandar prueba, String titulo, boolean showLegend) {
        List<Map<String, Object>> datos = resultadoPruebaService.getHistorialParaGrafico(judokaActual, prueba);
        if (datos.isEmpty()) return crearChartSinDatos(titulo);

        Map<String, List<Map<String, Object>>> datosAgrupadosPorFecha = datos.stream()
                .collect(Collectors.groupingBy(m -> (String) m.get("fecha")));
        List<String> fechas = new ArrayList<>(datosAgrupadosPorFecha.keySet());
        Collections.sort(fechas);

        List<String> nombresDeMetricas = datos.stream().map(m -> (String) m.get("metrica")).distinct().collect(Collectors.toList());
        List<Series> series = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<Double> strokeDashArrayValues = new ArrayList<>();

        String judokaColor = "#1A73E8"; // Primary color for judoka's data

        for (String metrica : nombresDeMetricas) {
            List<Double> valores = new ArrayList<>();
            for (String fecha : fechas) {
                Double valor = datosAgrupadosPorFecha.get(fecha).stream()
                        .filter(m -> m.get("metrica").equals(metrica))
                        .map(m -> (Double) m.get("valor"))
                        .findFirst().orElse(null);
                valores.add(valor);
            }
            series.add(new Series<>(metrica, valores.toArray(new Double[0])));
            colors.add(judokaColor);
            strokeDashArrayValues.add(0.0); // Solid line for judoka
        }

        List<Map<String, Object>> normas = resultadoPruebaService.getNormasParaGrafico(judokaActual, prueba);

        for (Map<String, Object> norma : normas) {
            Double[] valoresNorma = new Double[fechas.size()];
            Arrays.fill(valoresNorma, (Double) norma.get("valor"));

            String nombreNorma = (String) norma.get("nombre");
            series.add(new Series<>(nombreNorma, valoresNorma));

            // Find the enum by translated name to get the right color
            ClasificacionRendimiento clasificacion = Arrays.stream(ClasificacionRendimiento.values())
                    .filter(c -> traduccionService.get(c.getTraduccionKey()).equals(nombreNorma))
                    .findFirst()
                    .orElse(null);

            colors.add(COLOR_MAP.getOrDefault(clasificacion, "#808080")); // Default to gray
            strokeDashArrayValues.add(4.0); // Dashed line for benchmarks
        }

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.LINE).withHeight("350px").withZoom(ZoomBuilder.get().withEnabled(false).build()).withForeColor("#333").build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).withWidth(3.0).withDashArray(strokeDashArrayValues).build())
                .withColors(colors.toArray(new String[0]))
                .withTitle(TitleSubtitleBuilder.get().withText(titulo).withAlign(com.github.appreciated.apexcharts.config.subtitle.Align.LEFT).build())
                .withSeries(series.toArray(new Series[0]))
                .withLegend(createLegend(showLegend))
                .withXaxis(XAxisBuilder.get().withType(XAxisType.CATEGORIES).withCategories(fechas).build())
                .withMarkers(MarkersBuilder.get().withSize(5.0, 7.0).build()) // FIX: Pass two arguments
                .withTooltip(TooltipBuilder.get().withShared(true).withIntersect(false).build())
                .build();
    }

    private ApexCharts crearChartSinDatos(String titulo) {
        return ApexChartsBuilder.get().withChart(ChartBuilder.get().withHeight("350px").withForeColor("#000000").build()).withTitle(TitleSubtitleBuilder.get().withText(titulo + " (Sin datos)").build()).build();
    }

    private long ejecucionesHoy() { return resultadoPruebaService.contarEjecucionesTareaHoy(judokaActual); }
    private String diasHastaProximaEvaluacion() { return resultadoPruebaService.proximaEvaluacionEnDias(judokaActual).map(d -> d == 0 ? "¡Hoy!" : d + " días").orElse("-"); }
    private VerticalLayout crearEstadoVacio() {
        H3 titulo = new H3("Aún no tienes estadísticas");
        Paragraph p = new Paragraph("Completa tu primera evaluación para desbloquear tu Perfil de Combate.");
        VerticalLayout box = new VerticalLayout(titulo, p);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        box.addClassName("empty-state-card");
        return box;
    }
}