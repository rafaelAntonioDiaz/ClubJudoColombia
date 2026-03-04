package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
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
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Toolbar;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.Zoom;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.yaxis.Title;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
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
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;

@Route("dashboard-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR","ROLE_ACUDIENTE","ROLE_MASTER"})
@PageTitle("Combat Profile | Club Judo Colombia")
@CssImport("./styles/dashboard-judoka.css")
public class JudokaDashboardView extends JudokaLayout implements LocaleChangeObserver {

    private final JudokaDashboardService dashboardService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final SesionService sesionService;
    private final AsistenciaService asistenciaService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;
    private final JudokaRepository judokaRepository;
    private Judoka judokaActual;

    // Componentes UI
    private H2 tituloNombre;
    private Button btnAgenda;
    private Button btnTrofeos;
    private CombatRadarWidget radarWidget;
    private Div detailChartContainer;
    private Span lblInstruccionChart;
    private final SabiduriaService sabiduriaService;
    private Div seccionSabiduria;
    // Contenedor de botones para que sea accesible globalmente si se requiere
    private FlexLayout chipsLayout;

    @Autowired
    public JudokaDashboardView(JudokaDashboardService dashboardService,
                               SecurityService securityService,
                               TraduccionService traduccionService,
                               SesionService sesionService,
                               AccessAnnotationChecker accessChecker,
                               InsigniaRepository insigniaRepository,
                               JudokaInsigniaRepository judokaInsigniaRepository,
                               AsistenciaService asistenciaService,
                               SabiduriaService sabiduriaService,
                               JudokaRepository judokaRepository) {
        super(securityService, accessChecker, traduccionService, judokaRepository);
        System.out.println("🔧 JudokaDashboardView constructor iniciado");
        this.dashboardService = dashboardService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.sesionService = sesionService;
        this.asistenciaService = asistenciaService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
        this.sabiduriaService = sabiduriaService;
        this.judokaRepository = judokaRepository;

        initJudoka();
        buildModernDashboard();
        System.out.println("🔧 JudokaDashboardView constructor finalizado");
    }

    private void initJudoka() {
        // 1. Prioridad: ¿Viene por Magic Link?
        System.out.println("🔧 initJudoka() - Iniciando");
        Long judokaId = (Long) VaadinSession.getCurrent().getAttribute("JUDOKA_ACTUAL_ID");
        System.out.println("🔧 JUDOKA_ACTUAL_ID desde sesión: " + judokaId);
        if (judokaId != null) {
            this.judokaActual = judokaRepository.findByIdWithDetails(judokaId)
                    .orElseThrow(() -> new RuntimeException("Judoka no encontrado con ID: " + judokaId));
            System.out.println("🔧 Judoka obtenido por ID: " + judokaActual.getNombre());
        } else {
            // 2. Fallback: usuario autenticado normal (Acudiente/Sensei/Master)
            this.judokaActual = securityService.getAuthenticatedJudoka()
                    .orElseThrow(() -> new RuntimeException("Error: Judoka no autenticado."));
            System.out.println("🔧 Judoka obtenido por securityService");
        }
    }

    private void buildModernDashboard() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addClassName("judoka-dashboard-content");
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        mainLayout.setMaxWidth("900px");
        mainLayout.getStyle().set("margin", "0 auto").set("padding-bottom", "50px");

        // --- HEADER ---
        tituloNombre = new H2();
        tituloNombre.getStyle().set("margin", "0").set("color", "var(--lumo-header-text-color)");

        btnTrofeos = new Button(new Icon(VaadinIcon.TROPHY));
        btnTrofeos.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        btnTrofeos.getStyle().set("color", "#F1C40F");
        btnTrofeos.setTooltipText(traduccionService.get("tooltip.trofeos"));
        btnTrofeos.addClickListener(e -> abrirDialogoTrofeos());

        btnAgenda = new Button(new Icon(VaadinIcon.CALENDAR_CLOCK));
        btnAgenda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgenda.addClickListener(e -> new AgendaDialog(dashboardService, sesionService, traduccionService, judokaActual).open());

        Button btnPalmares = new Button(new Icon(VaadinIcon.MEDAL));
        btnPalmares.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        btnPalmares.getStyle().set("color", "#2ECC71");
        btnPalmares.setTooltipText(traduccionService.get("tooltip.palmares"));
        btnPalmares.addClickListener(e -> new PalmaresDialog(dashboardService, traduccionService, judokaActual).open());

