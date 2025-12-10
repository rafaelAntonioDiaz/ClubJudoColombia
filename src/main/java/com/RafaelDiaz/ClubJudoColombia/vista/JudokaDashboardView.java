package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AsistenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaDashboardService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.AgendaDialog;
import com.RafaelDiaz.ClubJudoColombia.vista.component.CheckInWidget;
import com.RafaelDiaz.ClubJudoColombia.vista.component.CombatRadarWidget;
import com.RafaelDiaz.ClubJudoColombia.vista.component.MiDoWidget;
import com.RafaelDiaz.ClubJudoColombia.vista.component.PalmaresDialog;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Chart;
import com.github.appreciated.apexcharts.config.Legend;
import com.github.appreciated.apexcharts.config.Stroke;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.chart.Toolbar;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.Zoom;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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
@PageTitle("Combat Profile | Club Judo Colombia") // Nota: Para título dinámico se requeriría HasDynamicTitle
@CssImport("./styles/dashboard-judoka.css")
public class JudokaDashboardView extends JudokaLayout implements LocaleChangeObserver {

    private final JudokaDashboardService dashboardService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final SesionService sesionService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;
    private final AsistenciaService asistenciaService;

    private Judoka judokaActual;

    // Componentes UI
    private H2 tituloNombre;
    private Button btnAgenda;
    private Button btnTrofeos;
    private CombatRadarWidget radarWidget;
    private Div detailChartContainer;
    private Span lblInstruccionChart;

    @Autowired
    public JudokaDashboardView(JudokaDashboardService dashboardService,
                               SecurityService securityService,
                               TraduccionService traduccionService,
                               SesionService sesionService,
                               AccessAnnotationChecker accessChecker,
                               InsigniaRepository insigniaRepository,
                               JudokaInsigniaRepository judokaInsigniaRepository,
                               AsistenciaService asistenciaService) {
        super(securityService, accessChecker, traduccionService);
        this.dashboardService = dashboardService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.sesionService = sesionService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
        this.asistenciaService = asistenciaService;

        initJudoka();
        buildModernDashboard();
    }

    private void initJudoka() {
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error: Judoka no autenticado."));
    }

    private void buildModernDashboard() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addClassName("judoka-dashboard-content");
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        mainLayout.setMaxWidth("800px");
        mainLayout.getStyle().set("margin", "0 auto");

        tituloNombre = new H2();
        tituloNombre.getStyle().set("margin", "0").set("color", "var(--lumo-header-text-color)");

        btnTrofeos = new Button(new Icon(VaadinIcon.TROPHY));
        btnTrofeos.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        btnTrofeos.getStyle().set("color", "#F1C40F");
        // i18n: Tooltip para trofeos
        btnTrofeos.setTooltipText(traduccionService.get("tooltip.trofeos"));
        btnTrofeos.addClickListener(e -> abrirDialogoTrofeos());

        btnAgenda = new Button(new Icon(VaadinIcon.CALENDAR_CLOCK));
        btnAgenda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgenda.addClickListener(
                e -> new AgendaDialog(dashboardService,
                        sesionService, traduccionService, judokaActual).open());

        Button btnPalmares = new Button(new Icon(VaadinIcon.MEDAL));
        btnPalmares.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        btnPalmares.getStyle().set("color", "#2ECC71");
        // i18n: Tooltip para palmarés
        btnPalmares.setTooltipText(traduccionService.get("tooltip.palmares"));
        btnPalmares.addClickListener(e -> new PalmaresDialog(dashboardService, traduccionService, judokaActual).open());

        HorizontalLayout header = new HorizontalLayout(tituloNombre, btnPalmares, btnTrofeos, btnAgenda);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        CheckInWidget checkInWidget = new CheckInWidget(asistenciaService, judokaActual, traduccionService);
        checkInWidget.getStyle().set("margin-bottom", "30px");

        radarWidget = new CombatRadarWidget();

        FlexLayout chipsLayout = new FlexLayout();
        chipsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chipsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chipsLayout.getStyle().set("gap", "10px").set("margin-top", "20px");

