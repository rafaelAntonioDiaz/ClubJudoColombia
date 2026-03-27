package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MesocicloATC;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.MicrocicloService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PruebaEstandarService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "microciclos", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiMicrociclosView extends VerticalLayout {

    private final MicrocicloService planService;
    private final TraduccionService traduccionService;
    private final PruebaEstandarService pruebaService;
    private final SecurityService securityService;
    private final GrupoEntrenamientoRepository grupoRepo;
    private final PruebaEstandarRepository pruebaRepo;
    private final TareaDiariaRepository tareaRepo;
    private final MacrocicloRepository macrocicloRepo;
    private final JudokaRepository judokaRepository;
    private List<Judoka> judokasDeLosGrupos = new ArrayList<>();
    private ProgressBar barraProgresoGlobal;
    private Span labelTiempoTotal;
    // --- MÁQUINA DE ESTADOS ---
    private VerticalLayout layoutDashboard;
    private VerticalLayout layoutEditor;
    private Sensei senseiActual;

    // --- COMPONENTES DASHBOARD ---
    private Grid<Microciclo> gridHistorial;

    // --- COMPONENTES EDITOR (FASE 4) ---
    private Microciclo planActual;
    private List<EjercicioPlanificado> ejerciciosDelPlan = new ArrayList<>();

    private TextField nombrePlanField;
    private Span intensidadGlobalLabel;
    private MultiSelectComboBox<GrupoEntrenamiento> gruposCombo;
    private DatePicker fechaInicioPicker;
    private DatePicker fechaFinPicker;
    private ComboBox<MesocicloATC> faseAtcCombo;
    private ComboBox<TipoMicrociclo> tipoMicrocicloCombo;
    private ComboBox<Macrociclo> macrocicloCombo;

    private Grid<TareaDiaria> gridTareas;
    private Grid<PruebaEstandar> gridPruebas;
    private Grid<EjercicioPlanificado> gridPlan;

    public SenseiMicrociclosView(MicrocicloService planService,
                                 GrupoEntrenamientoRepository grupoRepo,
                                 PruebaEstandarRepository pruebaRepo,
                                 TareaDiariaRepository tareaRepo,
                                 TraduccionService traduccionService, PruebaEstandarService pruebaService,
                                 SecurityService securityService,
                                 MacrocicloRepository macrocicloRepo, JudokaRepository judokaRepository) {
        this.planService = planService;
        this.grupoRepo = grupoRepo;
        this.pruebaRepo = pruebaRepo;
        this.tareaRepo = tareaRepo;
        this.traduccionService = traduccionService;
        this.pruebaService = pruebaService;
        this.securityService = securityService;

        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Error: Sensei no encontrado"));
        this.macrocicloRepo = macrocicloRepo;
        this.judokaRepository = judokaRepository;

        setSizeFull();
        setPadding(false);

        configurarDashboard();
        configurarEditor();

        add(layoutDashboard, layoutEditor);
        mostrarDashboard();
    }

    // =================================================================================
    // ESTADO 1: DASHBOARD (HISTORIAL)
    // =================================================================================
    private void configurarDashboard() {
        layoutDashboard = new VerticalLayout();
        layoutDashboard.setSizeFull();
        layoutDashboard.setPadding(true);

        HorizontalLayout cabecera = new HorizontalLayout();
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecera.setAlignItems(Alignment.BASELINE);
        barraProgresoGlobal = new ProgressBar();
        barraProgresoGlobal.setValue(0);
        barraProgresoGlobal.setWidth("200px");

        labelTiempoTotal = new Span("Tiempo ocupado: 0 / 120 min");
        labelTiempoTotal.getStyle().set("font-weight", "bold");
        labelTiempoTotal.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Button btnNuevo = new Button("Nuevo Microciclo", new Icon(VaadinIcon.PLUS));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> mostrarEditor(null));

        cabecera.add(new H1("Historial de Microciclos"), btnNuevo);

        gridHistorial = new Grid<>(Microciclo.class, false);
        gridHistorial.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES);

        gridHistorial.addColumn(Microciclo::getNombre).setHeader("Nombre").setFlexGrow(1);

        gridHistorial.addColumn(plan -> plan.getGruposAsignados().stream()
                        .map(GrupoEntrenamiento::getNombre).collect(Collectors.joining(", ")))
                .setHeader("Grupo(s)").setAutoWidth(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM");
        gridHistorial.addColumn(plan -> (plan.getFechaInicio() != null && plan.getFechaFin() != null)
                        ? plan.getFechaInicio().format(formatter) + " al " + plan.getFechaFin().format(formatter) : "Sin fechas")
                .setHeader("Fechas").setAutoWidth(true);

        gridHistorial.addComponentColumn(plan -> crearBadgeATC(plan.getMesocicloATC())).setHeader("Fase (ATC)").setAutoWidth(true);
        gridHistorial.addColumn(plan -> plan.getTipoMicrociclo() != null ? plan.getTipoMicrociclo().getDescripcion() : "").setHeader("Tipo");

        // --- NUEVO: COLUMNA DE INDICADOR DE EVALUACIONES ---
        gridHistorial.addComponentColumn(plan -> {
            if (plan.getPruebas() != null && !plan.getPruebas().isEmpty()) {
                Icon icon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
                icon.setColor("var(--lumo-success-text-color)");
                icon.setTooltipText("Contiene evaluaciones");
                return icon;
            }
            return new Span(); // Vacío si no tiene pruebas
        }).setHeader("Eval.").setAutoWidth(true);

        gridHistorial.addComponentColumn(plan -> {
            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
            btnEditar.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            btnEditar.addClickListener(e -> mostrarEditor(plan));
            return btnEditar;
        }).setHeader("Acciones").setAutoWidth(true);

        layoutDashboard.add(cabecera, gridHistorial);
    }
    private Span crearBadgeATC(MesocicloATC fase) {
        if (fase == null) return new Span("");
        Span badge = new Span(fase.getDescripcion());
        badge.getElement().getThemeList().add("badge");
        switch (fase) {
            case NIVELACION: case ADQUISICION: badge.getElement().getThemeList().add("primary"); break;
            case TRANSFERENCIA: badge.getElement().getThemeList().add("warning"); break;
            case COMPETENCIA: badge.getElement().getThemeList().add("error"); break;
            case REFUERZO: badge.getElement().getThemeList().add("contrast"); break;
            case RECUPERACION: badge.getElement().getThemeList().add("success"); break;
        }
        return badge;
    }

    // =================================================================================
    // ESTADO 2: EL TALLER CIENTÍFICO (EDITOR PRO)
    // =================================================================================
    private void configurarEditor() {
        layoutEditor = new VerticalLayout();
        layoutEditor.setSizeFull();
        layoutEditor.setPadding(true);

        // 1. ZONA A: Cabecera Parametrización
        FormLayout cabeceraPlan = new FormLayout();
        nombrePlanField = new TextField("Nombre del Microciclo");

        gruposCombo = new MultiSelectComboBox<>("Grupos Asignados");
        gruposCombo.setItems(grupoRepo.findAll());
        gruposCombo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);

        fechaInicioPicker = new DatePicker("Fecha Inicio");
        fechaFinPicker = new DatePicker("Fecha Fin");

        NumberField duracionField = new NumberField("Duración (Minutos)");
        duracionField.setStepButtonsVisible(true);
        duracionField.setMin(1);
        duracionField.setMax(120);
        duracionField.setSuffixComponent(new Span("min"));

        intensidadGlobalLabel = new Span("Intensidad global: -");
        intensidadGlobalLabel.getStyle().set("font-weight", "bold");
        intensidadGlobalLabel.getStyle().set("margin-bottom", "1em");

        faseAtcCombo = new ComboBox<>("Fase de Modelamiento (ATC)", MesocicloATC.values());
        faseAtcCombo.setItemLabelGenerator(MesocicloATC::getDescripcion);

        tipoMicrocicloCombo = new ComboBox<>("Tipo de Microciclo", TipoMicrociclo.values());
        tipoMicrocicloCombo.setItemLabelGenerator(TipoMicrociclo::getDescripcion);
        macrocicloCombo = new ComboBox<>("Macrociclo (Opcional)");
        macrocicloCombo.setItemLabelGenerator(Macrociclo::getNombre);
        macrocicloCombo.setClearButtonVisible(true);



        cabeceraPlan.add(macrocicloCombo, nombrePlanField, gruposCombo,
                fechaInicioPicker, fechaFinPicker, duracionField, intensidadGlobalLabel,
                faseAtcCombo, tipoMicrocicloCombo);

        cabeceraPlan.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));

        // 2. ZONA B: Biblioteca (Izquierda)
        VerticalLayout panelIzquierdo = new VerticalLayout();
        panelIzquierdo.setWidth("35%");
        panelIzquierdo.setPadding(false);

        Tab tabTareas = new Tab("Tareas");
        Tab tabPruebas = new Tab("Pruebas");
        Tabs tabsBiblioteca = new Tabs(tabTareas, tabPruebas);

        gridTareas = new Grid<>(TareaDiaria.class, false);
        gridTareas.addColumn(TareaDiaria::getNombre).setHeader("Nombre");
        gridTareas.addColumn(TareaDiaria::getCategoria).setHeader("Bloque");
        gridTareas.setItems(tareaRepo.findAll());
        // Al hacer clic, enviamos al plan
        gridTareas.addItemClickListener(e -> agregarEjercicioAlPlan(e.getItem(), true));

        gridPruebas = new Grid<>(PruebaEstandar.class, false);
        gridPruebas.addColumn(p -> p.getNombreMostrar(traduccionService)).setHeader("Nombre");
        gridPruebas.addColumn(PruebaEstandar::getCategoria).setHeader("Bloque");
        gridPruebas.setItems(pruebaRepo.findAll());
        gridPruebas.setVisible(false);
        // Al hacer clic, enviamos al plan
        gridPruebas.addItemClickListener(e -> agregarEjercicioAlPlan(e.getItem(), false));

        tabsBiblioteca.addSelectedChangeListener(e -> {
            boolean esTareas = e.getSelectedTab().equals(tabTareas);
            gridTareas.setVisible(esTareas);
            gridPruebas.setVisible(!esTareas);
        });

        panelIzquierdo.add(new H3("Catálogo"), tabsBiblioteca, gridTareas, gridPruebas);

        // 3. ZONA C: El Lienzo Editable (Derecha)
        VerticalLayout panelDerecho = new VerticalLayout();
        panelDerecho.setWidth("65%");
        panelDerecho.setPadding(false);

        gridPlan = new Grid<>(EjercicioPlanificado.class, false);

        gridPlan.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT);
        // Columna de Orden (Subir/Bajar)
        gridPlan.addComponentColumn(ej -> {
            Button btnUp = new Button(new Icon(VaadinIcon.ARROW_UP));
            btnUp.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnUp.addClickListener(e -> moverEjercicio(ej, -1));

            Button btnDown = new Button(new Icon(VaadinIcon.ARROW_DOWN));
            btnDown.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnDown.addClickListener(e -> moverEjercicio(ej, 1));

            return new HorizontalLayout(btnUp, btnDown);
        }).setHeader("Orden").setAutoWidth(true).setFlexGrow(0);

        // Nombre del Ejercicio
        gridPlan.addColumn(ej ->
                        ej.getTareaDiaria() != null ? "[T] "
                                + ej.getTareaDiaria().getNombre() :
                                "[P] " + ej.getPruebaEstandar()
                                        .getNombreMostrar(traduccionService))
                .setHeader("Ejercicio").setFlexGrow(1);