        HorizontalLayout header = new HorizontalLayout(tituloNombre, btnPalmares, btnTrofeos, btnAgenda);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Component widgetSabiduria = crearTarjetaSabiduria();
        // --- CHECK IN ---
        CheckInWidget checkInWidget = new CheckInWidget(asistenciaService, judokaActual, traduccionService);
        checkInWidget.getStyle().set("margin-bottom", "20px");

        // --- RADAR WIDGET ---
        radarWidget = new CombatRadarWidget();
        radarWidget.setMaxWidth("500px");
        radarWidget.getStyle().set("margin", "0 auto");

        // --- BOTONES DE CATEGORÍAS ---
        chipsLayout = new FlexLayout();
        chipsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chipsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chipsLayout.getStyle().set("gap", "12px").set("margin-top", "30px").set("margin-bottom", "10px");

        // CORRECCIÓN: Usamos las claves LARGAS que tu base de datos sí reconoce
        chipsLayout.add(crearChipFiltro("cat.fuerza", "ejercicio.abdominales_1min.nombre", VaadinIcon.HAMMER));
        chipsLayout.add(crearChipFiltro("cat.velocidad", "ejercicio.carrera_20m.nombre", VaadinIcon.TIMER));
        chipsLayout.add(crearChipFiltro("cat.resistencia", "ejercicio.sjft.nombre", VaadinIcon.HEART));
        chipsLayout.add(crearChipFiltro("cat.agilidad", "ejercicio.agilidad_4x4.nombre", VaadinIcon.RANDOM));
        chipsLayout.add(crearChipFiltro("cat.potencia", "ejercicio.salto_horizontal_proesp.nombre", VaadinIcon.FLASH));

        // --- GRÁFICA DE DETALLE ---
        detailChartContainer = new Div();
        detailChartContainer.setWidthFull();
        detailChartContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("margin-top", "10px")
                .set("min-height", "300px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        lblInstruccionChart = new Span(traduccionService.get("msg.selecciona.categoria.para.comparar"));
        lblInstruccionChart.getStyle().set("color", "var(--lumo-secondary-text-color)");
        detailChartContainer.add(lblInstruccionChart);

        mainLayout.add(header, widgetSabiduria, checkInWidget, radarWidget, chipsLayout, detailChartContainer);
        setContent(mainLayout);

        actualizarTextos();
        actualizarDatos();
    }

    private void actualizarDatos() {
        Double poder = dashboardService.getPoderDeCombate(judokaActual);
        Map<String, Double> radarData = dashboardService.getDatosRadar(judokaActual);

        if (poder == null || poder <= 0.0) {
            int edad = judokaActual.getEdad();
            String titulo = "¡Poder Inactivo! \uD83D\uDD12";
            String subtitulo;
            if (edad < 14) {
                subtitulo = "No te preocupes, todo gran guerrero empieza así. Pídele a tu Sensei que te evalúe.";
            } else {
                subtitulo = "Completa tu Bloque Definitorio para despertar tu potencial.";
            }
            radarWidget.mostrarModoIncognito(titulo, subtitulo);
        } else {
            radarWidget.updateData(
                    poder, radarData,
                    traduccionService.get("kpi.poder_combate"),
                    traduccionService.get("chart.radar.serie"),
                    traduccionService.get("chart.sin_datos")
            );
        }
    }

    private void actualizarTextos() {
        String nombre = (judokaActual != null) ? judokaActual.getNombre() : "Judoka";
        tituloNombre.setText(traduccionService.get("dashboard.welcome", nombre));
        btnAgenda.setText(traduccionService.get("kpi.tareas_hoy"));
    }

    private Button crearChipFiltro(String claveEtiqueta, String clavePrueba, VaadinIcon icono) {
        Button chip = new Button(traduccionService.get(claveEtiqueta), new Icon(icono));
        chip.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        // Estilo "Pill" o pastilla más moderno
        chip.getStyle()
                .set("border-radius", "24px")
                .set("padding-left", "15px")
                .set("padding-right", "15px");

        chip.addClickListener(e -> {
            // Lógica de selección visual
            chip.getParent().ifPresent(parent ->
                    parent.getChildren().filter(c -> c instanceof Button).forEach(c -> {
                        Button b = (Button)c;
                        b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        b.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                    })
            );
            chip.removeThemeVariants(ButtonVariant.LUMO_CONTRAST);
            chip.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            mostrarGraficoDetalle(clavePrueba);
        });
        return chip;
    }

