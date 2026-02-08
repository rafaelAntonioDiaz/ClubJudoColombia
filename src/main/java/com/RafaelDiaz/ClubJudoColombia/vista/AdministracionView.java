package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.math.BigDecimal;

@Route(value = "admin-sistema", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Configuración del Sistema | Club Judo Colombia")
public class AdministracionView extends VerticalLayout {

    private final ConfiguracionService configuracionService;
    private final TraduccionService traduccionService;

    // --- 1. DECLARACIÓN DE TODOS LOS CAMPOS ---
    private TextField nombreOrganizacion;
    private ComboBox<NivelOrganizacional> nivelSelector;
    private TextField telefono;
    private EmailField email;
    private TextField moneda;
    private NumberField mensualidadMasterField;
    private NumberField canonFijoField;
    private NumberField diaCobroField;
    private NumberField diaVencimientoField; // <--- CORRECCIÓN: Declarado

    public AdministracionView(ConfiguracionService configuracionService, TraduccionService traduccionService) {
        this.configuracionService = configuracionService;
        this.traduccionService = traduccionService;

        // --- 2. INSTANCIACIÓN OBLIGATORIA (Evita NPE) ---
        inicializarComponentes();

        addClassName("admin-view");
        setMaxWidth("800px");
        setMargin(true);
        setSpacing(true);

        add(new H2(traduccionService.get("admin.titulo")));
        add(new Paragraph(traduccionService.get("admin.descripcion")));

        FormLayout form = new FormLayout();
        configurarFormulario(form);

        cargarDatos();

        Button btnGuardar = new Button(traduccionService.get("btn.guardar_cambios"), VaadinIcon.CHECK.create());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> guardarDatos());

        add(form, btnGuardar, new H4(traduccionService.get("admin.note.title")), new Paragraph(traduccionService.get("admin.note.text")));
    }

    private void inicializarComponentes() {
        nombreOrganizacion = new TextField();
        nivelSelector = new ComboBox<>();
        telefono = new TextField();
        email = new EmailField();
        moneda = new TextField();
        mensualidadMasterField = new NumberField();
        canonFijoField = new NumberField();
        diaCobroField = new NumberField();
        diaVencimientoField = new NumberField();
    }

    private void configurarFormulario(FormLayout form) {
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        // i18n y Suffixes
        nombreOrganizacion.setLabel(traduccionService.get("config.nombre.org"));
        nivelSelector.setLabel(traduccionService.get("config.nivel.org"));
        nivelSelector.setItems(NivelOrganizacional.values());
        nivelSelector.setItemLabelGenerator(n -> traduccionService.get(n.name()));

        telefono.setLabel(traduccionService.get("config.telefono"));
        email.setLabel(traduccionService.get("config.email"));

        moneda.setLabel(traduccionService.get("config.moneda"));
        moneda.setReadOnly(true);

        // Campos Financieros
        canonFijoField.setLabel(traduccionService.get("config.fin.canon_saas"));
        canonFijoField.setSuffixComponent(new Span("COP"));

        mensualidadMasterField.setLabel(traduccionService.get("config.fin.mensualidad_master"));
        mensualidadMasterField.setSuffixComponent(new Span("COP"));

        diaCobroField.setLabel(traduccionService.get("config.fin.dia_cobro"));
        diaVencimientoField.setLabel(traduccionService.get("config.fin.dia_vencimiento"));

        form.add(nombreOrganizacion, nivelSelector, telefono, email, moneda,
                canonFijoField, mensualidadMasterField, diaCobroField, diaVencimientoField);

        form.setColspan(nombreOrganizacion, 2);
    }

    private void cargarDatos() {
        ConfiguracionSistema config = configuracionService.obtenerConfiguracion();

        nombreOrganizacion.setValue(config.getNombreOrganizacion());
        nivelSelector.setValue(config.getNivel());
        telefono.setValue(config.getTelefonoContacto() != null ? config.getTelefonoContacto() : "");
        email.setValue(config.getEmailSoporte() != null ? config.getEmailSoporte() : "");
        moneda.setValue(config.getMoneda());

        // Conversión segura de BigDecimal a Double para la UI
        canonFijoField.setValue(config.getFIN_SAAS_CANON_FIJO().doubleValue());
        mensualidadMasterField.setValue(config.getFIN_SENSEI_MASTER_MENSUALIDAD().doubleValue());
        diaCobroField.setValue(config.getFIN_DIA_COBRO_MENSUAL().doubleValue());
        diaVencimientoField.setValue(config.getFIN_DIA_VENCIMIENTO().doubleValue());
    }

    private void guardarDatos() {
        try {
            ConfiguracionSistema config = configuracionService.obtenerConfiguracion();

            config.setNombreOrganizacion(nombreOrganizacion.getValue());
            config.setNivel(nivelSelector.getValue());
            config.setTelefonoContacto(telefono.getValue());
            config.setEmailSoporte(email.getValue());

            // Conversión de Double (UI) a BigDecimal (Entidad)
            config.setFIN_SAAS_CANON_FIJO(BigDecimal.valueOf(canonFijoField.getValue()));
            config.setFIN_SENSEI_MASTER_MENSUALIDAD(BigDecimal.valueOf(mensualidadMasterField.getValue()));
            config.setFIN_DIA_COBRO_MENSUAL(BigDecimal.valueOf(diaCobroField.getValue()));
            config.setFIN_DIA_VENCIMIENTO(BigDecimal.valueOf(diaVencimientoField.getValue()));

            configuracionService.guardarConfiguracion(config);

            Notification.show(traduccionService.get("msg.success.config_saved"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}