package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaDashboardService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.AgendaDialog;
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
@PageTitle("Combat Profile | Club Judo Colombia")
@CssImport("./styles/dashboard-judoka.css")
public class JudokaDashboardView extends JudokaLayout implements LocaleChangeObserver {

    private final JudokaDashboardService dashboardService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final SesionService sesionService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;

    private Judoka judokaActual;

    private H2 tituloNombre;
    private Button btnAgenda;
    private Button btnTrofeos;
    private CombatRadarWidget radarWidget;
    private Div detailChartContainer;

    @Autowired
    public JudokaDashboardView(JudokaDashboardService dashboardService,
                               SecurityService securityService,
                               TraduccionService traduccionService,
                               SesionService sesionService,
                               AccessAnnotationChecker accessChecker,
                               InsigniaRepository insigniaRepository,
                               JudokaInsigniaRepository judokaInsigniaRepository) {
        super(securityService, accessChecker);
        this.dashboardService = dashboardService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.sesionService = sesionService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;

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
        btnTrofeos.addClickListener(e -> abrirDialogoTrofeos());

        btnAgenda = new Button(new Icon(VaadinIcon.CALENDAR_CLOCK));
        btnAgenda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgenda.addClickListener(
                e -> new AgendaDialog(dashboardService,
                        sesionService, traduccionService, judokaActual).open());
        Button btnPalmares = new Button(new Icon(VaadinIcon.MEDAL));
        btnPalmares.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        btnPalmares.getStyle().set("color", "#2ECC71"); // Verde esmeralda o el color que prefieras
        btnPalmares.addClickListener(e -> new PalmaresDialog(dashboardService, traduccionService, judokaActual).open());

        HorizontalLayout header = new HorizontalLayout(tituloNombre, btnPalmares, btnTrofeos, btnAgenda); // Añadido btnPalmares
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        radarWidget = new CombatRadarWidget();

        FlexLayout chipsLayout = new FlexLayout();
        chipsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chipsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chipsLayout.getStyle().set("gap", "10px").set("margin-top", "20px");

        chipsLayout.add(crearChipFiltro("Fuerza", "ejercicio.abdominales_1min.nombre"));
        chipsLayout.add(crearChipFiltro("Velocidad", "ejercicio.carrera_20m.nombre"));
        chipsLayout.add(crearChipFiltro("Resistencia Especial", "ejercicio.sjft.nombre"));
        chipsLayout.add(crearChipFiltro("Agilidad", "ejercicio.agilidad_4x4.nombre"));
        chipsLayout.add(crearChipFiltro("Potencia", "ejercicio.salto_horizontal_proesp.nombre"));

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

        detailChartContainer.add(new Span("Selecciona una categoría arriba para ver tu evolución."));

        mainLayout.add(header, radarWidget, chipsLayout, detailChartContainer);
        setContent(mainLayout);

        actualizarTextos();
        actualizarDatos();
    }

    private void actualizarDatos() {
        Double poder = dashboardService.getPoderDeCombate(judokaActual);
        Map<String, Double> radarData = dashboardService.getDatosRadar(judokaActual);

        radarWidget.updateData(
                poder,
                radarData,
                traduccionService.get("kpi.poder_combate"),
                traduccionService.get("chart.radar.serie"),
                traduccionService.get("chart.sin_datos")
        );
    }

    private void actualizarTextos() {
        // 1. Obtener el nombre de forma segura
        String nombre = (judokaActual != null && judokaActual.getUsuario() != null)
                ? judokaActual.getUsuario().getNombre() : "";

        // 2. CORRECCIÓN: Usar getTranslation() de Vaadin en lugar del servicio directo.
        // Esto invoca a CustomI18NProvider, que sí sabe reemplazar el {0} por el nombre.
        tituloNombre.setText(getTranslation("dashboard.welcome", nombre));

        // Lo mismo para el botón, para mantener coherencia con el idioma actual
        btnAgenda.setText(getTranslation("kpi.tareas_hoy"));
    }

