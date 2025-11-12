package com.RafaelDiaz.ClubJudoColombia.vista;

// --- Imports de Modelo (Refactorizados) ---
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.modelo.EjercicioPlanificado;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.PlanEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
// --- Repositorios (Refactorizados) ---
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
// --- Servicios ---
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
// --- Componentes UI ---
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashSet;
import java.util.Set;

/**
 * --- VISTA REFACTORIZADA ---
 * Ahora permite al Sensei crear planes mezclando
 * Pruebas Estándar (para evaluar) y Tareas Diarias (para entrenar).
 */
@Route("gestion-planes")
@RolesAllowed("ROLE_SENSEI")
public class SenseiPlanView extends VerticalLayout {

    // --- Servicios y Repositorios (Actualizados) ---
    private final PlanEntrenamientoService planEntrenamientoService;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository; // Refactorizado
    private final TareaDiariaRepository tareaDiariaRepository; // --- NUEVO ---
    private final TraduccionService traduccionService;
    private final SecurityService securityService;

    // --- Componentes de UI (Actualizados) ---
    private ComboBox<GrupoEntrenamiento> grupoComboBox;
    private Grid<PruebaEstandar> pruebasGrid; // Grid para Pruebas (Flujo 1)
    private Grid<TareaDiaria> tareasGrid; // Grid para Tareas (Flujo 2)
    private VerticalLayout panelIzquierdoBibliotecas; // Contenedor para los 2 grids
    private VerticalLayout panelDerechoPlan; // Contenedor para el plan actual
    private Button btnNuevoPlan;
    private TextField nombrePlanField;
    private Grid<EjercicioPlanificado> planGrid; // Grid para el plan actual
    private Button btnGuardarPlan;

    // --- Estado ---
    private PlanEntrenamiento planActual;
    private Sensei senseiActual;
    private Set<EjercicioPlanificado> ejerciciosDelPlan = new HashSet<>();

    /**
     * --- CONSTRUCTOR ACTUALIZADO ---
     */
    public SenseiPlanView(PlanEntrenamientoService planEntrenamientoService,
                          GrupoEntrenamientoRepository grupoEntrenamientoRepository,
                          PruebaEstandarRepository pruebaEstandarRepository,
                          TareaDiariaRepository tareaDiariaRepository, // --- NUEVO ---
                          TraduccionService traduccionService,
                          SecurityService securityService) {
        this.planEntrenamientoService = planEntrenamientoService;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.tareaDiariaRepository = tareaDiariaRepository; // --- NUEVO ---
        this.traduccionService = traduccionService;
        this.securityService = securityService;

        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Error de seguridad: No se pudo encontrar el perfil del Sensei logueado."));

        setSizeFull();
        add(new H1("Gestión de Planes de Entrenamiento"));

        HorizontalLayout panelSuperior = configurarPanelSuperior();
        HorizontalLayout contenido = configurarContenido();

        add(panelSuperior, contenido);
    }

