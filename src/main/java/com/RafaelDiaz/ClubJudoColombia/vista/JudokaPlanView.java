package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import com.RafaelDiaz.ClubJudoColombia.repositorio.EjecucionTareaRepository; // Necesario para chequear estado
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Route("mis-planes")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Mis Tareas | Club Judo Colombia")
@CssImport("./styles/plan-judoka.css") // Importamos el nuevo CSS
public class JudokaPlanView extends JudokaLayout {

    private final SecurityService securityService;
    private final PlanEntrenamientoService planService;
    private final EjecucionTareaService ejecucionService;
    private final EjecucionTareaRepository ejecucionRepository; // Para consultas rápidas de estado
    private final TraduccionService traduccionService;

    // Componentes UI
    private ComboBox<PlanEntrenamiento> planSelector;
    private ProgressBar barraProgreso;
    private Span textoProgreso;
    private Div tareasContainer; // Contenedor de tarjetas (reemplaza al Grid)

    // Estado
    private Judoka judokaActual;
    private PlanEntrenamiento planSeleccionado;
    private Double latitud;
    private Double longitud;

    @Autowired
    public JudokaPlanView(SecurityService securityService,
                          PlanEntrenamientoService planService,
                          EjecucionTareaService ejecucionService,
                          EjecucionTareaRepository ejecucionRepository,
                          TraduccionService traduccionService,
                          AccessAnnotationChecker accessChecker) {
        super(securityService, accessChecker);
        this.securityService = securityService;
        this.planService = planService;
        this.ejecucionService = ejecucionService;
        this.ejecucionRepository = ejecucionRepository;
        this.traduccionService = traduccionService;

        initJudoka();
        buildUI();
        obtenerUbicacion();
    }

    private void initJudoka() {
        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Judoka no autenticado"));
    }

    private void buildUI() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addClassName("plan-view-container");
        mainLayout.setSpacing(true);
        mainLayout.setPadding(false);

        // 1. Cabecera con Selector de Plan
        H2 titulo = new H2("Entrenamiento de Hoy");
        titulo.getStyle().set("color", "var(--judo-negro)");

        planSelector = new ComboBox<>("Selecciona tu Plan");
        planSelector.setItemLabelGenerator(PlanEntrenamiento::getNombre);
        planSelector.setWidthFull();
        planSelector.addValueChangeListener(e -> {
            this.planSeleccionado = e.getValue();
            cargarTareasDelPlan();
        });

        // 2. Sección de Progreso (Gamificación)
        VerticalLayout progressSection = new VerticalLayout();
        progressSection.addClassName("progress-section");

        textoProgreso = new Span("0% Completado");
        textoProgreso.addClassName("progress-label");

        barraProgreso = new ProgressBar();
        barraProgreso.setValue(0);

        progressSection.add(textoProgreso, barraProgreso);

        // 3. Contenedor de Tarjetas (Checklist)
        tareasContainer = new Div();
        tareasContainer.addClassName("tasks-container");
        tareasContainer.setWidthFull();

        mainLayout.add(titulo, planSelector, progressSection, tareasContainer);
        setContent(mainLayout);

