package com.RafaelDiaz.ClubJudoColombia.vista.aspirante;

import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@PageTitle("Completar Registro | Club Judo Colombia")
@Route("registro")
@AnonymousAllowed
public class CompletarRegistroView extends VerticalLayout implements HasUrlParameter<String> {

    private final AdmisionesService admisionesService;
    private final UserDetailsService userDetailsService;
    private final FinanzasService finanzasService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final TraduccionService traduccionService;

    private TokenInvitacion tokenActual;
    private Usuario usuarioActual;

    // Componentes UI
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formLayout = new FormLayout();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmarPasswordField = new PasswordField();
    private final Button btnActivar = new Button();

    // Constructor con todas las dependencias
    public CompletarRegistroView(AdmisionesService admisionesService,
                                 UserDetailsService userDetailsService, FinanzasService finanzasService,
                                 TraduccionService traduccionService) {
        this.admisionesService = admisionesService;
        this.userDetailsService = userDetailsService;
        this.finanzasService = finanzasService;
        this.traduccionService = traduccionService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, String tokenUuid) {
        try {
            tokenActual = admisionesService.validarTokenInvitacion(tokenUuid);
            usuarioActual = tokenActual.getUsuarioInvitado();
            construirFormulario();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void construirFormulario() {
        titulo.setText(traduccionService.get("vista.registro.titulo") + " " + usuarioActual.getNombre());
        descripcion.setText(traduccionService.get("vista.registro.descripcion"));

        passwordField.setLabel(traduccionService.get("label.contrasena"));
        confirmarPasswordField.setLabel(traduccionService.get("label.confirmar_contrasena"));
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
            NotificationHelper.error(traduccionService.get("error.campos_incompletos"));
            return;
        }
        if (!pass.equals(confirmPass)) {
            NotificationHelper.error(traduccionService.get("error.contrasenas_no_coinciden"));
            return;
        }

        try {
            // Activar usuario y consumir token
            AdmisionesService.ActivationResult result = admisionesService.activarInvitacionConPassword(tokenActual.getToken(), pass);
            Usuario usuario = result.getUsuario();
            Long judokaId = result.getJudokaId();

            // Si existe judoka y grupo, generar cobro de bienvenida (esto ya se hace en activarInvitacionConPassword)
            // pero lo dejamos aquí si es necesario (nota: el servicio ya lo hace internamente)
            if (judokaId != null && tokenActual.getGrupo() != null) {
                // Opcional: si el servicio no lo hizo, se puede llamar. En el código actual ya se llama en el servicio,
                // así que no es necesario repetir.
            }

            // Autenticar automáticamente
            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
            autenticarYRedirigir(userDetails, judokaId);

        } catch (Exception e) {
            NotificationHelper.error(traduccionService.get("error.activacion") + ": " + e.getMessage());
        }
    }

    private void autenticarYRedirigir(UserDetails userDetails, Long judokaId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Obtener request de Vaadin
        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        securityContextRepository.saveContext(context, request, null);

        // Redirigir según rol
        String redirect = determinarRedireccion(usuarioActual, judokaId);
        UI.getCurrent().navigate(redirect);
    }

    private String determinarRedireccion(Usuario usuario, Long judokaId) {
        // Extraemos el rol principal (el primero que coincida)
        String rol = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .filter(r -> r.equals("ROLE_SENSEI") ||
                        r.equals("ROLE_ACUDIENTE") ||
                        r.equals("ROLE_MECENAS") ||
                        r.equals("ROLE_JUDOKA_ADULTO"))
                .findFirst()
                .orElse("");

        return switch (rol) {
            case "ROLE_SENSEI" -> "/completar-perfil-sensei/" + tokenActual.getToken(); // ✅ Agregar token
            case "ROLE_ACUDIENTE" -> "/mi-familia";
            case "ROLE_MECENAS" -> "/dashboard-mecenas";
            case "ROLE_JUDOKA_ADULTO" -> (judokaId != null) ? "/completar-perfil-judoka/" + judokaId : "/";
            default -> "/";
        };
    }

    private void mostrarError(String mensaje) {
        removeAll();
        add(new H2(traduccionService.get("error.titulo_ops")));
        add(new Paragraph(mensaje));
    }
}