    private void mostrarGraficoDetalle(String codigoPrueba) {
        detailChartContainer.removeAll();
        Optional<PruebaEstandar> pruebaOpt = dashboardService.buscarPrueba(codigoPrueba);

        if (pruebaOpt.isPresent()) {
            PruebaEstandar prueba = pruebaOpt.get();
            String tituloCategoria = traduccionService.get(codigoPrueba);
            if (tituloCategoria.equals(codigoPrueba)) {
                try { tituloCategoria = prueba.getNombreKey(); }
                catch (Exception e) { tituloCategoria = codigoPrueba.toUpperCase(); }
            }

            List<Map<String, Object>> datos = dashboardService.getHistorialPrueba(judokaActual, prueba);

            if (datos == null || datos.isEmpty()) {
                detailChartContainer.add(crearEstadoVacio(tituloCategoria));
            } else {
                String unidad = obtenerUnidadMedida(codigoPrueba);

                // FASE 2: Calculamos la meta dinámicamente desde el backend
                Double metaAlcanzable = dashboardService.calcularMotivador(judokaActual, prueba);

                // Pasamos la meta al creador del gráfico
                ApexCharts chart = crearGraficoComparativo(datos, metaAlcanzable, tituloCategoria, unidad);
                chart.setWidth("100%");
                detailChartContainer.add(chart);

                UI.getCurrent().getPage().executeJs("setTimeout(() => window.dispatchEvent(new Event('resize')), 200);");
            }
        } else {
            Span errorSpan = new Span("Prueba no encontrada en BD: " + codigoPrueba);
            errorSpan.getStyle().set("color", "red").set("font-weight", "bold");
            detailChartContainer.add(errorSpan);
        }
    }

    // Simulación del "Mejor de la Clase"
    private Double obtenerRecordSimulado(String clavePrueba) {
        // Valores de REFERENCIA ELITE (Difíciles de superar)

        if (clavePrueba.contains("abdominales")) return 75.0; // Repeticiones (Más es mejor)

        // Carrera 20m: Usain Bolt corre 20m en ~2.8s lanzados.
        // Ponemos 2.9s para que sea un récord muy difícil. (Menos es mejor)
        if (clavePrueba.contains("carrera")) return 2.90;

        // SJFT: Un índice < 10 es nivel Olímpico. (Menos es mejor)
        if (clavePrueba.contains("sjft")) return 10.5;

        // Agilidad: Muy rápido. (Menos es mejor)
        if (clavePrueba.contains("agilidad")) return 13.5;

        // Salto: 260cm es una bestialidad de salto horizontal. (Más es mejor)
        if (clavePrueba.contains("salto")) return 260.0;

        return 100.0; // Valor por defecto
    }

