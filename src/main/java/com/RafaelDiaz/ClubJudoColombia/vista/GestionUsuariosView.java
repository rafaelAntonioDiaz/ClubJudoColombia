package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import com.RafaelDiaz.ClubJudoColombia.vista.form.UsuarioForm;
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
    private final TraduccionService traduccionService;

    // Quitamos la inicialización en línea para usar el servicio en el constructor
    private Button nuevoUsuarioBtn;

    public GestionUsuariosView(UsuarioService usuarioService, TraduccionService traduccionService) {
        this.usuarioService = usuarioService;
        this.traduccionService = traduccionService;

        // i18n: Texto del botón
        this.nuevoUsuarioBtn = new Button(traduccionService.get("btn.nuevo.usuario"));

        // Nota: Si UsuarioForm también necesita traducción, deberías pasarle el servicio aquí
        this.form = new UsuarioForm();
        form.setVisible(false);

        nuevoUsuarioBtn.addClickListener(e -> crearNuevoUsuario());
        HorizontalLayout toolbar = new HorizontalLayout(nuevoUsuarioBtn);

        HorizontalLayout contenido = new HorizontalLayout(grid, form);
        contenido.setSizeFull();
        contenido.expand(grid);

        configurarGrid();
        cargarUsuarios();

        // Registramos los listeners
        form.addSaveListener(this::guardarUsuario);
        form.addCancelListener(this::cancelarEdicion);

        grid.asSingleSelect().addValueChangeListener(e -> editarUsuario(e.getValue()));

        // i18n: Título de la vista
        add(new H1(traduccionService.get("view.gestion.usuarios.titulo")), toolbar, contenido);
        setSizeFull();
    }

    private void configurarGrid() {
        grid.removeAllColumns();
        // i18n: Encabezados de columna
        grid.addColumn(Usuario::getUsername)
                .setHeader(traduccionService.get("col.username"))
                .setSortable(true);
        grid.addColumn(Usuario::getNombre)
                .setHeader(traduccionService.get("col.nombre"))
                .setSortable(true);
        grid.addColumn(Usuario::getApellido)
                .setHeader(traduccionService.get("col.apellido"))
                .setSortable(true);
        grid.addColumn(Usuario::isActivo)
                .setHeader(traduccionService.get("col.activo"))
                .setSortable(true);
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
     * Handler para el evento 'SaveEvent' del formulario.
     */
    private void guardarUsuario(UsuarioForm.SaveEvent event) {
        // Guardamos usuario y contraseña plana por separado
        usuarioService.saveUsuario(event.getUsuario(), event.getPlainPassword());
        cargarUsuarios();
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