package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin-sistema", layout = SenseiLayout.class)
@RolesAllowed("ROLE_ADMIN")
@PageTitle("Configuración del Sistema | Club Judo Colombia")
public class AdministracionView extends VerticalLayout {

    private final ConfiguracionService configuracionService;
    private final TraduccionService traduccionService;

    // Campos del Formulario
    private TextField nombreOrganizacion;
    private ComboBox<NivelOrganizacional> nivelSelector;
    private TextField telefono;
    private EmailField email;
    private TextField moneda;

    public AdministracionView(ConfiguracionService configuracionService, TraduccionService traduccionService) {
        this.configuracionService = configuracionService;
        this.traduccionService = traduccionService;

        addClassName("admin-view");
        setMaxWidth("800px");
        setMargin(true);
        setSpacing(true);

        // Títulos
        add(new H2(traduccionService.get("admin.titulo")));
        add(new Paragraph(traduccionService.get("admin.descripcion")));

        // Formulario
        FormLayout form = new FormLayout();

        nombreOrganizacion = new TextField(traduccionService.get("admin.field.nombre_org"));
        nombreOrganizacion.setPlaceholder("Ej: Club Judo Kodokan");

        nivelSelector = new ComboBox<>(traduccionService.get("admin.field.nivel"));
        nivelSelector.setItems(NivelOrganizacional.values());
        // Usamos el método simple para el nombre en el combo
        nivelSelector.setItemLabelGenerator(n -> traduccionService.get(n));
        nivelSelector.setHelperText(traduccionService.get("admin.helper.nivel"));

        telefono = new TextField(traduccionService.get("admin.field.telefono"));
        email = new EmailField(traduccionService.get("admin.field.email"));
        moneda = new TextField(traduccionService.get("admin.field.moneda"));
        moneda.setValue("COP");

        form.add(nombreOrganizacion, nivelSelector, telefono, email, moneda);
        form.setColspan(nombreOrganizacion, 2);
        form.setColspan(nivelSelector, 2);

        // Cargar datos actuales
        cargarDatos();

        // Botón Guardar
        Button btnGuardar = new Button(traduccionService.get("btn.guardar_cambios"), VaadinIcon.CHECK.create());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> guardarDatos());

        // Sección Informativa
        H4 warningTitle = new H4(traduccionService.get("admin.note.title"));
        Paragraph warningText = new Paragraph(traduccionService.get("admin.note.text"));
        warningText.getStyle().set("white-space", "pre-line").set("color", "gray");

        add(form, btnGuardar, warningTitle, warningText);
    }

    private void cargarDatos() {
        ConfiguracionSistema config = configuracionService.obtenerConfiguracion();
        nombreOrganizacion.setValue(config.getNombreOrganizacion());
        nivelSelector.setValue(config.getNivel());
        telefono.setValue(config.getTelefonoContacto() != null ? config.getTelefonoContacto() : "");
        email.setValue(config.getEmailSoporte() != null ? config.getEmailSoporte() : "");
        moneda.setValue(config.getMoneda());
    }

    private void guardarDatos() {
        if (nombreOrganizacion.isEmpty() || nivelSelector.getValue() == null) {
            Notification.show(traduccionService.get("error.campos_obligatorios")).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        ConfiguracionSistema config = configuracionService.obtenerConfiguracion();
        config.setNombreOrganizacion(nombreOrganizacion.getValue());
        config.setNivel(nivelSelector.getValue());
        config.setTelefonoContacto(telefono.getValue());
        config.setEmailSoporte(email.getValue());
        config.setMoneda(moneda.getValue());

        configuracionService.guardarConfiguracion(config);

        Notification.show(traduccionService.get("msg.success.config_saved"))
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}