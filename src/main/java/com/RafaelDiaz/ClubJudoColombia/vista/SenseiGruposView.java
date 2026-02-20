package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.FiltroJudokaLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.GrupoForm;
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

    private SecurityService securityService;

    public SenseiGruposView(GrupoEntrenamientoService grupoService,
                            SecurityService securityService,
                            TraduccionService traduccionService) {
        this.grupoService = grupoService;
        this.traduccionService = traduccionService;
        this.securityService = securityService;

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
        btnMiembros.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(AsignacionJudokasView.class, grupo.getId()))
        );

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

    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(this::guardarGrupo);
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

    private void guardarGrupo(BaseForm.SaveEvent<GrupoEntrenamiento> event) {
        try {
            GrupoEntrenamiento grupo = event.getData();

            securityService.getAuthenticatedSensei().ifPresent(grupo::setSensei);

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