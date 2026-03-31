package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

@Route(value = "gestion-grupos", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Gestión de Grupos | Club Judo Colombia")
public class SenseiGruposView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SenseiGruposView.class);

    // ──────────────────────────────────────────────────────────────────────────
    // Dependencias
    // ──────────────────────────────────────────────────────────────────────────
    private final GrupoEntrenamientoService grupoService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;

    // ──────────────────────────────────────────────────────────────────────────
    // Componentes de la vista
    // ──────────────────────────────────────────────────────────────────────────
    private final Grid<GrupoEntrenamiento> gruposGrid = new Grid<>(GrupoEntrenamiento.class, false);
    private final GrupoForm form;
    private final Button btnNuevoGrupo;
    private final FiltroJudokaLayout filtros;

    // ──────────────────────────────────────────────────────────────────────────
    // Estado
    // ──────────────────────────────────────────────────────────────────────────
    private List<GrupoEntrenamiento> gruposOriginales;
    private ListDataProvider<GrupoEntrenamiento> dataProvider;
    private FiltroJudokaLayout.SearchParams currentFilter = null;

    public SenseiGruposView(GrupoEntrenamientoService grupoService,
                            SecurityService securityService,
                            TraduccionService traduccionService) {
        this.grupoService = grupoService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.form = new GrupoForm(traduccionService);
        this.btnNuevoGrupo = new Button(
                traduccionService.get("grupos.btn.nuevo"),
                new Icon(VaadinIcon.PLUS)
        );

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Sensei sensei = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        // CORRECCIÓN: Se filtra esTarifario=false para mostrar SOLO grupos deportivos.
        // Los grupos tarifarios (esTarifario=true) se gestionan en GestionTarifasView.
        // Se requiere que GrupoEntrenamientoService tenga el método:
        //   findBySenseiAndEsTarifarioWithJudokas(Sensei sensei, boolean esTarifario)
        // Si el servicio aún no lo tiene, agrégalo como:
        //   return repo.findBySenseiAndEsTarifario(sensei, false)  ← con JOIN FETCH judokas
        this.gruposOriginales = grupoService.findBySenseiAndEsTarifarioWithJudokas(sensei, false);

        this.dataProvider = new ListDataProvider<>(gruposOriginales);
        gruposGrid.setDataProvider(dataProvider);

        configureGrid();
        configureForm();
        configureBotonNuevo();

        this.filtros = new FiltroJudokaLayout(
                searchParams -> {
                    this.currentFilter = searchParams;
                    aplicarFiltro();
                }, traduccionService
        );

        buildLayout();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Filtro en memoria por nombre de grupo
    // ──────────────────────────────────────────────────────────────────────────
    private void aplicarFiltro() {
        if (currentFilter == null
                || currentFilter.nombre() == null
                || currentFilter.nombre().isEmpty()) {
            dataProvider.setFilter(null);
        } else {
            String filtro = currentFilter.nombre().toLowerCase();
            dataProvider.setFilter(
                    grupo -> grupo.getNombre().toLowerCase().contains(filtro)
            );
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Configuración del Grid
    // Solo columnas deportivas: nombre, descripción, miembros, GPS y acciones.
    // Sin columnas de tarifa (eso es responsabilidad de GestionTarifasView).
    // ──────────────────────────────────────────────────────────────────────────
    private void configureGrid() {
        // Columna: nombre del grupo deportivo
        gruposGrid.addColumn(GrupoEntrenamiento::getNombre)
                .setHeader(traduccionService.get("grupos.grid.nombre"))
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Columna: descripción del grupo
        gruposGrid.addColumn(GrupoEntrenamiento::getDescripcion)
                .setHeader(traduccionService.get("grupos.grid.descripcion"))
                .setAutoWidth(true)
                .setFlexGrow(2);

        // Columna: cantidad de judokas inscritos en este grupo deportivo
        gruposGrid.addColumn(new ComponentRenderer<>(grupo -> {
            Span count = new Span(
                    grupo.getJudokas().size() + " " + traduccionService.get("grupos.label.alumnos")
            );
            count.getElement().getThemeList().add("badge contrast");
            return count;
        })).setHeader(traduccionService.get("grupos.grid.miembros")).setAutoWidth(true);

        // Columna: indicador visual de si el grupo tiene ubicación GPS configurada
        gruposGrid.addComponentColumn(grupo -> {
            Icon gpsIcon = VaadinIcon.MAP_MARKER.create();
            if (grupo.getLatitud() != null && grupo.getLongitud() != null) {
                gpsIcon.setColor("green");
                gpsIcon.setTooltipText("GPS: " + grupo.getLatitud() + ", " + grupo.getLongitud()
                        + " (radio " + grupo.getRadioPermitidoMetros() + "m)");
            } else {
                gpsIcon.setColor("gray");
                gpsIcon.setTooltipText(traduccionService.get("grupos.tooltip.sin_ubicacion"));
            }
            return gpsIcon;
        }).setHeader("GPS").setAutoWidth(true);

        // Columna: botones de acción (gestionar miembros, editar, eliminar)
        gruposGrid.addComponentColumn(this::crearAccionesColumna)
                .setHeader(traduccionService.get("generic.acciones"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        gruposGrid.setPageSize(20);
        gruposGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Botones de acción por fila del grid
    // ──────────────────────────────────────────────────────────────────────────
    private Component crearAccionesColumna(GrupoEntrenamiento grupo) {
        HorizontalLayout actions = new HorizontalLayout();

        // Navegar a la vista de asignación de judokas a este grupo
        Button btnMiembros = new Button(new Icon(VaadinIcon.USERS));
        btnMiembros.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnMiembros.setTooltipText(traduccionService.get("grupos.tooltip.gestionar_miembros"));
        btnMiembros.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(AsignacionJudokasView.class, grupo.getId()))
        );

        // Abrir formulario de edición de datos deportivos del grupo
        Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnEditar.setTooltipText(traduccionService.get("btn.editar"));
        btnEditar.addClickListener(e -> abrirFormularioEdicion(grupo));

        // Eliminar el grupo deportivo
        Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnEliminar.setTooltipText(traduccionService.get("btn.eliminar"));
        btnEliminar.addClickListener(e -> confirmarEliminarGrupo(grupo));

        actions.add(btnMiembros, btnEditar, btnEliminar);
        return actions;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Configuración del formulario de creación/edición de grupo deportivo
    // ──────────────────────────────────────────────────────────────────────────
    private void configureForm() {
        form.setVisible(false);
        form.addSaveListener(this::guardarGrupo);
        form.addCancelListener(event -> cerrarFormulario());
    }

    private void configureBotonNuevo() {
        btnNuevoGrupo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevoGrupo.addClickListener(e -> abrirFormularioNuevo());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout principal
    // ──────────────────────────────────────────────────────────────────────────
    private void buildLayout() {
        HorizontalLayout toolbar = new HorizontalLayout(filtros, btnNuevoGrupo);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);

        VerticalLayout mainContent = new VerticalLayout(
                new H1(traduccionService.get("grupos.titulo")),
                toolbar,
                gruposGrid,
                form
        );
        mainContent.setSizeFull();
        mainContent.setPadding(false);

        add(mainContent);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Apertura del formulario
    // ──────────────────────────────────────────────────────────────────────────
    private void abrirFormularioNuevo() {
        // Al crear un grupo desde aquí, siempre será deportivo (esTarifario=false)
        GrupoEntrenamiento nuevo = new GrupoEntrenamiento();
        nuevo.setEsTarifario(false);
        form.setBean(nuevo);
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

    // ──────────────────────────────────────────────────────────────────────────
    // Persistencia del grupo deportivo
    // ──────────────────────────────────────────────────────────────────────────
    private void guardarGrupo(BaseForm.SaveEvent<GrupoEntrenamiento> event) {
        try {
            GrupoEntrenamiento grupo = event.getData();
            Sensei sensei = securityService.getAuthenticatedSensei()
                    .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

            if (grupo.getId() == null) {
                // Nuevo grupo deportivo: esTarifario=false siempre desde esta vista
                grupo = grupoService.crearGrupo(
                        sensei,
                        grupo.getNombre(),
                        grupo.getDescripcion(),
                        grupo.getTarifaMensual(),
                        grupo.isIncluyeMatricula(),
                        grupo.getMontoMatricula(),
                        grupo.getDiasGracia(),
                        false   // esTarifario = false — es grupo deportivo
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
            recargarLista();

        } catch (Exception e) {
            NotificationHelper.error(
                    traduccionService.get("error.generic") + ": " + e.getMessage()
            );
        }
    }

    private void confirmarEliminarGrupo(GrupoEntrenamiento grupo) {
        if (grupo.getId() == null) return;
        try {
            grupoService.deleteGrupo(grupo.getId());
            NotificationHelper.success(traduccionService.get("msg.success.deleted"));
            recargarLista();
        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.generic"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Recarga de la lista manteniendo el filtro activo y el tipo correcto
    // ──────────────────────────────────────────────────────────────────────────
    private void recargarLista() {
        Sensei sensei = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        // Solo grupos deportivos (esTarifario=false), igual que en el constructor
        gruposOriginales = grupoService.findBySenseiAndEsTarifarioWithJudokas(sensei, false);
        dataProvider = new ListDataProvider<>(gruposOriginales);
        gruposGrid.setDataProvider(dataProvider);

        // Mantener el filtro de búsqueda activo si lo había
        aplicarFiltro();
    }
}