package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "judokas")
public class Judoka implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_judoka")
    private Long id;
    /**
     * El Usuario que gestiona legal y financieramente a este Judoka.
     * CAMBIO: De @OneToOne a @ManyToOne para permitir hermanos/familias.
     * Antes se llamaba 'usuario', ahora 'acudiente' para claridad semántica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acudiente", nullable = false)
    private Usuario acudiente;

    /**
     * El Mecenas que patrocina a este Judoka (Opcional).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mecenas")
    private Mecenas mecenas;

    /**
     * Para mayores de edad o contacto secundario.
     */
    @Column(name = "nombre_contacto_emergencia")
    private String nombreContactoEmergencia;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    /**
     * Token único para el "Pase del Dojo" (Login sin password en celular del niño).
     */
    @Column(name = "token_acceso_directo", unique = true)
    private String tokenAccesoDirecto;

    @Column(name = "fecha_generacion_token")
    private LocalDateTime fechaGeneracionToken;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    // --- DATOS FÍSICOS ---
    @Column(name = "peso_kg")
    private Double peso;

    @Column(name = "estatura_cm")
    private Double estatura;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo")
    private Sexo sexo;

    @Column(name = "es_mayor_edad")
    private boolean mayorEdad; // Flag para lógica de UI

    // --- DATOS TÉCNICOS ---
    @Enumerated(EnumType.STRING)
    @Column(name = "grado_cinturon")
    private GradoCinturon grado= GradoCinturon.BLANCO;

    @Lob
    @Column(name = "palmares", columnDefinition = "TEXT")
    private String palmares;

    @Column(name = "ocupacion_principal", length = 200)
    private String ocupacionPrincipal;

    @Column(name = "es_competidor_activo")
    private boolean esCompetidorActivo = false;

    @Enumerated(EnumType.STRING)
    private EstadoJudoka estado = EstadoJudoka.PENDIENTE; // Por defecto entra pendiente

    // --- DATOS SALUD Y DOCUMENTOS ---

    @Column(name = "eps", length = 100)
    private String eps;
    /**
     * Ruta relativa donde se guarda el certificado de afiliación.
     * Ej: "documentos/eps/julian_eps.pdf"
     */
    @Column(name = "ruta_certificado_eps")
    private String rutaCertificadoEps;

    @Column(name = "celular", nullable = true, length = 13)
    private String celular;


    // --- DATOS DE ACUDIENTE Y LEGAL ---

    @Column(name = "nombre_acudiente", length = 255)
    private String nombreAcudiente;

    /**
     * Teléfono del acudiente (Funciona como teléfono de emergencia).
     */
    @Column(name = "telefono_acudiente", length = 20)
    private String telefonoAcudiente;

    /**
     * Ruta relativa donde se guarda el waiver firmado.
     * Ej: "documentos/waivers/julian_waiver.pdf"
     */
    @Column(name = "ruta_autorizacion_waiver")
    private String rutaAutorizacionWaiver;

    @Column(name = "url_foto_perfil")
    private String urlFotoPerfil;

    @Column(name = "nombre_atleta") // Puede ser nullable=false si ya limpiaste la BD
    private String nombre;

    @Column(name = "apellido_atleta")
    private String apellido;


    @Column(name = "matricula_pagada")
    private boolean matriculaPagada = false;

    @Column(name = "monto_mensualidad")
    private BigDecimal montoMensualidad; // Control financiero simple

    @OneToMany(mappedBy = "judoka", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoRequisito> documentos = new ArrayList<>();

    @Column(name = "fecha_vencimiento_suscripcion")
    private LocalDate fechaVencimientoSuscripcion;

    @Column(name = "suscripcion_activa")
    private boolean suscripcionActiva = false; // Por defecto no entra hasta que pague

    @Column(name = "tiene_judogi_alquilado")
    private boolean tieneJudogiAlquilado = false;

    @Column(name = "fecha_devolucion_judogi")
    private LocalDate fechaDevolucionJudogi;


    public Judoka() {}


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getMontoMensualidad() { return montoMensualidad; }
    public void setMontoMensualidad(BigDecimal montoMensualidad) {
        this.montoMensualidad = montoMensualidad;
    }

    // IMPORTANTE: Este método sustituye al antiguo getUsuario()
    public Usuario getAcudiente() {
        return acudiente;
    }

    public void setAcudiente(Usuario acudiente) {
        this.acudiente = acudiente;
    }

    // Método de compatibilidad temporal (Opcional, para no romper vista viejas ya mismo)
    public Usuario getUsuario() {
        return acudiente;
    }

    public Mecenas getMecenas() { return mecenas; }
    public void setMecenas(Mecenas mecenas) { this.mecenas = mecenas; }

    public String getNombreContactoEmergencia() { return nombreContactoEmergencia; }
    public void setNombreContactoEmergencia(String contactoEmergencia) { this.nombreContactoEmergencia = contactoEmergencia; }

    public String getTokenAccesoDirecto() { return tokenAccesoDirecto; }
    public void setTokenAccesoDirecto(String tokenAccesoDirecto) { this.tokenAccesoDirecto = tokenAccesoDirecto; }

    public void setFechaGeneracionToken(LocalDateTime fechaGeneracionToken) {
        this.fechaGeneracionToken = fechaGeneracionToken;
    }

    public boolean isTieneJudogiAlquilado() { return tieneJudogiAlquilado; }
    public void setTieneJudogiAlquilado(boolean tieneJudogiAlquilado) { this.tieneJudogiAlquilado = tieneJudogiAlquilado; }

    public LocalDate getFechaDevolucionJudogi() { return fechaDevolucionJudogi; }
    public void setFechaDevolucionJudogi(LocalDate fechaDevolucionJudogi) { this.fechaDevolucionJudogi = fechaDevolucionJudogi; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getEstatura() { return estatura; }
    public void setEstatura(Double estatura) { this.estatura = estatura; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public GradoCinturon getGrado() { return grado; }
    // Alias para compatibilidad con DataInitializer
    public void setGradoCinturon(GradoCinturon grado) { this.grado = grado; }
    public void setGrado(GradoCinturon grado) { this.grado = grado; }
    public GradoCinturon getGradoCinturon() {
        return grado;
    }

    public String getPalmares() { return palmares; }
    public void setPalmares(String palmares) { this.palmares = palmares; }

    public String getOcupacionPrincipal() { return ocupacionPrincipal; }
    public void setOcupacionPrincipal(String ocupacionPrincipal) {
        this.ocupacionPrincipal = ocupacionPrincipal; }

    public boolean isEsCompetidorActivo() { return esCompetidorActivo; }
    public void setEsCompetidorActivo(boolean esCompetidorActivo) {
        this.esCompetidorActivo = esCompetidorActivo; }

    public String getEps() { return eps; }
    public void setEps(String eps) { this.eps = eps; }

    public String getRutaCertificadoEps() { return rutaCertificadoEps; }
    public void setRutaCertificadoEps(String rutaCertificadoEps) {
        this.rutaCertificadoEps = rutaCertificadoEps; }

    public String getNombreAcudiente() { return nombreAcudiente; }
    public void setNombreAcudiente(String nombreAcudiente) {
        this.nombreAcudiente = nombreAcudiente; }

    public String getTelefonoAcudiente() { return telefonoAcudiente; }
    public void setTelefonoAcudiente(String telefonoAcudiente) {
        this.telefonoAcudiente = telefonoAcudiente; }

    public String getRutaAutorizacionWaiver() { return rutaAutorizacionWaiver; }
    public void setRutaAutorizacionWaiver(String rutaAutorizacionWaiver) {
        this.rutaAutorizacionWaiver = rutaAutorizacionWaiver; }
    // --- LÓGICA DE NEGOCIO ---

    @Transient
    public boolean esMenorDeEdad() {
        if (this.fechaNacimiento == null) return false;
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears() < 18;
    }

    @Transient
    public int getEdad() {
        if (this.fechaNacimiento == null) return 0;
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getUrlFotoPerfil() { return urlFotoPerfil; }

    public void setUrlFotoPerfil(String urlFotoPerfil) { this.urlFotoPerfil = urlFotoPerfil; }
    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getCelular() {
        return celular;
    }

    public EstadoJudoka getEstado() {
        return estado;
    }

    public void setEstado(EstadoJudoka estado) {
        this.estado = estado;
    }



    public void setMatriculaPagada(boolean matriculaPagada) {
        this.matriculaPagada = matriculaPagada;
    }

    public boolean isMatriculaPagada() {
        return matriculaPagada;
    }

    public List<DocumentoRequisito> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentoRequisito> documentos) {
        this.documentos = documentos;
    }

    public Sensei getSensei() {
        return sensei;
    }

    public void setSensei(Sensei sensei) {
        this.sensei = sensei;
    }
    // Helper para saber si puede entrenar hoy
    public boolean puedeEntrenar() {
        return suscripcionActiva &&
                fechaVencimientoSuscripcion != null &&
                fechaVencimientoSuscripcion.isAfter(LocalDate.now());
    }

    @Override
    public int hashCode() {
        if (acudiente != null && acudiente.getId() != null) {
            return acudiente.getId().hashCode();
        }
        return id != null ? id.hashCode() : super.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Judoka that = (Judoka) obj;
        if (acudiente != null && acudiente.getId() != null) {
            return acudiente.getId().equals(that.acudiente != null ? that.acudiente.getId() : null);
        }
        return id != null ? id.equals(that.id) : super.equals(obj);
    }

    public LocalDate getFechaVencimientoSuscripcion() {
        return fechaVencimientoSuscripcion;
    }

    public void setFechaVencimientoSuscripcion(LocalDate fechaSuscripcion) {
        int daySuscribed = fechaSuscripcion.getDayOfMonth();
        int daysMonth = fechaSuscripcion.lengthOfMonth();
        this.fechaVencimientoSuscripcion = fechaSuscripcion.plusDays(daysMonth - daySuscribed + 1);
    }

    public void setSuscripcionActiva(boolean activa) {
    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;

    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDateTime getFechaGeneracionToken() {
        return fechaGeneracionToken;
    }
    public boolean requiereAcompañante() {
        return !mayorEdad;
    }
    public void setMayorEdad(boolean mayorEdad) {
        this.mayorEdad = mayorEdad;
    }
    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(String telefonoEmergencia) {
        this.telefonoEmergencia = telefonoEmergencia;
    }
    public boolean isMayorEdad() {
        return mayorEdad;
    }

    public boolean isSuscripcionActiva() {
        return suscripcionActiva;
    }
}