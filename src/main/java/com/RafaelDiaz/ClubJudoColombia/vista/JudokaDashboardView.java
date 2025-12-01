package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia; // Importar
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia; // Importar
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository; // Nuevo Repo
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository; // Nuevo Repo
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ResultadoPruebaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.JudokaCalendar;
import com.RafaelDiaz.ClubJudoColombia.vista.component.KpiCard;
import com.RafaelDiaz.ClubJudoColombia.vista.component.MiDoWidget; // Importar Widget
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
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

    private final SecurityService securityService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final SesionService sesionService;
    // --- NUEVOS REPOSITORIOS PARA GAMIFICACIÓN ---
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
                               // Inyección de nuevos repositorios
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

        // 1. CALENDARIO
        JudokaCalendar calendario = new JudokaCalendar(sesionService, traduccionService, judokaActual);

        // 2. GAMIFICACIÓN (MI DO)
        // --- AQUÍ CARGAMOS LOS DATOS QUE FALTABAN ---
        List<Insignia> todasLasInsignias = insigniaRepository.findAll();
        List<JudokaInsignia> misLogros = judokaInsigniaRepository.findByJudoka(judokaActual);

        MiDoWidget insignias = new MiDoWidget(todasLasInsignias, misLogros, traduccionService);
        // ---------------------------------------------

        kpiContainer = new Div();
        kpiContainer.addClassName("dashboard-kpis");

        chartsGrid = new Div();
        chartsGrid.addClassName("dashboard-charts");

        // Añadimos todo al layout en orden
        mainContent.add(headerWrapper, kpiContainer, calendario, insignias, chartsGrid);
        setContent(mainContent);
    }

    private void cargarDatos() {
        // ... (Este método sigue IGUAL que antes, maneja KPIs y Gráficos)
        kpiContainer.removeAll();
        chartsGrid.removeAll();

        Double poderDeCombate = resultadoPruebaService.calcularPoderDeCombate(judokaActual);
        Map<String, Double> componentes = resultadoPruebaService.getPoderDeCombateComponentes(judokaActual);

        boolean sinDatos = componentes.isEmpty();

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

    // ... (Resto de métodos auxiliares: wrapInCard, crearGraficoRadar, crearGraficoHistorial, etc.)
    // COPIAR EL RESTO DE MÉTODOS PRIVADOS IGUAL QUE EN LA VERSIÓN ANTERIOR

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
                .withSeries(new Series<>("Nivel Actual", valores))
                .withXaxis(XAxisBuilder.get().withCategories(categorias).withLabels(LabelsBuilder.get().withStyle(StyleBuilder.get().withColors(coloresEtiquetas).withFontSize("13px").withFontFamily("Inter, sans-serif").build()).build()).build())
                .withYaxis(YAxisBuilder.get().withMin(0).withMax(5).withTickAmount(5.0).withShow(false).build())
                .build();
    }

    private ApexCharts crearGraficoHistorial(PruebaEstandar prueba, String titulo) {
        List<Map<String, Object>> datos = resultadoPruebaService.getHistorialParaGrafico(judokaActual, prueba);
        if (datos.isEmpty()) return crearChartSinDatos(titulo);
        List<String> fechas = datos.stream().map(m -> (String) m.get("fecha")).collect(Collectors.toList());
        List<Series<Double>> series = datos.stream().filter(m -> m.containsKey("metrica") && m.containsKey("valor")).collect(Collectors.groupingBy(m -> (String) m.get("metrica"))).entrySet().stream().map(e -> new Series<>(e.getKey(), e.getValue().stream().mapToDouble(m -> (Double) m.get("valor")).boxed().toArray(Double[]::new))).collect(Collectors.toList());
        return ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.LINE).withHeight("350px").withZoom(ZoomBuilder.get().withEnabled(false).build()).withForeColor("#000000").build()).withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).withWidth(3.0).build()).withColors("#FF6B6B", "#2C3E50").withTitle(TitleSubtitleBuilder.get().withText(titulo).withAlign(com.github.appreciated.apexcharts.config.subtitle.Align.LEFT).build()).withSeries(series.toArray(new Series[0])).withXaxis(XAxisBuilder.get().withType(XAxisType.CATEGORIES).withCategories(fechas).build()).build();
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