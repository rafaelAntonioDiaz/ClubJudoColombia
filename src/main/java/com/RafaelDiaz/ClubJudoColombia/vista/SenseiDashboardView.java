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

@Route(value = "dashboard-sensei", layout = SenseiLayout.class)
@PageTitle("Dashboard Sensei | Club Judo Colombia")
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiDashboardView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(SenseiDashboardView.class);
    private final SenseiDashboardService dashboardService;
    private final TraduccionService traduccionService; // NUEVO: Servicio de traducción

    public SenseiDashboardView(SenseiDashboardService dashboardService,
                               TraduccionService traduccionService) { // NUEVO: Parámetro añadido
        this.dashboardService = dashboardService;
        this.traduccionService = traduccionService; // NUEVO: Inicialización

        addClassName("dashboard-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- Encabezado con texto traducido ---
        add(new H2(traduccionService.get("dashboard.titulo")));

        // --- Acciones Rápidas con textos traducidos ---
        HorizontalLayout accionesRapidas = new HorizontalLayout();
        accionesRapidas.setWidthFull();

        Button btnAsistencia = new Button(
                traduccionService.get("dashboard.boton.tomar_asistencia"),
                new Icon(VaadinIcon.CHECK_CIRCLE)
        );
        btnAsistencia.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnAsistencia.getStyle().set("padding", "20px");
        btnAsistencia.addClickListener(e -> UI.getCurrent().navigate(RegistroAsistenciaView.class));

        accionesRapidas.add(btnAsistencia);
        add(accionesRapidas);

        // --- Construcción del contenido ---
        buildLayout();

        logger.info("SenseiDashboardView inicializada correctamente");
    }

    private void buildLayout() {
        int asistenciaPromedio = dashboardService.calcularAsistenciaPromedio();

        // NUEVO: KPIs con textos traducidos
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
}