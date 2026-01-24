package com.RafaelDiaz.ClubJudoColombia.vista.aspirante;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

@PageTitle("Completar Registro | Club Judo Colombia")
@Route("registro")
@AnonymousAllowed // Cualquier persona con el link debe poder acceder
public class CompletarRegistroView extends VerticalLayout implements HasUrlParameter<String> {

    private final TokenInvitacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JudokaRepository judokaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TraduccionService traduccionService;

    // Estado local
    private TokenInvitacion tokenActual;

    // Componentes visuales
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formLayout = new FormLayout();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmarPasswordField = new PasswordField();
    private final Button btnActivar = new Button();

    public CompletarRegistroView(TokenInvitacionRepository tokenRepository,
                                 UsuarioRepository usuarioRepository,
                                 JudokaRepository judokaRepository,
                                 PasswordEncoder passwordEncoder,
                                 TraduccionService traduccionService) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.judokaRepository = judokaRepository;
        this.passwordEncoder = passwordEncoder;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Este método se ejecuta automáticamente cuando el usuario entra a /registro/EL-TOKEN
     */
    @Override
    public void setParameter(BeforeEvent event, String tokenString) {
        Optional<TokenInvitacion> tokenOpt = tokenRepository.findByToken(tokenString);

        if (tokenOpt.isEmpty()) {
            mostrarError(traduccionService.get("error.token_invalido")); // "El enlace es inválido o no existe."
            return;
        }

        TokenInvitacion token = tokenOpt.get();

        // Verificar expiración (48 horas)
        if (token.getFechaCreacion().plusHours(48).isBefore(LocalDateTime.now())) {
            mostrarError(traduccionService.get("error.token_expirado")); // "El enlace ha expirado. Solicite uno nuevo al Sensei."
            tokenRepository.delete(token); // Limpieza de token expirado
            return;
        }

        // Si el token es válido, mostramos el formulario
        this.tokenActual = token;
        construirFormulario(token.getJudoka().getUsuario());
    }

    private void construirFormulario(Usuario usuario) {
        titulo.setText(traduccionService.get("vista.registro.titulo") + " " + usuario.getNombre()); // "Bienvenido al Dojo, Rafael"
        descripcion.setText(traduccionService.get("vista.registro.descripcion")); // "Crea tu contraseña para activar tu cuenta."

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

        if (pass.length() < 6) {
            Notificar(traduccionService.get("error.contrasena_corta"), NotificationVariant.LUMO_ERROR);
            return;
        }

        // 1. Cifrar y guardar la contraseña
        Usuario usuario = tokenActual.getJudoka().getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(pass)); // <- Corregido a setPasswordHash
        // Aún no lo activamos del todo, sigue en PENDIENTE hasta que suba los documentos,
        // pero su usuario ya tiene clave para poder loguearse al Asistente.
        usuarioRepository.save(usuario);

        // 2. Eliminar el token de un solo uso
        tokenRepository.delete(tokenActual);

        Notificar(traduccionService.get("exito.cuenta_creada"), NotificationVariant.LUMO_SUCCESS);

        // 3. Redirigir al Login para que entre al Asistente de Admisión
        UI.getCurrent().navigate("login");
    }

    private void mostrarError(String mensaje) {
        add(new H2(traduccionService.get("error.titulo_ops")));
        add(new Paragraph(mensaje));
    }

    private void Notificar(String mensaje, NotificationVariant variante) {
        Notification notification = Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variante);
    }
}