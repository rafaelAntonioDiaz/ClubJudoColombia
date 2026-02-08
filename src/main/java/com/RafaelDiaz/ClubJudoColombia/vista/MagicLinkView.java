package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.AccesoDojoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

@Route("acceso-dojo") // Misma ruta del link
@AnonymousAllowed // Permite entrar sin login previo
public class MagicLinkView extends VerticalLayout implements HasUrlParameter<String> {

    private final AccesoDojoService accesoDojoService;

    public MagicLinkView(AccesoDojoService accesoDojoService) {
        this.accesoDojoService = accesoDojoService;
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        Optional<Judoka> judokaOpt = accesoDojoService.validarPase(token);

        if (judokaOpt.isPresent()) {
            Judoka judoka = judokaOpt.get();
            loginProgramatico(judoka);
            // ¡Redirección inmediata al Dashboard existente!
            event.forwardTo(JudokaDashboardView.class);
        } else {
            // Si falla, al login normal con error
            event.forwardTo("login");
        }
    }

    private void loginProgramatico(Judoka judoka) {
        // 1. Guardamos el ID del judoka en la sesión de Vaadin para que el Dashboard sepa quién es
        VaadinSession.getCurrent().setAttribute("JUDOKA_ACTUAL_ID", judoka.getId());

        // 2. Creamos una autenticación temporal en Spring Security como "ROLE_JUDOKA"
        // Usamos el email del acudiente o un placeholder si es niño
        String principal = judoka.getAcudiente().getUsername();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_JUDOKA"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}