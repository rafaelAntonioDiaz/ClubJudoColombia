package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.FiltroJudokaLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.component.GridActionComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.GrupoForm;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Vista para que el Sensei gestione grupos con paginación real y API moderna.
 *
 * @author RafaelDiaz
 * @version 4.0 (Armonizada)
 * @since 2025-11-19
 */
@Route("gestion-grupos")
@RolesAllowed("ROLE_SENSEI")
public class SenseiGruposView extends SenseiLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SenseiGruposView.class);

    private final GrupoEntrenamientoService grupoService;

    private final Grid<GrupoEntrenamiento> gruposGrid = new Grid<>(GrupoEntrenamiento.class, false);
    private final GrupoForm form = new GrupoForm();
    private final FiltroJudokaLayout filtros;
    private final Button btnNuevoGrupo = new Button(new Icon(VaadinIcon.PLUS));
    private final Div botonFlotanteContenedor = new Div();

    public SenseiGruposView(GrupoEntrenamientoService grupoService,
                            SecurityService securityService,
                            AccessAnnotationChecker accessChecker) {
        super(securityService, accessChecker);
        this.grupoService = grupoService;

        configureBotonFlotante();
        configureGrid();
        this.filtros = new FiltroJudokaLayout(
                (SerializableConsumer<FiltroJudokaLayout.SearchParams>)
                        searchParams -> {
                    logger.debug("Filtros aplicados: {}", searchParams);
                    gruposGrid.getDataProvider().refreshAll();
                }
        );
        configureForm();
        buildLayout();

        logger.info("SenseiGruposView inicializada correctamente");
    }

    private void configureBotonFlotante() {
        btnNuevoGrupo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevoGrupo.addClassName("sensei-grupos-btn-nuevo");
        btnNuevoGrupo.setTooltipText("Crear Nuevo Grupo");

        botonFlotanteContenedor.add(btnNuevoGrupo);
        botonFlotanteContenedor.getStyle()
                .set("position", "fixed")
                .set("bottom", "30px")
                .set("right", "30px")
                .set("z-index", "1000");

        btnNuevoGrupo.addClickListener(
                (ComponentEventListener<ClickEvent<Button>> & Serializable)
                        event -> abrirFormularioNuevo()
        );
    }

    private void configureGrid() {
        gruposGrid.addColumn(GrupoEntrenamiento::getNombre)
                .setHeader("Nombre")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        gruposGrid.addColumn(GrupoEntrenamiento::getDescripcion)
                .setHeader("Descripción")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(2);

        gruposGrid.addColumn(g -> g.getJudokas().size())
                .setHeader("Judokas")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        gruposGrid.addComponentColumn(this::crearAccionesColumna)
                .setHeader("Acciones")
                .setAutoWidth(true)
                .setFlexGrow(0);

        gruposGrid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchGrupos,
                this::countGrupos
        ));

        gruposGrid.setPageSize(20);
        gruposGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gruposGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                abrirFormularioEdicion(event.getValue());
            } else {
                cerrarFormulario();
            }
        });
    }

    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(
                (ComponentEventListener<BaseForm.SaveEvent<GrupoEntrenamiento>> & Serializable)
                        event -> guardarGrupo(event.getData())
        );
        form.addCancelListener(
                (ComponentEventListener<BaseForm.CancelEvent<GrupoEntrenamiento>> & Serializable)
                        event -> cerrarFormulario()
        );
    }

    private void buildLayout() {
        VerticalLayout mainContent = new VerticalLayout(
                new H1("Gestión de Grupos de Entrenamiento"),
                filtros,
                gruposGrid,
                form
        );
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        VerticalLayout rootContainer = new VerticalLayout(mainContent, botonFlotanteContenedor);
        rootContainer.setSizeFull();
        rootContainer.setPadding(false);
        rootContainer.setSpacing(false);

        setContent(rootContainer);
    }

    private Component crearAccionesColumna(GrupoEntrenamiento grupo) {
        GridActionComponent actions = new GridActionComponent(
                () -> abrirFormularioEdicion(grupo),
                () -> confirmarEliminarGrupo(grupo)
        );
        actions.setEditVisible(grupo.getId() != null);
        return actions;
    }

    private void abrirFormularioNuevo() {
        form.setBean(new GrupoEntrenamiento());
        form.setVisible(true);
        btnNuevoGrupo.setEnabled(false);
        logger.debug("Formulario nuevo abierto");
    }

    private void abrirFormularioEdicion(GrupoEntrenamiento grupo) {
        form.setBean(grupo);
        form.setVisible(true);
        btnNuevoGrupo.setEnabled(false);
        gruposGrid.asSingleSelect().setValue(grupo);
        logger.debug("Formulario edición abierto: {}", grupo.getId());
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        form.setBean(null);
        btnNuevoGrupo.setEnabled(true);
        gruposGrid.asSingleSelect().clear();
        logger.debug("Formulario cerrado");
    }

    private void guardarGrupo(GrupoEntrenamiento grupo) {
        try {
            grupoService.save(grupo);
            NotificationHelper.success("Grupo guardado: " + grupo.getNombre());
            cerrarFormulario();
            gruposGrid.getDataProvider().refreshAll();
            logger.info("Grupo {} guardado", grupo.getId());
        } catch (Exception e) {
            NotificationHelper.error("Error al guardar: " + e.getMessage());
            logger.error("Error guardando grupo", e);
        }
    }

    private void confirmarEliminarGrupo(GrupoEntrenamiento grupo) {
        if (grupo.getId() == null) {
            NotificationHelper.warning("No se puede eliminar un grupo no guardado");
            return;
        }

        try {
            grupoService.deleteGrupo(grupo.getId());
            NotificationHelper.success("Grupo eliminado: " + grupo.getNombre());
            gruposGrid.getDataProvider().refreshAll();
            logger.warn("Grupo {} eliminado", grupo.getId());
        } catch (Exception e) {
            NotificationHelper.error("Error al eliminar: " + e.getMessage());
            logger.error("Error eliminando grupo", e);
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