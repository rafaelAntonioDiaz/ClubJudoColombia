package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.component.FiltroJudokaLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.form.GrupoForm;
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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@Route(value = "gestion-grupos", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Gestión de Grupos | Club Judo Colombia")
public class SenseiGruposView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SenseiGruposView.class);

    private final GrupoEntrenamientoService grupoService;
    private final TraduccionService traduccionService; // <--- SERVICIO I18N

    private final Grid<GrupoEntrenamiento> gruposGrid = new Grid<>(GrupoEntrenamiento.class, false);
    private final GrupoForm form = new GrupoForm();
    private final FiltroJudokaLayout filtros;
    private final Button btnNuevoGrupo;

    private Dialog dialogoMiembros;
    private Grid<Judoka> gridMiembros;
    private ComboBox<Judoka> comboCandidatos;

    public SenseiGruposView(GrupoEntrenamientoService grupoService,
                            SecurityService securityService,
                            AccessAnnotationChecker accessChecker,
                            ConfiguracionService configuracionService,
                            AuthenticationContext authenticationContext,
                            TraduccionService traduccionService) { // <--- CONSTRUCTOR
        this.grupoService = grupoService;
        this.traduccionService = traduccionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        this.btnNuevoGrupo = new Button(traduccionService.get("grupos.btn.nuevo"), new Icon(VaadinIcon.PLUS));

        configureGrid();

        this.filtros = new FiltroJudokaLayout(
                (SerializableConsumer<FiltroJudokaLayout.SearchParams>)
                        searchParams -> {
                            gruposGrid.getDataProvider().refreshAll();
                        }
        );

        configureForm();
        configureBotonNuevo();

        buildLayout();
    }

    private void configureBotonNuevo() {
        btnNuevoGrupo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevoGrupo.addClickListener(e -> abrirFormularioNuevo());
    }

    private void configureGrid() {
        gruposGrid.addColumn(GrupoEntrenamiento::getNombre)
                .setHeader(traduccionService.get("grupos.grid.nombre"))
                .setSortable(true).setAutoWidth(true).setFlexGrow(1);

        gruposGrid.addColumn(GrupoEntrenamiento::getDescripcion)
                .setHeader(traduccionService.get("grupos.grid.descripcion"))
                .setAutoWidth(true).setFlexGrow(2);

        gruposGrid.addColumn(new ComponentRenderer<>(grupo -> {
            Span count = new Span(grupo.getJudokas().size() + " " + traduccionService.get("grupos.label.alumnos"));
            count.getElement().getThemeList().add("badge contrast");
            return count;
        })).setHeader(traduccionService.get("grupos.grid.miembros")).setAutoWidth(true);

        gruposGrid.addComponentColumn(this::crearAccionesColumna)
                .setHeader(traduccionService.get("generic.acciones"))
                .setAutoWidth(true).setFlexGrow(0);

        gruposGrid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchGrupos,
                this::countGrupos
        ));

        gruposGrid.setPageSize(20);
        gruposGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private Component crearAccionesColumna(GrupoEntrenamiento grupo) {
        HorizontalLayout actions = new HorizontalLayout();

        Button btnMiembros = new Button(new Icon(VaadinIcon.USERS));
        btnMiembros.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnMiembros.setTooltipText(traduccionService.get("grupos.tooltip.gestionar_miembros"));
        btnMiembros.addClickListener(e -> abrirDialogoMiembros(grupo));

        Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEditar.setTooltipText(traduccionService.get("btn.editar"));
        btnEditar.addClickListener(e -> abrirFormularioEdicion(grupo));

        Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnEliminar.setTooltipText(traduccionService.get("btn.eliminar"));
        btnEliminar.addClickListener(e -> confirmarEliminarGrupo(grupo));

        actions.add(btnMiembros, btnEditar, btnEliminar);
        return actions;
    }

    private void abrirDialogoMiembros(GrupoEntrenamiento grupo) {
        dialogoMiembros = new Dialog();
        dialogoMiembros.setHeaderTitle(traduccionService.get("grupos.dialog.miembros.titulo") + ": " + grupo.getNombre());
        dialogoMiembros.setWidth("800px");
        dialogoMiembros.setHeight("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout addLayout = new HorizontalLayout();
        addLayout.setWidthFull();
        addLayout.setAlignItems(Alignment.BASELINE);

        comboCandidatos = new ComboBox<>(traduccionService.get("grupos.field.buscar_alumno"));
        comboCandidatos.setWidthFull();
        comboCandidatos.setItems(grupoService.findJudokasDisponibles(grupo.getId(), "", null, null));
        comboCandidatos.setItemLabelGenerator(j ->
                j.getUsuario().getNombre() + " " + j.getUsuario().getApellido() + " (" + j.getGradoCinturon() + ")"
        );

        Button btnAgregar = new Button(traduccionService.get("btn.agregar"), new Icon(VaadinIcon.PLUS));
        btnAgregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAgregar.addClickListener(e -> {
            Judoka seleccionado = comboCandidatos.getValue();
            if (seleccionado != null) {
                grupoService.addJudokaToGrupo(grupo.getId(), seleccionado.getId());
                Notification.show(traduccionService.get("msg.success.added")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                actualizarListaMiembros(grupo);
                comboCandidatos.clear();
                comboCandidatos.setItems(grupoService.findJudokasDisponibles(grupo.getId(), "", null, null));
                gruposGrid.getDataProvider().refreshItem(grupo);
            }
        });

        addLayout.add(comboCandidatos, btnAgregar);
        addLayout.expand(comboCandidatos);

        gridMiembros = new Grid<>(Judoka.class, false);
        gridMiembros.setSizeFull();
        gridMiembros.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.nombre")).setAutoWidth(true);
        gridMiembros.addColumn(Judoka::getGradoCinturon)
                .setHeader(traduccionService.get("generic.cinturon")).setAutoWidth(true);

        gridMiembros.addComponentColumn(judoka -> {
            Button btnSacar = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            btnSacar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnSacar.addClickListener(e -> {
                grupoService.removeJudokaFromGrupo(grupo.getId(), judoka.getId());
                actualizarListaMiembros(grupo);
                comboCandidatos.setItems(grupoService.findJudokasDisponibles(grupo.getId(), "", null, null));
                gruposGrid.getDataProvider().refreshItem(grupo);
            });
            return btnSacar;
        }).setHeader(traduccionService.get("btn.quitar"));

        actualizarListaMiembros(grupo);

        layout.add(new H3(traduccionService.get("grupos.section.agregar")), addLayout,
                new H3(traduccionService.get("grupos.section.actuales")), gridMiembros);

        Button btnCerrar = new Button(traduccionService.get("btn.cerrar"), e -> dialogoMiembros.close());
        dialogoMiembros.getFooter().add(btnCerrar);

        dialogoMiembros.add(layout);
        dialogoMiembros.open();
    }

    private void actualizarListaMiembros(GrupoEntrenamiento grupo) {
        List<Judoka> miembros = grupoService.findJudokasEnGrupo(grupo.getId(), "", null, null);
        gridMiembros.setItems(miembros);
    }

    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(event -> guardarGrupo(event.getData()));
        form.addCancelListener(event -> cerrarFormulario());
    }

    private void buildLayout() {
        HorizontalLayout toolbar = new HorizontalLayout(filtros, btnNuevoGrupo);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);

        VerticalLayout mainContent = new VerticalLayout(
                new H1(traduccionService.get("grupos.titulo")), // "Gestión de Grupos"
                toolbar,
                gruposGrid,
                form
        );
        mainContent.setSizeFull();
        mainContent.setPadding(false);

        add(mainContent);
    }

    private void abrirFormularioNuevo() {
        form.setBean(new GrupoEntrenamiento());
        form.setVisible(true);
        btnNuevoGrupo.setEnabled(false);
    }

    private void abrirFormularioEdicion(GrupoEntrenamiento grupo) {
        form.setBean(grupo);
        form.setVisible(true);
        btnNuevoGrupo.setEnabled(false);
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        form.setBean(null);
        btnNuevoGrupo.setEnabled(true);
        gruposGrid.asSingleSelect().clear();
    }

    private void guardarGrupo(GrupoEntrenamiento grupo) {
        try {
            grupoService.save(grupo);
            NotificationHelper.success(traduccionService.get("msg.success.saved") + ": " + grupo.getNombre());
            cerrarFormulario();
            gruposGrid.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.generic") + ": " + e.getMessage());
        }
    }

    private void confirmarEliminarGrupo(GrupoEntrenamiento grupo) {
        if (grupo.getId() == null) return;
        try {
            grupoService.deleteGrupo(grupo.getId());
            NotificationHelper.success(traduccionService.get("msg.success.deleted"));
            gruposGrid.getDataProvider().refreshAll();
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.generic"));
        }
    }

    private Stream<GrupoEntrenamiento> fetchGrupos(Query<GrupoEntrenamiento, FiltroJudokaLayout.SearchParams> query) {
        FiltroJudokaLayout.SearchParams filtros = query.getFilter().orElse(null);
        String filter = (filtros != null && filtros.nombre() != null) ? filtros.nombre() : "";
        return grupoService.findAll(query.getOffset(), query.getLimit(), filter).stream();
    }

    private int countGrupos(Query<GrupoEntrenamiento, FiltroJudokaLayout.SearchParams> query) {
        FiltroJudokaLayout.SearchParams filtros = query.getFilter().orElse(null);
        String filter = (filtros != null && filtros.nombre() != null) ? filtros.nombre() : "";
        return (int) grupoService.count(filter);
    }
}