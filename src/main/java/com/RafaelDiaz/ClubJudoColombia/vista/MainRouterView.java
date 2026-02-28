package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.acudiente.CompletarPerfilAcudienteView;
import com.RafaelDiaz.ClubJudoColombia.vista.sensei.CompletarPerfilSenseiView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Route("") // Ruta raíz (http://localhost:8080/)
@PermitAll
public class MainRouterView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private UsuarioRepository usuarioRepository;
    private final JudokaService judokaService;

    public MainRouterView(SecurityService securityService,
                          TraduccionService traduccionService,
                          UsuarioRepository usuarioRepository, JudokaService judokaService) {
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.usuarioRepository = usuarioRepository;
        this.judokaService = judokaService;
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UserDetails> userDetailsOpt = securityService.getAuthenticatedUserDetails();

        // 0. Si no hay sesión válida (por seguridad)
        if (userDetailsOpt.isEmpty()) {
            event.forwardTo("login");
            return;
        }

        UserDetails userDetails = userDetailsOpt.get();
        String rolPrincipal = obtenerRolPrincipal(userDetails);

        // --- ENRUTAMIENTO ELEGANTE CON SWITCH MODERNO ---
        switch (rolPrincipal) {
            case "ROLE_MASTER", "ROLE_SENSEI" -> {
                if (securityService.getAuthenticatedSensei().isEmpty()) {
                    // ¡Alto! Eres profesor pero no has bautizado tu club
                    event.forwardTo(CompletarPerfilSenseiView.class);
                } else {
                    event.forwardTo(SenseiDashboardView.class);
                }
            }

            case "ROLE_ACUDIENTE" -> {
                Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);

                // PRUEBA INFALIBLE: Un acudiente ya completó el onboarding si tiene al menos
                // un deportista registrado que esté EN_REVISION (esperando al Master) o ACTIVO.
                // Los perfiles fantasma de la invitación siempre están en PENDIENTE.
                boolean onboardingCompletado = usuario != null &&
                        judokaService.findByAcudiente(usuario).stream()
                                .anyMatch(hijo -> hijo.getEstado() == EstadoJudoka.EN_REVISION ||
                                        hijo.getEstado() == EstadoJudoka.ACTIVO);

                if (!onboardingCompletado) {
                    // Es nuevo o no ha terminado de registrar a los niños -> A la Aduana
                    event.forwardTo(CompletarPerfilAcudienteView.class);
                } else {
                    // Ya registró niños y pagó -> Al Panel de Control Familiar
                    event.forwardTo(PerfilAcudienteView.class);
                }
            }

            case "ROLE_MECENAS" -> event.forwardTo(MecenasDashboardView.class);

            case "ROLE_JUDOKA" -> {
                Optional<Judoka> judokaOpt = securityService.getAuthenticatedJudoka();
                if (judokaOpt.isPresent() && judokaOpt.get().getEstado() == EstadoJudoka.ACTIVO) {
                    event.forwardTo(JudokaDashboardView.class);
                } else {
                    event.forwardTo("completar-perfil"); // Vista de Onboarding Deportivo
                }
            }

            default -> mostrarPantallaSinRol(userDetails.getUsername());
        }
    }

    /**
     * Extrae el rol de mayor jerarquía del usuario para el enrutamiento.
     */
    private String obtenerRolPrincipal(UserDetails userDetails) {
        if (hasRole(userDetails, "ROLE_MASTER")) return "ROLE_MASTER";
        if (hasRole(userDetails, "ROLE_SENSEI")) return "ROLE_SENSEI";
        if (hasRole(userDetails, "ROLE_ACUDIENTE")) return "ROLE_ACUDIENTE";
        if (hasRole(userDetails, "ROLE_MECENAS")) return "ROLE_MECENAS";
        if (hasRole(userDetails, "ROLE_JUDOKA")) return "ROLE_JUDOKA";
        return "SIN_ROL";
    }

    private boolean hasRole(UserDetails userDetails, String role) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    private void mostrarPantallaSinRol(String nombre) {
        removeAll();
        add(new H1(traduccionService.get("main.bienvenido", nombre)));
        add(new Paragraph(traduccionService.get("main.error.sin_rol_1", "Tu cuenta aún no tiene un perfil configurado.")));
        add(new Paragraph(traduccionService.get("main.error.sin_rol_2", "Contacta al soporte deportivo.")));
        add(new Anchor("logout", traduccionService.get("btn.cerrar.sesion", "Cerrar Sesión")));
    }
}