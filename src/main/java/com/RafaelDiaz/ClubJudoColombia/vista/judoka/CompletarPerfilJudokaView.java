package com.RafaelDiaz.ClubJudoColombia.vista.judoka;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@PageTitle("Activación de Perfil | Club Judo Colombia")
@Route("completar-perfil-judoka")
@RolesAllowed("ROLE_JUDOKA")
public class CompletarPerfilJudokaView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CompletarPerfilJudokaView.class);

    private final SecurityService securityService;
    private final AdmisionesService admisionesService;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;

    private final Judoka judokaActual;
    private boolean pagoSubido = false;
    private Button btnFinalizar;

    public CompletarPerfilJudokaView(SecurityService securityService,
                                     AdmisionesService admisionesService,
                                     AlmacenamientoCloudService almacenamientoCloudService,
                                     JudokaRepository judokaRepository) {
        this.securityService = securityService;
        this.admisionesService = admisionesService;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;

        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error: Ningún judoka ha iniciado sesión."));

        configurarVista();
    }

    private void configurarVista() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca"); // Reutiliza tu clase CSS
        card.setMaxWidth("500px");
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);

        H2 titulo = new H2("¡Bienvenido al Dojo!");
        Paragraph subtitulo = new Paragraph("Para activar tu perfil y acceder a tu carnet digital, debes realizar el pago de tu primera mensualidad y adjuntar el comprobante.");
        subtitulo.getStyle().set("text-align", "center");

        // --- INSTRUCCIONES DE NEQUI ---
        VerticalLayout infoNequi = new VerticalLayout();
        infoNequi.setAlignItems(Alignment.CENTER);
        infoNequi.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        infoNequi.getStyle().set("border-radius", "8px");
        infoNequi.setPadding(true);
        infoNequi.setWidthFull();

        Span textoNequi = new Span("Transfiere a nuestra cuenta Nequi oficial:");

        // El número idealmente debería extraerse del Sensei asociado, pero usamos este placeholder por ahora.
        H3 numeroNequi = new H3("📱 300 123 4567");
        numeroNequi.getStyle().set("color", "#6710ba"); // Color representativo de Nequi
        numeroNequi.getStyle().set("margin-top", "var(--lumo-space-s)");

        infoNequi.add(textoNequi, numeroNequi);

        // --- UPLOAD DEL COMPROBANTE ---
        Upload uploadPago = configurarUploadComprobante();

        btnFinalizar = new Button("Enviar a Revisión", VaadinIcon.CHECK_CIRCLE.create());
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnFinalizar.setWidthFull();
        btnFinalizar.setEnabled(false); // Permanece bloqueado hasta que la imagen esté en la nube

        btnFinalizar.addClickListener(e -> finalizarOnboarding());

        card.add(titulo, subtitulo, infoNequi, uploadPago, btnFinalizar);
        add(card);
    }

    private Upload configurarUploadComprobante() {
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("image/png", ".png", "image/jpeg", ".jpg", ".jpeg");
        upload.setMaxFileSize(10 * 1024 * 1024); // Límite de 10MB
        upload.setDropLabel(new Span("Sube aquí la captura de pantalla de Nequi (.png o .jpg)"));
        upload.setMaxFiles(1);

        upload.setUploadHandler(UploadHandler.inMemory((metadata, bytes) -> {
            String originalFileName = metadata.fileName();
            log.info("Iniciando subida de comprobante de pago: {}", originalFileName);

            try {
                InputStream in = new ByteArrayInputStream(bytes);
                String keyEnLaNube = almacenamientoCloudService.subirArchivo(judokaActual.getId(), originalFileName, in);
                String urlEnLaNube = almacenamientoCloudService.obtenerUrl(judokaActual.getId(), keyEnLaNube);

                // Vinculamos el comprobante al Judoka en la base de datos
                admisionesService.cargarRequisito(judokaActual, TipoDocumento.COMPROBANTE_PAGO, urlEnLaNube);

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("¡Comprobante subido con éxito!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    pagoSubido = true;
                    btnFinalizar.setEnabled(true);
                }));

            } catch (Exception ex) {
                log.error("Error al subir el comprobante", ex);
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Error al subir el archivo: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        }));

        return upload;
    }

    private void finalizarOnboarding() {
        try {
            // Actualizamos el estado del Judoka para que el Sensei lo pueda auditar
            judokaActual.setEstado(EstadoJudoka.EN_REVISION);
            judokaRepository.save(judokaActual);

            Notification.show("Tu pago está en revisión por el Sensei. ¡Pronto serás activado!", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Una vez en revisión, lo dirigimos a la vista general o a una sala de espera visual
            getUI().ifPresent(ui -> ui.navigate(""));

        } catch (Exception e) {
            Notification.show("Error al finalizar: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}