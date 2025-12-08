package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaDashboardService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AgendaDialog extends Dialog {

    public AgendaDialog(JudokaDashboardService service,
                        SesionService sesionService,
                        TraduccionService traduccionService,
                        Judoka judoka) {

        // Título del Diálogo
        setHeaderTitle(traduccionService.get("kpi.tareas_hoy"));
        setWidth("600px");
        setMaxWidth("95vw");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // 1. Sección de KPIs (Indicadores Clave)
        HorizontalLayout kpis = new HorizontalLayout();
        kpis.setWidthFull();
        kpis.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Cálculo de días para próxima evaluación
        String diasProx = service.getDiasProximaEvaluacion(judoka)
                .map(d -> d == 0 ? traduccionService.get("kpi.hoy") : d + " " + traduccionService.get("kpi.dias"))
                .orElse("-");

        kpis.add(
                new KpiCard(traduccionService.get("kpi.planes_activos"), String.valueOf(service.getPlanesActivos(judoka)), new Icon(VaadinIcon.CALENDAR)),
                new KpiCard(traduccionService.get("kpi.tareas_hoy"), String.valueOf(service.getTareasHoy(judoka)), new Icon(VaadinIcon.CHECK_CIRCLE)),
                new KpiCard(traduccionService.get("kpi.proxima_eval"), diasProx, new Icon(VaadinIcon.CLOCK))
        );
        content.add(kpis);

        // 2. Calendario de Sesiones
        JudokaCalendar calendario = new JudokaCalendar(sesionService, traduccionService, judoka);
        content.add(calendario);

        // 3. Botón de Acción Principal "Ir a Entrenar"
        Button btnIrTareas = new Button(traduccionService.get("dashboard.btn.tareas"), new Icon(VaadinIcon.ARROW_RIGHT));
        btnIrTareas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnIrTareas.setWidthFull();
        btnIrTareas.addClickListener(e -> {
            this.close(); // Cerramos el diálogo antes de navegar
            UI.getCurrent().navigate("mis-planes"); // Navegación a la vista de ejecución
        });
        content.add(btnIrTareas);

        add(content);

        // Botón de Cerrar (Footer)
        Button cerrar = new Button(traduccionService.get("btn.cerrar"), e -> this.close());
        getFooter().add(cerrar);
    }
}