// Columna de Intensidad
        gridPlan.addComponentColumn(ej -> {
            ComboBox<Integer> combo = new ComboBox<>();
            combo.setItems(1, 2, 3, 4, 5);
            combo.setValue(ej.getIntensidad());
            combo.addValueChangeListener(event -> {
                ej.setIntensidad(event.getValue());
                actualizarIntensidadGlobal(intensidadGlobalLabel, ejerciciosDelPlan);
            });
            combo.setWidth("80px");
            return combo;
        }).setHeader("Intensidad (1-5)").setWidth("100px");
// Columna "Para casa" (autónomo) - Checkbox
        gridPlan.addComponentColumn(ej -> {
            Checkbox check = new Checkbox();
            check.setValue(!ej.isRequiereSupervision()); // true si es para casa
            check.addValueChangeListener(event -> {
                ej.setRequiereSupervision(!event.getValue());
                // Refrescar la fila para que el combo de judokas se habilite/deshabilite
                gridPlan.getDataProvider().refreshItem(ej);
            });
            return check;
        }).setHeader("Para casa").setWidth("100px");

// Columna "Asignado a" (judoka individual) - ComboBox
        gridPlan.addComponentColumn(ej -> {
            ComboBox<Judoka> combo = new ComboBox<>();
            combo.setItems(judokasDeLosGrupos);
            combo.setItemLabelGenerator(j -> j.getNombre() + " " + j.getApellido());

            // Buscar el judoka correspondiente en la lista por ID
            Judoka judokaAsignado = null;
            if (ej.getJudokaAsignado() != null && ej.getJudokaAsignado().getId() != null) {
                judokaAsignado = judokasDeLosGrupos.stream()
                        .filter(j -> j.getId().equals(ej.getJudokaAsignado().getId()))
                        .findFirst()
                        .orElse(null);
            }
            combo.setValue(judokaAsignado);

            combo.addValueChangeListener(event -> ej.setJudokaAsignado(event.getValue()));
            combo.setEnabled(!ej.isRequiereSupervision());
            return combo;
        }).setHeader("Asignado a").setWidth("200px");

