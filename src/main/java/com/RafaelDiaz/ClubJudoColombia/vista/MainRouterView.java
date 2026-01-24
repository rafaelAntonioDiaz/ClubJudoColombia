package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

/**
 * El "Semáforo" del Dojo.
 * Esta vista no tiene diseño visual. Su único trabajo es redirigir al usuario
 * a su dashboard correcto apenas inicia sesión.
 */
@Route("") // Ruta raíz (http://localhost:8080/)
@PermitAll // Cualquier usuario logueado llega aquí primero
public class MainRouterView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;

    public MainRouterView(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // 1. ¿Es el SENSEI? -> Lo mandamos a su panel de control principal
        if (securityService.isSensei()) {
            event.forwardTo("sensei/admisiones"); // O "dashboard-sensei" si prefieres
            return;
        }

        // 2. ¿Es un JUDOKA? -> Depende de su estado de admisión
        Optional<Judoka> judokaOpt = securityService.getAuthenticatedJudoka();
        if (judokaOpt.isPresent()) {
            Judoka judoka = judokaOpt.get();

            if (judoka.getEstado() == EstadoJudoka.ACTIVO) {
                // ¡ÉXITO! El Sensei lo aprobó, entra al Dashboard completo
                event.forwardTo("dashboard-judoka");
            } else {
                // Aún es PENDIENTE o RECHAZADO, se queda en el Asistente de Admisión
                event.forwardTo("completar-perfil");
            }
            return;
        }

        // 3. Fallback de seguridad por si alguien queda en el limbo
        event.forwardTo("login");
    }
}