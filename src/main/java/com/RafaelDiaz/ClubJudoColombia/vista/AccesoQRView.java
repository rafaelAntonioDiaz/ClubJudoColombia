package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.AccesoDojoService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;
import java.util.Optional;

@Route("acceso-dojo")
@AnonymousAllowed
public class AccesoQRView extends VerticalLayout implements HasUrlParameter<String> {

    private final AccesoDojoService accesoDojoService;

    public AccesoQRView(AccesoDojoService accesoDojoService) {
        this.accesoDojoService = accesoDojoService;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        add(new H2("Validando tu Pase Mágico... \uD83E\uDD4B"));
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {
        System.out.println("🔍 AccesoQRView.setParameter - token: " + token);
        if (token == null || token.isEmpty()) {
            mostrarError("Pase inválido o URL incompleta.");
            return;
        }

        Optional<Judoka> judokaOpt = accesoDojoService.validarPase(token);
        System.out.println("🔍 Judoka encontrado: " + judokaOpt.isPresent());

        if (judokaOpt.isPresent()) {
            Judoka judoka = judokaOpt.get();
            System.out.println("🔍 Judoka ID: " + judoka.getId() + ", nombre: " + judoka.getNombre());

            // Guardar en sesión Vaadin
            VaadinSession.getCurrent().setAttribute("JUDOKA_ACTUAL_ID", judoka.getId());

            // Crear UserDetails
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username("magic_" + judoka.getId())
                    .password("")
                    .authorities("ROLE_JUDOKA")
                    .build();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // Guardar en sesión HTTP
            HttpSession httpSession = VaadinServletRequest.getCurrent().getHttpServletRequest().getSession(true);
            httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            System.out.println("🔍 Contexto de seguridad guardado en sesión HTTP");
            // Navegar
            System.out.println("🔍 Navegando a JudokaDashboardView...");
            //UI.getCurrent().navigate(JudokaDashboardView.class);
            UI.getCurrent().getPage().setLocation("/dashboard-judoka");

            System.out.println("🔍 Navegación ejecutada");
        } else {
            mostrarError("Pase Mágico Inválido o Expirado");
        }
    }
    private void mostrarError(String mensaje) {
        removeAll();
        add(new H2(mensaje));
        add(new Button("Ir al inicio de sesión", e -> UI.getCurrent().navigate("login")));
    }
}