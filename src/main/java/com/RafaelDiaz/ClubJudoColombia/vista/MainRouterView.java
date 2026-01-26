package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@Route("") // Ruta raíz (http://localhost:8080/)
@PermitAll
public class MainRouterView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final TraduccionService traduccionService; // <--- Traído de MainView

    public MainRouterView(SecurityService securityService, TraduccionService traduccionService) {
        this.securityService = securityService;
        this.traduccionService = traduccionService;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // 1. ¿Es el SENSEI? -> Al Dashboard Principal del Sensei
        if (securityService.isSensei()) {
            event.forwardTo(SenseiDashboardView.class);
            return;
        }

        // 2. ¿Es un JUDOKA? -> El Semáforo de Admisiones
        Optional<Judoka> judokaOpt = securityService.getAuthenticatedJudoka();
        if (judokaOpt.isPresent()) {
            Judoka judoka = judokaOpt.get();

            if (judoka.getEstado() == EstadoJudoka.ACTIVO) {
                event.forwardTo(JudokaDashboardView.class);
            } else {
                event.forwardTo("completar-perfil");
            }
            return;
        }

        // 3. Fallback: Usuario logueado pero sin rol reconocido
        String username = securityService.getAuthenticatedUserDetails()
                .map(u -> u.getUsername()).orElse("Usuario");
        mostrarPantallaSinRol(username);
    }

    // --- Pantalla de error segura y traducida ---
    private void mostrarPantallaSinRol(String nombre) {
        removeAll();
        add(new H1(traduccionService.get("main.bienvenido", nombre)));
        add(new Paragraph(traduccionService.get("main.error.sin_rol_1")));
        add(new Paragraph(traduccionService.get("main.error.sin_rol_2")));
        add(new Anchor("logout", traduccionService.get("btn.cerrar.sesion")));
    }
}