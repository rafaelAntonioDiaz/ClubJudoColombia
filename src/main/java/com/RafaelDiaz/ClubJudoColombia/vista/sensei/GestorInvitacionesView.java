package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@PageTitle("Gestor de Invitaciones | Club Judo Colombia")
@Route(value = "invitaciones", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class GestorInvitacionesView extends VerticalLayout {

    private final AdmisionesService admisionesService;
    private final SecurityService securityService;
    private final GrupoEntrenamientoService grupoService;

    // --- Componentes del Formulario Base ---
    private final ComboBox<OpcionRol> comboRoles = new ComboBox<>("¿A quién deseas invitar?");
    private final ComboBox<String> comboTipoSensei = new ComboBox<>("Tipo de Sensei");
    private final TextField nombreField = new TextField("Nombre");
    private final TextField apellidoField = new TextField("Apellido");
    private final TextField celularField = new TextField("Celular / WhatsApp");
    private final EmailField emailField = new EmailField("Email (Será su Usuario)");
    private final Button btnGenerar = new Button("Generar Enlace", VaadinIcon.MAGIC.create());

    // Campos condicionales
    private final ComboBox<GrupoEntrenamiento> comboTarifas = new ComboBox<>("Grupo tarifario");
    private final NumberField porcentajeComision = new NumberField("Porcentaje de comisión para el sensei (%)");

    // --- Componentes del Panel de Resultados ---
    private final VerticalLayout panelResultado = new VerticalLayout();
    private final TextArea mensajeWhatsApp = new TextArea("Mensaje listo para WhatsApp");
    private final Button btnNuevaInvitacion = new Button("Invitar a otro contacto", VaadinIcon.REFRESH.create());

    public GestorInvitacionesView(AdmisionesService admisionesService,
                                  SecurityService securityService,
                                  GrupoEntrenamientoService grupoService) {
        this.admisionesService = admisionesService;
        this.securityService = securityService;
        this.grupoService = grupoService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        configurarSelectorRoles();
        configurarFormulario();
        configurarPanelResultado();

        // Asegurar visibilidad inicial correcta
        actualizarVisibilidadSegunRol();
    }

    private void configurarSelectorRoles() {
        List<OpcionRol> opciones = new ArrayList<>();

        boolean isMaster = securityService.getAuthenticatedUserDetails()
                .map(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER")))
                .orElse(false);

        if (isMaster) {
            opciones.add(new OpcionRol("Profesor de Judo (Nuevo Dojo)", "ROLE_SENSEI"));
            opciones.add(new OpcionRol("Deportista Adulto (mayor de edad)", "ROLE_JUDOKA_ADULTO"));
            opciones.add(new OpcionRol("Padre de Familia / Acudiente", "ROLE_ACUDIENTE"));
            opciones.add(new OpcionRol("Deportista (Judoka)", "ROLE_JUDOKA"));
            opciones.add(new OpcionRol("Mecenas / Patrocinador", "ROLE_MECENAS"));
        } else {
            opciones.add(new OpcionRol("Deportista (Judoka)", "ROLE_JUDOKA"));
        }

        comboRoles.setItems(opciones);
        comboRoles.setItemLabelGenerator(OpcionRol::nombreVisible);
        comboRoles.setValue(opciones.get(0));
        comboRoles.setEnabled(isMaster);
        comboRoles.setWidthFull();

        comboTipoSensei.setItems("Club Propio", "Externo (SaaS)");
        comboTipoSensei.setValue("Externo (SaaS)");
        comboTipoSensei.setVisible(false);

        // Listener para cambiar visibilidad
        comboRoles.addValueChangeListener(e -> actualizarVisibilidadSegunRol());
    }

    private void actualizarVisibilidadSegunRol() {
        OpcionRol selected = comboRoles.getValue();
        if (selected == null) return;
        String rol = selected.rolDb();

        // Visibilidad del tipo de sensei (solo para invitación de sensei)
        boolean isSensei = "ROLE_SENSEI".equals(rol);
        comboTipoSensei.setVisible(isSensei);
        porcentajeComision.setVisible(isSensei);

        // Visibilidad del grupo tarifario (solo para acudiente o judoka adulto)
        boolean requiereGrupo = "ROLE_ACUDIENTE".equals(rol) || "ROLE_JUDOKA_ADULTO".equals(rol);
        comboTarifas.setVisible(requiereGrupo);

        // Cargar grupos si es necesario
        if (requiereGrupo) {
            securityService.getAuthenticatedSensei().ifPresent(sensei -> {
                comboTarifas.setItems(grupoService.findBySensei(sensei));
                comboTarifas.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
            });
        }
    }

    private void configurarFormulario() {
        H2 titulo = new H2("Centro de Invitaciones");
        Paragraph subtitulo = new Paragraph("Genera un acceso seguro y compártelo por WhatsApp.");

        nombreField.setRequired(true);
        apellidoField.setRequired(true);
        celularField.setRequired(true);
        emailField.setRequiredIndicatorVisible(true);

        // Configurar campo de porcentaje
        porcentajeComision.setStep(0.01);
        porcentajeComision.setMin(0);
        porcentajeComision.setMax(100);
        porcentajeComision.setValue(0.0);
        porcentajeComision.setVisible(false); // inicialmente oculto

        // Configurar grupo tarifario
        comboTarifas.setVisible(false);
        comboTarifas.setWidthFull();
        comboTarifas.setRequired(true);

        btnGenerar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGenerar.setWidthFull();
        btnGenerar.addClickListener(e -> procesarInvitacion());

        FormLayout form = new FormLayout();
        form.add(nombreField, apellidoField, celularField, emailField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));

        VerticalLayout cardForm = new VerticalLayout();
        cardForm.addClassName("card-blanca");
        cardForm.setMaxWidth("600px");
        cardForm.setPadding(true);
        cardForm.add(titulo, subtitulo, comboRoles, comboTipoSensei, comboTarifas, porcentajeComision, form, btnGenerar);
        add(cardForm);
    }

    private void configurarPanelResultado() {
        panelResultado.setMaxWidth("600px");
        panelResultado.addClassName("card-blanca");
        panelResultado.setVisible(false);

        mensajeWhatsApp.setWidthFull();
        mensajeWhatsApp.setReadOnly(true);
        mensajeWhatsApp.setMinHeight("150px");

        Button btnCopiar = new Button("Copiar Mensaje", VaadinIcon.COPY.create());
        btnCopiar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnCopiar.setWidthFull();
        btnCopiar.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "navigator.clipboard.writeText($0).then(function() { " +
                            "  $1.$server.notificarCopia(); " +
                            "});", mensajeWhatsApp.getValue(), getElement()
            ));
        });

        Button btnVerQR = new Button("Ver QR", VaadinIcon.QRCODE.create());
        btnVerQR.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnVerQR.setWidthFull();
        btnVerQR.addClickListener(e -> {
            String link = mensajeWhatsApp.getValue().split("👉 ")[1].trim();
            mostrarDialogoQR(link);
        });

        btnNuevaInvitacion.setWidthFull();
        btnNuevaInvitacion.addClickListener(e -> reiniciarParaLote());

        panelResultado.add(new H2("¡Enlace Generado!"), mensajeWhatsApp, btnCopiar, btnVerQR, btnNuevaInvitacion);
        add(panelResultado);
    }

    private void procesarInvitacion() {
        if (nombreField.isEmpty() || apellidoField.isEmpty() || emailField.isEmpty() || celularField.isEmpty()) {
            NotificationHelper.error("Por favor completa todos los campos.");
            return;
        }

        try {
            String rolElegido = comboRoles.getValue().rolDb();
            boolean esClubPropio = "Club Propio".equals(comboTipoSensei.getValue());

            Long grupoId = null;
            BigDecimal comisionPorcentaje = null;

            if ("ROLE_ACUDIENTE".equals(rolElegido) || "ROLE_JUDOKA_ADULTO".equals(rolElegido)) {
                if (comboTarifas.getValue() == null) {
                    NotificationHelper.error("Debes seleccionar un grupo tarifario.");
                    return;
                }
                grupoId = comboTarifas.getValue().getId();
            } else if ("ROLE_SENSEI".equals(rolElegido)) {
                Double valor = porcentajeComision.getValue();
                if (valor == null || valor <= 0) {
                    NotificationHelper.error("Debes ingresar un porcentaje de comisión válido (>0).");
                    return;
                }
                comisionPorcentaje = BigDecimal.valueOf(valor);
            }

            String tokenGenerado = admisionesService.generarInvitacion(
                    nombreField.getValue().trim(),
                    apellidoField.getValue().trim(),
                    emailField.getValue().trim(),
                    celularField.getValue().trim(),
                    rolElegido,
                    obtenerBaseUrl(),
                    esClubPropio,
                    grupoId,
                    comisionPorcentaje
            );

            String linkFinal = obtenerBaseUrl() + "/acceso/" + tokenGenerado;

            boolean isMaster = securityService.getAuthenticatedUserDetails()
                    .map(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER")))
                    .orElse(false);
            String msj = redactarMensajeInteligente(isMaster, nombreField.getValue(), linkFinal, rolElegido);
            mensajeWhatsApp.setValue(msj);

            btnGenerar.getParent().ifPresent(parent -> ((VerticalLayout) parent).setVisible(false));
            panelResultado.setVisible(true);

            NotificationHelper.success("Invitación registrada en el sistema.");

        } catch (Exception ex) {
            NotificationHelper.error("Error al generar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String redactarMensajeInteligente(boolean isMaster, String nombreInvitado, String link, String rol) {
        String remitente = isMaster ? "el Profesor Rafael" : "tu Profesor de Judo";
        String base = "¡Hola " + nombreInvitado + "! Soy " + remitente + ".\n\n";

        if (rol.equals("ROLE_MECENAS")) {
            base += "Te invito a unirte a nuestra plataforma como Patrocinador para apoyar una mejor sociedad. Haz clic en el siguiente enlace para configurar tu perfil y ver tu impacto:\n\n";
        } else if (rol.equals("ROLE_SENSEI")) {
            base = "¡Hola " + nombreInvitado + "!\n\nBienvenido a la plataforma Club Judo. Usa el siguiente enlace para registrar tu Dojo y tu grado actual:\n\n";
        } else if (rol.equals("ROLE_ACUDIENTE")) {
            base += "Te invito a unirte a nuestro portal deportivo como Acudiente. Haz clic en este enlace seguro para registrar a tus hijos, subir la documentación requerida (Waiver/EPS) y gestionar la mensualidad:\n\n";
        } else {
            base += "Te invito a unirte a nuestro portal deportivo. Haz clic en este enlace seguro para completar tu perfil y acceder a tu carnet digital:\n\n";
        }

        return base + "👉 " + link;
    }

    private void reiniciarParaLote() {
        nombreField.clear();
        apellidoField.clear();
        celularField.clear();
        emailField.clear();
        panelResultado.setVisible(false);
        btnGenerar.getParent().ifPresent(parent -> ((VerticalLayout) parent).setVisible(true));
        nombreField.focus();
    }

    private String obtenerBaseUrl() {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        } else {
            return scheme + "://" + serverName + ":" + serverPort;
        }
    }

    private void mostrarDialogoQR(String link) {
        try {
            byte[] imageBytes = generarQRBytes(link, 300, 300);
            StreamResource resource = new StreamResource("qr.png", () -> new ByteArrayInputStream(imageBytes));
            resource.setContentType("image/png");

            Image qrImage = new Image(resource, "Código QR");
            qrImage.setWidth("300px");
            qrImage.setHeight("300px");

            Dialog dialog = new Dialog();
            dialog.add(new VerticalLayout(qrImage, new Button("Cerrar", e -> dialog.close())));
            dialog.open();
        } catch (Exception ex) {
            Notification.show("Error al generar QR: " + ex.getMessage());
        }
    }

    private byte[] generarQRBytes(String texto, int ancho, int alto) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public record OpcionRol(String nombreVisible, String rolDb) {}

    public String generarQRBase64(String texto, int ancho, int alto) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }
}