package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.ComunidadComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.html.H3;
@Route(value = "comunidad-sensei", layout = SenseiLayout.class) // Layout del SENSEI
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Comunidad del Dojo | Panel Sensei")
public class ComunidadSenseiView extends VerticalLayout {

    @Autowired
    public ComunidadSenseiView(SecurityService securityService,
                               TraduccionService traduccionService,
                               FileStorageService fileStorageService,
                               PublicacionService publicacionService,
                               ChatService chatService,
                               FotoPerfilService fotoPerfilService) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        Sensei senseiActual = securityService.getAuthenticatedSensei()
                .orElse(null);
        if (senseiActual == null) {
            Notification.show("Debes completar tu perfil antes de acceder aquí.")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            UI.getCurrent().navigate("completar-perfil-sensei");
            return;
        }

        ComunidadComponent comunidad = new ComunidadComponent(senseiActual.getId(),
                        securityService, traduccionService, fileStorageService, publicacionService, fotoPerfilService, chatService);

        add(comunidad);
    }
}