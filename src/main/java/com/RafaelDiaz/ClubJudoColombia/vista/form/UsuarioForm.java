package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class UsuarioForm extends FormLayout {

    TextField username = new TextField("Username");
    PasswordField password = new PasswordField("Contraseña");
    TextField nombre = new TextField("Nombre");
    TextField apellido = new TextField("Apellido");

    Button save = new Button("Guardar");
    Button cancel = new Button("Cancelar");

    private Binder<Usuario> binder = new Binder<>(Usuario.class);
    private Usuario usuarioActual;

    public UsuarioForm() {
        // Bindeo (igual que antes, SIN el password)
        binder.bind(username, Usuario::getUsername, Usuario::setUsername);
        binder.bind(nombre, Usuario::getNombre, Usuario::setNombre);
        binder.bind(apellido, Usuario::getApellido, Usuario::setApellido);

        add(username, nombre, apellido, password);
        HorizontalLayout botonesLayout = new HorizontalLayout(save, cancel);
        add(botonesLayout);

        save.addClickListener(event -> validarYGuardar());
        cancel.addClickListener(event -> fireEvent(new CancelEvent(this)));
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        binder.readBean(usuario);
        password.clear(); // Limpiamos el campo de contraseña
    }

    /**
     * --- MODIFICADO ---
     * Valida el formulario y dispara el evento de guardado.
     */
    private void validarYGuardar() {
        if (usuarioActual == null) {
            return;
        }

        try {
            // 1. Escribe los valores del formulario (username, nombre, apellido)
            //    dentro del objeto 'usuarioActual'.
            binder.writeBean(usuarioActual);

            // --- CAMBIO CLAVE ---
            // 2. Ya NO ponemos el password en el objeto 'usuarioActual'.
            //    Simplemente capturamos el valor del campo.
            String plainPassword = password.getValue();

            // 3. Disparamos el evento 'SaveEvent' pasando el usuario
            //    Y la contraseña en texto plano por separado.
            fireEvent(new SaveEvent(this, usuarioActual, plainPassword)); // --- MODIFICADO ---

        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    // --- MODIFICADO: Sistema de Eventos Personalizados ---

    public static abstract class UsuarioFormEvent extends ComponentEvent<UsuarioForm> {
        private Usuario usuario;

        protected UsuarioFormEvent(UsuarioForm source, Usuario usuario) {
            super(source, false);
            this.usuario = usuario;
        }

        public Usuario getUsuario() {
            return usuario;
        }
    }

    // --- MODIFICADO: Evento de Guardar ---
    public static class SaveEvent extends UsuarioFormEvent {
        // --- NUEVO: Campo para el password ---
        private String plainPassword;

        // --- MODIFICADO: Constructor ---
        SaveEvent(UsuarioForm source, Usuario usuario, String plainPassword) {
            super(source, usuario);
            this.plainPassword = plainPassword;
        }

        // --- NUEVO: Getter para el password ---
        public String getPlainPassword() {
            return plainPassword;
        }
    }

    // Evento de Cancelar (sin cambios)
    public static class CancelEvent extends UsuarioFormEvent {
        CancelEvent(UsuarioForm source) {
            super(source, null);
        }
    }

    // Métodos para escuchar (sin cambios)
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}