        // i18n: Usamos claves de traducción para los botones de filtro
        chipsLayout.add(crearChipFiltro("cat.fuerza", "ejercicio.abdominales_1min.nombre"));
        chipsLayout.add(crearChipFiltro("cat.velocidad", "ejercicio.carrera_20m.nombre"));
        chipsLayout.add(crearChipFiltro("cat.resistencia", "ejercicio.sjft.nombre"));
        chipsLayout.add(crearChipFiltro("cat.agilidad", "ejercicio.agilidad_4x4.nombre"));
        chipsLayout.add(crearChipFiltro("cat.potencia", "ejercicio.salto_horizontal_proesp.nombre"));

        detailChartContainer = new Div();
        detailChartContainer.setWidthFull();
        detailChartContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("margin-top", "20px")
                .set("min-height", "200px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        // i18n: Texto de instrucción inicial
        lblInstruccionChart = new Span(traduccionService.get("msg.selecciona.categoria"));
        detailChartContainer.add(lblInstruccionChart);

        mainLayout.add(header, checkInWidget, radarWidget, chipsLayout, detailChartContainer);
        setContent(mainLayout);

        actualizarTextos();
        actualizarDatos();
    }

    private void actualizarDatos() {
        Double poder = dashboardService.getPoderDeCombate(judokaActual);
        Map<String, Double> radarData = dashboardService.getDatosRadar(judokaActual);

        // i18n: Títulos del radar
        radarWidget.updateData(
                poder,
                radarData,
                traduccionService.get("kpi.poder_combate"),
                traduccionService.get("chart.radar.serie"),
                traduccionService.get("chart.sin_datos")
        );
    }

    private void actualizarTextos() {
        String nombre = (judokaActual != null && judokaActual.getUsuario() != null)
                ? judokaActual.getUsuario().getNombre() : "";

        // i18n: Bienvenida con formato usando el servicio
        // Se asume que la clave "dashboard.welcome" es "Hola, {0}" o similar
        String welcomeMsg = String.format(traduccionService.get("dashboard.welcome"), nombre);
        tituloNombre.setText(welcomeMsg);

        // i18n: Texto del botón agenda
        btnAgenda.setText(traduccionService.get("kpi.tareas_hoy"));
    }

    private Button crearChipFiltro(String claveEtiqueta, String clavePrueba) {
        // i18n: Traducir la etiqueta del botón
        Button chip = new Button(traduccionService.get(claveEtiqueta));
        chip.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        chip.getStyle().set("border-radius", "20px");
        chip.addClickListener(e -> {
            chip.getParent().ifPresent(parent ->
                    parent.getChildren().filter(c -> c instanceof Button).forEach(c ->
                            ((Button)c).removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    )
            );
            chip.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            mostrarGraficoDetalle(clavePrueba);
        });
        return chip;
    }

    private void mostrarGraficoDetalle(String clavePrueba) {
        detailChartContainer.removeAll();

        Optional<PruebaEstandar> pruebaOpt = dashboardService.buscarPrueba(clavePrueba);

        if (pruebaOpt.isPresent()) {
            // i18n: Título de la prueba
            String tituloCategoria = traduccionService.get(clavePrueba);

            List<Map<String, Object>> datos = dashboardService.getHistorialPrueba(judokaActual, pruebaOpt.get());
            List<Map<String, Object>> normas = dashboardService.getMetaPrueba(judokaActual, pruebaOpt.get());

            if (datos.isEmpty()) {
                detailChartContainer.add(crearEstadoVacio(tituloCategoria));
            } else {
                ApexCharts chart = crearGraficoHistorial(datos, normas, tituloCategoria);
                chart.setWidth("100%");
                detailChartContainer.add(chart);

                UI.getCurrent().getPage().executeJs(
                        "setTimeout(() => window.dispatchEvent(new Event('resize')), 200);");
            }
        } else {
            // i18n: Mensaje de error técnico
            String errorMsg = traduccionService.get("err.config.no_encontrada") + ": " + clavePrueba;
            detailChartContainer.add(new Span(errorMsg));
        }
    }

