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
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CompletarRegistroView.class);

    private final AdmisionesService admisionesService;
    private final UserDetailsService userDetailsService;
    private final FinanzasService finanzasService;
    private final TraduccionService traduccionService;

    // CORRECCIÓN: Se mantiene HttpSessionSecurityContextRepository como implementación
    // concreta porque necesitamos llamar saveContext() con el HttpServletResponse real.
    // Con la interfaz SecurityContextRepository también funciona, pero la implementación
    // por defecto en Spring Security 6 es DelegatingSecurityContextRepository que puede
    // ignorar el response según la configuración. Ser explícito es más seguro.
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private TokenInvitacion tokenActual;
    private Usuario usuarioActual;

    // Componentes UI
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formLayout = new FormLayout();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmarPasswordField = new PasswordField();
    private final Button btnActivar = new Button();

    public CompletarRegistroView(AdmisionesService admisionesService,
                                 UserDetailsService userDetailsService,
                                 FinanzasService finanzasService,
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

            // ELIMINADO: Ya no redirigimos prematuramente.
            // Todos los usuarios deben ver el formulario para establecer su contraseña primero.

            construirFormulario();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private boolean esAcudiente() {
        return usuarioActual.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ACUDIENTE"));
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
            // Activar usuario y consumir token en una sola transacción
            AdmisionesService.ActivationResult result =
                    admisionesService.activarInvitacionConPassword(tokenActual.getToken(), pass);

            Usuario usuario = result.getUsuario();
            Long judokaId = result.getJudokaId();

            // Cargar los UserDetails frescos desde BD para que Spring Security
            // tenga los GrantedAuthority correctos (especialmente ROLE_JUDOKA_ADULTO)
            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());

            autenticarYRedirigir(userDetails, judokaId);

        } catch (Exception e) {
            log.error("Error en procesarActivacion", e);
            NotificationHelper.error(traduccionService.get("error.activacion") + ": " + e.getMessage());
        }
    }

    private void autenticarYRedirigir(UserDetails userDetails, Long judokaId) {
        // Construir el token de autenticación con los authorities del usuario
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        // Crear y poblar el SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // ── CORRECCIÓN CRÍTICA ────────────────────────────────────────────────
        // El código original llamaba:
        //   securityContextRepository.saveContext(context, request, null)
        //
        // El tercer parámetro (HttpServletResponse) era null.
        // HttpSessionSecurityContextRepository.saveContext() necesita el response
        // para escribir la cookie de sesión (JSESSIONID) en el navegador.
        // Con null, el contexto se guardaba en la sesión del servidor pero el
        // navegador no recibía la cookie actualizada.
        // Resultado: el siguiente request (navegación a completar-perfil-judoka)
        // llegaba sin cookie válida → Spring Security no encontraba el contexto
        // → getAuthenticatedUsuario() devolvía Optional.empty()
        // → cargarDatos() redirigía a "" sin construir el formulario
        // → el DatePicker nunca aparecía en pantalla.
        //
        // Solución: obtener el HttpServletResponse real desde VaadinServletResponse.
        // ─────────────────────────────────────────────────────────────────────
        HttpServletRequest request =
                VaadinServletRequest.getCurrent().getHttpServletRequest();
        HttpServletResponse response =
                VaadinServletResponse.getCurrent().getHttpServletResponse();

        securityContextRepository.saveContext(context, request, response);

        log.debug("Contexto de seguridad guardado para usuario: {} con roles: {}",
                userDetails.getUsername(), userDetails.getAuthorities());

        // Navegar según el rol del usuario recién autenticado
        String destino = determinarRedireccion(usuarioActual, judokaId);
        log.debug("Redirigiendo a: {}", destino);
        UI.getCurrent().navigate(destino);
    }

    private String determinarRedireccion(Usuario usuario, Long judokaId) {
        // Buscamos el rol relevante. Se incluye ROLE_JUDOKA para el flujo de menores.
        String rol = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .filter(r -> r.equals("ROLE_SENSEI") ||
                        r.equals("ROLE_ACUDIENTE") ||
                        r.equals("ROLE_MECENAS") ||
                        r.equals("ROLE_JUDOKA") ||
                        r.equals("ROLE_JUDOKA_ADULTO"))
                .findFirst()
                .orElse("");

        return switch (rol) {
            case "ROLE_SENSEI" -> "/completar-perfil-sensei/" + tokenActual.getToken();
            case "ROLE_ACUDIENTE" -> "/completar-perfil-acudiente?token=" + tokenActual.getToken();
            case "ROLE_MECENAS" -> "/dashboard-mecenas";
            // Si es judoka (adulto o menor), vamos a completar su perfil deportivo
            case "ROLE_JUDOKA", "ROLE_JUDOKA_ADULTO" -> judokaId != null
                    ? "/completar-perfil-judoka/" + judokaId
                    : "/";
            default -> "/";
        };
    }

    private void mostrarError(String mensaje) {
        removeAll();
        add(new H2(traduccionService.get("error.titulo_ops")));
        add(new Paragraph(mensaje));
    }
}