    private HorizontalLayout configurarPanelSuperior() {
        // ... (Este método no cambia) ...
        grupoComboBox = new ComboBox<>("Seleccionar Grupo");
        grupoComboBox.setItems(grupoEntrenamientoRepository.findAll());
        grupoComboBox.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        btnNuevoPlan = new Button("Crear Nuevo Plan", event -> habilitarFormularioPlan());
        nombrePlanField = new TextField("Nombre del Plan");
        nombrePlanField.setVisible(false);
        HorizontalLayout panelSuperior = new HorizontalLayout(grupoComboBox, btnNuevoPlan, nombrePlanField);
        panelSuperior.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return panelSuperior;
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * Ahora crea dos Grids a la izquierda (Pruebas y Tareas).
     */
    private HorizontalLayout configurarContenido() {

        // --- Panel Izquierdo (Bibliotecas) ---
        panelIzquierdoBibliotecas = new VerticalLayout();
        panelIzquierdoBibliotecas.setWidth("50%");
        panelIzquierdoBibliotecas.setHeightFull();

        // 1. Grid de Pruebas Estándar (SJFT, Salto, etc.)
        configurarGridPruebas();

        // 2. Grid de Tareas Diarias (Flexiones, etc.)
        configurarGridTareas();

        panelIzquierdoBibliotecas.add(new H3("Biblioteca de Pruebas (Evaluación)"), pruebasGrid,
                new H3("Biblioteca de Tareas (Acondicionamiento)"), tareasGrid);

        // --- Panel Derecho (Plan Actual) ---
        panelDerechoPlan = new VerticalLayout();
        panelDerechoPlan.setWidth("50%");

        configurarGridPlanActual(); // Configura el grid de la derecha

        btnGuardarPlan = new Button("Guardar Plan", event -> guardarPlan());

        panelDerechoPlan.add(new H3("Plan Actual"), planGrid, btnGuardarPlan);
        panelDerechoPlan.setVisible(false); // Oculto al inicio

        return new HorizontalLayout(panelIzquierdoBibliotecas, panelDerechoPlan);
    }

    private void configurarGridPruebas() {
        pruebasGrid = new Grid<>(PruebaEstandar.class);
        pruebasGrid.setHeight("300px");
        pruebasGrid.removeAllColumns();

        pruebasGrid.addComponentColumn(prueba -> {
            String nombre = traduccionService.get(prueba.getNombreKey());
            Span nombreSpan = new Span(nombre);
            if (prueba.getVideoUrl() != null && !prueba.getVideoUrl().isBlank()) {
                Icon videoIcon = new Icon(VaadinIcon.MOVIE);
                videoIcon.getStyle().set("margin-left", "10px").setColor("#1E90FF");
                Anchor link = new Anchor(prueba.getVideoUrl(), videoIcon);
                link.setTarget("_blank");
                return new HorizontalLayout(nombreSpan, link);
            } else {
                return nombreSpan;
            }
        }).setHeader("Prueba (Haga clic para añadir)").setFlexGrow(1);

        pruebasGrid.setItems(pruebaEstandarRepository.findAll());
        pruebasGrid.asSingleSelect().addValueChangeListener(event ->
                addPruebaAPlan(event.getValue()) // Llama al nuevo método
        );
    }

    private void configurarGridTareas() {
        tareasGrid = new Grid<>(TareaDiaria.class);
        tareasGrid.setHeight("300px");
        tareasGrid.removeAllColumns();

        tareasGrid.addColumn(TareaDiaria::getNombre)
                .setHeader("Tarea (Haga clic para añadir)")
                .setAutoWidth(true);

        tareasGrid.addColumn(TareaDiaria::getMetaTexto)
                .setHeader("Meta")
                .setAutoWidth(true);

        tareasGrid.asSingleSelect().addValueChangeListener(event ->
                addTareaAPlan(event.getValue()) // Llama al nuevo método
        );

        // Cargamos las tareas que el Sensei ha creado (desde BibliotecaView)
        tareasGrid.setItems(tareaDiariaRepository.findAll());
    }

    /**
     * --- MÉTODO REFACTORIZADO ---
     * El Grid de la derecha ahora debe mostrar inteligentemente
     * si el item es una Prueba o una Tarea.
     */
    private void configurarGridPlanActual() {
        planGrid = new Grid<>();
        planGrid.addColumn(ep -> {
            if (ep.getPruebaEstandar() != null) {
                // Es una Prueba
                return "[Prueba] " + traduccionService.get(ep.getPruebaEstandar().getNombreKey());
            } else if (ep.getTareaDiaria() != null) {
                // Es una Tarea
                return "[Tarea] " + ep.getTareaDiaria().getNombre();
            }
            return "Item inválido";
        }).setHeader("Ejercicios en este Plan");
    }

    private void habilitarFormularioPlan() {
        if (grupoComboBox.getValue() == null) {
            Notification.show("Por favor, seleccione un grupo primero.", 3000, Notification.Position.MIDDLE);
            return;
        }
        planActual = new PlanEntrenamiento();
        planActual.setSensei(senseiActual);
        planActual.getGruposAsignados().add(grupoComboBox.getValue());
        ejerciciosDelPlan.clear();
        planGrid.setItems(ejerciciosDelPlan);
        nombrePlanField.setVisible(true);
        panelDerechoPlan.setVisible(true); // Panel derecho
        btnNuevoPlan.setEnabled(false);
    }

    /**
     * --- NUEVO MÉTODO ---
     * Se llama al hacer clic en el Grid de Pruebas.
     */
    private void addPruebaAPlan(PruebaEstandar prueba) {
        if (planActual == null) {
            Notification.show("Por favor, cree un 'Nuevo Plan' antes de añadir.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (prueba == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setPruebaEstandar(prueba); // <-- Vincula a la PRUEBA
        nuevaTarea.setPlanEntrenamiento(planActual);

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan); // Refresca el Grid de la derecha
    }

    /**
     * --- NUEVO MÉTODO ---
     * Se llama al hacer clic en el Grid de Tareas.
     */
    private void addTareaAPlan(TareaDiaria tarea) {
        if (planActual == null) {
            Notification.show("Por favor, cree un 'Nuevo Plan' antes de añadir.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (tarea == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setTareaDiaria(tarea); // <-- Vincula a la TAREA
        nuevaTarea.setPlanEntrenamiento(planActual);

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan); // Refresca el Grid de la derecha
    }

    private void guardarPlan() {
        // ... (Este método no cambia, gracias a Cascade.ALL) ...
        if (nombrePlanField.getValue().isEmpty()) {
            Notification.show("Por favor, ingrese un nombre para el plan.", 3000, Notification.Position.MIDDLE);
            return;
        }
        if (ejerciciosDelPlan.isEmpty()) {
            Notification.show("Por favor, añada al menos un ejercicio o prueba al plan.", 3000, Notification.Position.MIDDLE);
            return;
        }
        planActual.setNombre(nombrePlanField.getValue());
        for (EjercicioPlanificado tarea : ejerciciosDelPlan) {
            planActual.addEjercicio(tarea);
        }
        try {
            planEntrenamientoService.guardarPlan(planActual);
            Notification.show("¡Plan '" + planActual.getNombre() + "' guardado con éxito!", 3000, Notification.Position.MIDDLE);
            resetearVista();
        } catch (Exception e) {
            Notification.show("Error al guardar el plan: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void resetearVista() {
        planActual = null;
        ejerciciosDelPlan.clear();
        planGrid.setItems(ejerciciosDelPlan);
        panelDerechoPlan.setVisible(false);
        nombrePlanField.setVisible(false);
        nombrePlanField.clear();
        grupoComboBox.clear();
        btnNuevoPlan.setEnabled(true);
    }
}