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
// --- Imports de UI ---
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select; // --- NUEVO IMPORT ---
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.DayOfWeek; // --- NUEVO IMPORT ---
import java.time.LocalDate; // --- NUEVO IMPORT ---
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Route("mis-planes")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
public class JudokaPlanView extends VerticalLayout {

    // --- Servicios ---
    private final SecurityService securityService;
    private final PlanEntrenamientoService planEntrenamientoService;
    private final TraduccionService traduccionService;
    private final EjecucionTareaService ejecucionTareaService;

    // --- Componentes de UI ---
    private Grid<PlanEntrenamiento> planesGrid = new Grid<>(PlanEntrenamiento.class);
    private Grid<EjercicioPlanificado> tareasGrid = new Grid<>(EjercicioPlanificado.class);
    private Span infoGps = new Span("Obteniendo ubicación...");
    private Select<String> filtroDia; // --- NUEVO FILTRO ---

    // --- Estado ---
    private Judoka judokaActual;
    private PlanEntrenamiento planSeleccionado; // --- NUEVO (para guardar estado) ---
    private final DayOfWeek diaActual = LocalDate.now().getDayOfWeek(); // --- NUEVO ---
    private Double latitud;
    private Double longitud;

    public JudokaPlanView(SecurityService securityService,
                          PlanEntrenamientoService planEntrenamientoService,
                          TraduccionService traduccionService,
                          EjecucionTareaService ejecucionTareaService) {
        this.securityService = securityService;
        this.planEntrenamientoService = planEntrenamientoService;
        this.traduccionService = traduccionService;
        this.ejecucionTareaService = ejecucionTareaService;

        setSizeFull();

        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error de seguridad: No se pudo encontrar el perfil del Judoka logueado."));

        add(new H1("Mis Planes y Tareas de Acondicionamiento"));
        add(infoGps);
        obtenerUbicacion();

        // --- NUEVO: Configurar Filtro de Día ---
        configurarFiltroDia();
        add(filtroDia);

        // Configurar Grids
        configurarGridPlanes();
        configurarGridTareas();

        HorizontalLayout contenido = new HorizontalLayout(planesGrid, tareasGrid);
        contenido.setSizeFull();
        add(contenido);

        cargarPlanesAsignados();
    }

    private void configurarFiltroDia() {
        filtroDia = new Select<>();
        filtroDia.setLabel("Mostrar Tareas:");
        filtroDia.setItems("Tareas para Hoy (" + traduccionService.get(diaActual.name()) + ")", "Todas las Tareas del Plan");
        filtroDia.setValue("Tareas para Hoy (" + traduccionService.get(diaActual.name()) + ")");

        // Cuando el filtro cambia, refrescamos el grid de tareas
        filtroDia.addValueChangeListener(e -> cargarTareasDelPlan(planSeleccionado));
    }

    private void configurarGridPlanes() {
        planesGrid.setWidth("50%");
        planesGrid.removeAllColumns();
        planesGrid.addColumn(PlanEntrenamiento::getNombre).setHeader("Mis Planes de Entrenamiento").setAutoWidth(true);
        planesGrid.addColumn(plan -> traduccionService.get(plan.getEstado().name())).setHeader("Estado").setAutoWidth(true);
        planesGrid.addColumn(PlanEntrenamiento::getFechaAsignacion).setHeader("Fecha Asignación").setAutoWidth(true);

        planesGrid.asSingleSelect().addValueChangeListener(event -> {
            this.planSeleccionado = event.getValue(); // Guardamos el plan seleccionado
            cargarTareasDelPlan(planSeleccionado); // Cargamos sus tareas
        });
    }