    private Button crearChipFiltro(String etiqueta, String clavePrueba) {
        Button chip = new Button(etiqueta);
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

        // 1. Buscamos la prueba (Metadata)
        Optional<PruebaEstandar> pruebaOpt = dashboardService.buscarPrueba(clavePrueba);

        if (pruebaOpt.isPresent()) {
            String tituloCategoria = traduccionService.get(clavePrueba);

            // 2. Buscamos los datos históricos
            List<Map<String, Object>> datos = dashboardService.getHistorialPrueba(judokaActual, pruebaOpt.get());

            // 3. --- NUEVO: Buscamos la Meta (Mejor marca esperada) ---
            List<Map<String, Object>> normas = dashboardService.getMetaPrueba(judokaActual, pruebaOpt.get());

            if (datos.isEmpty()) {
                detailChartContainer.add(crearEstadoVacio(tituloCategoria));
            } else {
                // Si hay datos, mostramos el gráfico lineal pasando también las normas
                ApexCharts chart = crearGraficoHistorial(datos, normas, tituloCategoria); // <--- CAMBIO AQUÍ
                chart.setWidth("100%");
                detailChartContainer.add(chart);

                UI.getCurrent().getPage().executeJs(
                        "setTimeout(() => window.dispatchEvent(new Event('resize')), 200);");
            }
        } else {
            detailChartContainer.add(new Span("Configuración no encontrada para: " + clavePrueba));
        }
    }

    // --- Método Actualizado para incluir la Meta ---
    private ApexCharts crearGraficoHistorial(List<Map<String, Object>> datos,
                                             List<Map<String, Object>> normas, // <--- Nuevo Parámetro
                                             String titulo) {

        List<String> fechas = datos.stream().map(m -> (String) m.get("fecha")).distinct().collect(Collectors.toList());
        String metricaPrincipal = datos.get(0).get("metrica").toString();

        // Serie 1: Usuario (Azul)
        Double[] valoresUsuario = datos.stream()
                .filter(m -> m.get("metrica").equals(metricaPrincipal))
                .map(m -> Double.valueOf(m.get("valor").toString()))
                .toArray(Double[]::new);

        Series<Double> serieUsuario = new Series<>(traduccionService.get("legend.progreso"), valoresUsuario);

        // Serie 2: Meta (Dorado) --- LÓGICA NUEVA ---
        Series<Double> serieMeta = null;
        if (normas != null && !normas.isEmpty()) {
            try {
                // Asumimos que la meta es un valor constante (Ej: "Excelente" = 35 repeticiones)
                Double valorMeta = Double.valueOf(normas.get(0).get("valor").toString());
                Double[] valoresMeta = new Double[fechas.size()]; // Creamos una línea recta a lo largo del tiempo
                Arrays.fill(valoresMeta, valorMeta);
                serieMeta = new Series<>(traduccionService.get("legend.meta"), valoresMeta);
            } catch (Exception e) {
                // Si falla conversión, simplemente no mostramos la meta
                System.err.println("Error al procesar meta para gráfico: " + e.getMessage());
            }
        }

        // Definimos las series a mostrar
        Series[] seriesChart;
        String[] colores;

        if (serieMeta != null) {
            seriesChart = new Series[]{serieUsuario, serieMeta};
            // Azul para usuario, Dorado para la meta (Objetivo)
            colores = new String[]{"#1A73E8", "#FBC02D"};
        } else {
            seriesChart = new Series[]{serieUsuario};
            colores = new String[]{"#1A73E8"};
        }

        // Configuración Gráfica
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
                .withColors(colores) // Aplicamos colores (Azul/Dorado)
                .withStroke(stroke)
                .withLegend(legend)
                .withSeries(seriesChart) // Pasamos 1 o 2 series
                .withXaxis(xaxis)
                .build();
    }

    private ApexCharts crearChartSinDatos(String titulo) {
        TitleSubtitle title = new TitleSubtitle();
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

        H3 titulo = new H3(nombreCategoria + " " +
                traduccionService.get("badge.estado.bloqueada"));
        titulo.getStyle().set("color", "#78909C").set("margin", "0");

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