package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan; // Importante
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup; // --- NUEVO ---
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant; // --- NUEVO ---
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.DayOfWeek; // --- NUEVO ---
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Route("gestion-planes")
@RolesAllowed("ROLE_SENSEI")
public class SenseiPlanView extends VerticalLayout {

    private final PlanEntrenamientoService planEntrenamientoService;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final TareaDiariaRepository tareaDiariaRepository;
    private final TraduccionService traduccionService;
    private final SecurityService securityService;

    private ComboBox<GrupoEntrenamiento> grupoComboBox;
    private Grid<PruebaEstandar> pruebasGrid;
    private Grid<TareaDiaria> tareasGrid;
    private VerticalLayout panelIzquierdoBibliotecas;
    private VerticalLayout panelDerechoPlan;
    private Button btnNuevoPlan;
    private TextField nombrePlanField;
    private Grid<EjercicioPlanificado> planGrid;
    private Button btnGuardarPlan;
    private Button btnCompletarPlan; // --- NUEVO BOTÓN ---

    // --- NUEVO COMPONENTE: Selector de Días ---
    private CheckboxGroup<DayOfWeek> diasSelector;

    private PlanEntrenamiento planActual;
    private Sensei senseiActual;
    private Set<EjercicioPlanificado> ejerciciosDelPlan = new HashSet<>();

    public SenseiPlanView(PlanEntrenamientoService planEntrenamientoService,
                          GrupoEntrenamientoRepository grupoEntrenamientoRepository,
                          PruebaEstandarRepository pruebaEstandarRepository,
                          TareaDiariaRepository tareaDiariaRepository,
                          TraduccionService traduccionService,
                          SecurityService securityService) {
        this.planEntrenamientoService = planEntrenamientoService;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.tareaDiariaRepository = tareaDiariaRepository;
        this.traduccionService = traduccionService;
        this.securityService = securityService;

        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Error de seguridad"));

        setSizeFull();
        add(new H1("Gestión de Planes de Entrenamiento"));

        HorizontalLayout panelSuperior = configurarPanelSuperior();
        HorizontalLayout contenido = configurarContenido();

        add(panelSuperior, contenido);
    }

    private HorizontalLayout configurarPanelSuperior() {
        grupoComboBox = new ComboBox<>("Seleccionar Grupo");
        grupoComboBox.setItems(grupoEntrenamientoRepository.findAll());
        grupoComboBox.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        btnNuevoPlan = new Button("Crear Nuevo Plan", event -> habilitarFormularioPlan());
        nombrePlanField = new TextField("Nombre del Plan");
        nombrePlanField.setVisible(false);

        // --- Selector de Días (MODIFICADO) ---
        diasSelector = new CheckboxGroup<>();
        diasSelector.setLabel("Asignar para los días:");
        diasSelector.setItems(DayOfWeek.values());
        diasSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        diasSelector.setVisible(false);

        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
        // Usamos el servicio de traducción. day.name() devuelve "MONDAY", "TUESDAY", etc.
        diasSelector.setItemLabelGenerator(day ->
                traduccionService.get(day.name()) // Llama al TraduccionService
        );

        HorizontalLayout panelSuperior = new HorizontalLayout(grupoComboBox, btnNuevoPlan, nombrePlanField, diasSelector);
        panelSuperior.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return panelSuperior;
    }
    private HorizontalLayout configurarContenido() {
        panelIzquierdoBibliotecas = new VerticalLayout();
        panelIzquierdoBibliotecas.setWidth("50%");
        panelIzquierdoBibliotecas.setHeightFull();

        configurarGridPruebas();
        configurarGridTareas();

        panelIzquierdoBibliotecas.add(new H3(
                "Bibliotecas (Seleccione días arriba primero)"), pruebasGrid, tareasGrid);

        panelDerechoPlan = new VerticalLayout();
        panelDerechoPlan.setWidth("50%");

        configurarGridPlanActual();

        btnGuardarPlan = new Button("Guardar Cambios",
                event -> guardarPlan());
        btnGuardarPlan.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // --- NUEVO: Botón para Completar ---
        btnCompletarPlan = new Button("Marcar Plan como COMPLETADO", event -> completarPlan());
        btnCompletarPlan.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCompletarPlan.setVisible(false); // Solo visible si el plan ya existe

        HorizontalLayout botonesPlan = new HorizontalLayout(btnGuardarPlan, btnCompletarPlan);

        panelDerechoPlan.add(new H3("Plan Actual"), planGrid, botonesPlan);
        panelDerechoPlan.setVisible(false);

        return new HorizontalLayout(panelIzquierdoBibliotecas, panelDerechoPlan);
    }

