package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("espera-autorizacion")
@PageTitle("En espera de autorización | Club Judo Colombia")
@AnonymousAllowed
public class EsperaAutorizacionView extends VerticalLayout {

    @Autowired
    public EsperaAutorizacionView(SecurityService securityService, TraduccionService traduccionService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("600px");
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);

        H2 titulo = new H2(traduccionService.get("espera.titulo"));
        Paragraph mensaje = new Paragraph(traduccionService.get("espera.mensaje"));

        // Obtener email del usuario autenticado
        String email = securityService.getAuthenticatedUsuario()
                .map(usuario -> usuario.getEmail() != null ? usuario.getEmail() : usuario.getUsername())
                .orElse("el correo que usaste para registrarte");

        Paragraph usuarioMsg = new Paragraph(traduccionService.get("espera.usuario") + ": " + email);

        card.add(titulo, mensaje, usuarioMsg);
        add(card);
    }
}