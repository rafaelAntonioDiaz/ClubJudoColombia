package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Invitar Nuevo Judoka | Club Judo Colombia")
@Route(value = "sensei/invitar") // Asume que tienes un MainLayout, agrégalo si es así: layout = MainLayout.class
@RolesAllowed("ROLE_SENSEI") // Seguridad: Solo el Sensei puede ver esta página
public class InvitarAspiranteView extends VerticalLayout {

    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService;

    // Campos del formulario
    private final TextField nombreField = new TextField();
    private final TextField apellidoField = new TextField();
    private final EmailField emailField = new EmailField();
    private final Button btnEnviar = new Button();

    @Autowired
    public InvitarAspiranteView(AdmisionesService admisionesService, TraduccionService traduccionService) {
        this.admisionesService = admisionesService;
        this.traduccionService = traduccionService;

        configurarVista();
        configurarFormulario();
        configurarEventos();
    }

    private void configurarVista() {
        // Títulos (Usando tu servicio de internacionalización)
        H2 titulo = new H2(traduccionService.get("vista.invitar.titulo")); // "Invitar Aspirante"
        Paragraph descripcion = new Paragraph(traduccionService.get("vista.invitar.descripcion")); // "Envíe un 'Magic Link' al correo..."

        setAlignItems(Alignment.CENTER);
        add(titulo, descripcion);
    }

    private void configurarFormulario() {
        nombreField.setLabel(traduccionService.get("label.nombre"));
        nombreField.setRequired(true);

        apellidoField.setLabel(traduccionService.get("label.apellido"));
        apellidoField.setRequired(true);

        // EmailField ya incluye validación de formato (ej: falta el @)
        emailField.setLabel(traduccionService.get("label.email"));
        emailField.setRequiredIndicatorVisible(true);
        emailField.setErrorMessage(traduccionService.get("error.email_invalido"));

        btnEnviar.setText(traduccionService.get("boton.enviar_invitacion"));
        btnEnviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnEnviar.setIcon(VaadinIcon.PAPERPLANE.create());

        FormLayout formLayout = new FormLayout();
        formLayout.add(nombreField, apellidoField, emailField, btnEnviar);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        // El botón ocupa las dos columnas en pantallas grandes
        formLayout.setColspan(emailField, 2);
        formLayout.setColspan(btnEnviar, 2);

        formLayout.setMaxWidth("600px");
        add(formLayout);
    }

    private void configurarEventos() {
        btnEnviar.addClickListener(event -> procesarInvitacion());
    }

    private void procesarInvitacion() {
        // 1. Validación básica de campos llenos
        if (nombreField.isEmpty() || apellidoField.isEmpty() || emailField.isEmpty() || emailField.isInvalid()) {
            mostrarNotificacion(traduccionService.get("error.campos_incompletos"), NotificationVariant.LUMO_ERROR);
            return;
        }

        // 2. Obtener la URL base dinámicamente para el Magic Link
        String baseUrl = obtenerBaseUrl();

        try {
            // 3. Llamar al servicio que acabamos de revisar
            admisionesService.generarInvitacion(
                    nombreField.getValue().trim(),
                    apellidoField.getValue().trim(),
                    emailField.getValue().trim(),
                    baseUrl
            );

            // 4. Éxito: Limpiar formulario y notificar
            mostrarNotificacion(traduccionService.get("exito.invitacion_enviada"), NotificationVariant.LUMO_SUCCESS);
            limpiarFormulario();

        } catch (Exception e) {
            mostrarNotificacion(traduccionService.get("error.sistema") + ": " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private String obtenerBaseUrl() {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        String scheme = request.getScheme(); // http o https
        String serverName = request.getServerName(); // localhost o tudominio.com
        int serverPort = request.getServerPort(); // 8080 u 80/443

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        } else {
            return scheme + "://" + serverName + ":" + serverPort;
        }
    }

    private void limpiarFormulario() {
        nombreField.clear();
        apellidoField.clear();
        emailField.clear();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notification = Notification.show(mensaje, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variante);
    }
}