    private ApexCharts crearGraficoHistorial(List<Map<String, Object>> datos,
                                             List<Map<String, Object>> normas,
                                             String titulo) {

        List<String> fechas = datos.stream().map(m -> (String) m.get("fecha")).distinct().collect(Collectors.toList());
        String metricaPrincipal = datos.get(0).get("metrica").toString();

        Double[] valoresUsuario = datos.stream()
                .filter(m -> m.get("metrica").equals(metricaPrincipal))
                .map(m -> Double.valueOf(m.get("valor").toString()))
                .toArray(Double[]::new);

        // i18n: Leyenda Progreso
        Series<Double> serieUsuario = new Series<>(traduccionService.get("legend.progreso"), valoresUsuario);

        Series<Double> serieMeta = null;
        if (normas != null && !normas.isEmpty()) {
            try {
                Double valorMeta = Double.valueOf(normas.get(0).get("valor").toString());
                Double[] valoresMeta = new Double[fechas.size()];
                Arrays.fill(valoresMeta, valorMeta);
                // i18n: Leyenda Meta
                serieMeta = new Series<>(traduccionService.get("legend.meta"), valoresMeta);
            } catch (Exception e) {
                System.err.println("Error al procesar meta: " + e.getMessage());
            }
        }

        Series[] seriesChart;
        String[] colores;

        if (serieMeta != null) {
            seriesChart = new Series[]{serieUsuario, serieMeta};
            colores = new String[]{"#1A73E8", "#FBC02D"};
        } else {
            seriesChart = new Series[]{serieUsuario};
            colores = new String[]{"#1A73E8"};
        }

        Toolbar toolbar = new Toolbar();
        toolbar.setShow(false);
        Zoom zoom = new Zoom();
        zoom.setEnabled(false);
        Chart chartConfig = new Chart();
        chartConfig.setType(Type.LINE);
        chartConfig.setHeight("300px");
        chartConfig.setZoom(zoom);
        chartConfig.setToolbar(toolbar);

        Stroke stroke = new Stroke();
        stroke.setCurve(Curve.SMOOTH);
        stroke.setWidth(4.0);

        Legend legend = new Legend();
        legend.setShow(true);
        legend.setPosition(Position.TOP);

        XAxis xaxis = new XAxis();
        xaxis.setCategories(fechas);

        return ApexChartsBuilder.get()
                .withChart(chartConfig)
                .withColors(colores)
                .withStroke(stroke)
                .withLegend(legend)
                .withSeries(seriesChart)
                .withXaxis(xaxis)
                .build();
    }

    private ApexCharts crearChartSinDatos(String titulo) {
        TitleSubtitle title = new TitleSubtitle();
        // i18n: Título gráfico vacío
        title.setText(traduccionService.get("chart.sin_datos"));
        title.setAlign(Align.CENTER);

        Chart chartConfig = new Chart();
        chartConfig.setHeight("200px");

        return ApexChartsBuilder.get()
                .withChart(chartConfig)
                .withTitle(title)
                .build();
    }

    private void abrirDialogoTrofeos() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxWidth("95vw");

        List<Insignia> todas = dashboardService.getCatalogoInsignias();
        List<JudokaInsignia> mios = dashboardService.getInsigniasGanadas(judokaActual);

        MiDoWidget widget = new MiDoWidget(todas, mios, traduccionService);
        dialog.add(widget);

        // i18n: Botón cerrar
        Button cerrar = new Button(traduccionService.get("btn.cerrar"), e -> dialog.close());
        dialog.getFooter().add(cerrar);
        dialog.open();
    }

    private Component crearEstadoVacio(String nombreCategoria) {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setHeight("300px");

        Icon icon = VaadinIcon.LOCK.create();
        icon.setSize("40px");
        icon.setColor("#B0BEC5");

        // i18n: Estado bloqueada
        H3 titulo = new H3(nombreCategoria + " " +
                traduccionService.get("badge.estado.bloqueada"));
        titulo.getStyle().set("color", "#78909C").set("margin", "0");

        // i18n: Descripción vacía
        Span mensaje = new Span(traduccionService.get("empty.desc"));
        mensaje.getStyle().set("color", "#B0BEC5").set("font-style", "italic").set("text-align", "center");

        layout.add(icon, titulo, mensaje);
        return layout;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        actualizarTextos();
        actualizarDatos();
    }
}