// Columna de Duración (minutos)
        gridPlan.addComponentColumn(ej -> {
            NumberField field = new NumberField();
            field.setValue(ej.getDuracionMinutos() != null ? ej.getDuracionMinutos().doubleValue() : null);
            field.setStep(5);
            field.setMin(0);
            field.setMax(120);
            field.addValueChangeListener(event -> {
                ej.setDuracionMinutos(event.getValue() != null ? event.getValue().intValue() : null);
                actualizarBarraProgresoTiempo();
            });
            field.setWidth("100px");
            return field;
        }).setHeader("Dur (min)").setWidth("100px");
        // Columna de Dosificación (¡Micro-ajustes en vivo!)
        gridPlan.addComponentColumn(ej -> {
            TextField campoDosis = new TextField();
            campoDosis.setPlaceholder("Ej: 4x15 vel. max");
            campoDosis.setValue(ej.getNotaAjuste() != null ? ej.getNotaAjuste() : "");
            campoDosis.setWidthFull();
            campoDosis.addValueChangeListener(e -> ej.setNotaAjuste(e.getValue()));
            return campoDosis;
        }).setHeader("Dosificación Específica").setFlexGrow(2);

        // Botón Eliminar
        gridPlan.addComponentColumn(ej -> {
            Button btnRemover = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            btnRemover.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            btnRemover.addClickListener(e -> {
                ejerciciosDelPlan.remove(ej);
                recalcularOrdenes();
                gridPlan.getDataProvider().refreshAll();
                actualizarIntensidadGlobal(intensidadGlobalLabel, ejerciciosDelPlan);
                actualizarBarraProgresoTiempo();
            });
            return btnRemover;
        }).setAutoWidth(true).setFlexGrow(0);

        // Botones de Acción (Guardar/Cancelar)
        HorizontalLayout botonesFooter = new HorizontalLayout();
        Button btnCancelar = new Button("Cancelar", e -> mostrarDashboard());
        Button btnGuardar = new Button("Guardar Microciclo", new Icon(VaadinIcon.CHECK));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> guardarPlan());
        botonesFooter.add(btnCancelar, btnGuardar);
        botonesFooter.setJustifyContentMode(JustifyContentMode.END);
        botonesFooter.setWidthFull();

        panelDerecho.add(new H3("Microciclo Activo"), gridPlan, botonesFooter);

        HorizontalLayout splitLayout = new HorizontalLayout(panelIzquierdo, panelDerecho);
        splitLayout.setSizeFull();
        splitLayout.setFlexGrow(1, panelDerecho);

        layoutEditor.add(cabeceraPlan, splitLayout);
    }

    private void agregarEjercicioAlPlan(Object item, boolean esTarea) {
        EjercicioPlanificado nuevoEj = new EjercicioPlanificado();
        nuevoEj.setMicrociclo(planActual);
        if (esTarea) nuevoEj.setTareaDiaria((TareaDiaria) item);
        else nuevoEj.setPruebaEstandar((PruebaEstandar) item);
        nuevoEj.setIntensidad(3); // valor por defecto
        nuevoEj.setRequiereSupervision(true); // Por defecto, las tareas son supervisadas (clase)
        nuevoEj.setJudokaAsignado(null);
        ejerciciosDelPlan.add(nuevoEj);
        recalcularOrdenes();
        gridPlan.setItems(ejerciciosDelPlan);
        actualizarIntensidadGlobal(intensidadGlobalLabel, ejerciciosDelPlan);
        actualizarBarraProgresoTiempo();
    }

    private void moverEjercicio(EjercicioPlanificado ej, int direccion) {
        int index = ejerciciosDelPlan.indexOf(ej);
        int nuevoIndex = index + direccion;

        if (nuevoIndex >= 0 && nuevoIndex < ejerciciosDelPlan.size()) {
            ejerciciosDelPlan.remove(index);
            ejerciciosDelPlan.add(nuevoIndex, ej);
            recalcularOrdenes();
            gridPlan.getDataProvider().refreshAll();
        }
        actualizarIntensidadGlobal(intensidadGlobalLabel, ejerciciosDelPlan);
        actualizarBarraProgresoTiempo();
    }

    private void recalcularOrdenes() {
        for (int i = 0; i < ejerciciosDelPlan.size(); i++) {
            ejerciciosDelPlan.get(i).setOrden(i + 1);
        }
    }

    private void guardarPlan() {
        if (nombrePlanField.isEmpty() || faseAtcCombo.isEmpty()) {
            NotificationHelper.error("Debe ingresar el nombre y la Fase ATC");
            return;
        }

        planActual.setNombre(nombrePlanField.getValue());
        planActual.setGruposAsignados(gruposCombo.getValue());
        planActual.setFechaInicio(fechaInicioPicker.getValue());
        planActual.setFechaFin(fechaFinPicker.getValue());
        planActual.setMesocicloATC(faseAtcCombo.getValue());
        planActual.setTipoMicrociclo(tipoMicrocicloCombo.getValue());
        planActual.setMacrociclo(macrocicloCombo.getValue());

        // Sincronizamos la lista de TODOS los ejercicios al Microciclo
        planActual.getEjerciciosPlanificados().clear();
        planActual.getEjerciciosPlanificados().addAll(ejerciciosDelPlan);

        // --- MAGIA: EXTRAER LAS PRUEBAS AUTOMÁTICAMENTE ---
        // Filtramos de la lista del lienzo (gridPlan) todos los que sean tipo Prueba,
        // los extraemos y los metemos en el Set de pruebas del Microciclo
        Set<PruebaEstandar> pruebasDelPlan = ejerciciosDelPlan.stream()
                .filter(ej -> ej.getPruebaEstandar() != null)
                .map(EjercicioPlanificado::getPruebaEstandar)
                .collect(Collectors.toSet());

        planActual.setPruebas(pruebasDelPlan);

        try {
            planService.guardarPlan(planActual);
            NotificationHelper.success("Microciclo guardado exitosamente");
            mostrarDashboard();
        } catch (Exception e) {
            NotificationHelper.error("Error al guardar: " + e.getMessage());
        }
    }
    // =================================================================================
    // NAVEGACIÓN (MÁQUINA DE ESTADOS)
    // =================================================================================
    private void mostrarDashboard() {
        layoutEditor.setVisible(false);
        layoutDashboard.setVisible(true);
        gridHistorial.setItems(planService.obtenerHistorialDelSensei(senseiActual));
    }

    private void mostrarEditor(Microciclo plan) {
        layoutDashboard.setVisible(false);
        layoutEditor.setVisible(true);
        // Llena el combo con los macrociclos del Sensei
        macrocicloCombo.setItems(macrocicloRepo.findBySenseiOrderByFechaInicioDesc(senseiActual));

        if (plan == null) {
            planActual = new Microciclo();
            planActual.setSensei(senseiActual);
            planActual.setEstado(EstadoMicrociclo.BORRADOR);
            ejerciciosDelPlan.clear();

            nombrePlanField.clear();
            gruposCombo.clear();
            fechaInicioPicker.clear();
            fechaFinPicker.clear();
            faseAtcCombo.clear();
            tipoMicrocicloCombo.clear();
            macrocicloCombo.clear();
        } else {
            // Modo Edición
            planActual = plan;
            if (planActual.getGruposAsignados() != null && !planActual.getGruposAsignados().isEmpty()) {
                judokasDeLosGrupos = planActual.getGruposAsignados().stream()
                        .flatMap(g -> judokaRepository.findByGrupo(g).stream())
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                judokasDeLosGrupos = new ArrayList<>();
            }
            nombrePlanField.setValue(plan.getNombre() != null ? plan.getNombre() : "");
            gruposCombo.setValue(plan.getGruposAsignados());
            fechaInicioPicker.setValue(plan.getFechaInicio());
            fechaFinPicker.setValue(plan.getFechaFin());
            faseAtcCombo.setValue(plan.getMesocicloATC());
            tipoMicrocicloCombo.setValue(plan.getTipoMicrociclo());

            // Cargamos los ejercicios y los ordenamos por su atributo 'orden'
            ejerciciosDelPlan = new ArrayList<>(plan.getEjerciciosPlanificados());
            ejerciciosDelPlan.sort(Comparator.comparing(e -> e.getOrden() != null ? e.getOrden() : 99));
            macrocicloCombo.setValue(plan.getMacrociclo());
        }

        gridPlan.setItems(ejerciciosDelPlan);
        actualizarIntensidadGlobal(intensidadGlobalLabel, ejerciciosDelPlan);
        actualizarBarraProgresoTiempo();
    }

    private void actualizarBarraProgresoTiempo() {
        // Usar ejerciciosDelPlan, no listaEjerciciosDelPlan
        int minutosTotales = ejerciciosDelPlan.stream()
                .mapToInt(ej -> ej.getDuracionMinutos() != null ? ej.getDuracionMinutos() : 0)
                .sum();

        double porcentaje = (double) minutosTotales / 120; // Asumiendo 120 min máximo

        barraProgresoGlobal.setValue(Math.min(porcentaje, 1.0));
        labelTiempoTotal.setText(String.format("Tiempo ocupado: %d / 120 min", minutosTotales));

        if (porcentaje > 1.0) {
            barraProgresoGlobal.getElement().setAttribute("theme", "error");
            labelTiempoTotal.getStyle().set("color", "var(--lumo-error-text-color)");
        } else {
            barraProgresoGlobal.getElement().removeAttribute("theme");
            labelTiempoTotal.getStyle().set("color", "var(--lumo-primary-text-color)");
        }
    }

    private void actualizarIntensidadGlobal(Span label, List<EjercicioPlanificado> lista) {
        if (lista.isEmpty()) {
            label.getElement().setProperty("innerHTML", "Intensidad: —");
            return;
        }

        double promedio = lista.stream()
                .filter(e -> e.getIntensidad() != null)
                .mapToInt(EjercicioPlanificado::getIntensidad)
                .average()
                .orElse(0);

        int intensidadRedondeada = (int) Math.round(promedio);

        StringBuilder html = new StringBuilder();
        html.append("<span style='font-weight: 500; margin-right: 8px;'>Intensidad:</span>");

        for (int i = 1; i <= 5; i++) {
            html.append("<span style='display: inline-block; width: 28px; height: 28px; ");
            html.append("border-radius: 50%; text-align: center; line-height: 28px; ");
            html.append("margin-right: 4px; font-weight: bold; ");

            if (i == intensidadRedondeada) {
                String color = getColorPorIntensidad(i);
                html.append(String.format("background-color: %s; color: white; border: 2px solid %s; ", color, color));
            } else {
                html.append("border: 2px solid #ccc; color: #999; background-color: transparent; ");
            }

            html.append("'>").append(i).append("</span>");
        }

        // Añadimos el promedio exacto como información adicional
        html.append("<span style='margin-left: 8px; font-size: 0.9em; color: #666;' title='Promedio exacto'>");
        html.append(String.format("(%.1f)", promedio));
        html.append("</span>");

        label.getElement().setProperty("innerHTML", html.toString());
    }
    private String getColorPorIntensidad(int intensidad) {
        switch (intensidad) {
            case 1: return "#4caf50"; // Verde
            case 2: return "#8bc34a"; // Verde claro
            case 3: return "#ffc107"; // Amarillo
            case 4: return "#ff9800"; // Naranja
            case 5: return "#f44336"; // Rojo
            default: return "#9e9e9e"; // Gris
        }
    }
}