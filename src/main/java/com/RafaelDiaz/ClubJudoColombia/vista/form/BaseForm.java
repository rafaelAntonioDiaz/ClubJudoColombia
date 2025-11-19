package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase base abstracta para todos los formularios del sistema.
 * Proporciona:
 * <ul>
 *   <li>Botones Guardar/Cancelar estandarizados</li>
 *   <li>Manejo de eventos SaveEvent/CancelEvent</li>
 *   <li>Validación automática con Binder</li>
 *   <li>Feedback visual consistente</li>
 * </ul>
 *
 * <p><b>Uso:</b> Tu formulario debe extender esta clase y proporcionar el tipo de entidad.
 *
 * @param <T> Tipo de la entidad que maneja el formulario
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
public abstract class BaseForm<T> extends FormLayout {

    private static final Logger logger = LoggerFactory.getLogger(BaseForm.class);

    protected final Button btnGuardar = new Button("Guardar", new Icon(VaadinIcon.CHECK));
    protected final Button btnCancelar = new Button("Cancelar", new Icon(VaadinIcon.CLOSE));
    protected final HorizontalLayout buttonLayout = new HorizontalLayout(btnGuardar, btnCancelar);

    protected Binder<T> binder;
    protected T currentBean;

    protected BaseForm() {
        configureButtons();
        configureButtonLayout();
        addListeners();
    }

    /**
     * Configura los estilos y variantes de los botones.
     */
    private void configureButtons() {
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClassName("base-form-save");

        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCancelar.addClassName("base-form-cancel");
    }

    /**
     * Configura el layout de botones con alineación a la derecha.
     */
    private void configureButtonLayout() {
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        buttonLayout.addClassName("base-form-buttons");

        // Agregar al formulario
        add(buttonLayout);
    }

    /**
     * Agrega los listeners a los botones.
     */
    private void addListeners() {
        btnGuardar.addClickListener(event -> handleSave());
        btnCancelar.addClickListener(event -> handleCancel());
    }

    /**
     * Maneja el evento de guardar: valida y dispara SaveEvent.
     * Se ejecuta en el UI thread.
     */
    private void handleSave() {
        try {
            // Verificar que hay un bean cargado
            if (currentBean == null) {
                logger.warn("Intento de guardar sin bean cargado en {}", getClass().getSimpleName());
                return;
            }

            // Validar y escribir los valores del formulario al bean
            if (binder != null) {
                binder.writeBean(currentBean);
            }

            // Disparar evento de guardado
            fireEvent(new SaveEvent<>(this, currentBean));
            logger.debug("SaveEvent disparado para {}", currentBean.getClass().getSimpleName());

        } catch (ValidationException e) {
            // Vaadin maneja automáticamente la visualización de errores
            logger.debug("Validación fallida en {}: {}", getClass().getSimpleName(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al guardar en {}", getClass().getSimpleName(), e);
        }
    }

    /**
     * Maneja el evento de cancelar: dispara CancelEvent.
     */
    private void handleCancel() {
        fireEvent(new CancelEvent<>(this));
        logger.debug("CancelEvent disparado");
    }

    /**
     * Método abstracto que las subclases deben implementar para crear el Binder.
     * Se llama en setBean(T bean).
     *
     * @return Binder configurado para la entidad T
     */
    protected abstract Binder<T> createBinder();

    /**
     * Carga una entidad en el formulario.
     * Crea el binder si no existe y lee los valores del bean.
     *
     * @param bean Entidad a cargar, puede ser null para formulario vacío
     */
    public void setBean(T bean) {
        this.currentBean = bean;

        if (bean == null) {
            logger.warn("setBean llamado con null en {}", getClass().getSimpleName());
            return;
        }

        // Crear binder si es la primera vez
        if (binder == null) {
            binder = createBinder();
            logger.debug("Binder creado para {}", bean.getClass().getSimpleName());
        }

        binder.readBean(bean);
        logger.debug("Bean cargado en formulario: {}", bean);
    }

    /**
     * Limpia el formulario y el bean actual.
     */
    public void clear() {
        if (binder != null) {
            binder.readBean(null);
        }
        currentBean = null;
        logger.debug("Formulario limpiado");
    }

    /**
     * Evento de guardado genérico.
     * Contiene el bean validado y listo para persistir.
     *
     * @param <T> Tipo de la entidad
     */
    public static class SaveEvent<T> extends ComponentEvent<BaseForm<T>> {
        private final T data;

        public SaveEvent(BaseForm<T> source, T data) {
            super(source, false);
            this.data = data;
        }

        /**
         * @return El bean validado con los datos del formulario
         */
        public T getData() {
            return data;
        }
    }

    /**
     * Evento de cancelación genérico.
     *
     * @param <T> Tipo de la entidad
     */
    public static class CancelEvent<T> extends ComponentEvent<BaseForm<T>> {
        public CancelEvent(BaseForm<T> source) {
            super(source, false);
        }
    }

    /**
     * Registra un listener para el evento SaveEvent.
     *
     * @param listener Función a ejecutar cuando se guarda
     * @return Registration para poder remover el listener
     */
    public Registration addSaveListener(ComponentEventListener<SaveEvent<T>> listener) {
        return addListener(SaveEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Registra un listener para el evento CancelEvent.
     *
     * @param listener Función a ejecutar cuando se cancela
     * @return Registration para poder remover el listener
     */
    public Registration addCancelListener(ComponentEventListener<CancelEvent<T>> listener) {
        return addListener(CancelEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Método de conveniencia para deshabilitar/habilitar el botón de guardar.
     * Útil para bloquear ediciones mientras se procesa.
     *
     * @param enabled true para habilitar, false para deshabilitar
     */
    protected void setSaveEnabled(boolean enabled) {
        btnGuardar.setEnabled(enabled);
    }
}