package com.RafaelDiaz.ClubJudoColombia.vista;

// --- Imports Refactorizados ---
import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.modelo.EjercicioPlanificado;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PlanEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.servicio.EjecucionTareaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * --- VISTA REFACTORIZADA (Página de "Cargar Tareas") ---
 * Esta vista se encarga del Flujo 2 (Acondicionamiento):
 * 1. Ver planes asignados.
 * 2. Ver TAREAS DIARIAS del plan.
 * 3. Marcar tareas como "Completado" (con check + GPS).
 */
@Route("mis-planes")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaPlanView extends VerticalLayout {

    // --- Servicios y Repositorios (Actualizados) ---
    private final SecurityService securityService;
    private final PlanEntrenamientoService planEntrenamientoService;
    private final TraduccionService traduccionService;
    private final EjecucionTareaService ejecucionTareaService; // --- NUEVO SERVICIO ---

    // --- Componentes de UI ---
    private Grid<PlanEntrenamiento> planesGrid = new Grid<>(PlanEntrenamiento.class);
    private Grid<EjercicioPlanificado> tareasGrid = new Grid<>(EjercicioPlanificado.class); // Renombrado
    private Span infoGps = new Span("Obteniendo ubicación...");

    // --- Estado ---
    private Judoka judokaActual;
    private Double latitud;
    private Double longitud;

    public JudokaPlanView(SecurityService securityService,
                          PlanEntrenamientoService planEntrenamientoService,
                          TraduccionService traduccionService,
                          EjecucionTareaService ejecucionTareaService) { // --- Constructor Actualizado ---
        this.securityService = securityService;
        this.planEntrenamientoService = planEntrenamientoService;
        this.traduccionService = traduccionService;
        this.ejecucionTareaService = ejecucionTareaService; // --- NUEVO ---

        setSizeFull();

        // 1. Obtener el Judoka
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error de seguridad: No se pudo encontrar el perfil del Judoka logueado."));

        add(new H1("Mis Planes y Tareas de Acondicionamiento"));
        add(infoGps); // Muestra el estado del GPS

        // 2. Pedir GPS al cargar la vista
        obtenerUbicacion();

        // 3. Configurar Grids
        configurarGridPlanes();
        configurarGridTareas(); // Refactorizado

        // 4. Layout Principal
        HorizontalLayout contenido = new HorizontalLayout(planesGrid, tareasGrid);
        contenido.setSizeFull();

        add(contenido);

        // 5. Cargar datos
        cargarPlanesAsignados();
    }

    private void configurarGridPlanes() {
        planesGrid.setWidth("50%");
        planesGrid.removeAllColumns();
        planesGrid.addColumn(PlanEntrenamiento::getNombre).setHeader("Mis Planes de Entrenamiento").setAutoWidth(true);
        planesGrid.addColumn(plan -> traduccionService.get(plan.getEstado().name())).setHeader("Estado").setAutoWidth(true);
        planesGrid.addColumn(PlanEntrenamiento::getFechaAsignacion).setHeader("Fecha Asignación").setAutoWidth(true);

        // Al seleccionar un plan, cargamos sus tareas
        planesGrid.asSingleSelect().addValueChangeListener(event ->
                cargarTareasDelPlan(event.getValue())
        );
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * Ahora muestra Tareas Diarias y un botón de "Completar".
     */
    private void configurarGridTareas() {
        tareasGrid.setWidth("50%");
        tareasGrid.removeAllColumns();

        // Columna 1: Nombre de la Tarea y Video
        tareasGrid.addComponentColumn(ep -> {
            TareaDiaria tarea = ep.getTareaDiaria(); // Obtenemos la Tarea Diaria
            Span nombreSpan = new Span(tarea.getNombre());

            // Lógica del Video
            if (tarea.getVideoUrl() != null && !tarea.getVideoUrl().isBlank()) {
                Icon videoIcon = new Icon(VaadinIcon.MOVIE);
                videoIcon.getStyle().set("margin-left", "10px").setColor("#1E90FF");
                Anchor link = new Anchor(tarea.getVideoUrl(), videoIcon);
                link.setTarget("_blank");
                return new HorizontalLayout(nombreSpan, link);
            } else {
                return nombreSpan;
            }
        }).setHeader("Tarea de Acondicionamiento").setFlexGrow(1);

        // Columna 2: Meta (ej. "4x15 reps")
        tareasGrid.addColumn(ep -> ep.getTareaDiaria().getMetaTexto())
                .setHeader("Meta")
                .setAutoWidth(true);

        // Columna 3: El "Check" (Botón de Completar)
        tareasGrid.addComponentColumn(ep -> {
            Button checkButton = new Button(new Icon(VaadinIcon.CHECK));
            checkButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            checkButton.addClickListener(e -> completarTarea(ep));
            // (Aquí podríamos añadir lógica para deshabilitarlo si ya se completó hoy)
            return checkButton;
        }).setHeader("Completar").setFlexGrow(0);
    }

    private void cargarPlanesAsignados() {
        if (judokaActual != null) {
            List<PlanEntrenamiento> planes = planEntrenamientoService.buscarPlanesPorJudoka(judokaActual);
            planesGrid.setItems(planes);
        }
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * Filtra la lista de 'ejerciciosPlanificados' para mostrar
     * SOLO las Tareas Diarias (Flujo 2).
     */
    private void cargarTareasDelPlan(PlanEntrenamiento plan) {
        if (plan == null) {
            tareasGrid.setItems(Collections.emptyList());
        } else {
            // Filtramos la lista para obtener solo las tareas (no las pruebas)
            List<EjercicioPlanificado> tareas = plan.getEjerciciosPlanificados().stream()
                    .filter(ep -> ep.getTareaDiaria() != null) // ¡La clave está aquí!
                    .collect(Collectors.toList());
            tareasGrid.setItems(tareas);
        }
    }

    /**
     * --- NUEVO MÉTODO ---
     * Se llama al hacer clic en el botón "check".
     * Crea y guarda una EjecucionTarea con el GPS.
     */
    private void completarTarea(EjercicioPlanificado ejercicioPlan) {
        try {
            EjecucionTarea ejecucion = new EjecucionTarea();
            ejecucion.setJudoka(judokaActual);
            ejecucion.setEjercicioPlanificado(ejercicioPlan);
            ejecucion.setCompletado(true);
            ejecucion.setFechaRegistro(LocalDateTime.now());

            // Asignamos el GPS que obtuvimos al cargar la vista
            ejecucion.setLatitud(this.latitud);
            ejecucion.setLongitud(this.longitud);

            ejecucionTareaService.registrarEjecucion(ejecucion);

            Notification.show(
                    "¡Tarea '" + ejercicioPlan.getTareaDiaria().getNombre() + "' completada!",
                    3000,
                    Notification.Position.MIDDLE
            );

            // (Opcional: refrescar el grid para deshabilitar el botón)

        } catch (Exception e) {
            Notification.show("Error al registrar la tarea: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }


    // --- LÓGICA DE GPS (Movida aquí desde ResultadoForm) ---

    private void obtenerUbicacion() {
        UI.getCurrent().getPage().executeJs(
                "navigator.geolocation.getCurrentPosition(" +
                        "function(pos) {" +
                        "    $0.$server.setUbicacion(pos.coords.latitude, pos.coords.longitude);" +
                        "}, " +
                        "function(err) {" +
                        "    $0.$server.setUbicacionError(err.message);" +
                        "}, " +
                        "{ enableHighAccuracy: true, timeout: 5000, maximumAge: 0 });",
                this.getElement()
        );
    }

    @ClientCallable
    public void setUbicacion(double lat, double lon) {
        this.latitud = lat;
        this.longitud = lon;
        infoGps.setText("Ubicación registrada (Lat: " + lat + ", Lon: " + lon + ")");
        infoGps.getStyle().setColor("green");
    }

    @ClientCallable
    public void setUbicacionError(String errorMensaje) {
        this.latitud = null;
        this.longitud = null;
        infoGps.setText("Error de ubicación: " + errorMensaje);
        infoGps.getStyle().setColor("red");
        Notification.show("No se pudo obtener la ubicación. El registro podría ser marcado como no verificado.", 5000, Notification.Position.MIDDLE);
    }
}