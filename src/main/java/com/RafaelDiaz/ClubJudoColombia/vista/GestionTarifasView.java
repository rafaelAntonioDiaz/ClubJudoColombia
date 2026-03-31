package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.TarifaForm;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Route(value = "gestion-tarifas", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Gestión de Tarifas | Club Judo Colombia")
public class GestionTarifasView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;

    // ──────────────────────────────────────────────────────────────────────────
    // Dependencias
    // ──────────────────────────────────────────────────────────────────────────
    private final GrupoEntrenamientoService grupoService;
    private final TraduccionService traduccionService;
    private final SecurityService securityService;
    private final ConfiguracionService configuracionService;
    private final JudokaRepository judokaRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // Componentes de la vista
    // ──────────────────────────────────────────────────────────────────────────
    private final Grid<GrupoEntrenamiento> tarifasGrid = new Grid<>(GrupoEntrenamiento.class, false);
    private final TarifaForm form;
    private final TextField filtroNombre = new TextField();
    private final Button btnNuevoPlan = new Button();

    // ──────────────────────────────────────────────────────────────────────────
    // Estado
    // ──────────────────────────────────────────────────────────────────────────
    private String currentFilter = null;

    public GestionTarifasView(GrupoEntrenamientoService grupoService,
                              SecurityService securityService,
                              TraduccionService traduccionService,
                              ConfiguracionService configuracionService,
                              JudokaRepository judokaRepository) {
        this.grupoService = grupoService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.configuracionService = configuracionService;
        this.form = new TarifaForm(traduccionService);
        this.judokaRepository = judokaRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureForm();
        configureToolbar();
        buildLayout();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Configuración del Grid de planes tarifarios
    // Solo muestra grupos con esTarifario=true (filtro aplicado en fetchTarifas)
    // ──────────────────────────────────────────────────────────────────────────
    private void configureGrid() {

        // Columna: nombre del plan tarifario
        tarifasGrid.addColumn(GrupoEntrenamiento::getNombre)
                .setHeader(traduccionService.get("tarifas.grid.nombre"))
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Columna: monto de la tarifa mensual formateado según la moneda configurada
        tarifasGrid.addColumn(grupo -> {
                    BigDecimal monto = grupo.getTarifaMensual();
                    if (monto == null) monto = BigDecimal.ZERO;
                    return configuracionService.obtenerFormatoMoneda().format(monto);
                }).setHeader(traduccionService.get("tarifas.grid.tarifa"))
                .setAutoWidth(true);

        // Columna: monto de matrícula (si aplica)
        tarifasGrid.addColumn(grupo -> {
                    if (grupo.isIncluyeMatricula() && grupo.getMontoMatricula() != null) {
                        return configuracionService.obtenerFormatoMoneda().format(grupo.getMontoMatricula());
                    }
                    return "-";
                }).setHeader(traduccionService.get("tarifas.grid.matricula"))
                .setAutoWidth(true);

        // Columna: días de gracia para el pago
        tarifasGrid.addColumn(GrupoEntrenamiento::getDiasGracia)
                .setHeader(traduccionService.get("tarifas.grid.dias_gracia"))
                .setAutoWidth(true);

        // Columna: botones de acción (miembros, editar, eliminar)
        tarifasGrid.addComponentColumn(this::crearAccionesColumna)
                .setHeader(traduccionService.get("generic.acciones"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        // DataProvider con paginación del lado del servidor.
        // fetchTarifas y countTarifas ya filtran esTarifario=true internamente.
        tarifasGrid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchTarifas,
                this::countTarifas
        ));

        tarifasGrid.setPageSize(20);
        tarifasGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Botones de acción por fila del grid de tarifas
    // ──────────────────────────────────────────────────────────────────────────
    private Component crearAccionesColumna(GrupoEntrenamiento grupo) {
        HorizontalLayout actions = new HorizontalLayout();

        // CORRECCIÓN: El método abrirDialogoMiembros existía pero nunca se conectaba
        // a ningún botón en el grid. Se agrega aquí el botón que lo invoca.
        Button btnMiembros = new Button(new Icon(VaadinIcon.USERS));
        btnMiembros.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnMiembros.setTooltipText(traduccionService.get("tarifas.tooltip.ver_miembros"));
        btnMiembros.addClickListener(e -> abrirDialogoMiembros(grupo));

        // Abrir formulario de edición de datos tarifarios
        Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEditar.setTooltipText(traduccionService.get("btn.editar"));
        btnEditar.addClickListener(e -> abrirFormularioEdicion(grupo));

        // Eliminar el plan tarifario
        Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnEliminar.setTooltipText(traduccionService.get("btn.eliminar"));
        btnEliminar.addClickListener(e -> confirmarEliminarGrupo(grupo));

        actions.add(btnMiembros, btnEditar, btnEliminar);
        return actions;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Formulario de edición de plan tarifario
    // ──────────────────────────────────────────────────────────────────────────
    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(this::guardarPlan);
        form.addCancelListener(event -> cerrarFormulario());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Barra de herramientas: filtro por nombre + botón "Nuevo plan"
    // ──────────────────────────────────────────────────────────────────────────
    private void configureToolbar() {
        filtroNombre.setPlaceholder(traduccionService.get("tarifas.filtro.nombre"));
        filtroNombre.addValueChangeListener(e -> {
            currentFilter = e.getValue();
            tarifasGrid.getDataProvider().refreshAll();
        });

        btnNuevoPlan.setText(traduccionService.get("tarifas.btn.nuevo"));
        btnNuevoPlan.setIcon(new Icon(VaadinIcon.PLUS));
        btnNuevoPlan.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevoPlan.addClickListener(e -> abrirFormularioNuevo());

        HorizontalLayout toolbar = new HorizontalLayout(filtroNombre, btnNuevoPlan);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setSpacing(true);

        add(toolbar);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout principal
    // ──────────────────────────────────────────────────────────────────────────
    private void buildLayout() {
        VerticalLayout main = new VerticalLayout(
                new H1(traduccionService.get("tarifas.titulo")),
                tarifasGrid,
                form
        );
        main.setSizeFull();
        main.setPadding(false);
        add(main);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Apertura del formulario
    // ──────────────────────────────────────────────────────────────────────────
    private void abrirFormularioNuevo() {
        // Al crear un plan desde aquí, siempre será tarifario (esTarifario=true)
        GrupoEntrenamiento nuevo = new GrupoEntrenamiento();
        nuevo.setEsTarifario(true);
        nuevo.setTarifaMensual(BigDecimal.ZERO);
        nuevo.setComisionSensei(BigDecimal.ZERO);
        nuevo.setDiasGracia(5);
        nuevo.setIncluyeMatricula(false);
        nuevo.setMontoMatricula(BigDecimal.ZERO);
        form.setBean(nuevo);
        form.setVisible(true);
        btnNuevoPlan.setEnabled(false);
    }

    private void abrirFormularioEdicion(GrupoEntrenamiento grupo) {
        form.setBean(grupo);
        form.setVisible(true);
        btnNuevoPlan.setEnabled(false);
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        form.setBean(null);
        btnNuevoPlan.setEnabled(true);
        tarifasGrid.asSingleSelect().clear();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistencia del plan tarifario
    // ──────────────────────────────────────────────────────────────────────────
    private void guardarPlan(BaseForm.SaveEvent<GrupoEntrenamiento> event) {
        try {
            GrupoEntrenamiento grupo = event.getData();
            Sensei sensei = securityService.getAuthenticatedSensei()
                    .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

            if (grupo.getId() == null) {
                // Nuevo plan tarifario: esTarifario=true siempre desde esta vista
                grupo = grupoService.crearGrupo(
                        sensei,
                        grupo.getNombre(),
                        grupo.getDescripcion(),
                        grupo.getTarifaMensual(),
                        grupo.isIncluyeMatricula(),
                        grupo.getMontoMatricula(),
                        grupo.getDiasGracia(),
                        true    // esTarifario = true — es plan tarifario
                );
            } else {
                grupoService.actualizarGrupo(
                        grupo.getId(),
                        grupo.getNombre(),
                        grupo.getDescripcion(),
                        grupo.getTarifaMensual(),
                        grupo.isIncluyeMatricula(),
                        grupo.getMontoMatricula(),
                        grupo.getDiasGracia()
                );
            }

            NotificationHelper.success(
                    traduccionService.get("msg.success.saved") + ": " + grupo.getNombre()
            );
            cerrarFormulario();
            tarifasGrid.getDataProvider().refreshAll();

        } catch (Exception e) {
            NotificationHelper.error(
                    traduccionService.get("error.generic") + ": " + e.getMessage()
            );
        }
    }

    private void confirmarEliminarGrupo(GrupoEntrenamiento grupo) {
        try {
            grupoService.deleteGrupo(grupo.getId());
            NotificationHelper.success(traduccionService.get("msg.success.deleted"));
            tarifasGrid.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error(
                    traduccionService.get("error.generic") + ": " + e.getMessage()
            );
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DataProvider con paginación del lado del servidor
    // Ambos métodos delegan en el servicio que filtra esTarifario=true
    // ──────────────────────────────────────────────────────────────────────────
    private Stream<GrupoEntrenamiento> fetchTarifas(Query<GrupoEntrenamiento, Void> query) {
        String filter = currentFilter != null ? currentFilter : "";
        return grupoService.findAll(query.getOffset(), query.getLimit(), filter, true).stream();
    }

    private int countTarifas(Query<GrupoEntrenamiento, Void> query) {
        String filter = currentFilter != null ? currentFilter : "";
        return (int) grupoService.count(filter, true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Diálogo para ver y reasignar el plan tarifario de los judokas del grupo
    // CORRECCIÓN: Este método existía pero nunca se invocaba desde el grid.
    // Ahora se llama desde el botón btnMiembros en crearAccionesColumna().
    // ──────────────────────────────────────────────────────────────────────────
    private void abrirDialogoMiembros(GrupoEntrenamiento grupoTarifario) {
        // Cargar judokas cuyo grupoFacturacion apunta a este plan tarifario
        List<Judoka> judokas = judokaRepository.findByGrupoFacturacionWithAcudiente(grupoTarifario);
        if (judokas.isEmpty()) {
            NotificationHelper.info(traduccionService.get("tarifas.msg.sin_judokas"));
            return;
        }

        // Obtener todos los planes tarifarios del sensei para el ComboBox de reasignación
        Sensei sensei = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));
        List<GrupoEntrenamiento> gruposTarifarios = grupoService.findBySenseiAndEsTarifario(sensei, true);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(
                traduccionService.get("tarifas.dialog.titulo", grupoTarifario.getNombre())
        );
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidthFull();

        // Una fila por judoka: nombre + ComboBox para seleccionar su nuevo plan tarifario
        for (Judoka judoka : judokas) {
            ComboBox<GrupoEntrenamiento> combo = new ComboBox<>();
            combo.setItems(gruposTarifarios);
            combo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
            // Preseleccionar el plan actual del judoka
            combo.setValue(judoka.getGrupoFacturacion());
            combo.setWidthFull();

            HorizontalLayout fila = new HorizontalLayout();
            fila.setWidthFull();
            fila.setAlignItems(Alignment.CENTER);
            fila.setSpacing(true);

            Span label = new Span(obtenerNombreJudoka(judoka));
            label.setWidth("200px");
            fila.add(label, combo);
            layout.add(fila);
        }

        // Botón guardar: aplica los cambios de plan tarifario por judoka
        Button btnGuardar = new Button(traduccionService.get("btn.guardar"));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> {
            for (int i = 0; i < judokas.size(); i++) {
                Judoka judoka = judokas.get(i);
                HorizontalLayout fila = (HorizontalLayout) layout.getComponentAt(i);
                @SuppressWarnings("unchecked")
                ComboBox<GrupoEntrenamiento> combo =
                        (ComboBox<GrupoEntrenamiento>) fila.getComponentAt(1);
                GrupoEntrenamiento nuevoGrupo = combo.getValue();

                // Solo persistir si el plan seleccionado es diferente al actual
                if (nuevoGrupo != null && !nuevoGrupo.equals(judoka.getGrupoFacturacion())) {
                    try {
                        grupoService.cambiarGrupoFacturacionJudoka(judoka.getId(), nuevoGrupo.getId());
                    } catch (Exception ex) {
                        NotificationHelper.error(
                                traduccionService.get("error.generic") + ": " + ex.getMessage()
                        );
                    }
                }
            }
            NotificationHelper.success(traduccionService.get("msg.success.saved"));
            dialog.close();
            tarifasGrid.getDataProvider().refreshAll();
        });

        Button btnCancelar = new Button(traduccionService.get("btn.cancelar"), e -> dialog.close());

        HorizontalLayout buttonBar = new HorizontalLayout(btnGuardar, btnCancelar);
        buttonBar.setJustifyContentMode(JustifyContentMode.END);
        buttonBar.setWidthFull();

        layout.add(buttonBar);
        dialog.add(layout);
        dialog.open();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Resolución del nombre visible del judoka con fallback por prioridad
    // ──────────────────────────────────────────────────────────────────────────
    private String obtenerNombreJudoka(Judoka judoka) {
        // Prioridad: nombre propio del judoka → usuario vinculado → acudiente → ID
        if (judoka.getNombre() != null && !judoka.getNombre().isEmpty()) {
            return judoka.getNombre() + " " + judoka.getApellido();
        } else if (judoka.getUsuario() != null) {
            return judoka.getUsuario().getNombre() + " " + judoka.getUsuario().getApellido();
        } else if (judoka.getAcudiente() != null) {
            return judoka.getAcudiente().getNombre() + " " + judoka.getAcudiente().getApellido();
        }
        return "Judoka ID: " + judoka.getId();
    }
}