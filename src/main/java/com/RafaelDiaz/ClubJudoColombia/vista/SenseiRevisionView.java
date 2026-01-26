package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.servicio.EjecucionTareaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // NUEVO IMPORT
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route("revision-tareas")
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiRevisionView extends VerticalLayout {

    private final EjecucionTareaService ejecucionTareaService;
    private final TraduccionService traduccionService; // NUEVO: Servicio de traducción

    private Grid<EjecucionTarea> grid;

    // NUEVO: Inyectar TraduccionService en el constructor
    public SenseiRevisionView(EjecucionTareaService ejecucionTareaService,
                              TraduccionService traduccionService) {
        this.ejecucionTareaService = ejecucionTareaService;
        this.traduccionService = traduccionService; // NUEVO: Inicialización

        setSizeFull();
        // NUEVO: Título traducido
        add(new H1(traduccionService.get("revision.titulo")));

        configurarGrid();
        cargarEjecuciones();

        add(grid);
    }

    private void configurarGrid() {
        grid = new Grid<>(EjecucionTarea.class);
        grid.setSizeFull();
        grid.removeAllColumns();

        // Formateador de fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Columna 1: Cuándo
        grid.addColumn(ejecucion -> ejecucion.getFechaRegistro().format(formatter))
                .setHeader(traduccionService.get("revision.grid.fecha_hora"))
                .setSortable(true)
                .setAutoWidth(true);

        // Columna 2: Quién
        grid.addColumn(ejecucion -> {
            String nombre = ejecucion.getJudoka().getUsuario().getNombre();
            String apellido = ejecucion.getJudoka().getUsuario().getApellido();
            return nombre + " " + apellido;
        }).setHeader(traduccionService.get("revision.grid.judoka")).setSortable(true);

        // Columna 3: Qué
        grid.addColumn(ejecucion -> ejecucion.getEjercicioPlanificado().getTareaDiaria().getNombre())
                .setHeader(traduccionService.get("revision.grid.tarea_realizada"))
                .setSortable(true);

        // Columna 4: Ubicación
        grid.addColumn(new ComponentRenderer<>(ejecucion -> {
            Double lat = ejecucion.getLatitud();
            Double lon = ejecucion.getLongitud();

            if (lat == null || lon == null) {
                // NUEVO: Texto traducido para "Sin GPS"
                return new Span(traduccionService.get("revision.grid.sin_gps"));
            }

            String url = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lon);

            // NUEVO: Texto traducido para "Ver Mapa"
            Anchor link = new Anchor(url, traduccionService.get("revision.grid.ver_mapa"));
            link.setTarget("_blank");
            link.addComponentAsFirst(new Icon(VaadinIcon.MAP_MARKER));
            return link;

        })).setHeader(traduccionService.get("revision.grid.ubicacion_gps"));
    }

    private void cargarEjecuciones() {
        grid.setItems(ejecucionTareaService.findAllWithDetails());
    }
}