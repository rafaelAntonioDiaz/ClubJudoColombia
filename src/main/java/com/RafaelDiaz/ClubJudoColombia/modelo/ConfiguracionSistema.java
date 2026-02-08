package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

    @Id
    private Long id = 1L; // Siempre será el ID 1

    @Column(nullable = false)
    @NotBlank(message = "{validation.config.nombre_obligatorio}") // <--- I18n Key
    private String nombreOrganizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "{validation.config.nivel_obligatorio}") // <--- I18n Key
    private NivelOrganizacional nivel;

    private String moneda = "COP"; // Por defecto

    private String telefonoContacto;

    @Email(message = "{validation.email.invalido}") // <--- I18n Key
    private String emailSoporte;

    // URL del logo para recibos y cabecera
    private String urlLogo;

    private BigDecimal FIN_SAAS_CANON_FIJO;//Canon fijo mensual paga el judoka por el servicio SaaS.
    private BigDecimal FIN_SAAS_COMISION_CLUB;//Comisión cobra al club por el servicio SaaS.
    private BigDecimal SEC_TOKEN_EXPIRACION_HS;//Horas de vida del Magic Link.
    private BigDecimal FIN_DIA_COBRO_MENSUAL;//Día del mes en que corre el Cron Job.
    private BigDecimal FIN_DIA_VENCIMIENTO;// Día en que la cuenta pasa a estado VENCIDO.
    private BigDecimal COMISION_SENSEI_MENSUALIDAD;
    private BigDecimal FIN_SENSEI_MASTER_MENSUALIDAD;
    private BigDecimal FIN_ALQUILER_JUDOGI_ANUAL;
    private BigDecimal FIN_MATRICULA_ANUAL;

    // En su constructor de ConfiguracionSistema.java añada:
    public ConfiguracionSistema() {
        this.nombreOrganizacion = "Mi Club de Judo";
        this.nivel = NivelOrganizacional.CLUB;
        // Valores por defecto para evitar NullPointerException en cobros
        this.FIN_SAAS_CANON_FIJO = new BigDecimal("15000");
        this.COMISION_SENSEI_MENSUALIDAD = new BigDecimal("5000");
        this.FIN_SAAS_COMISION_CLUB = new BigDecimal("10000");
        this.SEC_TOKEN_EXPIRACION_HS = new BigDecimal("24");
        this.FIN_DIA_COBRO_MENSUAL = new BigDecimal("1");
        this.FIN_DIA_VENCIMIENTO = new BigDecimal("5");
        this.FIN_SENSEI_MASTER_MENSUALIDAD = new BigDecimal("50000");
        this.FIN_ALQUILER_JUDOGI_ANUAL = new BigDecimal("70000");
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreOrganizacion() { return nombreOrganizacion; }
    public void setNombreOrganizacion(String nombreOrganizacion) { this.nombreOrganizacion = nombreOrganizacion; }
    public NivelOrganizacional getNivel() { return nivel; }
    public void setNivel(NivelOrganizacional nivel) { this.nivel = nivel; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
    public String getEmailSoporte() { return emailSoporte; }
    public void setEmailSoporte(String emailSoporte) { this.emailSoporte = emailSoporte; }
    public String getUrlLogo() { return urlLogo; }
    public void setUrlLogo(String urlLogo) { this.urlLogo = urlLogo; }

    public void setFIN_SAAS_CANON_FIJO(BigDecimal FIN_SAAS_CANON_FIJO) {
        this.FIN_SAAS_CANON_FIJO = FIN_SAAS_CANON_FIJO;
    }

    public void setFIN_SAAS_COMISION_CLUB(BigDecimal FIN_SAAS_COMISION_CLUB) {
        this.FIN_SAAS_COMISION_CLUB = FIN_SAAS_COMISION_CLUB;
    }

    public void setSEC_TOKEN_EXPIRACION_HS(BigDecimal SEC_TOKEN_EXPIRACION_HS) {
        this.SEC_TOKEN_EXPIRACION_HS = SEC_TOKEN_EXPIRACION_HS;
    }

    public void setFIN_DIA_COBRO_MENSUAL(BigDecimal FIN_DIA_COBRO_MENSUAL) {
        this.FIN_DIA_COBRO_MENSUAL = FIN_DIA_COBRO_MENSUAL;
    }

    public void setFIN_DIA_VENCIMIENTO(BigDecimal FIN_DIA_VENCIMIENTO) {
        this.FIN_DIA_VENCIMIENTO = FIN_DIA_VENCIMIENTO;
    }

    public void setCOMISION_SENSEI_MENSUALIDAD(BigDecimal COMISION_SENSEI_MENSUALIDAD) {
        this.COMISION_SENSEI_MENSUALIDAD = COMISION_SENSEI_MENSUALIDAD;
    }

    public void setFIN_SENSEI_MASTER_MENSUALIDAD(BigDecimal FIN_SENSEI_MASTER_MENSUALIDAD) {
        this.FIN_SENSEI_MASTER_MENSUALIDAD = FIN_SENSEI_MASTER_MENSUALIDAD;
    }

    public BigDecimal getFIN_SAAS_COMISION_CLUB() {
        return FIN_SAAS_COMISION_CLUB;
    }

    public BigDecimal getSEC_TOKEN_EXPIRACION_HS() {
        return SEC_TOKEN_EXPIRACION_HS;
    }

    public BigDecimal getFIN_DIA_COBRO_MENSUAL() {
        return FIN_DIA_COBRO_MENSUAL;
    }

    public BigDecimal getFIN_DIA_VENCIMIENTO() {
        return FIN_DIA_VENCIMIENTO;
    }

    public BigDecimal getFIN_SENSEI_MASTER_MENSUALIDAD() {
        return FIN_SENSEI_MASTER_MENSUALIDAD;
    }

    public BigDecimal getCOMISION_SENSEI_MENSUALIDAD() {
        return COMISION_SENSEI_MENSUALIDAD;
    }
    public BigDecimal getFIN_SAAS_CANON_FIJO() {
        return FIN_SAAS_CANON_FIJO;
    }
    public BigDecimal getFIN_ALQUILER_JUDOGI_ANUAL() {
        return FIN_ALQUILER_JUDOGI_ANUAL;
    }

    public void setFIN_ALQUILER_JUDOGI_ANUAL(BigDecimal FIN_ALQUILER_JUDOGI_ANUAL) {
        this.FIN_ALQUILER_JUDOGI_ANUAL = FIN_ALQUILER_JUDOGI_ANUAL;
    }
    public BigDecimal getFIN_MATRICULA_ANUAL() {
        return FIN_MATRICULA_ANUAL;
    }

    public void setFIN_MATRICULA_ANUAL(BigDecimal FIN_MATRICULA_ANUAL) {
        this.FIN_MATRICULA_ANUAL = FIN_MATRICULA_ANUAL;
    }
}