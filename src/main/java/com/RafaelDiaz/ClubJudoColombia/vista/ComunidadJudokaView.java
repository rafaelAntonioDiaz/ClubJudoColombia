package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.ComunidadComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "comunidad-judoka", layout = JudokaLayout.class)
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

        // 1. OBTENER EL JUDOKA ACTUAL
        Judoka yo = securityService.getAuthenticatedJudoka().orElseThrow();

        // 2. OBTENER EL ID DEL SENSEI DE ESE JUDOKA (¡El ID del Dojo!)
        Long miDojoId = yo.getSensei().getId();

        // 3. PASAR EL ID AL COMPONENTE
        ComunidadComponent comunidad = new ComunidadComponent(
                miDojoId,
                securityService, traduccionService, fileStorageService, publicacionService, chatService);

        add(comunidad);
    }
}