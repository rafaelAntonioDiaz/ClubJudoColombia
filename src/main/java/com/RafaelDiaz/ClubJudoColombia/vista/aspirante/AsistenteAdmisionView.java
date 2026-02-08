package com.RafaelDiaz.ClubJudoColombia.vista.aspirante;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@PageTitle("Completar Perfil | Club Judo Colombia")
@Route("completar-perfil")
@PermitAll
public class AsistenteAdmisionView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AsistenteAdmisionView.class);

    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final ConfiguracionService configuracionService;
    private final SecurityService securityService;
    // Flag para controlar la lógica visual
    private boolean esSaaS = false;

    private static final String MASTER_ADMIN_USERNAME = "master_admin";
    private final Judoka judokaActual;

    private final VerticalLayout contenidoPaso = new VerticalLayout();
    private int pasoActual = 1;

    // --- VARIABLES DE CONTROL UX ---
    private boolean waiverSubido = false;
    private boolean epsSubida = false;
    private boolean pagoSubido = false;
    private Button btnFinalizar;

    private final DatePicker fechaNacimientoField = new DatePicker();
    private final NumberField pesoField = new NumberField();

    public AsistenteAdmisionView(AdmisionesService admisionesService,
                                 TraduccionService traduccionService,
                                 AlmacenamientoCloudService almacenamientoCloudService, ConfiguracionService configuracionService, SecurityService securityService1,
                                 SecurityService securityService) {
        this.admisionesService = admisionesService;
        this.traduccionService = traduccionService;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.configuracionService = configuracionService;
        this.securityService = securityService;

        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Error: Ningún aspirante ha iniciado sesión."));
        // 1. DETECTAR PERFIL
        this.esSaaS = !judokaActual.getSensei().getUsuario().getUsername().equals(MASTER_ADMIN_USERNAME);

        // 2. OBTENER PRECIO REAL
        ConfiguracionSistema config = configuracionService.obtenerConfiguracion();
        String precioFmt = configuracionService.obtenerFormatoMoneda().format(config.getFIN_SAAS_CANON_FIJO());

        configurarVista();
        renderizarPaso();
    }

    private void configurarVista() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        contenidoPaso.setMaxWidth("600px");
        add(new H2(traduccionService.get("vista.wizard.titulo")), contenidoPaso);
    }

    private void renderizarPaso() {
        contenidoPaso.removeAll();

        switch (pasoActual) {
            case 1 -> mostrarPaso1DatosPersonales();
            case 2 -> mostrarPaso2Documentos();
            case 3 -> mostrarPaso3Confirmacion();
        }
    }

    private void mostrarPaso1DatosPersonales() {
        H3 tituloPaso = new H3(traduccionService.get("vista.wizard.paso1.titulo"));
        Paragraph desc = new Paragraph(traduccionService.get("vista.wizard.paso1.desc"));

        fechaNacimientoField.setLabel(traduccionService.get("label.fecha_nacimiento"));
        pesoField.setLabel(traduccionService.get("label.peso_kg"));
        pesoField.setSuffixComponent(new Span("kg"));

        FormLayout formLayout = new FormLayout(fechaNacimientoField, pesoField);

        Button btnSiguiente = new Button(traduccionService.get("btn.siguiente.paso"), VaadinIcon.ARROW_RIGHT.create());
        btnSiguiente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSiguiente.addClickListener(e -> {
            if(pesoField.isEmpty() || fechaNacimientoField.isEmpty()){
                Notification.show(traduccionService.get("msg.error.campos.incompletos"), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            pasoActual = 2;
            renderizarPaso();
        });

        contenidoPaso.add(tituloPaso, desc, formLayout, btnSiguiente);
    }

    private void mostrarPaso2Documentos() {
        H3 tituloPaso = new H3(traduccionService.get("vista.wizard.paso2.titulo"));
        Paragraph desc = new Paragraph(traduccionService.get("vista.wizard.paso2.desc.completa"));
        FormLayout gridDocumentos = new FormLayout();
        if (!esSaaS) {
            Upload uploadWaiver = configurarUploadComponente(
                    "msg.waiver.instruccion", TipoDocumento.WAIVER);
            Upload uploadEps = configurarUploadComponente(
                    "msg.eps.instruccion", TipoDocumento.EPS);
            gridDocumentos.add(uploadWaiver, uploadEps);
        }
        Upload uploadPago = configurarUploadComponente(
                "msg.pago.instruccion", TipoDocumento.COMPROBANTE_PAGO);

        gridDocumentos.add(uploadPago);
        gridDocumentos.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );

        HorizontalLayout botonesNavegacion = new HorizontalLayout();
        Button btnAtras = new Button(traduccionService.get("btn.atras"),
                VaadinIcon.ARROW_LEFT.create(), e -> { pasoActual = 1; renderizarPaso(); });

        btnFinalizar = new Button(traduccionService.get("btn.finalizar"),
                VaadinIcon.CHECK.create(), e -> { pasoActual = 3; renderizarPaso(); });
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        btnFinalizar.setEnabled(waiverSubido && epsSubida && pagoSubido);

        botonesNavegacion.add(btnAtras, btnFinalizar);

        contenidoPaso.add(tituloPaso, desc, gridDocumentos, botonesNavegacion);
    }

    private void mostrarPaso3Confirmacion() {
        VaadinIcon icon = VaadinIcon.HOURGLASS;
        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", "var(--lumo-primary-color)").set("font-size", "48px");

        H3 titulo = new H3(traduccionService.get("vista.wizard.paso3.titulo"));
        Paragraph mensaje = new Paragraph(traduccionService.get("vista.wizard.paso3.mensaje"));
        mensaje.getStyle().set("text-align", "center");

        Button btnCerrarSesion = new Button(traduccionService.get("btn.cerrar.sesion"),
                VaadinIcon.SIGN_OUT.create(), e -> {
            com.vaadin.flow.server.VaadinSession.getCurrent().getSession().invalidate();
            getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
        });
        btnCerrarSesion.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        contenidoPaso.setHorizontalComponentAlignment(Alignment.CENTER,
                iconSpan, titulo, mensaje, btnCerrarSesion);
        contenidoPaso.add(iconSpan, titulo, mensaje, btnCerrarSesion);
    }

    private Upload configurarUploadComponente(String i18nLabelKey,
                                              TipoDocumento tipoDoc) {
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("application/pdf", "image/png", "image/jpeg"); // Importante: Aceptar imágenes para Nequi
        upload.setDropLabel(new Span(traduccionService.get(i18nLabelKey)));
        upload.setMaxFiles(1);

        upload.setUploadHandler(event -> {
            log.info(">>>> Iniciando intento de subida para el archivo: {}", event.getFileName());

            try {
                InputStream in = event.getInputStream();
                String originalFileName = event.getFileName();

                String keyEnLaNube = almacenamientoCloudService.subirArchivo(judokaActual.getId(), originalFileName, in);
                String urlEnLaNube = almacenamientoCloudService.obtenerUrl(judokaActual.getId(), keyEnLaNube);
                admisionesService.cargarRequisito(judokaActual, tipoDoc, urlEnLaNube);

                log.info("<<<< ÉXITO: Archivo {} subido a la nube correctamente.", originalFileName);

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show(traduccionService.get("msg.exito.archivo_subido"),
                                    3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    if (tipoDoc == TipoDocumento.WAIVER) waiverSubido = true;
                    if (tipoDoc == TipoDocumento.EPS) epsSubida = true;
                    if (tipoDoc == TipoDocumento.COMPROBANTE_PAGO) pagoSubido = true;

                    // Si los 3 están subidos, habilitamos el botón
                    if (waiverSubido && epsSubida && pagoSubido && btnFinalizar != null) {
                        btnFinalizar.setEnabled(true);
                        // --- I18n APLICADO AQUÍ ---
                        Notification.show(traduccionService.get("msg.exito.puede_continuar"),
                                        2000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    }
                }));

            } catch (Exception ex) {
                log.error("!!!! ERROR CRÍTICO al subir a la nube !!!!", ex);
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show(traduccionService.get("msg.error.nube")
                                    + ": " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        });
        return upload;
    }
}