        cargarPlanes();
    }

    private void cargarPlanes() {
        List<PlanEntrenamiento> planes = planService.buscarPlanesPorJudoka(judokaActual);
        planSelector.setItems(planes);

        // Seleccionar el primero activo por defecto
        planes.stream()
                .filter(p -> p.getEstado() == EstadoPlan.ACTIVO)
                .findFirst()
                .ifPresent(planSelector::setValue);
    }

    private void cargarTareasDelPlan() {
        tareasContainer.removeAll();
        barraProgreso.setValue(0);
        textoProgreso.setText("0% Completado");

        if (planSeleccionado == null) return;

        // Filtramos tareas del día actual (ej: MONDAY)
        DayOfWeek hoy = LocalDate.now().getDayOfWeek();

        List<EjercicioPlanificado> tareasHoy = planSeleccionado.getEjerciciosPlanificados().stream()
                .filter(ep -> ep.getTareaDiaria() != null && ep.getDiasAsignados().contains(hoy))
                .collect(Collectors.toList());

        if (tareasHoy.isEmpty()) {
            mostrarMensajeSinTareas();
            return;
        }

        int totalTareas = tareasHoy.size();
        int tareasCompletadas = 0;

        for (EjercicioPlanificado tarea : tareasHoy) {
            // Verificar si ya se hizo hoy
            boolean completada = verificarSiCompletadaHoy(tarea);
            if (completada) tareasCompletadas++;

            // Crear y añadir la tarjeta
            tareasContainer.add(crearTarjetaTarea(tarea, completada));
        }

        actualizarProgreso(tareasCompletadas, totalTareas);
    }

    private Component crearTarjetaTarea(EjercicioPlanificado ep, boolean yaCompletada) {
        Div card = new Div();
        card.addClassName("task-card");
        if (yaCompletada) card.addClassName("completed");

        // Título
        H3 titulo = new H3(ep.getTareaDiaria().getNombre());
        titulo.addClassName("task-title");

        // Meta (Reps)
        Span meta = new Span(ep.getTareaDiaria().getMetaTexto());
        meta.addClassName("task-meta");

        // Video (si existe)
        String videoUrl = ep.getTareaDiaria().getVideoUrl();
        if (videoUrl != null && !videoUrl.isBlank()) {
            Anchor videoLink = new Anchor(videoUrl, new Icon(VaadinIcon.YOUTUBE));
            videoLink.setTarget("_blank");
            videoLink.addClassName("video-link");
            card.add(videoLink);
        }

        // Botón de Acción
        Button btnAccion = new Button(yaCompletada ? "¡Completado!" : "Marcar como Hecho");
        btnAccion.addClassName("btn-completar");

        if (yaCompletada) {
            btnAccion.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btnAccion.setIcon(new Icon(VaadinIcon.CHECK));
            btnAccion.setEnabled(false); // Ya no se puede cliquear
        } else {
            btnAccion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnAccion.addClickListener(e -> {
                completarTarea(ep, btnAccion, card);
            });
        }

        card.add(titulo, meta, btnAccion);
        return card;
    }

    private void completarTarea(EjercicioPlanificado ep, Button btn, Div card) {
        try {
            EjecucionTarea ejecucion = new EjecucionTarea();
            ejecucion.setJudoka(judokaActual);
            ejecucion.setEjercicioPlanificado(ep);
            ejecucion.setCompletado(true);
            ejecucion.setFechaRegistro(LocalDateTime.now());

            // GPS (si está disponible)
            if (latitud != null) ejecucion.setLatitud(latitud);
            if (longitud != null) ejecucion.setLongitud(longitud);

            ejecucionService.registrarEjecucion(ejecucion);

            // Feedback Visual Inmediato
            btn.setText("¡Completado!");
            btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btn.setIcon(new Icon(VaadinIcon.CHECK));
            btn.setEnabled(false);
            card.addClassName("completed");

            // Actualizar barra de progreso
            actualizarProgresoDinamico();

            Notification.show("¡Excelente trabajo!", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean verificarSiCompletadaHoy(EjercicioPlanificado ep) {
        LocalDate hoy = LocalDate.now();
        return ejecucionRepository.countByJudokaAndEjercicioPlanificadoAndFechaRegistroBetween(
                judokaActual,
                ep,
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay()
        ) > 0;
    }

    private void actualizarProgreso(int completadas, int total) {
        double valor = (double) completadas / total;
        barraProgreso.setValue(valor);
        int porcentaje = (int) (valor * 100);
        textoProgreso.setText(porcentaje + "% Completado del Día");

        if (porcentaje == 100) {
            textoProgreso.setText("¡Entrenamiento del día finalizado! 🥋🔥");
            textoProgreso.getStyle().set("color", "var(--judo-success)");
        }
    }

    private void actualizarProgresoDinamico() {
        // Recalcular visualmente (truco rápido sin recargar DB)
        double actual = barraProgreso.getValue();
        // Asumimos que avanzó 1 paso. (Nota: Para precisión total, recargar desde DB)
        // Pero para UX fluida, incrementamos basándonos en el total visual.
        // ... (Implementación simple: recargar todo el grid es más seguro)
        cargarTareasDelPlan();
    }

    private void mostrarMensajeSinTareas() {
        Div box = new Div();
        box.addClassName("empty-state-card"); // Reusamos estilo del dashboard
        box.setText("Hoy es día de descanso. ¡Recupérate!");
        tareasContainer.add(box);
    }

    // GPS
    private void obtenerUbicacion() {
        UI.getCurrent().getPage().executeJs(
                "navigator.geolocation.getCurrentPosition(" +
                        "  pos => $0.$server.setUbicacion(pos.coords.latitude, pos.coords.longitude)," +
                        "  err => $0.$server.setUbicacionError(err.message)" +
                        ")", this);
    }

    @ClientCallable
    public void setUbicacion(double lat, double lon) {
        this.latitud = lat;
        this.longitud = lon;
    }

    @ClientCallable
    public void setUbicacionError(String msg) {
        System.out.println("GPS Error: " + msg);
    }
}