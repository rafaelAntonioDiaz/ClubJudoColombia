package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.ComunidadComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "comunidad-judoka", layout = JudokaLayout.class) // Layout del ALUMNO
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Comunidad | Club Judo Colombia")
public class ComunidadJudokaView extends VerticalLayout {

    @Autowired
    public ComunidadJudokaView(SecurityService securityService,
                               TraduccionService traduccionService,
                               FileStorageService fileStorageService,
                               PublicacionService publicacionService,
                               ChatService chatService) {

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Inyectamos el componente reutilizable
        ComunidadComponent comunidad = new ComunidadComponent(
                securityService, traduccionService, fileStorageService, publicacionService, chatService);

        add(comunidad);
    }
}