    // FASE 2: Gráfico rediseñado para usar una línea de "Motivador" en lugar de un Récord Mundial
// FASE 2: Gráfico rediseñado con el Motivador
    private ApexCharts crearGraficoComparativo(List<Map<String, Object>> datosUsuario, Double valorMotivador, String titulo, String unidad) {
        List<String> fechas = new java.util.ArrayList<>();
        List<Double> valoresUsuario = new java.util.ArrayList<>();
        List<Double> valoresMotivador = new java.util.ArrayList<>();

        for (Map<String, Object> fila : datosUsuario) {
            fechas.add(fila.get("fecha").toString());
            valoresUsuario.add(((Number) fila.get("valor")).doubleValue());
            valoresMotivador.add(valorMotivador != null ? valorMotivador : 0.0);
        }

        String tituloUsuario = traduccionService.get("chart.tu_progreso", "Tu Progreso");
        String tituloMotivador = "Tu Motivador \uD83C\uDFAF";

        // SOLUCIÓN ERROR 5: Usar la clase Title específica del eje Y
        com.github.appreciated.apexcharts.config.yaxis.Title yTitle = new com.github.appreciated.apexcharts.config.yaxis.Title();
        yTitle.setText(unidad);

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(com.github.appreciated.apexcharts.config.stroke.Curve.SMOOTH)
                        .withWidth(3.0) // <--- SOLUCIÓN ERROR 4: Un solo número aplica el grosor a ambas líneas
                        .withDashArray(Collections.singletonList(5.0))
                        .build())
                .withColors("#00E396", "#FFD700")
                .withSeries(
                        new com.github.appreciated.apexcharts.helper.Series<>(tituloUsuario, valoresUsuario.toArray(new Double[0])),
                        new com.github.appreciated.apexcharts.helper.Series<>(tituloMotivador, valoresMotivador.toArray(new Double[0]))
                )
                .withXaxis(XAxisBuilder.get().withCategories(fechas).build())
                .withYaxis(YAxisBuilder.get().withTitle(yTitle).build()) // <--- Aplicamos el YTitle corregido
                .withLegend(LegendBuilder.get().withPosition(com.github.appreciated.apexcharts.config.legend.Position.TOP).build())
                .build();
    }
    private void abrirDialogoTrofeos() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxWidth("95vw");

        List<Insignia> todas = insigniaRepository.findAll();
        List<JudokaInsignia> mios = judokaInsigniaRepository.findByJudoka(judokaActual);

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

        Icon icon = VaadinIcon.BAR_CHART.create(); // Icono de gráfica vacía
        icon.setSize("40px");
        icon.setColor("#B0BEC5");

        H3 titulo = new H3(nombreCategoria + " " + traduccionService.get("badge.estado.sin_datos"));
        titulo.getStyle().set("color", "#78909C").set("margin", "0");

        Span mensaje = new Span(traduccionService.get("empty.desc.realiza_pruebas"));
        mensaje.getStyle().set("color", "#B0BEC5").set("font-style", "italic").set("text-align", "center");

        layout.add(icon, titulo, mensaje);
        return layout;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        actualizarTextos();
        actualizarDatos();
        actualizarContenidoSabiduria();
    }

    private String obtenerUnidadMedida(String clavePrueba) {
        if (clavePrueba == null) return "";

        if (clavePrueba.contains("carrera") || clavePrueba.contains("agilidad") ||
                clavePrueba.contains("velocidad") || clavePrueba.contains("tiempo")) {
            return "Tiempo (s)";
        }

        if (clavePrueba.contains("salto") || clavePrueba.contains("lanzamiento") ||
                clavePrueba.contains("flexibilidad") || clavePrueba.contains("alcance")) {
            return "Distancia (cm)";
        }

        if (clavePrueba.contains("fuerza") || clavePrueba.contains("abdominales") ||
                clavePrueba.contains("flexiones") || clavePrueba.contains("burpees") ||
                clavePrueba.contains("sentadillas")) {
            return "Repeticiones";
        }

        if (clavePrueba.contains("sjft")) {
            return "Índice";
        }

        return "Valor";
    }
    private Component crearTarjetaSabiduria() {
        seccionSabiduria = new Div();
        seccionSabiduria.setWidthFull();
        seccionSabiduria.getStyle()
                .set("background", "linear-gradient(90deg, #fdfbfb 0%, #ebedee 100%)") // Sutil gris zen
                .set("border-left", "6px solid var(--lumo-primary-color)")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)")
                .set("margin-bottom", "25px");

        actualizarContenidoSabiduria(); // Llenar datos
        return seccionSabiduria;
    }

    private void actualizarContenidoSabiduria() {
        seccionSabiduria.removeAll();
        // Detectar idioma simple (puedes mejorar esto según tu lógica de locale)
        String lang = UI.getCurrent().getLocale().getLanguage().equals("en") ? "en" : "es";

        Traduccion frase = sabiduriaService.obtenerFraseSemanal(lang);

        if (frase != null) {
            H3 titulo = new H3(traduccionService.get("sabiduria.titulo", "Sabiduría del Sensei"));
            titulo.getStyle().set("margin", "0 0 10px 0").set("font-size", "1.1rem").set("color", "#555");

            Paragraph texto = new Paragraph("“" + frase.getTexto() + "”");
            texto.getStyle().set("font-style", "italic").set("font-size", "1.2rem").set("font-weight", "500");

            seccionSabiduria.add(titulo, texto);
            seccionSabiduria.setVisible(true);
        } else {
            seccionSabiduria.setVisible(false);
        }
    }
}