package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

/**
 * --- FORMULARIO REFACTORIZADO ---
 * Formulario para crear o editar una TareaDiaria (Acondicionamiento).
 * (Antes 'EjercicioForm')
 */
public class TareaDiariaForm extends FormLayout {

    // --- Campos del Formulario (Alineados con TareaDiaria.java) ---
    TextField nombre = new TextField("Nombre de la Tarea");
    TextField metaTexto = new TextField("Meta (ej. 4x15 reps, 30 min)");
    TextArea descripcion = new TextArea("Descripción/Procedimiento");
    TextField videoUrl = new TextField("URL del Video (YouTube)");

    // --- Botones ---
    Button save = new Button("Guardar Tarea");
    Button cancel = new Button("Cancelar");

    /**
     * Binder para la entidad TareaDiaria.
     */
    private Binder<TareaDiaria> binder = new Binder<>(TareaDiaria.class);

    /**
     * Constructor del formulario.
     */
    public TareaDiariaForm() {

        // --- Configurar Campos ---
        nombre.setRequired(true);
        metaTexto.setRequired(true);

        // --- Bindear (Vincular) campos de la entidad TareaDiaria ---
        binder.bind(nombre, TareaDiaria::getNombre, TareaDiaria::setNombre);
        binder.bind(metaTexto, TareaDiaria::getMetaTexto, TareaDiaria::setMetaTexto);
        binder.bind(descripcion, TareaDiaria::getDescripcion, TareaDiaria::setDescripcion);
        binder.bind(videoUrl, TareaDiaria::getVideoUrl, TareaDiaria::setVideoUrl);

        // --- Layout ---
        add(nombre, metaTexto, descripcion, videoUrl);

        HorizontalLayout botonesLayout = new HorizontalLayout(save, cancel);
        add(botonesLayout);

        // --- Listeners de Botones ---
        save.addClickListener(event -> validarYGuardar());
        cancel.addClickListener(event -> fireEvent(new CancelEvent(this)));
    }

    /**
     * Método para poner una TareaDiaria existente en el formulario (para editar).
     */
    public void setTarea(TareaDiaria tarea) {
        binder.setBean(tarea);
    }

    private void validarYGuardar() {
        try {
            // 1. Validar y obtener el objeto TareaDiaria
            TareaDiaria tarea = binder.getBean();
            binder.writeBean(tarea); // Escribe los valores del form al objeto

            // 2. Disparar el evento de guardado
            fireEvent(new SaveEvent(this, tarea));

        } catch (ValidationException e) {
            // (La validación falló, Vaadin resalta los campos)
        }
    }

    // --- Sistema de Eventos Personalizados (Actualizado) ---

    public static abstract class TareaDiariaFormEvent extends ComponentEvent<TareaDiariaForm> {
        private TareaDiaria tarea;

        protected TareaDiariaFormEvent(TareaDiariaForm source, TareaDiaria tarea) {
            super(source, false);
            this.tarea = tarea;
        }

        public TareaDiaria getTarea() { return tarea; }
    }

    // Evento de Guardar
    public static class SaveEvent extends TareaDiariaFormEvent {
        SaveEvent(TareaDiariaForm source, TareaDiaria tarea) {
            super(source, tarea);
        }
    }

    // Evento de Cancelar
    public static class CancelEvent extends TareaDiariaFormEvent {
        CancelEvent(TareaDiariaForm source) {
            super(source, null);
        }
    }

    // Métodos para que la Vista (el 'padre') pueda escuchar
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}