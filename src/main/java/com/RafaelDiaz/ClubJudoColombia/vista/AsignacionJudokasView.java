package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- Importado
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.stream.Stream;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
/**
 * Vista para asignar y remover Judokas de Grupos de Entrenamiento.
 * Actualizada con TraduccionService.
 */
@Route("asignar-judokas")
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class AsignacionJudokasView extends SenseiLayout implements Serializable, HasUrlParameter<Long> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AsignacionJudokasView.class);

    private final GrupoEntrenamientoService grupoService;
    private final JudokaService judokaService;
    private final TraduccionService traduccionService;

    private final Grid<Judoka> gridDisponibles = new Grid<>(Judoka.class, false);
    private final Grid<Judoka> gridAsignados = new Grid<>(Judoka.class, false);
    private final com.vaadin.flow.component.combobox.ComboBox<GrupoEntrenamiento> grupoCombo; // Inicializar en constructor
    private final FiltroJudokaLayout filtrosDisponibles;
    private final FiltroJudokaLayout filtrosAsignados;

    private GrupoEntrenamiento grupoSeleccionado;

    public AsignacionJudokasView(GrupoEntrenamientoService grupoService,
                                 JudokaService judokaService,
                                 SecurityService securityService,
                                 AccessAnnotationChecker accessChecker,
                                 ConfiguracionService configuracionService,
                                 AuthenticationContext authenticationContext,
                                 TraduccionService traduccionService) { // <--- Inyección

        super(securityService, accessChecker, configuracionService, authenticationContext);

        this.grupoService = grupoService;
        this.judokaService = judokaService;
        this.traduccionService = traduccionService;

        // Inicializar componentes UI con traducción
        this.grupoCombo = new com.vaadin.flow.component.combobox.ComboBox<>(
                traduccionService.get("lbl.seleccionar.grupo")
        );

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
        // --- Grid de Judokas Disponibles ---
        gridDisponibles.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("col.nombre.completo"))
                .setSortable(true)
                .setAutoWidth(true);

        // i18n: Traducción automática de Enum Grado
        gridDisponibles.addColumn(j -> traduccionService.get(j.getGrado()))
                .setHeader(traduccionService.get("col.grado"))
                .setSortable(true)
                .setAutoWidth(true);

        // i18n: Traducción automática de Enum Sexo (si existe como Enum)
        gridDisponibles.addColumn(j -> traduccionService.get(j.getSexo()))
                .setHeader(traduccionService.get("col.sexo"))
                .setSortable(true)
                .setAutoWidth(true);

        gridDisponibles.addColumn(j -> j.getEdad() + " " + traduccionService.get("lbl.anios"))
                .setHeader(traduccionService.get("col.edad"))
                .setSortable(true)
                .setAutoWidth(true);

        gridDisponibles.addComponentColumn(this::crearBotonAsignar)
                .setHeader(traduccionService.get("col.accion"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        // --- Grid de Judokas Asignados ---
        gridAsignados.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("col.nombre.completo"))
                .setSortable(true)
                .setAutoWidth(true);

        gridAsignados.addColumn(j -> traduccionService.get(j.getGrado()))
                .setHeader(traduccionService.get("col.grado"))
                .setSortable(true)
                .setAutoWidth(true);

        gridAsignados.addColumn(j -> traduccionService.get(j.getSexo()))
                .setHeader(traduccionService.get("col.sexo"))
                .setSortable(true)
                .setAutoWidth(true);

        gridAsignados.addColumn(j -> j.getEdad() + " " + traduccionService.get("lbl.anios"))
                .setHeader(traduccionService.get("col.edad"))
                .setSortable(true)
                .setAutoWidth(true);

        gridAsignados.addComponentColumn(this::crearBotonRemover)
                .setHeader(traduccionService.get("col.accion"))
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
        // i18n: Tooltip dinámico
        String tooltip = traduccionService.get("tooltip.asignar.grupo") +
                (grupoSeleccionado != null ? " " + grupoSeleccionado.getNombre() : "");
        btn.setTooltipText(tooltip);

        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> asignarJudoka(judoka));
        return btn;
    }

    private com.vaadin.flow.component.Component crearBotonRemover(Judoka judoka) {
        Button btn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        // i18n: Tooltip
        btn.setTooltipText(traduccionService.get("tooltip.remover.grupo"));

        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> removerJudoka(judoka));
        return btn;
    }

    private void buildLayout() {
        // i18n: Título
        H2 titulo = new H2(traduccionService.get("view.asignacion.titulo"));

        VerticalLayout panelDisponibles = new VerticalLayout(
                new Span(traduccionService.get("lbl.judokas.disponibles")),
                filtrosDisponibles,
                gridDisponibles
        );
        panelDisponibles.setWidth("50%");

        VerticalLayout panelAsignados = new VerticalLayout(
                new Span(traduccionService.get("lbl.judokas.grupo")),
                filtrosAsignados,
                gridAsignados
        );
        panelAsignados.setWidth("50%");

        HorizontalLayout gridsLayout = new HorizontalLayout(panelDisponibles, panelAsignados);
        gridsLayout.setSizeFull();

        VerticalLayout mainContent = new VerticalLayout(titulo, grupoCombo, gridsLayout);
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        com.vaadin.flow.component.html.Div wrapper = new com.vaadin.flow.component.html.Div(mainContent);
        wrapper.setSizeFull();
        setContent(wrapper);
    }

    private void actualizarAmbosGrids() {
        gridDisponibles.getDataProvider().refreshAll();
        gridAsignados.getDataProvider().refreshAll();
    }

    private void asignarJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) return;
        try {
            grupoService.addJudokaToGrupo(grupoSeleccionado.getId(), judoka.getId());
            // i18n: Mensaje de éxito
            NotificationHelper.success(traduccionService.get("msg.exito.asignacion") +
                    " " + grupoSeleccionado.getNombre());
            actualizarAmbosGrids();
            logger.info("Asignación exitosa: Judoka {} -> Grupo {}", judoka.getId(), grupoSeleccionado.getId());
        } catch (Exception e) {
            // i18n: Mensaje de error
            NotificationHelper.error(traduccionService.get("msg.error.asignacion") + ": " + e.getMessage());
            logger.error("Error en asignación", e);
        }
    }

    private void removerJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) return;
        try {
            grupoService.removeJudokaFromGrupo(grupoSeleccionado.getId(), judoka.getId());
            // i18n: Mensaje de éxito
            NotificationHelper.success(traduccionService.get("msg.exito.remocion"));
            actualizarAmbosGrids();
            logger.info("Remoción exitosa: Judoka {} <- Grupo {}", judoka.getId(), grupoSeleccionado.getId());
        } catch (Exception e) {
            // i18n: Mensaje de error
            NotificationHelper.error(traduccionService.get("msg.error.remocion") + ": " + e.getMessage());
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
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long grupoId) {
        if (grupoId != null) {
            // Asumiendo que tienes un método findById en tu servicio.
            // Si devuelve Optional, usamos ifPresent. Si devuelve la entidad directo, ajústalo.
            grupoService.findById(grupoId).ifPresent(grupo -> {
                this.grupoSeleccionado = grupo;
                this.grupoCombo.setValue(grupo);
                this.grupoCombo.setReadOnly(true); // Bloqueamos el combo para que el Sensei no se salga del contexto
                actualizarAmbosGrids();
            });
        }
    }
}