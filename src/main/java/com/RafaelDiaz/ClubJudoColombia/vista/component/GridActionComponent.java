package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Componente reutilizable para mostrar botones de acción en columnas de Grid.
 *
 * <p><b>Botones incluidos:</b>
 * <ul>
 *   <li>Editar (icono lápiz)</li>
 *   <li>Eliminar (icono basurero, con confirmación)</li>
 *   <li>Ver Detalles (icono ojo, opcional)</li>
 * </ul>
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Métodos setEditVisible() y setDeleteVisible() para controlar visibilidad</li>
 *   <li>Diálogo de confirmación para eliminar</li>
 *   <li>Tooltips integrados</li>
 *   <li>Estilos consistentes con el tema</li>
 * </ul>
 *
 * <p><b>Uso:</b>
 * <pre>
 * grid.addComponentColumn(item -> {
 *     GridActionComponent actions = new GridActionComponent(
 *         () -> editarItem(item),
 *         () -> confirmarEliminar(item)
 *     );
 *     // Mostrar/ocultar basado en el estado
 *     actions.setEditVisible(item.getId() != null);
 *     return actions;
 * }).setHeader("Acciones");
 * </pre>
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
public class GridActionComponent extends HorizontalLayout {

    private static final Logger logger = LoggerFactory.getLogger(GridActionComponent.class);

    private final Button editButton;
    private final Button deleteButton;
    private final Button viewButton;

    /**
     * Constructor con botones Editar y Eliminar.
     *
     * @param onEdit Callback para editar el ítem
     * @param onDelete Callback para eliminar el ítem
     */
    public GridActionComponent(Runnable onEdit, Runnable onDelete) {
        this(onEdit, onDelete, null);
    }

    /**
     * Constructor con botones Editar, Eliminar y Ver Detalles.
     *
     * @param onEdit Callback para editar el ítem
     * @param onDelete Callback para eliminar el ítem
     * @param onView Callback para ver detalles (null si no se usa)
     */
    public GridActionComponent(Runnable onEdit, Runnable onDelete, Runnable onView) {
        super();

        configureLayout();
        this.editButton = createEditButton(onEdit);
        this.deleteButton = createDeleteButton(onDelete);
        this.viewButton = onView != null ? createViewButton(onView) : null;

        add(editButton, deleteButton);
        if (viewButton != null) {
            add(viewButton);
        }
    }

    /**
     * Configura el layout horizontal.
     */
    private void configureLayout() {
        setSpacing(false);
        setMargin(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        addClassName("grid-action-component");
    }

    /**
     * Crea el botón de editar.
     */
    private Button createEditButton(Runnable onEdit) {
        Button button = new Button(new Icon(VaadinIcon.EDIT), event -> {
            try {
                onEdit.run();
                logger.debug("Acción 'editar' ejecutada");
            } catch (Exception e) {
                logger.error("Error en acción 'editar'", e);
            }
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClassName("grid-action-edit");
        button.setTooltipText("Editar");
        return button;
    }

    /**
     * Crea el botón de eliminar con confirmación.
     */
    private Button createDeleteButton(Runnable onDelete) {
        Button button = new Button(new Icon(VaadinIcon.TRASH), event -> {
            // Crear diálogo de confirmación
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Confirmar Eliminación");
            confirmDialog.add("¿Está seguro de que desea eliminar este registro?");

            Button confirmButton = new Button("Eliminar", e -> {
                try {
                    onDelete.run();
                    logger.info("Acción 'eliminar' confirmada y ejecutada");
                } catch (Exception ex) {
                    logger.error("Error en acción 'eliminar'", ex);
                } finally {
                    confirmDialog.close();
                }
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button cancelButton = new Button("Cancelar", e -> {
                confirmDialog.close();
                logger.debug("Acción 'eliminar' cancelada");
            });

            confirmDialog.getFooter().add(cancelButton, confirmButton);
            confirmDialog.open();
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        button.addClassName("grid-action-delete");
        button.setTooltipText("Eliminar");
        return button;
    }

    /**
     * Crea el botón de ver detalles.
     */
    private Button createViewButton(Runnable onView) {
        Button button = new Button(new Icon(VaadinIcon.EYE), event -> {
            try {
                onView.run();
                logger.debug("Acción 'ver' ejecutada");
            } catch (Exception e) {
                logger.error("Error en acción 'ver'", e);
            }
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClassName("grid-action-view");
        button.setTooltipText("Ver Detalles");
        return button;
    }

    /**
     * Habilita/deshabilita el botón de editar.
     * @param enabled true para habilitar
     */
    public void setEditEnabled(boolean enabled) {
        editButton.setEnabled(enabled);
    }

    /**
     * Habilita/deshabilita el botón de eliminar.
     * @param enabled true para habilitar
     */
    public void setDeleteEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }

    /**
     * Habilita/deshabilita el botón de ver detalles.
     * @param enabled true para habilitar
     */
    public void setViewEnabled(boolean enabled) {
        if (viewButton != null) {
            viewButton.setEnabled(enabled);
        }
    }

    /**
     * Muestra/oculta el botón de editar.
     * @param visible true para mostrar, false para ocultar
     */
    public void setEditVisible(boolean visible) {
        editButton.setVisible(visible);
    }

    /**
     * Muestra/oculta el botón de eliminar.
     * @param visible true para mostrar, false para ocultar
     */
    public void setDeleteVisible(boolean visible) {
        deleteButton.setVisible(visible);
    }

    /**
     * Muestra/oculta el botón de ver detalles.
     * @param visible true para mostrar, false para ocultar
     */
    public void setViewVisible(boolean visible) {
        if (viewButton != null) {
            viewButton.setVisible(visible);
        }
    }
}