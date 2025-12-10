package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TareaDiariaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // NUEVO IMPORT
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.stream.Stream;

@Route(value = "biblioteca-tareas", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Biblioteca de Ejercicios | Club Judo Colombia")
public class BibliotecaView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BibliotecaView.class);

    private final TareaDiariaService tareaDiariaService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService; // NUEVO: Servicio de traducción

    private final Grid<TareaDiaria> grid = new Grid<>(TareaDiaria.class, false);
    private final TareaDiariaForm form = new TareaDiariaForm();
    private final Button btnNuevaTarea;

    private Sensei senseiActual;

    public BibliotecaView(TareaDiariaService tareaDiariaService,
                          SecurityService securityService,
                          TraduccionService traduccionService) { // NUEVO: Parámetro añadido
        this.tareaDiariaService = tareaDiariaService;
        this.securityService = securityService;
        this.traduccionService = traduccionService; // NUEVO: Inicialización

        // NUEVO: Botón con texto traducido
        this.btnNuevaTarea = new Button(traduccionService.get("biblioteca.boton.nueva_tarea"));

        configureLayout();
        configureGrid();
        configureForm();
        configureButton();

        loadSensei();
    }

    private void configureLayout() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setSizeFull();
        content.setFlexGrow(1, grid);
        content.setFlexGrow(0.4, form);

        // NUEVO: Título traducido
        add(new H1(traduccionService.get("biblioteca.titulo")), btnNuevaTarea, content);
    }

    private void configureGrid() {
        grid.setWidthFull();

        grid.addColumn(TareaDiaria::getNombre)
                .setHeader(traduccionService.get("biblioteca.grid.nombre_tarea"))
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(TareaDiaria::getMetaTexto)
                .setHeader(traduccionService.get("biblioteca.grid.meta"))
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(TareaDiaria::getDescripcion)
                .setHeader(traduccionService.get("biblioteca.grid.descripcion"))
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addComponentColumn(tarea -> {
                    if (tarea.getVideoUrl() != null && !tarea.getVideoUrl().isBlank()) {
                        Icon icon = new Icon(VaadinIcon.MOVIE);
                        icon.setTooltipText(traduccionService.get("biblioteca.grid.tooltip.tiene_video"));
                        return icon;
                    }
                    return new Span("");
                }).setHeader(traduccionService.get("biblioteca.grid.video"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(tarea -> {
                    Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
                    editBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    editBtn.setTooltipText(traduccionService.get("biblioteca.grid.tooltip.editar_tarea"));
                    editBtn.addClickListener(
                            (ComponentEventListener<ClickEvent<Button>> & Serializable)
                                    e -> abrirFormularioEdicion(tarea)
                    );
                    return editBtn;
                }).setHeader(traduccionService.get("biblioteca.grid.acciones"))
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Lazy loading con paginación
        grid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchTareas,
                this::countTareas
        ));
        grid.setPageSize(20);
    }

    private void configureForm() {
        form.setVisible(false);
        form.setWidth("100%");

        form.addSaveListener(
                (ComponentEventListener<BaseForm.SaveEvent<TareaDiaria>> & Serializable)
                        event -> guardarTarea(event)
        );
        form.addCancelListener(
                (ComponentEventListener<BaseForm.CancelEvent<TareaDiaria>> & Serializable)
                        event -> cerrarFormulario()
        );
    }

    private void configureButton() {
        btnNuevaTarea.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevaTarea.setIcon(new Icon(VaadinIcon.PLUS));
        btnNuevaTarea.addClickListener(
                (ComponentEventListener<ClickEvent<Button>> & Serializable)
                        event -> abrirFormularioNuevo()
        );
    }

    private void loadSensei() {
        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException(traduccionService.get("biblioteca.error.sensei_no_autenticado")));
        logger.info("Sensei {} cargado", senseiActual.getId());
    }

    private void abrirFormularioNuevo() {
        form.setBean(new TareaDiaria());
        form.setVisible(true);
        btnNuevaTarea.setEnabled(false);
        grid.asSingleSelect().clear();
        logger.debug("Formulario nuevo abierto");
    }

    private void abrirFormularioEdicion(TareaDiaria tarea) {
        form.setBean(tarea);
        form.setVisible(true);
        btnNuevaTarea.setEnabled(false);
        grid.asSingleSelect().setValue(tarea);
        logger.debug("Formulario edición abierto: {}", tarea.getId());
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        form.setBean(null);
        btnNuevaTarea.setEnabled(true);
        grid.asSingleSelect().clear();
        logger.debug("Formulario cerrado");
    }

    private void guardarTarea(BaseForm.SaveEvent<TareaDiaria> event) {
        try {
            TareaDiaria tarea = event.getData();
            tareaDiariaService.guardarTarea(tarea, senseiActual);

            // NUEVO: Mensaje traducido
            NotificationHelper.success(
                    String.format(traduccionService.get("biblioteca.notificacion.tarea_guardada"),
                            tarea.getNombre())
            );
            cerrarFormulario();
            grid.getDataProvider().refreshAll();

            logger.info("Tarea {} guardada", tarea.getId());
        } catch (Exception e) {
            // NUEVO: Mensaje de error traducido
            NotificationHelper.error(
                    traduccionService.get("biblioteca.notificacion.error_guardar") + e.getMessage()
            );
            logger.error("Error guardando tarea", e);
        }
    }

    // ==================== LAZY LOADING ====================

    private Stream<TareaDiaria> fetchTareas(Query<TareaDiaria, Void> query) {
        return tareaDiariaService.findAll().stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    private int countTareas(Query<TareaDiaria, Void> query) {
        return (int) tareaDiariaService.count();
    }
}