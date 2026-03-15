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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Route(value = "admin-sistema", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Configuración del Sistema | Club Judo Colombia")
public class AdministracionView extends VerticalLayout {

    private final ConfiguracionService configuracionService;
    private final TraduccionService traduccionService;

    // Campos de identidad
    private TextField nombreOrganizacion;
    private ComboBox<NivelOrganizacional> nivelSelector;
    private TextField telefono;
    private EmailField email;
    private TextField moneda;
    private TextField urlLogo;

    // Campos financieros (tarifas y comisiones)
    private NumberField canonSaaS;
    private NumberField comisionClub;
    private NumberField comisionSensei;
    private NumberField mensualidadMaster;
    private NumberField alquilerJudogi;
    private NumberField matriculaAnual;

    // Campos de fechas de cobro
    private NumberField diaCobro;
    private NumberField diaVencimiento;

    // Campos de seguridad
    private NumberField tokenExpiracionHoras;

    public AdministracionView(ConfiguracionService configuracionService, TraduccionService traduccionService) {
        this.configuracionService = configuracionService;
        this.traduccionService = traduccionService;

        addClassName("admin-view");
        setMaxWidth("1000px");
        setMargin(true);
        setSpacing(true);

        add(new H2(traduccionService.get("admin.titulo")));
        add(new Paragraph(traduccionService.get("admin.descripcion")));

        inicializarComponentes();

        Tabs tabs = new Tabs();
        Tab tabGeneral = new Tab(traduccionService.get("admin.tab.general"));
        Tab tabFinanzas = new Tab(traduccionService.get("admin.tab.finanzas"));
        Tab tabSeguridad = new Tab(traduccionService.get("admin.tab.seguridad"));
        tabs.add(tabGeneral, tabFinanzas, tabSeguridad);

        Map<Tab, FormLayout> forms = new HashMap<>();
        forms.put(tabGeneral, crearFormGeneral());
        forms.put(tabFinanzas, crearFormFinanzas());
        forms.put(tabSeguridad, crearFormSeguridad());

        cargarDatos();

        Button btnGuardar = new Button(traduccionService.get("btn.guardar_cambios"), VaadinIcon.CHECK.create());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> guardarDatos());

        tabs.addSelectedChangeListener(event -> {
            forms.values().forEach(f -> f.setVisible(false));
            forms.get(tabs.getSelectedTab()).setVisible(true);
        });

        // Mostrar solo el primer formulario inicialmente
        forms.get(tabGeneral).setVisible(true);
        forms.get(tabFinanzas).setVisible(false);
        forms.get(tabSeguridad).setVisible(false);

        add(tabs, forms.get(tabGeneral), forms.get(tabFinanzas), forms.get(tabSeguridad), btnGuardar);
    }

    private void inicializarComponentes() {
        nombreOrganizacion = new TextField();
        nivelSelector = new ComboBox<>();
        telefono = new TextField();
        email = new EmailField();
        moneda = new TextField();
        urlLogo = new TextField();

        canonSaaS = new NumberField();
        comisionClub = new NumberField();
        comisionSensei = new NumberField();
        mensualidadMaster = new NumberField();
        alquilerJudogi = new NumberField();
        matriculaAnual = new NumberField();

        diaCobro = new NumberField();
        diaVencimiento = new NumberField();

        tokenExpiracionHoras = new NumberField();
    }

    private FormLayout crearFormGeneral() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        nombreOrganizacion.setLabel(traduccionService.get("config.nombre.org"));
        nivelSelector.setLabel(traduccionService.get("config.nivel.org"));
        nivelSelector.setItems(NivelOrganizacional.values());
        nivelSelector.setItemLabelGenerator(n -> traduccionService.get(n.name()));

        telefono.setLabel(traduccionService.get("config.telefono"));
        email.setLabel(traduccionService.get("config.email"));
        moneda.setLabel(traduccionService.get("config.moneda"));
        moneda.setHelperText(traduccionService.get("config.moneda.helper"));
        urlLogo.setLabel(traduccionService.get("config.url_logo"));
        urlLogo.setHelperText(traduccionService.get("config.url_logo.helper"));

        form.add(nombreOrganizacion, nivelSelector, telefono, email, moneda, urlLogo);
        form.setColspan(nombreOrganizacion, 2);
        form.setColspan(urlLogo, 2);
        return form;
    }

    private FormLayout crearFormFinanzas() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        canonSaaS.setLabel(traduccionService.get("config.fin.canon_saas"));
        canonSaaS.setSuffixComponent(new Span("COP"));
        canonSaaS.setStep(1000);
        canonSaaS.setMin(0);

        comisionClub.setLabel(traduccionService.get("config.fin.comision_club"));
        comisionClub.setSuffixComponent(new Span("COP"));
        comisionClub.setStep(1000);
        comisionClub.setMin(0);

        comisionSensei.setLabel(traduccionService.get("config.fin.comision_sensei"));
        comisionSensei.setSuffixComponent(new Span("COP"));
        comisionSensei.setStep(1000);
        comisionSensei.setMin(0);

        mensualidadMaster.setLabel(traduccionService.get("config.fin.mensualidad_master"));
        mensualidadMaster.setSuffixComponent(new Span("COP"));
        mensualidadMaster.setStep(1000);
        mensualidadMaster.setMin(0);

        alquilerJudogi.setLabel(traduccionService.get("config.fin.alquiler_judogi"));
        alquilerJudogi.setSuffixComponent(new Span("COP"));
        alquilerJudogi.setStep(1000);
        alquilerJudogi.setMin(0);

        matriculaAnual.setLabel(traduccionService.get("config.fin.matricula_anual"));
        matriculaAnual.setSuffixComponent(new Span("COP"));
        matriculaAnual.setStep(1000);
        matriculaAnual.setMin(0);

        diaCobro.setLabel(traduccionService.get("config.fin.dia_cobro"));
        diaCobro.setMin(1);
        diaCobro.setMax(28);
        diaCobro.setStep(1);
        diaCobro.setHelperText(traduccionService.get("config.fin.dia_cobro.helper"));

        diaVencimiento.setLabel(traduccionService.get("config.fin.dia_vencimiento"));
        diaVencimiento.setMin(1);
        diaVencimiento.setMax(28);
        diaVencimiento.setStep(1);
        diaVencimiento.setHelperText(traduccionService.get("config.fin.dia_vencimiento.helper"));

        form.add(canonSaaS, comisionClub, comisionSensei, mensualidadMaster, alquilerJudogi, matriculaAnual,
                diaCobro, diaVencimiento);
        return form;
    }

    private FormLayout crearFormSeguridad() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        tokenExpiracionHoras.setLabel(traduccionService.get("config.seg.token_expiracion"));
        tokenExpiracionHoras.setSuffixComponent(new Span(traduccionService.get("config.seg.horas")));
        tokenExpiracionHoras.setMin(1);
        tokenExpiracionHoras.setMax(720);
        tokenExpiracionHoras.setStep(1);
        tokenExpiracionHoras.setHelperText(traduccionService.get("config.seg.token_expiracion.helper"));

        form.add(tokenExpiracionHoras);
        return form;
    }

    private void cargarDatos() {
        ConfiguracionSistema config = configuracionService.obtenerConfiguracion();

        nombreOrganizacion.setValue(config.getNombreOrganizacion());
        nivelSelector.setValue(config.getNivel());
        telefono.setValue(config.getTelefonoContacto() != null ? config.getTelefonoContacto() : "");
        email.setValue(config.getEmailSoporte() != null ? config.getEmailSoporte() : "");
        moneda.setValue(config.getMoneda());
        urlLogo.setValue(config.getUrlLogo() != null ? config.getUrlLogo() : "");

        canonSaaS.setValue(config.getFIN_SAAS_CANON_FIJO().doubleValue());
        comisionClub.setValue(config.getFIN_SAAS_COMISION_CLUB().doubleValue());
        comisionSensei.setValue(config.getCOMISION_SENSEI_MENSUALIDAD().doubleValue());
        mensualidadMaster.setValue(config.getFIN_SENSEI_MASTER_MENSUALIDAD().doubleValue());
        alquilerJudogi.setValue(config.getFIN_ALQUILER_JUDOGI_ANUAL().doubleValue());
        matriculaAnual.setValue(config.getFIN_MATRICULA_ANUAL().doubleValue());

        diaCobro.setValue(config.getFIN_DIA_COBRO_MENSUAL().doubleValue());
        diaVencimiento.setValue(config.getFIN_DIA_VENCIMIENTO().doubleValue());

        tokenExpiracionHoras.setValue(config.getSEC_TOKEN_EXPIRACION_HS().doubleValue());
    }

    private void guardarDatos() {
        try {
            ConfiguracionSistema config = configuracionService.obtenerConfiguracion();

            config.setNombreOrganizacion(nombreOrganizacion.getValue());
            config.setNivel(nivelSelector.getValue());
            config.setTelefonoContacto(telefono.getValue());
            config.setEmailSoporte(email.getValue());
            config.setMoneda(moneda.getValue());
            config.setUrlLogo(urlLogo.getValue());

            config.setFIN_SAAS_CANON_FIJO(BigDecimal.valueOf(canonSaaS.getValue()));
            config.setFIN_SAAS_COMISION_CLUB(BigDecimal.valueOf(comisionClub.getValue()));
            config.setCOMISION_SENSEI_MENSUALIDAD(BigDecimal.valueOf(comisionSensei.getValue()));
            config.setFIN_SENSEI_MASTER_MENSUALIDAD(BigDecimal.valueOf(mensualidadMaster.getValue()));
            config.setFIN_ALQUILER_JUDOGI_ANUAL(BigDecimal.valueOf(alquilerJudogi.getValue()));
            config.setFIN_MATRICULA_ANUAL(BigDecimal.valueOf(matriculaAnual.getValue()));

            config.setFIN_DIA_COBRO_MENSUAL(BigDecimal.valueOf(diaCobro.getValue()));
            config.setFIN_DIA_VENCIMIENTO(BigDecimal.valueOf(diaVencimiento.getValue()));

            config.setSEC_TOKEN_EXPIRACION_HS(BigDecimal.valueOf(tokenExpiracionHoras.getValue()));

            configuracionService.guardarConfiguracion(config);

            Notification.show(traduccionService.get("msg.success.config_saved"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}