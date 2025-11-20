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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Dashboard principal del Sensei con KPIs y gráficos.
 *
 * @author RafaelDiaz
 * @version 1.2 (Corregida)
 * @since 2025-11-20
 */
@Route("dashboard-sensei")
@RolesAllowed("ROLE_SENSEI")
public class SenseiDashboardView extends SenseiLayout {

    private static final Logger logger = LoggerFactory.getLogger(SenseiDashboardView.class);

    private final SenseiDashboardService dashboardService;

    public SenseiDashboardView(SenseiDashboardService dashboardService,
                               SecurityService securityService,
                               AccessAnnotationChecker accessChecker) {
        super(securityService, accessChecker);
        this.dashboardService = dashboardService;

        buildLayout();
        logger.info("SenseiDashboardView inicializada");
    }

    private void buildLayout() {
        H2 titulo = new H2("Dashboard del Sensei");

        int asistenciaPromedio = dashboardService.calcularAsistenciaPromedio();

        // ✅ CORREGIDO: Crear Icon a partir de VaadinIcon enum
        HorizontalLayout kpiRow = new HorizontalLayout(
                new KpiCard("Total Judokas", dashboardService.getTotalJudokas(), new Icon(VaadinIcon.USERS)),
                new KpiCard("Grupos Activos", dashboardService.getTotalGrupos(), new Icon(VaadinIcon.GROUP)),
                new KpiCard("Pruebas Hoy", dashboardService.getPruebasHoy(), new Icon(VaadinIcon.CLIPBOARD_CHECK)),
                new KpiCard("Asistencia Promedio", asistenciaPromedio + "%", new Icon(VaadinIcon.CHART_LINE))
        );
        kpiRow.setWidthFull();

        HorizontalLayout chartsRow = new HorizontalLayout(
                crearGraficoPoderDeCombate(),
                crearGraficoAsistenciaMensual()
        );
        chartsRow.setSizeFull();

        VerticalLayout mainContent = new VerticalLayout(titulo, kpiRow, chartsRow);
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        setContent(mainContent);  // ← ESTA ES LA LÍNEA CLAVE EN VAADIN 24

    }

    private ApexCharts crearGraficoPoderDeCombate() {
        Map<String, Double> promedioPorGrupo = dashboardService.getPromedioPoderDeCombatePorGrupo();

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withTitle(TitleSubtitleBuilder.get().withText("Poder de Combate por Grupo").build())
                .withSeries(new Series<>("Promedio", promedioPorGrupo.values().toArray(new Double[0])))
                .withXaxis(XAxisBuilder.get().withCategories(promedioPorGrupo.keySet().toArray(new String[0])).build())
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
                .withTitle(TitleSubtitleBuilder.get().withText("Asistencia últimos 30 días").build())
                .withSeries(new Series<>("Asistencia %", datos.toArray(new Double[0])))
                .build();
    }
}