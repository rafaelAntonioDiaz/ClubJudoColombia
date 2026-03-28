package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.stream.Stream;

@Route(value = "gestion-tarifas", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Gestión de Tarifas | Club Judo Colombia")
public class GestionTarifasView extends VerticalLayout implements Serializable {

    private final GrupoEntrenamientoService grupoService;
    private final TraduccionService traduccionService;
    private final SecurityService securityService;
    private final ConfiguracionService configuracionService;

    private final Grid<GrupoEntrenamiento> tarifasGrid = new Grid<>(GrupoEntrenamiento.class, false);
    private final TarifaForm form;

    private final TextField filtroNombre = new TextField();
    private final Button btnNuevoPlan = new Button();
    private String currentFilter = null;

    public GestionTarifasView(GrupoEntrenamientoService grupoService,
                              SecurityService securityService,
                              TraduccionService traduccionService,
                              ConfiguracionService configuracionService) {
        this.grupoService = grupoService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.configuracionService = configuracionService;
        this.form = new TarifaForm(traduccionService);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureForm();
        configureToolbar();

        buildLayout();
    }

    private void configureGrid() {
        tarifasGrid.addColumn(GrupoEntrenamiento::getNombre)
                .setHeader(traduccionService.get("tarifas.grid.nombre"))
                .setAutoWidth(true).setFlexGrow(1);

        tarifasGrid.addColumn(grupo -> {
                    BigDecimal monto = grupo.getTarifaMensual();
                    if (monto == null) monto = BigDecimal.ZERO;
                    return configuracionService.obtenerFormatoMoneda().format(monto);
                }).setHeader(traduccionService.get("tarifas.grid.tarifa"))
                .setAutoWidth(true);

        tarifasGrid.addColumn(grupo -> {
                    if (grupo.isIncluyeMatricula() && grupo.getMontoMatricula() != null) {
                        return configuracionService.obtenerFormatoMoneda().format(grupo.getMontoMatricula());
                    }
                    return "-";
                }).setHeader(traduccionService.get("tarifas.grid.matricula"))
                .setAutoWidth(true);

        tarifasGrid.addColumn(GrupoEntrenamiento::getDiasGracia)
                .setHeader(traduccionService.get("tarifas.grid.dias_gracia"))
                .setAutoWidth(true);

        tarifasGrid.addComponentColumn(this::crearAccionesColumna)
                .setHeader(traduccionService.get("generic.acciones"))
                .setAutoWidth(true).setFlexGrow(0);

        tarifasGrid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchTarifas,
                this::countTarifas
        ));

        tarifasGrid.setPageSize(20);
        tarifasGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private Component crearAccionesColumna(GrupoEntrenamiento grupo) {
        HorizontalLayout actions = new HorizontalLayout();

        Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEditar.setTooltipText(traduccionService.get("btn.editar"));
        btnEditar.addClickListener(e -> abrirFormularioEdicion(grupo));

        Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnEliminar.setTooltipText(traduccionService.get("btn.eliminar"));
        btnEliminar.addClickListener(e -> confirmarEliminarGrupo(grupo));

        actions.add(btnEditar, btnEliminar);
        return actions;
    }

    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(this::guardarPlan);
        form.addCancelListener(event -> cerrarFormulario());
    }

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

    private void abrirFormularioNuevo() {
        GrupoEntrenamiento nuevo = new GrupoEntrenamiento();
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

    private void guardarPlan(BaseForm.SaveEvent<GrupoEntrenamiento> event) {
        try {
            GrupoEntrenamiento grupo = event.getData();
            Sensei sensei = securityService.getAuthenticatedSensei()
                    .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

            if (grupo.getId() == null) {
                grupo = grupoService.crearGrupo(sensei, grupo.getNombre(), grupo.getDescripcion(),
                        grupo.getTarifaMensual(), grupo.isIncluyeMatricula(),
                        grupo.getMontoMatricula(), grupo.getDiasGracia(), true);
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

            NotificationHelper.success(traduccionService.get("msg.success.saved") + ": " + grupo.getNombre());
            cerrarFormulario();
            tarifasGrid.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.generic") + ": " + e.getMessage());
        }
    }

    private void confirmarEliminarGrupo(GrupoEntrenamiento grupo) {
        try {
            grupoService.deleteGrupo(grupo.getId());
            NotificationHelper.success(traduccionService.get("msg.success.deleted"));
            tarifasGrid.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.generic") + ": " + e.getMessage());
        }
    }

    private Stream<GrupoEntrenamiento> fetchTarifas(Query<GrupoEntrenamiento, Void> query) {
        String filter = currentFilter != null ? currentFilter : "";
        return grupoService.findAll(query.getOffset(), query.getLimit(), filter, true).stream();
    }

    private int countTarifas(Query<GrupoEntrenamiento, Void> query) {
        String filter = currentFilter != null ? currentFilter : "";
        return (int) grupoService.count(filter, true);
    }
}