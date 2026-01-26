package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.ComunidadComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
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
                               ChatService chatService) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        Long miSenseiId = securityService.getSenseiIdActual();
        if (miSenseiId == null) {
            add(new H3("Error: No tienes un perfil de Sensei activo."));
            return;
        }
        ComunidadComponent comunidad = new ComunidadComponent(miSenseiId,
                securityService, traduccionService, fileStorageService, publicacionService, chatService);

        add(comunidad);
    }
}