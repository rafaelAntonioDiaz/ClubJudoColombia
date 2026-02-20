package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MetricaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.PruebaEstandarService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TareaDiariaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.PruebaEstandarForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.TareaDiariaForm;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "biblioteca-tareas", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Biblioteca de Ejercicios y Pruebas | Club Judo Colombia")
public class BibliotecaView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BibliotecaView.class);

    private final TareaDiariaService tareaDiariaService;
    private final PruebaEstandarService pruebaEstandarService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;

    // --- Componentes Modo Tareas ---
    private final Grid<TareaDiaria> gridTareas = new Grid<>(TareaDiaria.class, false);
    private final TareaDiariaForm formTareas = new TareaDiariaForm();

    // --- Componentes Modo Pruebas ---
    private final Grid<PruebaEstandar> gridPruebas = new Grid<>(PruebaEstandar.class, false);
    private final PruebaEstandarForm formPruebas;

    private final Button btnNuevo;
    private boolean modoPruebas = false; // Estado principal (false = Tareas, true = Pruebas)
    private List<CategoriaEjercicio> categoriasFiltroActual = null;
    private  CategoriaEjercicio categoriaEjercicio;
    private Sensei senseiActual;

    public BibliotecaView(TareaDiariaService tareaDiariaService,
                          PruebaEstandarService pruebaEstandarService,
                          MetricaRepository metricaRepository,
                          SecurityService securityService,
                          TraduccionService traduccionService) {

        this.tareaDiariaService = tareaDiariaService;
        this.pruebaEstandarService = pruebaEstandarService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;

        this.formPruebas = new PruebaEstandarForm(metricaRepository.findAll(), traduccionService);
        this.btnNuevo = new Button();

        loadSensei();

        configureGridTareas();
        configureGridPruebas();
        configureForms();
        configureButton();

        buildLayout();

        categoriaEjercicio = null;
    }

    private void buildLayout() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // 1. Cabecera y botón
        HorizontalLayout cabecera = new HorizontalLayout(new H1(traduccionService.get("biblioteca.titulo")), btnNuevo);
        cabecera.setAlignItems(Alignment.BASELINE);
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // 2. Interruptor Maestro (Tabs Principales)
        Tab tabTareas = new Tab(new Icon(VaadinIcon.LIST), new Span(" Acondicionamiento"));
        Tab tabPruebas = new Tab(new Icon(VaadinIcon.TIMER), new Span(" Evaluaciones"));
        Tabs mainTabs = new Tabs(tabTareas, tabPruebas);
        mainTabs.addSelectedChangeListener(e -> alternarModo(e.getSelectedTab().equals(tabPruebas)));

        // 3. Pestañas de Bloques (Filtro Secundario)
        Tabs pestanasBloques = crearPestanasBloques();

        // 4. Contenedores de Grids y Forms
        HorizontalLayout layoutTareas = new HorizontalLayout(gridTareas, formTareas);
        layoutTareas.setSizeFull();
        layoutTareas.setFlexGrow(1, gridTareas);
        layoutTareas.setFlexGrow(0.4, formTareas);

        HorizontalLayout layoutPruebas = new HorizontalLayout(gridPruebas, formPruebas);
        layoutPruebas.setSizeFull();
        layoutPruebas.setFlexGrow(1, gridPruebas);
        layoutPruebas.setFlexGrow(0.4, formPruebas);
        layoutPruebas.setVisible(false); // Oculto por defecto

        // Actualizamos las referencias para el cambio de modo
        mainTabs.addSelectedChangeListener(e -> {
            boolean esPruebas = e.getSelectedTab().equals(tabPruebas);
            layoutTareas.setVisible(!esPruebas);
            layoutPruebas.setVisible(esPruebas);
        });

        add(cabecera, mainTabs, pestanasBloques, layoutTareas, layoutPruebas);
    }

    private void alternarModo(boolean esModoPruebas) {
        this.modoPruebas = esModoPruebas;
        cerrarFormulario();
        actualizarBotonNuevo();
        if (modoPruebas) {
            gridPruebas.getDataProvider().refreshAll();
        } else {
            gridTareas.getDataProvider().refreshAll();
        }
    }

    private void actualizarBotonNuevo() {
        if (modoPruebas) {
            btnNuevo.setText("Nueva Prueba");
            btnNuevo.setIcon(new Icon(VaadinIcon.PLUS_CIRCLE_O));
        } else {
            btnNuevo.setText(traduccionService.get("biblioteca.boton.nueva_tarea"));
            btnNuevo.setIcon(new Icon(VaadinIcon.PLUS));
        }
    }

    private void configureGridTareas() {
        gridTareas.setWidthFull();
        gridTareas.addColumn(TareaDiaria::getCategoria).setHeader("Bloque").setSortable(true).setAutoWidth(true);
        gridTareas.addColumn(TareaDiaria::getNombre).setHeader("Nombre").setSortable(true).setFlexGrow(1);
        gridTareas.addColumn(TareaDiaria::getMetaTexto).setHeader("Meta").setAutoWidth(true);

        gridTareas.addComponentColumn(tarea -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> abrirFormularioEdicion(tarea));
            return editBtn;
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        gridTareas.setDataProvider(DataProvider.fromFilteringCallbacks(this::fetchTareas, this::countTareas));
        gridTareas.setPageSize(20);
    }

    private void configureGridPruebas() {
        gridPruebas.setWidthFull();

        // Columna Inteligente: Ícono Global vs Autor
        gridPruebas.addComponentColumn(prueba -> {
            Icon icon = prueba.isEsGlobal() ? new Icon(VaadinIcon.GLOBE) : new Icon(VaadinIcon.USER);
            icon.setColor(prueba.isEsGlobal() ? "var(--lumo-primary-color)" : "var(--lumo-success-color)");
            icon.setTooltipText(prueba.isEsGlobal() ? "Prueba Estándar (CBJ/PROESP)" : "Prueba de Autor (Sensei)");
            return icon;
        }).setHeader("Tipo").setFlexGrow(0).setAutoWidth(true);

        gridPruebas.addColumn(PruebaEstandar::getCategoria)
                .setHeader("Bloque")
                .setSortable(true)
                .setAutoWidth(true);
        gridPruebas.addColumn(p ->
                p.getNombreMostrar(traduccionService))
                .setHeader("Nombre")
                .setSortable(true)
                .setFlexGrow(1);

        // Columna de Unidades de Medida
        gridPruebas.addColumn(p ->
                        p.getMetricas().stream()
                        .map(m ->
                                traduccionService
                                        .get(m.getNombreKey()))
                                        .collect(Collectors
                                            .joining(", ")))
                .setHeader("Evalúa").setAutoWidth(true);

        gridPruebas.addComponentColumn(prueba -> {
            // Si es global, NO se puede editar
            if (prueba.isEsGlobal()) {
                Icon lock = new Icon(VaadinIcon.LOCK);
                lock.setColor("var(--lumo-disabled-text-color)");
                lock.setTooltipText("Las pruebas globales no se pueden editar");
                return lock;
            } else {
                Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
                editBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                editBtn.addClickListener(e -> abrirFormularioEdicion(prueba));
                return editBtn;
            }
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        gridPruebas.setDataProvider(DataProvider.fromFilteringCallbacks(this::fetchPruebas, this::countPruebas));
        gridPruebas.setPageSize(20);
    }

    private void configureForms() {
        formTareas.setVisible(false);
        formPruebas.setVisible(false);

        formTareas.addSaveListener(event -> guardarTarea(event.getData()));
        formTareas.addCancelListener(event -> cerrarFormulario());

        formPruebas.addSaveListener(event -> guardarPrueba(event.getData()));
        formPruebas.addCancelListener(event -> cerrarFormulario());
    }

    private void configureButton() {
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actualizarBotonNuevo();
        btnNuevo.addClickListener(e -> abrirFormularioNuevo());
    }

    private void loadSensei() {
        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));
    }

    // --- LÓGICA DE FORMULARIOS ---

    private void abrirFormularioNuevo() {
        cerrarFormulario();
        btnNuevo.setEnabled(false);
        if (modoPruebas) {
            formPruebas.setBean(new PruebaEstandar());
            formPruebas.setVisible(true);
        } else {
            formTareas.setBean(new TareaDiaria());
            formTareas.setVisible(true);
        }
    }

    private void abrirFormularioEdicion(Object item) {
        cerrarFormulario();
        btnNuevo.setEnabled(false);
        if (item instanceof PruebaEstandar) {
            formPruebas.setBean((PruebaEstandar) item);
            formPruebas.setVisible(true);
            gridPruebas.asSingleSelect().setValue((PruebaEstandar) item);
        } else if (item instanceof TareaDiaria) {
            formTareas.setBean((TareaDiaria) item);
            formTareas.setVisible(true);
            gridTareas.asSingleSelect().setValue((TareaDiaria) item);
        }
    }

    private void cerrarFormulario() {
        formTareas.setVisible(false);
        formTareas.setBean(null);
        formPruebas.setVisible(false);
        formPruebas.setBean(null);
        btnNuevo.setEnabled(true);
        gridTareas.asSingleSelect().clear();
        gridPruebas.asSingleSelect().clear();
    }

    private void guardarTarea(TareaDiaria tarea) {
        try {
            tareaDiariaService.guardarTarea(tarea, senseiActual);
            NotificationHelper.success("Tarea guardada exitosamente");
            cerrarFormulario();
            gridTareas.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error("Error al guardar: " + e.getMessage());
        }
    }

    private void guardarPrueba(PruebaEstandar prueba) {
        try {
            pruebaEstandarService.guardarPruebaDeAutor(prueba, senseiActual);
            NotificationHelper.success("Evaluación guardada exitosamente");
            cerrarFormulario();
            gridPruebas.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error("Error al guardar: " + e.getMessage());
        }
    }

    // --- PESTAÑAS Y LAZY LOADING (AMBOS MODOS) ---

    private Tabs crearPestanasBloques() {
        // 1. Instanciamos las pestañas con la nomenclatura metodológica correcta
        Tab tabTodos = new Tab("Todos");
        tabTodos.setId("todos");

        Tab tabTecnico = new Tab("1. Técnico-Coordinativo");
        tabTecnico.setId("tecnico");

        Tab tabDeterminante = new Tab("2. Determinante");
        tabDeterminante.setId("determinante");

        Tab tabSustento = new Tab("3. Sustento");
        tabSustento.setId("sustento");

        Tab tabEficiencia = new Tab("4. Eficiencia");
        tabEficiencia.setId("eficiencia");

        Tab tabProteccion = new Tab("5. Protección");
        tabProteccion.setId("proteccion");

        Tabs tabs = new Tabs(tabTodos, tabTecnico, tabDeterminante, tabSustento, tabEficiencia, tabProteccion);
        tabs.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            String tabId = event.getSelectedTab().getId().orElse("todos");

            this.categoriasFiltroActual = switch (tabId) {
                case "tecnico" -> List.of(
                        CategoriaEjercicio.TECNICA,
                        CategoriaEjercicio.AGILIDAD,
                        CategoriaEjercicio.ANTICIPACION
                );
                case "determinante" -> List.of(
                        CategoriaEjercicio.POTENCIA,
                        CategoriaEjercicio.VELOCIDAD
                );
                case "sustento" -> List.of(
                        CategoriaEjercicio.RESISTENCIA_DINAMICA,
                        CategoriaEjercicio.RESISTENCIA_ISOMETRICA,
                        CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA
                );
                case "eficiencia" -> List.of(
                        CategoriaEjercicio.APTITUD_ANAEROBICA,
                        CategoriaEjercicio.APTITUD_AEROBICA
                );
                case "proteccion" -> List.of(
                        CategoriaEjercicio.FLEXIBILIDAD
                );
                default -> null; // Caso "todos"
            };

            // Refrescamos el grid correspondiente según el interruptor maestro
            if (modoPruebas) {
                gridPruebas.getDataProvider().refreshAll();
            } else {
                gridTareas.getDataProvider().refreshAll();
            }
        });

        return tabs;
    }

    private Stream<TareaDiaria> fetchTareas(Query<TareaDiaria, Void> query) {
        if (categoriasFiltroActual == null || categoriasFiltroActual.isEmpty()) {
            return tareaDiariaService.findAll().stream().skip(query.getOffset()).limit(query.getLimit());
        }
        return tareaDiariaService.findByCategoriaIn(categoriasFiltroActual).stream().skip(query.getOffset()).limit(query.getLimit());
    }

    private int countTareas(Query<TareaDiaria, Void> query) {
        if (categoriasFiltroActual == null || categoriasFiltroActual.isEmpty()) return (int) tareaDiariaService.count();
        return (int) tareaDiariaService.countByCategoriaIn(categoriasFiltroActual);
    }

    private Stream<PruebaEstandar> fetchPruebas(Query<PruebaEstandar, Void> query) {
        if (categoriasFiltroActual == null || categoriasFiltroActual.isEmpty()) {
            return pruebaEstandarService.findPruebasVisiblesParaSensei(senseiActual).stream().skip(query.getOffset()).limit(query.getLimit());
        }
        return pruebaEstandarService.findPruebasVisiblesPorCategoria(categoriasFiltroActual, senseiActual).stream().skip(query.getOffset()).limit(query.getLimit());
    }

    private int countPruebas(Query<PruebaEstandar, Void> query) {
        if (categoriasFiltroActual == null || categoriasFiltroActual.isEmpty()) {
            return pruebaEstandarService.findPruebasVisiblesParaSensei(senseiActual).size();
        }
        return pruebaEstandarService.findPruebasVisiblesPorCategoria(categoriasFiltroActual, senseiActual).size();
    }
}