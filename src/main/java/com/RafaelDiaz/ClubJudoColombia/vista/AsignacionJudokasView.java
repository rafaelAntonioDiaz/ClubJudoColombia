package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

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
    private final com.vaadin.flow.component.combobox.ComboBox<GrupoEntrenamiento> grupoCombo;
    private final FiltroJudokaLayout filtrosDisponibles;
    private final FiltroJudokaLayout filtrosAsignados;

    private GrupoEntrenamiento grupoSeleccionado;

    public AsignacionJudokasView(GrupoEntrenamientoService grupoService,
                                 JudokaService judokaService,
                                 SecurityService securityService,
                                 AccessAnnotationChecker accessChecker,
                                 ConfiguracionService configuracionService,
                                 AuthenticationContext authenticationContext,
                                 TraduccionService traduccionService) {

        super(securityService, accessChecker, configuracionService, authenticationContext);

        this.grupoService = grupoService;
        this.judokaService = judokaService;
        this.traduccionService = traduccionService;

        this.grupoCombo = new com.vaadin.flow.component.combobox.ComboBox<>(
                traduccionService.get("lbl.seleccionar.grupo")
        );

        configureGrupoCombo();
        configureGrids();

        // Al filtrar, también usamos inyección directa
        this.filtrosDisponibles = new FiltroJudokaLayout(
                (FiltroJudokaLayout.SearchParams searchParams) -> actualizarAmbosGrids(), traduccionService
        );
        this.filtrosAsignados = new FiltroJudokaLayout(
                (FiltroJudokaLayout.SearchParams searchParams) -> actualizarAmbosGrids(), traduccionService
        );

        buildLayout();
        logger.info("AsignacionJudokasView inicializada correctamente");
    }

    private String obtenerNombreCompleto(Judoka j) {
        if (j.getNombre() != null && !j.getNombre().isEmpty()) {
            return j.getNombre() + " " + j.getApellido();
        } else if (j.getUsuario() != null) {
            return j.getUsuario().getNombre() + " " + j.getUsuario().getApellido();
        }
        return traduccionService.get("msg.error.SinNombre");
    }

    private void configureGrupoCombo() {
        // Obtener solo grupos de entrenamiento (esTarifario = false)
        grupoCombo.setItems(grupoService.findAll(0, 100, "", false));
        grupoCombo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        grupoCombo.setWidth("400px");
        grupoCombo.addValueChangeListener(event -> {
            this.grupoSeleccionado = event.getValue();
            actualizarAmbosGrids();
        });
    }

    private void configureGrids() {
        // --- Grid de Judokas Disponibles ---
        gridDisponibles.addColumn(this::obtenerNombreCompleto)
                .setHeader(traduccionService.get("col.nombre.completo"))
                .setSortable(true).setAutoWidth(true);
        gridDisponibles.addColumn(j -> traduccionService.get(j.getGrado()))
                .setHeader(traduccionService.get("col.grado"))
                .setSortable(true).setAutoWidth(true);
        gridDisponibles.addColumn(j -> traduccionService.get(j.getSexo()))
                .setHeader(traduccionService.get("col.sexo"))
                .setSortable(true).setAutoWidth(true);
        gridDisponibles.addColumn(j -> j.getEdad() + " " + traduccionService.get("lbl.anios"))
                .setHeader(traduccionService.get("col.edad"))
                .setSortable(true).setAutoWidth(true);
        gridDisponibles.addComponentColumn(this::crearBotonAsignar)
                .setHeader(traduccionService.get("col.accion"))
                .setAutoWidth(true).setFlexGrow(0);

        // --- Grid de Judokas Asignados ---
        gridAsignados.addColumn(this::obtenerNombreCompleto)
                .setHeader(traduccionService.get("col.nombre.completo"))
                .setSortable(true).setAutoWidth(true);
        gridAsignados.addColumn(j -> traduccionService.get(j.getGrado()))
                .setHeader(traduccionService.get("col.grado"))
                .setSortable(true).setAutoWidth(true);
        gridAsignados.addColumn(j -> traduccionService.get(j.getSexo()))
                .setHeader(traduccionService.get("col.sexo"))
                .setSortable(true).setAutoWidth(true);
        gridAsignados.addColumn(j -> j.getEdad() + " " + traduccionService.get("lbl.anios"))
                .setHeader(traduccionService.get("col.edad"))
                .setSortable(true).setAutoWidth(true);
        gridAsignados.addComponentColumn(this::crearBotonRemover)
                .setHeader(traduccionService.get("col.accion"))
                .setAutoWidth(true).setFlexGrow(0);

        gridDisponibles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridAsignados.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // ¡MAGIA! Hemos eliminado los DataProviders de Vaadin que causaban el caché fantasma.
    }

    private com.vaadin.flow.component.Component crearBotonAsignar(Judoka judoka) {
        Button btn = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        btn.setTooltipText(traduccionService.get("tooltip.asignar.grupo"));
        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> asignarJudoka(judoka));
        return btn;
    }

    private com.vaadin.flow.component.Component crearBotonRemover(Judoka judoka) {
        Button btn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        btn.setTooltipText(traduccionService.get("tooltip.remover.grupo"));
        btn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> removerJudoka(judoka));
        return btn;
    }

    private void buildLayout() {
        H2 titulo = new H2(traduccionService.get("view.asignacion.titulo"));

        VerticalLayout panelDisponibles = new VerticalLayout(
                new Span(traduccionService.get("lbl.judokas.disponibles") + " (" + traduccionService.get("lbl.sala.espera") + ")"),
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

    // --- EL CORAZÓN DE LA SOLUCIÓN: INYECCIÓN DIRECTA DE ITEMS ---
// --- EL CORAZÓN DE LA SOLUCIÓN: LIST DATA PROVIDER CON ID REFORZADO ---
    private void actualizarAmbosGrids() {
        Long groupId = grupoSeleccionado != null ? grupoSeleccionado.getId() : null;

        // 1. Extraer listas frescas y limpias desde la Base de Datos
        FiltroJudokaLayout.SearchParams pDisp = filtrosDisponibles.getSearchParams();
        List<Judoka> disponibles = grupoService.findJudokasDisponibles(
                groupId,
                pDisp != null ? pDisp.nombre() : null,
                pDisp != null ? pDisp.sexo() : null,
                pDisp != null ? pDisp.grado() : null
        );

        // 🛡️ ESCUDO DE IDENTIDAD: Recreamos el proveedor forzando el ID (Destruye caché y evita clones)
        com.vaadin.flow.data.provider.ListDataProvider<Judoka> dpDisp = new com.vaadin.flow.data.provider.ListDataProvider<>(disponibles) {
            @Override
            public Object getId(Judoka item) {
                return item.getId(); // ¡La regla de oro de identidad!
            }
        };
        gridDisponibles.setDataProvider(dpDisp);

        // 2. Repetir la misma protección para los asignados
        if (groupId != null) {
            FiltroJudokaLayout.SearchParams pAsig = filtrosAsignados.getSearchParams();
            List<Judoka> asignados = grupoService.findJudokasEnGrupo(
                    groupId,
                    pAsig != null ? pAsig.nombre() : null,
                    pAsig != null ? pAsig.sexo() : null,
                    pAsig != null ? pAsig.grado() : null
            );

            com.vaadin.flow.data.provider.ListDataProvider<Judoka> dpAsig = new com.vaadin.flow.data.provider.ListDataProvider<>(asignados) {
                @Override
                public Object getId(Judoka item) {
                    return item.getId();
                }
            };
            gridAsignados.setDataProvider(dpAsig);

        } else {
            gridAsignados.setDataProvider(new com.vaadin.flow.data.provider.ListDataProvider<>(java.util.Collections.emptyList()));
        }
    }

    private void asignarJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) {
            NotificationHelper.error(traduccionService.get("msg.error.sinGrupo"));
            return;
        }
        try {
            grupoService.addJudokaToGrupo(grupoSeleccionado.getId(), judoka.getId());
            NotificationHelper.success(traduccionService.get("msg.exito.asignacion") + " " + grupoSeleccionado.getNombre());
            actualizarAmbosGrids(); // Repinta en tiempo real inyectando las listas nuevas
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("msg.error.asignacion") + ": " + e.getMessage());
        }
    }

    private void removerJudoka(Judoka judoka) {
        if (grupoSeleccionado == null) return;
        try {
            grupoService.removeJudokaFromGrupo(grupoSeleccionado.getId(), judoka.getId());
            NotificationHelper.success(traduccionService.get("msg.exito.remocion"));
            actualizarAmbosGrids(); // Repinta en tiempo real inyectando las listas nuevas
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("msg.error.remocion") + ": " + e.getMessage());
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long grupoId) {
        if (grupoId != null) {
            grupoService.findById(grupoId).ifPresent(grupo -> {
                this.grupoSeleccionado = grupo;
                this.grupoCombo.setValue(grupo);
                this.grupoCombo.setReadOnly(true);
                actualizarAmbosGrids(); // Carga Inicial
            });
        } else {
            actualizarAmbosGrids(); // Carga la sala de espera si entra sin ID
        }
    }
}