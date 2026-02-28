package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Gestor de Invitaciones | Club Judo Colombia")
@Route(value = "invitaciones", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class GestorInvitacionesView extends VerticalLayout {

    private final AdmisionesService admisionesService;
    private final SecurityService securityService;

    // --- Componentes del Formulario Base ---
    private final ComboBox<OpcionRol> comboRoles = new ComboBox<>("¿A quién deseas invitar?");
    private final TextField nombreField = new TextField("Nombre");
    private final TextField apellidoField = new TextField("Apellido");
    private final TextField celularField = new TextField("Celular / WhatsApp");
    private final EmailField emailField = new EmailField("Email (Será su Usuario)");
    private final Button btnGenerar = new Button("Generar Enlace", VaadinIcon.MAGIC.create());

    // --- Componentes del Panel de Resultados ---
    private final VerticalLayout panelResultado = new VerticalLayout();
    private final TextArea mensajeWhatsApp = new TextArea("Mensaje listo para WhatsApp");
    private final Button btnNuevaInvitacion = new Button("Invitar a otro contacto", VaadinIcon.REFRESH.create());

    public GestorInvitacionesView(AdmisionesService admisionesService, SecurityService securityService) {
        this.admisionesService = admisionesService;
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        configurarSelectorRoles();
        configurarFormulario();
        configurarPanelResultado();
    }

    private void configurarSelectorRoles() {
        List<OpcionRol> opciones = new ArrayList<>();

        boolean isMaster = securityService.getAuthenticatedUserDetails()
                .map(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER")))
                .orElse(false);

        if (isMaster) {
            // Con control total.
            opciones.add(new OpcionRol("Profesor de Judo (Nuevo Dojo)", "ROLE_SENSEI"));
            opciones.add(new OpcionRol("Deportista Adulto", "ROLE_JUDOKA"));
            opciones.add(new OpcionRol("Padre de Familia / Acudiente", "ROLE_ACUDIENTE"));
            opciones.add(new OpcionRol("Mecenas / Patrocinador", "ROLE_MECENAS"));
        } else {
            // EL PROFESOR INVITADO: Solo puede invitar a sus alumnos.
            // Eliminamos la opción de invitar a otros profes o padres (si así lo prefieres para simplificar)
            opciones.add(new OpcionRol("Deportista (Judoka)", "ROLE_JUDOKA"));
        }

        comboRoles.setItems(opciones);
        comboRoles.setItemLabelGenerator(OpcionRol::nombreVisible);
        comboRoles.setValue(opciones.get(0));

        // Si no es master, deshabilitamos el combo para que no intente "hackear" el rol
        comboRoles.setEnabled(isMaster);
        comboRoles.setWidthFull();
    }
    private void configurarFormulario() {
        VerticalLayout cardForm = new VerticalLayout();
        cardForm.addClassName("card-blanca"); // Usa tu estilo existente
        cardForm.setMaxWidth("600px");
        cardForm.setPadding(true);

        H2 titulo = new H2("Centro de Invitaciones");
        Paragraph subtitulo = new Paragraph("Genera un acceso seguro y compártelo por WhatsApp.");

        nombreField.setRequired(true);
        apellidoField.setRequired(true);
        celularField.setRequired(true);
        emailField.setRequiredIndicatorVisible(true);

        btnGenerar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGenerar.setWidthFull();
        btnGenerar.addClickListener(e -> procesarInvitacion());

        FormLayout form = new FormLayout();
        form.add(nombreField, apellidoField, celularField, emailField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("400px", 2));

        cardForm.add(titulo, subtitulo, comboRoles, form, btnGenerar);
        add(cardForm);
    }

    private void configurarPanelResultado() {
        panelResultado.setMaxWidth("600px");
        panelResultado.addClassName("card-blanca");
        panelResultado.setVisible(false); // Oculto hasta que se genere un enlace

        mensajeWhatsApp.setWidthFull();
        mensajeWhatsApp.setReadOnly(true);
        mensajeWhatsApp.setMinHeight("150px");

        // Botón que usa JavaScript nativo para copiar al portapapeles
        Button btnCopiar = new Button("Copiar Mensaje", VaadinIcon.COPY.create());
        btnCopiar.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnCopiar.setWidthFull();
        btnCopiar.addClickListener(e -> {
            // Ejecuta script en el navegador para copiar al clipboard
            getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "navigator.clipboard.writeText($0).then(function() { " +
                            "  $1.$server.notificarCopia(); " +
                            "});", mensajeWhatsApp.getValue(), getElement()
            ));
        });

        btnNuevaInvitacion.setWidthFull();
        btnNuevaInvitacion.addClickListener(e -> reiniciarParaLote());

        panelResultado.add(new H2("¡Enlace Generado!"), mensajeWhatsApp, btnCopiar, btnNuevaInvitacion);
        add(panelResultado);
    }

    private void procesarInvitacion() {
        if (nombreField.isEmpty() || apellidoField.isEmpty() || emailField.isEmpty() || celularField.isEmpty()) {
            NotificationHelper.error("Por favor completa todos los campos.");
            return;
        }

        try {
            String rolElegido = comboRoles.getValue().rolDb();
            String tokenGenerado = admisionesService.generarInvitacion(
                    nombreField.getValue().trim(),
                    apellidoField.getValue().trim(),
                    emailField.getValue().trim(),
                    celularField.getValue().trim(),
                    rolElegido,
                    obtenerBaseUrl()
            );

            // Generar el texto contextual
            String linkFinal = obtenerBaseUrl() + "/acceso-dojo/" + tokenGenerado;
            boolean isMaster = securityService.getAuthenticatedUserDetails()
                    .map(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER")))
                    .orElse(false);

            String msj = redactarMensajeInteligente(isMaster, nombreField.getValue(), linkFinal, rolElegido);
            mensajeWhatsApp.setValue(msj);

            // Transición de UI
            btnGenerar.getParent().ifPresent(parent -> ((VerticalLayout) parent).setVisible(false)); // Oculta formulario
            panelResultado.setVisible(true); // Muestra resultados

            NotificationHelper.success("Invitación registrada en el sistema.");

        } catch (Exception ex) {
            NotificationHelper.error("Error al generar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String redactarMensajeInteligente(boolean isMaster, String nombreInvitado, String link, String rol) {
        String remitente = isMaster ? "el Profesor Rafael" : "tu Profesor de Judo";

        // Usamos \n\n para forzar párrafos en WhatsApp y aislar el enlace
        String base = "¡Hola " + nombreInvitado + "! Soy " + remitente + ".\n\n";

        if (rol.equals("ROLE_MECENAS")) {
            base += "Te invito a unirte a nuestra plataforma como Patrocinador para apoyar una mejor sociedad. Haz clic en el siguiente enlace para configurar tu perfil y ver tu impacto:\n\n";
        } else if (rol.equals("ROLE_SENSEI")) {
            base = "¡Hola " + nombreInvitado + "!\n\nBienvenido a la plataforma Club Judo. Usa el siguiente enlace para registrar tu Dojo y tu grado actual:\n\n";
        } else if (rol.equals("ROLE_ACUDIENTE")) {
            // NUEVO MENSAJE PARA PADRES DE FAMILIA
            base += "Te invito a unirte a nuestro portal deportivo como Acudiente. Haz clic en este enlace seguro para registrar a tus hijos, subir la documentación requerida (Waiver/EPS) y gestionar la mensualidad:\n\n";
        } else {
            base += "Te invito a unirte a nuestro portal deportivo. Haz clic en este enlace seguro para completar tu perfil y acceder a tu carnet digital:\n\n";
        }

        // El emoji y el salto de línea garantizan que WhatsApp lo vuelva Hipertexto azul
        return base + "👉 " + link;
    }
    private void reiniciarParaLote() {
        // Limpiamos campos de texto
        nombreField.clear();
        apellidoField.clear();
        celularField.clear();
        emailField.clear();

        // Restauramos UI
        panelResultado.setVisible(false);
        btnGenerar.getParent().ifPresent(parent -> ((VerticalLayout) parent).setVisible(true));

        // Foco automático para escribir rápido
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

    // Helper Record para el ComboBox
    public record OpcionRol(String nombreVisible, String rolDb) {}
}