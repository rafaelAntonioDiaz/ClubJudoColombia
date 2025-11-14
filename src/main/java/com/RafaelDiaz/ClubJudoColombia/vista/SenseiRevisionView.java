package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.servicio.EjecucionTareaService;
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

/**
 * --- NUEVA VISTA (Flujo 2: Supervisión) ---
 * Esta vista es para el SENSEI.
 * Le permite revisar las Tareas Diarias (checks)
 * que sus Judokas han reportado.
 */
@Route("revision-tareas")
@RolesAllowed("ROLE_SENSEI")
public class SenseiRevisionView extends VerticalLayout {

    private final EjecucionTareaService ejecucionTareaService;

    private Grid<EjecucionTarea> grid;

    public SenseiRevisionView(EjecucionTareaService ejecucionTareaService) {
        this.ejecucionTareaService = ejecucionTareaService;

        setSizeFull();
        add(new H1("Revisión de Tareas Diarias Completadas"));

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
                .setHeader("Fecha y Hora")
                .setSortable(true)
                .setAutoWidth(true);

        // Columna 2: Quién
        grid.addColumn(ejecucion -> {
            String nombre = ejecucion.getJudoka().getUsuario().getNombre();
            String apellido = ejecucion.getJudoka().getUsuario().getApellido();
            return nombre + " " + apellido;
        }).setHeader("Judoka").setSortable(true);

        // Columna 3: Qué
        grid.addColumn(ejecucion -> ejecucion.getEjercicioPlanificado().getTareaDiaria().getNombre())
                .setHeader("Tarea Realizada")
                .setSortable(true);

        // Columna 4: Ubicación (¡Tu requisito!)
        grid.addColumn(new ComponentRenderer<>(ejecucion -> {
            Double lat = ejecucion.getLatitud();
            Double lon = ejecucion.getLongitud();

            if (lat == null || lon == null) {
                return new Span("Sin GPS");
            }

            // --- CORRECCIÓN DE SINTAXIS ---

            // 1. Creamos el enlace a Google Maps
            // (Nota: Corregí el formato de la URL de Google Maps también)
            String url = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lon);

            // 2. Creamos el Anchor (enlace)
            Anchor link = new Anchor(url, "Ver Mapa");

            // 3. Establecemos el Target (nueva pestaña)
            link.setTarget("_blank");

            // --- FIN DE LA CORRECCIÓN ---

            link.addComponentAsFirst(new Icon(VaadinIcon.MAP_MARKER));
            return link;

        })).setHeader("Ubicación (GPS)");
    }

    private void cargarEjecuciones() {
        // Usamos el nuevo método del servicio que hace el fetch
        grid.setItems(ejecucionTareaService.findAllWithDetails());
    }
}