    // ... (configurarGridPruebas y configurarGridTareas igual que antes, llaman a add...APlan) ...
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
        }).setHeader("Prueba").setFlexGrow(1);
        pruebasGrid.setItems(pruebaEstandarRepository.findAll());
        pruebasGrid.asSingleSelect().addValueChangeListener(event -> addPruebaAPlan(event.getValue()));
    }

    private void configurarGridTareas() {
        tareasGrid = new Grid<>(TareaDiaria.class);
        tareasGrid.setHeight("300px");
        tareasGrid.removeAllColumns();
        tareasGrid.addColumn(TareaDiaria::getNombre).setHeader("Tarea");
        tareasGrid.addColumn(TareaDiaria::getMetaTexto).setHeader("Meta");
        tareasGrid.setItems(tareaDiariaRepository.findAll());
        tareasGrid.asSingleSelect().addValueChangeListener(event -> addTareaAPlan(event.getValue()));
    }

    private void configurarGridPlanActual() {
        planGrid = new Grid<>();
        planGrid.addColumn(ep -> {
            String nombre = "";
            if (ep.getPruebaEstandar() != null) {
                nombre = "[Prueba] " + traduccionService.get(ep.getPruebaEstandar().getNombreKey());
            } else if (ep.getTareaDiaria() != null) {
                nombre = "[Tarea] " + ep.getTareaDiaria().getNombre();
            }
            return nombre;
        }).setHeader("Ejercicio");

        // --- Columna para mostrar los días ---
        planGrid.addColumn(ep -> {
            if (ep.getDiasAsignados().isEmpty()) return "Cualquier día";
            return ep.getDiasAsignados().stream()
                    .map(d -> d.name().substring(0, 3)) // MON, TUE
                    .collect(Collectors.joining(", "));
        }).setHeader("Días");
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
        diasSelector.setVisible(true); // Mostrar selector
        diasSelector.setValue(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)); // Default

        panelDerechoPlan.setVisible(true);
        btnNuevoPlan.setEnabled(false);
        btnCompletarPlan.setVisible(false); // No se puede completar un plan nuevo
    }

    private void addPruebaAPlan(PruebaEstandar prueba) {
        if (planActual == null || prueba == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setPruebaEstandar(prueba);
        nuevaTarea.setPlanEntrenamiento(planActual);
        // --- ASIGNAR DÍAS ---
        nuevaTarea.setDiasAsignados(new HashSet<>(diasSelector.getValue()));

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan);
    }

    private void addTareaAPlan(TareaDiaria tarea) {
        if (planActual == null || tarea == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setTareaDiaria(tarea);
        nuevaTarea.setPlanEntrenamiento(planActual);
        // --- ASIGNAR DÍAS ---
        nuevaTarea.setDiasAsignados(new HashSet<>(diasSelector.getValue()));

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan);
    }

    private void guardarPlan() {
        if (nombrePlanField.getValue().isEmpty()) {
            Notification.show("Ingrese nombre.", 3000, Notification.Position.MIDDLE);
            return;
        }
        planActual.setNombre(nombrePlanField.getValue());
        // Limpiar y re-agregar para asegurar integridad (en edición real sería diferente)
        planActual.getEjerciciosPlanificados().clear();
        for (EjercicioPlanificado tarea : ejerciciosDelPlan) {
            planActual.addEjercicio(tarea);
        }
        try {
            planActual = planEntrenamientoService.guardarPlan(planActual); // Guardar y actualizar referencia
            Notification.show("Plan guardado.", 3000, Notification.Position.MIDDLE);

            // Ahora que está guardado, mostramos el botón completar si aplica
            btnCompletarPlan.setVisible(true);

        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    /**
     * --- NUEVO MÉTODO ---
     * Marca el plan como COMPLETADO.
     */
    private void completarPlan() {
        if (planActual == null || planActual.getId() == null) return;

        planEntrenamientoService.actualizarEstadoPlan(planActual.getId(), EstadoPlan.COMPLETADO);
        Notification.show("Plan marcado como COMPLETADO.", 3000, Notification.Position.MIDDLE);
        resetearVista();
    }

    private void resetearVista() {
        planActual = null;
        ejerciciosDelPlan.clear();
        planGrid.setItems(ejerciciosDelPlan);
        panelDerechoPlan.setVisible(false);
        nombrePlanField.setVisible(false);
        diasSelector.setVisible(false);
        nombrePlanField.clear();
        grupoComboBox.clear();
        btnNuevoPlan.setEnabled(true);
    }
}