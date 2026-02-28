package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.KpiCard;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import java.util.Optional;

@Route(value = "dashboard-sensei", layout = SenseiLayout.class)
@PageTitle("Dashboard Sensei | Club Judo Colombia")
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiDashboardView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(SenseiDashboardView.class);
    private final SenseiDashboardService dashboardService;
    private final TraduccionService traduccionService;
    private final SecurityService securityService;
    public SenseiDashboardView(SenseiDashboardService dashboardService,
                               TraduccionService traduccionService,
                               SecurityService securityService) {
        this.dashboardService = dashboardService;
        this.traduccionService = traduccionService;
        this.securityService = securityService;
        addClassName("dashboard-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- Encabezado con texto traducido ---
        add(construirEncabezadoPersonalizado());
        // --- Acciones Rápidas con textos traducidos ---
        // --- Acciones Rápidas ---
        HorizontalLayout accionesRapidas = new HorizontalLayout();
        accionesRapidas.setWidthFull();
        Button btnTatami = new Button("ENTRAR AL TATAMI", new Icon(VaadinIcon.PLAY));
// Le damos estilo de botón principal, grande y llamativo (Verde)
        btnTatami.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_SUCCESS);
        btnTatami.setWidthFull(); // Que ocupe todo el ancho en el celular

// La acción de navegación
        btnTatami.addClickListener(e ->
                btnTatami.getUI().ifPresent(ui -> ui.navigate(SenseiTatamiView.class))
        );        Button btnAsistencia = new Button(
                traduccionService.get("dashboard.boton.tomar_asistencia"),
                new Icon(VaadinIcon.CHECK_CIRCLE)
        );
        btnAsistencia.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        // NUEVO: Botón para que el Sensei gestione sus invitaciones
        Button btnInvitar = new Button(traduccionService.get("dashboard.boton.invitar"),
                new Icon(VaadinIcon.PAPERPLANE));
        btnInvitar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        btnInvitar.addClickListener(e -> UI.getCurrent().navigate("invitaciones"));

        accionesRapidas.add(btnAsistencia, btnInvitar, btnTatami);
        add(accionesRapidas);

        // --- Construcción del contenido ---
        buildLayout();

        logger.info("SenseiDashboardView inicializada correctamente");
    }

    private void buildLayout() {
        Optional<Sensei> senseiActual = securityService.getAuthenticatedSensei();

        if (senseiActual.isEmpty()) {
            // Si no hay perfil de Sensei todavía (onboarding incompleto)
            add(new com.vaadin.flow.component.html.Span("Configurando tu dojo... por favor completa tu perfil."));
            return;
        }
        int asistenciaPromedio = (int) dashboardService.calcularAsistenciaPromedio(senseiActual.get().getId());

        HorizontalLayout kpiRow = new HorizontalLayout(
                new KpiCard(
                        traduccionService.get("dashboard.kpi.total_judokas"),
                        dashboardService.getTotalJudokas(),
                        new Icon(VaadinIcon.USERS)
                ),
                new KpiCard(
                        traduccionService.get("dashboard.kpi.grupos_activos"),
                        dashboardService.getTotalGrupos(),
                        new Icon(VaadinIcon.GROUP)
                ),
                new KpiCard(
                        traduccionService.get("dashboard.kpi.pruebas_hoy"),
                        dashboardService.getPruebasHoy(),
                        new Icon(VaadinIcon.CLIPBOARD_CHECK)
                ),
                new KpiCard(
                        traduccionService.get("dashboard.kpi.asistencia_promedio"),
                        asistenciaPromedio + "%",
                        new Icon(VaadinIcon.CHART_LINE)
                )
        );
        kpiRow.setWidthFull();

        // NUEVO: Gráficos con títulos traducidos
        HorizontalLayout chartsRow = new HorizontalLayout(
                crearGraficoPoderDeCombate(),
                crearGraficoAsistenciaMensual()
        );
        chartsRow.setSizeFull();

        add(kpiRow, chartsRow);
    }

    private ApexCharts crearGraficoPoderDeCombate() {
        Map<String, Double> promedioPorGrupo = dashboardService.getPromedioPoderDeCombatePorGrupo();

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(traduccionService.get("dashboard.grafico.poder_combate_titulo"))
                        .build())
                .withSeries(new Series<>(
                        traduccionService.get("dashboard.grafico.promedio"),
                        promedioPorGrupo.values().toArray(new Double[0])
                ))
                .withXaxis(XAxisBuilder.get()
                        .withCategories(promedioPorGrupo.keySet().toArray(new String[0]))
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .build())
                        .build())
                .build();
    }

    private ApexCharts crearGraficoAsistenciaMensual() {
        List<Double> datos = dashboardService.getAsistenciaUltimos30Dias();

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.LINE).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(traduccionService.get("dashboard.grafico.asistencia_30dias_titulo"))
                        .build())
                .withSeries(new Series<>(
                        traduccionService.get("dashboard.grafico.asistencia_porcentaje"),
                        datos.toArray(new Double[0])
                ))
                .build();
    }
    private com.vaadin.flow.component.Component construirEncabezadoPersonalizado() {
        // 1. Intentamos obtener el perfil del Sensei logueado
        Optional<Sensei> senseiOpt = securityService.getAuthenticatedSensei();

        String nombreAMostrar;

        if (senseiOpt.isPresent() && senseiOpt.get().getNombreDojo() != null && !senseiOpt.get().getNombreDojo().isEmpty()) {
            // Es un Profesor: Mostramos el nombre de SU club
            nombreAMostrar = senseiOpt.get().getNombreDojo();
        } else {
            // Es el Master o alguien sin perfil de Sensei aún
            nombreAMostrar = traduccionService.get("dashboard.titulo");
        }

        // 2. Creamos el componente visual
        H2 titulo = new H2("¡Bienvenido a " + nombreAMostrar + "!");
        titulo.getStyle().set("color", "var(--lumo-primary-color)");
        titulo.getStyle().set("margin-top", "0");

        return titulo;
    }
}