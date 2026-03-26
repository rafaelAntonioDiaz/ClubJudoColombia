package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
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
    private final TraduccionService traduccionService;

    // --- Componentes del Formulario Base ---
    private final ComboBox<OpcionRol> comboRoles;
    private final ComboBox<String> comboTipoSensei;
    private final TextField nombreField;
    private final TextField apellidoField;
    private final TextField celularField;
    private final EmailField emailField;
    private final Button btnGenerar;

    // Campos condicionales
    private final ComboBox<GrupoEntrenamiento> comboTarifas;
    private final NumberField porcentajeComision;

    // --- Componentes del Panel de Resultados ---
    private final VerticalLayout panelResultado;
    private final TextArea mensajeWhatsApp;
    private final Button btnNuevaInvitacion;

    public GestorInvitacionesView(AdmisionesService admisionesService,
                                  SecurityService securityService,
                                  GrupoEntrenamientoService grupoService,
                                  TraduccionService traduccionService) {
        this.admisionesService = admisionesService;
        this.securityService = securityService;
        this.grupoService = grupoService;
        this.traduccionService = traduccionService;

        // Inicializar componentes con textos traducidos
        comboRoles = new ComboBox<>(traduccionService.get("invitaciones.rol.pregunta"));
        comboTipoSensei = new ComboBox<>(traduccionService.get("invitaciones.tipo_sensei"));
        nombreField = new TextField(traduccionService.get("invitaciones.campo.nombre"));
        apellidoField = new TextField(traduccionService.get("invitaciones.campo.apellido"));
        celularField = new TextField(traduccionService.get("invitaciones.campo.celular"));
        emailField = new EmailField(traduccionService.get("invitaciones.campo.email"));
        btnGenerar = new Button(traduccionService.get("invitaciones.btn.generar"), VaadinIcon.MAGIC.create());

        comboTarifas = new ComboBox<>(traduccionService.get("invitaciones.campo.grupo_tarifario"));
        porcentajeComision = new NumberField(traduccionService.get("invitaciones.campo.porcentaje_comision"));

        panelResultado = new VerticalLayout();
        mensajeWhatsApp = new TextArea(traduccionService.get("invitaciones.panel.mensaje_whatsapp"));
        btnNuevaInvitacion = new Button(traduccionService.get("invitaciones.btn.nueva_invitacion"), VaadinIcon.REFRESH.create());

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
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.sensei"), "ROLE_SENSEI"));
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.judoka_adulto"), "ROLE_JUDOKA_ADULTO"));
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.acudiente"), "ROLE_ACUDIENTE"));
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.judoka"), "ROLE_JUDOKA"));
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.mecenas"), "ROLE_MECENAS"));
        } else {
            opciones.add(new OpcionRol(traduccionService.get("invitaciones.rol.judoka"), "ROLE_JUDOKA"));
        }

        comboRoles.setItems(opciones);
        comboRoles.setItemLabelGenerator(OpcionRol::nombreVisible);
        comboRoles.setValue(opciones.get(0));
        comboRoles.setEnabled(isMaster);
        comboRoles.setWidthFull();

        comboTipoSensei.setItems(
                traduccionService.get("invitaciones.tipo_sensei.club_propio"),
                traduccionService.get("invitaciones.tipo_sensei.externo")
        );
        comboTipoSensei.setValue(traduccionService.get("invitaciones.tipo_sensei.externo"));
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
        H2 titulo = new H2(traduccionService.get("invitaciones.titulo"));
        Paragraph subtitulo = new Paragraph(traduccionService.get("invitaciones.subtitulo"));

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

        Button btnCopiar = new Button(traduccionService.get("invitaciones.btn.copiar_mensaje"), VaadinIcon.COPY.create());
        btnCopiar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnCopiar.setWidthFull();
        btnCopiar.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "navigator.clipboard.writeText($0).then(function() { " +
                            "  $1.$server.notificarCopia(); " +
                            "});", mensajeWhatsApp.getValue(), getElement()
            ));
        });

        Button btnVerQR = new Button(traduccionService.get("invitaciones.btn.ver_qr"), VaadinIcon.QRCODE.create());
        btnVerQR.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnVerQR.setWidthFull();
        btnVerQR.addClickListener(e -> {
            String link = mensajeWhatsApp.getValue().split("👉 ")[1].trim();
            mostrarDialogoQR(link);
        });

        btnNuevaInvitacion.setWidthFull();
        btnNuevaInvitacion.addClickListener(e -> reiniciarParaLote());

        panelResultado.add(new H2(traduccionService.get("invitaciones.panel.titulo_enlace")), mensajeWhatsApp, btnCopiar, btnVerQR, btnNuevaInvitacion);
        add(panelResultado);
    }

    private void procesarInvitacion() {
        if (nombreField.isEmpty() || apellidoField.isEmpty() || emailField.isEmpty() || celularField.isEmpty()) {
            NotificationHelper.error(traduccionService.get("invitaciones.error.campos_incompletos"));
            return;
        }

        try {
            String rolElegido = comboRoles.getValue().rolDb();
            boolean esClubPropio = traduccionService.get("invitaciones.tipo_sensei.club_propio").equals(comboTipoSensei.getValue());

            Long grupoId = null;
            BigDecimal comisionPorcentaje = null;

            if ("ROLE_ACUDIENTE".equals(rolElegido) || "ROLE_JUDOKA_ADULTO".equals(rolElegido)) {
                if (comboTarifas.getValue() == null) {
                    NotificationHelper.error(traduccionService.get("invitaciones.error.seleccionar_grupo"));
                    return;
                }
                grupoId = comboTarifas.getValue().getId();
            } else if ("ROLE_SENSEI".equals(rolElegido)) {
                Double valor = porcentajeComision.getValue();
                if (valor == null || valor <= 0) {
                    NotificationHelper.error(traduccionService.get("invitaciones.error.porcentaje_invalido"));
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

            NotificationHelper.success(traduccionService.get("invitaciones.success.registro"));

        } catch (Exception ex) {
            NotificationHelper.error(traduccionService.get("invitaciones.error.generar") + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String redactarMensajeInteligente(boolean isMaster, String nombreInvitado, String link, String rol) {
        String remitente = isMaster ?
                traduccionService.get("invitaciones.mensaje.remitente.master") :
                traduccionService.get("invitaciones.mensaje.remitente.sensei");

        String saludo = traduccionService.get("invitaciones.mensaje.saludo", nombreInvitado, remitente);

        String cuerpo;
        if (rol.equals("ROLE_MECENAS")) {
            cuerpo = traduccionService.get("invitaciones.mensaje.cuerpo.mecenas");
        } else if (rol.equals("ROLE_SENSEI")) {
            cuerpo = traduccionService.get("invitaciones.mensaje.cuerpo.sensei");
        } else if (rol.equals("ROLE_ACUDIENTE")) {
            cuerpo = traduccionService.get("invitaciones.mensaje.cuerpo.acudiente");
        } else {
            cuerpo = traduccionService.get("invitaciones.mensaje.cuerpo.otros");
        }

        return saludo + cuerpo + "\n\n👉 " + link;
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

            Image qrImage = new Image(resource, traduccionService.get("invitaciones.qr.titulo"));
            qrImage.setWidth("300px");
            qrImage.setHeight("300px");

            Dialog dialog = new Dialog();
            dialog.add(new VerticalLayout(qrImage, new Button(traduccionService.get("invitaciones.qr.cerrar"), e -> dialog.close())));
            dialog.open();
        } catch (Exception ex) {
            Notification.show(traduccionService.get("invitaciones.error.generar") + ex.getMessage());
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