    /**
     * --- MÉTODO REFACTORIZADO (CON CHECKBOX REAL) ---
     * Ahora muestra un Checkbox en lugar de un botón.
     * El guardado se dispara automáticamente al marcarlo.
     */
    private void configurarGridTareas() {
        tareasGrid.setWidth("50%");
        tareasGrid.removeAllColumns();

        // Columna 1: Nombre de la Tarea y Video (Sin cambios)
        tareasGrid.addComponentColumn(ep -> {
            TareaDiaria tarea = ep.getTareaDiaria();
            Span nombreSpan = new Span(tarea.getNombre());
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

        // Columna 2: Meta (ej. "4x15 reps") (Sin cambios)
        tareasGrid.addColumn(ep -> ep.getTareaDiaria().getMetaTexto())
                .setHeader("Meta")
                .setAutoWidth(true);

        // --- COLUMNA 3 (CHECKBOX) REFACTORIZADA ---
        tareasGrid.addComponentColumn(ep -> {
            // 1. Creamos un Checkbox real
            Checkbox check = new Checkbox();

            // (En el futuro, aquí pondríamos la lógica para ver si ya está completado)
            // ej. boolean completadoHoy = ...;
            // check.setValue(completadoHoy);
            // check.setEnabled(!completadoHoy);

            // 2. Añadimos el "listener" (oyente)
            // Cuando el valor cambia (el usuario hace clic)...
            check.addValueChangeListener(event -> {
                if (event.getValue() == true) { // Si el usuario lo marcó (no si lo desmarcó)
                    try {
                        // Llamamos al método que guarda en BD
                        completarTarea(ep);

                        // Damos feedback visual inmediato
                        check.setEnabled(false); // Deshabilitamos el checkbox
                        // (La notificación ya la muestra 'completarTarea')

                    } catch (Exception ex) {
                        // Si falla (ej. error de GPS), revertimos el check
                        check.setValue(false);
                    }
                }
            });
            return check;
        }).setHeader("Ejecución").setFlexGrow(0); // Cambiamos el Header
    }
    private void cargarPlanesAsignados() {
        if (judokaActual != null) {
            List<PlanEntrenamiento> planes = planEntrenamientoService.buscarPlanesPorJudoka(judokaActual);
            planesGrid.setItems(planes);
        }
    }

    /**
     * --- ¡MÉTODO REFACTORIZADO CON FILTRO! ---
     * Filtra la lista de 'ejerciciosPlanificados' para mostrar
     * SOLO Tareas Diarias y (opcionalmente) filtra por el día de hoy.
     */
    /**
     * --- ¡MÉTODO REFACTORIZADO CON FILTRO CORREGIDO! ---
     * Filtra la lista de 'ejerciciosPlanificados' para mostrar
     * SOLO Tareas Diarias y (opcionalmente) filtra por el día de hoy.
     */
    private void cargarTareasDelPlan(PlanEntrenamiento plan) {
        if (plan == null) {
            tareasGrid.setItems(Collections.emptyList());
            return;
        }

        // 1. Filtramos solo las Tareas Diarias (Flujo 2)
        List<EjercicioPlanificado> todasLasTareas = plan.getEjerciciosPlanificados().stream()
                .filter(ep -> ep.getTareaDiaria() != null)
                .collect(Collectors.toList());

        // 2. Aplicamos el filtro de día
        String filtroActual = filtroDia.getValue();
        if (filtroActual != null && filtroActual.startsWith("Tareas para Hoy")) {

            List<EjercicioPlanificado> tareasDeHoy = todasLasTareas.stream()
                    .filter(ep ->
                            // --- ¡LÓGICA CORREGIDA! ---
                            // Solo muéstralo si la lista de días asignados
                            // contiene el día actual.
                            ep.getDiasAsignados().contains(diaActual)
                    )
                    .collect(Collectors.toList());
            tareasGrid.setItems(tareasDeHoy);

        } else {
            // Mostramos todas las tareas del plan
            tareasGrid.setItems(todasLasTareas);
        }
    }
    /**
     * --- MÉTODO MODIFICADO ---
     * Ahora también actualiza el estado del Plan a "EN PROGRESO".
     */
    private void completarTarea(EjercicioPlanificado ejercicioPlan) {
        try {
            // 1. Guardar la ejecución (como antes)
            EjecucionTarea ejecucion = new EjecucionTarea();
            ejecucion.setJudoka(judokaActual);
            ejecucion.setEjercicioPlanificado(ejercicioPlan);
            ejecucion.setCompletado(true);
            ejecucion.setFechaRegistro(LocalDateTime.now());
            ejecucion.setLatitud(this.latitud);
            ejecucion.setLongitud(this.longitud);
            ejecucionTareaService.registrarEjecucion(ejecucion);

            // 2. Actualizar el estado del Plan
            PlanEntrenamiento planPadre = ejercicioPlan.getPlanEntrenamiento();
            if (planPadre.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan.PENDIENTE) {
                planEntrenamientoService.actualizarEstadoPlan(planPadre.getId(), com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan.EN_PROGRESO);
                // Refrescamos el grid de planes
                cargarPlanesAsignados();
            }

            Notification.show(
                    "¡Tarea '" + ejercicioPlan.getTareaDiaria().getNombre() + "' completada!",
                    3000,
                    Notification.Position.MIDDLE
            );

        } catch (Exception e) {
            Notification.show("Error al registrar la tarea: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    // --- (Lógica de GPS: obtenerUbicacion, setUbicacion, setUbicacionError no cambian) ---
    private void obtenerUbicacion() { /* ... */ }
    @ClientCallable
    public void setUbicacion(double lat, double lon) { /* ... */ }
    @ClientCallable
    public void setUbicacionError(String errorMensaje) { /* ... */ }
}