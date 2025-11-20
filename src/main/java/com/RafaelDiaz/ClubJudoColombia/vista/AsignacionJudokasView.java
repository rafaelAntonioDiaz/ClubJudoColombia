package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.FiltroJudokaLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Vista para asignar y remover Judokas de Grupos de Entrenamiento.
 * Lazy loading real, filtros avanzados y persistencia inmediata.
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-20
 */
@Route("asignar-judokas")
@RolesAllowed("ROLE_SENSEI")
public class AsignacionJudokasView extends SenseiLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AsignacionJudokasView.class);

    private final GrupoEntrenamientoService grupoService;
    private final JudokaService judokaService;

    private final Grid<Judoka> gridDisponibles = new Grid<>(Judoka.class, false);
    private final Grid<Judoka> gridAsignados = new Grid<>(Judoka.class, false);
    private final com.vaadin.flow.component.combobox.ComboBox<GrupoEntrenamiento> grupoCombo = new com.vaadin.flow.component.combobox.ComboBox<>("Seleccionar Grupo");
    private final FiltroJudokaLayout filtrosDisponibles;
    private final FiltroJudokaLayout filtrosAsignados;

    private GrupoEntrenamiento grupoSeleccionado;

    public AsignacionJudokasView(GrupoEntrenamientoService grupoService,
                                 JudokaService judokaService,
                                 SecurityService securityService,
                                 AccessAnnotationChecker accessChecker) {
        super(securityService, accessChecker);
        this.grupoService = grupoService;
        this.judokaService = judokaService;

        configureGrupoCombo();
        configureGrids();

        this.filtrosDisponibles = new FiltroJudokaLayout(
                (FiltroJudokaLayout.SearchParams searchParams) -> gridDisponibles.getDataProvider().refreshAll()
        );
        this.filtrosAsignados = new FiltroJudokaLayout(
                (FiltroJudokaLayout.SearchParams searchParams) -> gridAsignados.getDataProvider().refreshAll()
        );

        buildLayout();
        logger.info("AsignacionJudokasView inicializada correctamente");
    }

    private void configureGrupoCombo() {
        grupoCombo.setItems(grupoService.findAll(0, 100, ""));
        grupoCombo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        grupoCombo.setWidth("400px");
        grupoCombo.addValueChangeListener(event -> {
            this.grupoSeleccionado = event.getValue();
            if (grupoSeleccionado != null) {
                actualizarAmbosGrids();
            }
        });
    }

    private void configureGrids() {
        // Grid de Judokas Disponibles
        gridDisponibles.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader("Nombre Completo")
                .setSortable(true)
                .setAutoWidth(true);
        gridDisponibles.addColumn(Judoka::getGrado)
                .setHeader("Grado")
                .setSortable(true)
                .setAutoWidth(true);
        gridDisponibles.addColumn(Judoka::getSexo)
                .setHeader("Sexo")
                .setSortable(true)
                .setAutoWidth(true);
        gridDisponibles.addColumn(j -> j.getEdad() + " años")
                .setHeader("Edad")
                .setSortable(true)
                .setAutoWidth(true);
        gridDisponibles.addComponentColumn(this::crearBotonAsignar)
                .setHeader("Acción")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Grid de Judokas Asignados
        gridAsignados.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader("Nombre Completo")
                .setSortable(true)
                .setAutoWidth(true);
        gridAsignados.addColumn(Judoka::getGrado)
                .setHeader("Grado")
                .setSortable(true)
                .setAutoWidth(true);
        gridAsignados.addColumn(Judoka::getSexo)
                .setHeader("Sexo")
                .setSortable(true)
                .setAutoWidth(true);
        gridAsignados.addColumn(j -> j.getEdad() + " años")
                .setHeader("Edad")
                .setSortable(true)
                .setAutoWidth(true);
        gridAsignados.addComponentColumn(this::crearBotonRemover)
                .setHeader("Acción")
                .setAutoWidth(true)
                .setFlexGrow(0);

        gridDisponibles.setPageSize(20);
        gridAsignados.setPageSize(20);
        gridDisponibles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridAsignados.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gridDisponibles.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchJudokasDisponibles,
                this::countJudokasDisponibles
        ));
        gridAsignados.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchJudokasAsignados,
                this::countJudokasAsignados
        ));
    }

    private com.vaadin.flow.component.Component crearBotonAsignar(Judoka judoka) {
        Button btn = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btn.setTooltipText("Asignar a " + grupoSeleccionado.getNombre());
        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> asignarJudoka(judoka));
        return btn;
    }

    private com.vaadin.flow.component.Component crearBotonRemover(Judoka judoka) {
        Button btn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        btn.setTooltipText("Remover del grupo");
        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> removerJudoka(judoka));
        return btn;
    }

    private void buildLayout() {
        H2 titulo = new H2("Asignación de Judokas a Grupos");

        VerticalLayout panelDisponibles = new VerticalLayout(
                new Span("Judokas Disponibles"), filtrosDisponibles, gridDisponibles
        );
        panelDisponibles.setWidth("50%");

        VerticalLayout panelAsignados = new VerticalLayout(
                new Span("Judokas en el Grupo"), filtrosAsignados, gridAsignados
        );
        panelAsignados.setWidth("50%");

        HorizontalLayout gridsLayout = new HorizontalLayout(panelDisponibles, panelAsignados);
        gridsLayout.setSizeFull();

        VerticalLayout mainContent = new VerticalLayout(grupoCombo, gridsLayout);
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        com.vaadin.flow.component.html.Div content =
                (com.vaadin.flow.component.html.Div) getContent();
        content.add(mainContent);
    }

    private void actualizarAmbosGrids() {
        gridDisponibles.getDataProvider().refreshAll();
        gridAsignados.getDataProvider().refreshAll();
    }

    private void asignarJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) return;
        try {
            grupoService.addJudokaToGrupo(grupoSeleccionado.getId(), judoka.getId());
            NotificationHelper.success("Judoka asignado a " + grupoSeleccionado.getNombre());
            actualizarAmbosGrids();
            logger.info("Asignación exitosa: Judoka {} -> Grupo {}", judoka.getId(), grupoSeleccionado.getId());
        } catch (Exception e) {
            NotificationHelper.error("Error al asignar: " + e.getMessage());
            logger.error("Error en asignación", e);
        }
    }

    private void removerJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) return;
        try {
            grupoService.removeJudokaFromGrupo(grupoSeleccionado.getId(), judoka.getId());
            NotificationHelper.success("Judoka removido del grupo");
            actualizarAmbosGrids();
            logger.info("Remoción exitosa: Judoka {} <- Grupo {}", judoka.getId(), grupoSeleccionado.getId());
        } catch (Exception e) {
            NotificationHelper.error("Error al remover: " + e.getMessage());
            logger.error("Error en remoción", e);
        }
    }

    // Lazy Loading para Judokas Disponibles
    private Stream<Judoka> fetchJudokasDisponibles(Query<Judoka, Void> query) {
        if (grupoSeleccionado == null) return Stream.empty();
        FiltroJudokaLayout.SearchParams params = filtrosDisponibles.getSearchParams();
        return grupoService.findJudokasDisponibles(
                grupoSeleccionado.getId(),
                params.nombre(),
                params.sexo(),
                params.grado()
        ).stream().skip(query.getOffset()).limit(query.getLimit());
    }

    private int countJudokasDisponibles(Query<Judoka, Void> query) {
        if (grupoSeleccionado == null) return 0;
        FiltroJudokaLayout.SearchParams params = filtrosDisponibles.getSearchParams();
        return grupoService.findJudokasDisponibles(
                grupoSeleccionado.getId(),
                params.nombre(),
                params.sexo(),
                params.grado()
        ).size();
    }

    // Lazy Loading para Judokas Asignados
    private Stream<Judoka> fetchJudokasAsignados(Query<Judoka, Void> query) {
        if (grupoSeleccionado == null) return Stream.empty();
        FiltroJudokaLayout.SearchParams params = filtrosAsignados.getSearchParams();
        return grupoService.findJudokasEnGrupo(
                grupoSeleccionado.getId(),
                params.nombre(),
                params.sexo(),
                params.grado()
        ).stream().skip(query.getOffset()).limit(query.getLimit());
    }

    private int countJudokasAsignados(Query<Judoka, Void> query) {
        if (grupoSeleccionado == null) return 0;
        FiltroJudokaLayout.SearchParams params = filtrosAsignados.getSearchParams();
        return grupoService.findJudokasEnGrupo(
                grupoSeleccionado.getId(),
                params.nombre(),
                params.sexo(),
                params.grado()
        ).size();
    }
}