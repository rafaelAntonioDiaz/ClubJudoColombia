package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TareaDiariaService;
import com.RafaelDiaz.ClubJudoColombia.vista.form.BaseForm;
import com.RafaelDiaz.ClubJudoColombia.vista.form.TareaDiariaForm;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * --- VISTA ARMONIZADA ---
 * Gestión de biblioteca de Tareas Diarias con:
 * <ul>
 *   <li>Lazy loading real</li>
 *   <li>Serialización para Spring DevTools</li>
 *   <li>API coherente con BaseForm (setBean, event.getData)</li>
 *   <li>Notificaciones estandarizadas</li>
 * </ul>
 *
 * @author RafaelDiaz
 * @version 2.0 (Armonizada)
 * @since 2025-11-19
 */
@Route("biblioteca-tareas")
@RolesAllowed("ROLE_SENSEI")
public class BibliotecaView extends VerticalLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BibliotecaView.class);

    private final TareaDiariaService tareaDiariaService;
    private final SecurityService securityService;

    private final Grid<TareaDiaria> grid = new Grid<>(TareaDiaria.class, false);
    private final TareaDiariaForm form = new TareaDiariaForm();
    private final Button btnNuevaTarea = new Button("Añadir Nueva Tarea");

    private Sensei senseiActual;

    public BibliotecaView(TareaDiariaService tareaDiariaService,
                          SecurityService securityService) {
        this.tareaDiariaService = tareaDiariaService;
        this.securityService = securityService;

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

        add(new H1("Biblioteca de Tareas Diarias"), btnNuevaTarea, content);
    }

    private void configureGrid() {
        grid.setWidthFull();

        grid.addColumn(TareaDiaria::getNombre)
                .setHeader("Nombre Tarea")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(TareaDiaria::getMetaTexto)
                .setHeader("Meta")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(TareaDiaria::getDescripcion)
                .setHeader("Descripción")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addComponentColumn(tarea -> {
                    if (tarea.getVideoUrl() != null && !tarea.getVideoUrl().isBlank()) {
                        Icon icon = new Icon(VaadinIcon.MOVIE);
                        icon.setTooltipText("Tiene video");
                        return icon;
                    }
                    return new Span("");
                }).setHeader("Video")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(tarea -> {
                    Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
                    editBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
                    editBtn.setTooltipText("Editar tarea");
                    editBtn.addClickListener(
                            (ComponentEventListener<ClickEvent<Button>> & Serializable)
                                    e -> abrirFormularioEdicion(tarea)
                    );
                    return editBtn;
                }).setHeader("Acciones")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // ✅ Lazy loading con paginación
        grid.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchTareas,
                this::countTareas
        ));
        grid.setPageSize(20);
    }

    private void configureForm() {
        form.setVisible(false);
        form.setWidth("100%");

        // ✅ CORREGIDO: Cast serializable y event.getData()
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
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));
        logger.info("Sensei {} cargado", senseiActual.getId());
    }

    private void abrirFormularioNuevo() {
        form.setBean(new TareaDiaria());
        form.setVisible(true);
        btnNuevaTarea.setEnabled(false);
        grid.asSingleSelect().clear(); // ✅ CORRECTO
        logger.debug("Formulario nuevo abierto");
    }

    private void abrirFormularioEdicion(TareaDiaria tarea) {
        form.setBean(tarea);
        form.setVisible(true);
        btnNuevaTarea.setEnabled(false);
        grid.asSingleSelect().setValue(tarea); // ✅ CORREGIDO: setValue() en lugar de select()
        logger.debug("Formulario edición abierto: {}", tarea.getId());
    }

    private void cerrarFormulario() {
        form.setVisible(false);
        form.setBean(null);
        btnNuevaTarea.setEnabled(true);
        grid.asSingleSelect().clear(); // ✅ CORRECTO
        logger.debug("Formulario cerrado");
    }
    /**
     * ✅ CORREGIDO: Recibe SaveEvent y usa event.getData()
     */
    private void guardarTarea(BaseForm.SaveEvent<TareaDiaria> event) {
        try {
            TareaDiaria tarea = event.getData();
            tareaDiariaService.guardarTarea(tarea, senseiActual);

            NotificationHelper.success("Tarea guardada: " + tarea.getNombre());
            cerrarFormulario();
            grid.getDataProvider().refreshAll();

            logger.info("Tarea {} guardada", tarea.getId());
        } catch (Exception e) {
            NotificationHelper.error("Error al guardar: " + e.getMessage());
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