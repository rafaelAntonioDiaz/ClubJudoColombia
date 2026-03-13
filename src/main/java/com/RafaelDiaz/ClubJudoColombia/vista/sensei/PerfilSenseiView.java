package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SenseiService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "perfil-sensei", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Mi Perfil | Sensei")
public class PerfilSenseiView extends VerticalLayout {

    private final SecurityService securityService;
    private final SenseiService senseiService;
    private final SenseiRepository senseiRepository;
    private final TraduccionService traduccionService;

    private Sensei senseiActual;
    private Div avatarContainer;

    public PerfilSenseiView(SecurityService securityService,
                            SenseiService senseiService,
                            SenseiRepository senseiRepository,
                            TraduccionService traduccionService) {
        this.securityService = securityService;
        this.senseiService = senseiService;
        this.senseiRepository = senseiRepository;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        construirUI();
    }

    private void construirUI() {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("500px");
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);

        H2 titulo = new H2(traduccionService.get("perfil.sensei.titulo", "Mi Perfil"));
        card.add(titulo);

        // Contenedor del avatar
        avatarContainer = new Div();
        avatarContainer.setWidth("150px");
        avatarContainer.setHeight("150px");
        avatarContainer.getStyle()
                .set("border-radius", "50%")
                .set("overflow", "hidden")
                .set("border", "4px solid white")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)")
                .set("background-color", "#eee")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        cargarImagenEnAvatar();

        // Upload
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/jpg");
        upload.setMaxFiles(1);
        upload.setDropAllowed(false);

        Button uploadBtn = new Button(new Icon(VaadinIcon.CAMERA));
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        uploadBtn.setTooltipText(traduccionService.get("tooltip.cambiar.foto"));
        upload.setUploadButton(uploadBtn);

        upload.setUploadHandler(event -> {
            try {
                senseiService.actualizarFotoPerfil(senseiActual, event.getInputStream(), event.getFileName());
                senseiActual = senseiRepository.findById(senseiActual.getId()).orElse(senseiActual);
                getUI().ifPresent(ui -> ui.access(() -> {
                    cargarImagenEnAvatar();
                    Notification.show(traduccionService.get("msg.foto.actualizada"), 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    upload.clearFileList();
                    // Recargar la página para actualizar el layout
                    ui.getPage().executeJs("setTimeout(function() { location.reload(); }, 1500);");
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("msg.error.general") + ": " + e.getMessage())
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)
                ));
            }
        });

        Div avatarWrapper = new Div(avatarContainer, upload);
        avatarWrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("align-items", "center");

        card.add(avatarWrapper);
        add(card);
    }

    private void cargarImagenEnAvatar() {
        avatarContainer.removeAll();
        String url = senseiActual.getUrlFotoPerfil();
        if (url != null && !url.isEmpty()) {
            Element img = new Element("img");
            img.setAttribute("src", url);
            img.setAttribute("style", "width:100%; height:100%; object-fit:cover;");
            avatarContainer.getElement().appendChild(img);
        } else {
            Avatar placeholder = new Avatar(senseiActual.getUsuario().getNombre());
            placeholder.setWidth("100%");
            placeholder.setHeight("100%");
            avatarContainer.add(placeholder);
        }
    }
}