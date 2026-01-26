package com.RafaelDiaz.ClubJudoColombia.vista.aspirante;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@PageTitle("Completar Registro | Club Judo Colombia")
@Route("registro")
@AnonymousAllowed
public class CompletarRegistroView extends VerticalLayout implements HasUrlParameter<String> {

    private final AdmisionesService admisionesService;
    private final TokenInvitacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TraduccionService traduccionService;

    // --- Variables para recordar quién es el usuario ---
    private Usuario usuarioActual;
    private String tokenUuidActual;

    // Componentes visuales
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formLayout = new FormLayout();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmarPasswordField = new PasswordField();
    private final Button btnActivar = new Button();

    public CompletarRegistroView(AdmisionesService admisionesService,
                                 TokenInvitacionRepository tokenRepository,
                                 UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 TraduccionService traduccionService) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.traduccionService = traduccionService;
        this.admisionesService = admisionesService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, String tokenUuid) {
        try {
            // 1. Buscamos el Judoka
            Judoka judoka = admisionesService.obtenerJudokaPorToken(tokenUuid);

            // --- Guardamos los datos para usarlos en el botón "Activar" ---
            this.usuarioActual = judoka.getUsuario();
            this.tokenUuidActual = tokenUuid;

            removeAll();
            construirFormulario(this.usuarioActual);

        } catch (RuntimeException e) {

            mostrarError(e.getMessage());
        }
    }

    private void construirFormulario(Usuario usuario) {
        titulo.setText(traduccionService.get("vista.registro.titulo") + " " + usuario.getNombre());
        descripcion.setText(traduccionService.get("vista.registro.descripcion"));

        passwordField.setLabel(traduccionService.get("label.contrasena"));
        passwordField.setRequired(true);

        confirmarPasswordField.setLabel(traduccionService.get("label.confirmar_contrasena"));
        confirmarPasswordField.setRequired(true);

        btnActivar.setText(traduccionService.get("boton.activar_cuenta"));
        btnActivar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnActivar.addClickListener(e -> procesarActivacion());

        formLayout.add(passwordField, confirmarPasswordField, btnActivar);
        formLayout.setMaxWidth("400px");

        add(titulo, descripcion, formLayout);
    }

    private void procesarActivacion() {
        String pass = passwordField.getValue();
        String confirmPass = confirmarPasswordField.getValue();

        if (pass.isEmpty() || confirmPass.isEmpty()) {
            Notificar(traduccionService.get("error.campos_incompletos"), NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!pass.equals(confirmPass)) {
            Notificar(traduccionService.get("error.contrasenas_no_coinciden"), NotificationVariant.LUMO_ERROR);
            return;
        }

        // --- Usar la variable segura que guardamos al inicio ---
        usuarioActual.setPasswordHash(passwordEncoder.encode(pass));
        usuarioActual.setActivo(true);
        usuarioRepository.save(usuarioActual);

        // 2. Eliminar el token de un solo uso (Buscándolo de nuevo para evitar errores de Lazy)
        tokenRepository.findByToken(tokenUuidActual).ifPresent(tokenRepository::delete);

        Notificar(traduccionService.get("exito.cuenta_creada"), NotificationVariant.LUMO_SUCCESS);

        // 3. Redirigir al Login
        UI.getCurrent().navigate("login");
    }

    // --- Método mostrarError limpio ---
    private void mostrarError(String mensaje) {
        removeAll(); // Limpiamos la pantalla por si acaso
        add(new H2(traduccionService.get("error.titulo_ops")));
        add(new Paragraph(mensaje));
    }

    private void Notificar(String mensaje, NotificationVariant variante) {
        Notification notification = Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variante);
    }
}