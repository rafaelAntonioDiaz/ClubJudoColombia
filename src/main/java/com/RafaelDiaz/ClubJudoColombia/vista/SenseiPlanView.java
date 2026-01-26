package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.PlanEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
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

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "gestion-planes", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
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
    private Button btnCompletarPlan;

    private CheckboxGroup<DayOfWeek> diasSelector;

    // Campo de clase para el tipo de sesión
    private ComboBox<TipoSesion> tipoSesionCombo;

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
        // i18n: Título traducido
        add(new H1(traduccionService.get("view.sensei.plan.titulo")));

        HorizontalLayout panelSuperior = configurarPanelSuperior();
        HorizontalLayout contenido = configurarContenido();

        add(panelSuperior, contenido);
    }

    private HorizontalLayout configurarPanelSuperior() {
        // i18n: Etiqueta traducida
        tipoSesionCombo = new ComboBox<>(traduccionService.get("lbl.tipo.sesion"));
        tipoSesionCombo.setItems(TipoSesion.values());
        tipoSesionCombo.setValue(TipoSesion.ENTRENAMIENTO);

        // i18n: Traducción automática de Enums usando el servicio
        tipoSesionCombo.setItemLabelGenerator(traduccionService::get);

        tipoSesionCombo.addValueChangeListener(
                event -> {
                    if (planActual != null) {
                        planActual.setTipoSesion(event.getValue());
                    }
                });

        grupoComboBox = new ComboBox<>(traduccionService.get("lbl.seleccionar.grupo"));
        grupoComboBox.setItems(grupoEntrenamientoRepository.findAll());
        grupoComboBox.setItemLabelGenerator(GrupoEntrenamiento::getNombre);

        btnNuevoPlan = new Button(traduccionService.get("btn.crear.plan"), event -> habilitarFormularioPlan());
        nombrePlanField = new TextField(traduccionService.get("lbl.nombre.plan"));
        nombrePlanField.setVisible(false);

        diasSelector = new CheckboxGroup<>();
        diasSelector.setLabel(traduccionService.get("lbl.asignar.dias"));
        diasSelector.setItems(DayOfWeek.values());
        diasSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        diasSelector.setVisible(false);

        // i18n: Traducción automática de Enums (DayOfWeek)
        diasSelector.setItemLabelGenerator(traduccionService::get);

        HorizontalLayout panelSuperior = new HorizontalLayout(grupoComboBox, tipoSesionCombo, btnNuevoPlan, nombrePlanField, diasSelector);
        panelSuperior.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        return panelSuperior;
    }

    private HorizontalLayout configurarContenido() {
        panelIzquierdoBibliotecas = new VerticalLayout();
        // CAMBIO 1: Aseguramos que la biblioteca tenga espacio suficiente
        panelIzquierdoBibliotecas.setWidth("50%");
        panelIzquierdoBibliotecas.setMinWidth("450px"); // Evita que se vuelva ilegible
        panelIzquierdoBibliotecas.setHeightFull();

        configurarGridPruebas();
        configurarGridTareas();

        panelIzquierdoBibliotecas.add(new H3(
                traduccionService.get("menu.biblioteca")), pruebasGrid, tareasGrid);

        panelDerechoPlan = new VerticalLayout();
        panelDerechoPlan.setWidth("50%");
        // CAMBIO 2: Aseguramos que el panel del plan también tenga un mínimo decente
        panelDerechoPlan.setMinWidth("400px");
        panelDerechoPlan.setHeightFull();

        configurarGridPlanActual();

        btnGuardarPlan = new Button(traduccionService.get("btn.guardar"),
                event -> guardarPlan());
        btnGuardarPlan.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        btnCompletarPlan = new Button(traduccionService.get("msg.exito.plan.completado"), event -> completarPlan());
        btnCompletarPlan.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCompletarPlan.setVisible(false);

        HorizontalLayout botonesPlan = new HorizontalLayout(btnGuardarPlan, btnCompletarPlan);

        panelDerechoPlan.add(new H3(traduccionService.get("view.sensei.plan.titulo")), planGrid, botonesPlan);
        panelDerechoPlan.setVisible(false);

        // CAMBIO 3: Configuramos el contenedor principal para usar todo el espacio disponible
        HorizontalLayout layoutPrincipal = new HorizontalLayout(panelIzquierdoBibliotecas, panelDerechoPlan);
        layoutPrincipal.setSizeFull(); // ¡Esto estira la vista a lo ancho y alto!
        layoutPrincipal.setSpacing(true);

        return layoutPrincipal;
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
        }).setHeader(traduccionService.get("col.prueba")).setFlexGrow(1); // i18n: Header

        pruebasGrid.setItems(pruebaEstandarRepository.findAll());
        pruebasGrid.asSingleSelect().addValueChangeListener(event -> addPruebaAPlan(event.getValue()));
    }

    private void configurarGridTareas() {
        tareasGrid = new Grid<>(TareaDiaria.class);
        tareasGrid.setHeight("300px");
        tareasGrid.removeAllColumns();
        // i18n: Headers
        tareasGrid.addColumn(TareaDiaria::getNombre).setHeader(traduccionService.get("col.tarea"));
        tareasGrid.addColumn(TareaDiaria::getMetaTexto).setHeader(traduccionService.get("col.meta"));

        tareasGrid.setItems(tareaDiariaRepository.findAll());
        tareasGrid.asSingleSelect().addValueChangeListener(event -> addTareaAPlan(event.getValue()));
    }

    private void configurarGridPlanActual() {
        planGrid = new Grid<>();
        planGrid.addColumn(ep -> {
            String nombre = "";
            if (ep.getPruebaEstandar() != null) {
                // i18n: Prefijo y nombre traducidos
                nombre = "[" + traduccionService.get("tipo.prueba") + "] " +
                        traduccionService.get(ep.getPruebaEstandar().getNombreKey());
            } else if (ep.getTareaDiaria() != null) {
                // i18n: Prefijo y nombre
                nombre = "[" + traduccionService.get("tipo.tarea") + "] " +
                        ep.getTareaDiaria().getNombre();
            }
            return nombre;
        }).setHeader(traduccionService.get("col.ejercicio")); // i18n: Header

        planGrid.addColumn(ep -> {
            if (ep.getDiasAsignados().isEmpty()) return traduccionService.get("txt.cualquier.dia");
            return ep.getDiasAsignados().stream()
                    // i18n: Traducción y formateo del día
                    .map(d -> traduccionService.get(d).substring(0, 3).toUpperCase())
                    .collect(Collectors.joining(", "));
        }).setHeader(traduccionService.get("col.dias")); // i18n: Header
    }

    private void habilitarFormularioPlan() {
        if (grupoComboBox.getValue() == null) {
            // i18n: Mensaje de error
            Notification.show(traduccionService.get("msg.error.seleccionar.grupo"),
                    3000, Notification.Position.MIDDLE);
            return;
        }
        planActual = new PlanEntrenamiento();
        planActual.setSensei(senseiActual);
        planActual.getGruposAsignados().add(grupoComboBox.getValue());
        ejerciciosDelPlan.clear();
        planGrid.setItems(ejerciciosDelPlan);

        nombrePlanField.setVisible(true);
        diasSelector.setVisible(true);
        diasSelector.setValue(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        panelDerechoPlan.setVisible(true);
        btnNuevoPlan.setEnabled(false);
        btnCompletarPlan.setVisible(false);
    }

    private void addPruebaAPlan(PruebaEstandar prueba) {
        if (planActual == null || prueba == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setPruebaEstandar(prueba);
        nuevaTarea.setPlanEntrenamiento(planActual);
        nuevaTarea.setDiasAsignados(new HashSet<>(diasSelector.getValue()));

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan);
    }

    private void addTareaAPlan(TareaDiaria tarea) {
        if (planActual == null || tarea == null) return;

        EjercicioPlanificado nuevaTarea = new EjercicioPlanificado();
        nuevaTarea.setTareaDiaria(tarea);
        nuevaTarea.setPlanEntrenamiento(planActual);
        nuevaTarea.setDiasAsignados(new HashSet<>(diasSelector.getValue()));

        ejerciciosDelPlan.add(nuevaTarea);
        planGrid.setItems(ejerciciosDelPlan);
    }

    private void guardarPlan() {
        if (nombrePlanField.getValue().isEmpty()) {
            // i18n: Mensaje de validación
            Notification.show(traduccionService.get("msg.error.nombre.vacio"),
                    3000, Notification.Position.MIDDLE);
            return;
        }
        planActual.setNombre(nombrePlanField.getValue());
        planActual.getEjerciciosPlanificados().clear();
        for (EjercicioPlanificado tarea : ejerciciosDelPlan) {
            planActual.addEjercicio(tarea);
        }

        // Guardar tipoSesion antes de persistir
        planActual.setTipoSesion(tipoSesionCombo.getValue());

        try {
            planActual = planEntrenamientoService.guardarPlan(planActual);
            // i18n: Mensaje de éxito
            Notification.show(traduccionService.get("msg.exito.plan.guardado"),
                    3000, Notification.Position.MIDDLE);
            btnCompletarPlan.setVisible(true);
        } catch (Exception e) {
            // i18n: Mensaje de error general
            Notification.show(traduccionService.get("msg.error.general") + ": " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }

    private void completarPlan() {
        if (planActual == null || planActual.getId() == null) return;

        planEntrenamientoService.actualizarEstadoPlan(planActual.getId(), EstadoPlan.COMPLETADO);
        // i18n: Mensaje de éxito
        Notification.show(traduccionService.get("msg.exito.plan.completado"),
                3000, Notification.Position.MIDDLE);
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