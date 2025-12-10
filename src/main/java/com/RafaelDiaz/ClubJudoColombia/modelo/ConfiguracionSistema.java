package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    public ConfiguracionSistema() {
        this.nombreOrganizacion = "Mi Club de Judo";
        this.nivel = NivelOrganizacional.CLUB;
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
}