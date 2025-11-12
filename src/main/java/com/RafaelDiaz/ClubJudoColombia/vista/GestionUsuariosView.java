package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("gestion-usuarios")
@PermitAll
public class GestionUsuariosView extends VerticalLayout {

    private Grid<Usuario> grid = new Grid<>(Usuario.class);
    private UsuarioForm form;
    private final UsuarioService usuarioService;

    private Button nuevoUsuarioBtn = new Button("Nuevo Usuario");

    public GestionUsuariosView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;

        this.form = new UsuarioForm();
        form.setVisible(false);

        nuevoUsuarioBtn.addClickListener(e -> crearNuevoUsuario());
        HorizontalLayout toolbar = new HorizontalLayout(nuevoUsuarioBtn); // Corregido el typo

        HorizontalLayout contenido = new HorizontalLayout(grid, form);
        contenido.setSizeFull();
        contenido.expand(grid);

        configurarGrid();
        cargarUsuarios();

        // Registramos los listeners (igual que antes)
        form.addSaveListener(this::guardarUsuario);
        form.addCancelListener(this::cancelarEdicion);

        grid.asSingleSelect().addValueChangeListener(e -> editarUsuario(e.getValue()));

        add(new H1("Gestión de Usuarios"), toolbar, contenido);
        setSizeFull();
    }

    private void configurarGrid() {
        grid.removeAllColumns();
        grid.addColumn(Usuario::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(Usuario::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Usuario::getApellido).setHeader("Apellido").setSortable(true);
        grid.addColumn(Usuario::isActivo).setHeader("Activo").setSortable(true);
    }

    private void cargarUsuarios() {
        grid.setItems(usuarioService.findAllUsuarios());
    }

    private void editarUsuario(Usuario usuario) {
        if (usuario == null) {
            cerrarEditor();
        } else {
            form.setUsuario(usuario);
            form.setVisible(true);
        }
    }

    private void crearNuevoUsuario() {
        grid.asSingleSelect().clear();
        editarUsuario(new Usuario());
    }

    /**
     * --- MÉTODO MODIFICADO ---
     * Handler para el evento 'SaveEvent' del formulario.
     */
    private void guardarUsuario(UsuarioForm.SaveEvent event) {
        // --- ESTE ES EL CAMBIO CLAVE ---
        // 1. Ahora llamamos al método de servicio actualizado, pasando
        //    el usuario y la contraseña en texto plano por separado.
        usuarioService.saveUsuario(event.getUsuario(), event.getPlainPassword());

        // (El resto es igual)
        // 2. Refresca el Grid
        cargarUsuarios();

        // 3. Cierra el formulario
        cerrarEditor();
    }

    private void cancelarEdicion(UsuarioForm.CancelEvent event) {
        cerrarEditor();
    }

    private void cerrarEditor() {
        form.setVisible(false);
        grid.asSingleSelect().clear();
    }
}