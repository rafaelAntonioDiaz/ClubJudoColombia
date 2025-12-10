package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import jakarta.persistence.*;
import java.io.Serializable;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;

    // --- DATOS FÍSICOS ---
    @Column(name = "peso_kg")
    private Double peso;

    @Column(name = "estatura_cm")
    private Double estatura;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    // --- DATOS TÉCNICOS ---
    @Enumerated(EnumType.STRING)
    @Column(name = "grado_cinturon", nullable = false)
    private GradoCinturon grado;

    @Lob
    @Column(name = "palmares", columnDefinition = "TEXT")
    private String palmares;

    @Column(name = "ocupacion_principal", length = 200)
    private String ocupacionPrincipal;

    @Column(name = "es_competidor_activo", nullable = false)
    private boolean esCompetidorActivo = false;

    // --- DATOS SALUD Y DOCUMENTOS (NUEVO) ---

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

// --- NUEVOS CAMPOS PARA ADMISIONES ---

    @Enumerated(EnumType.STRING)
    private EstadoJudoka estado = EstadoJudoka.PENDIENTE; // Por defecto entra pendiente

    private LocalDateTime fechaPreRegistro = LocalDateTime.now(); // Inicia el reloj de 15 días

    private boolean matriculaPagada = false; // Control financiero simple

    @OneToMany(mappedBy = "judoka", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoRequisito> documentos = new ArrayList<>();
    // --- CONSTRUCTORES ---
    public Judoka() {}

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

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

    // --- NUEVOS GETTERS/SETTERS ---

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

    public void setFechaPreRegistro(LocalDateTime fechaPreRegistro) {
        this.fechaPreRegistro = fechaPreRegistro;
    }

    public LocalDateTime getFechaPreRegistro() {
        return fechaPreRegistro;
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

    @Override
    public int hashCode() {
        if (usuario != null && usuario.getId() != null) {
            return usuario.getId().hashCode();
        }
        return id != null ? id.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Judoka that = (Judoka) obj;
        if (usuario != null && usuario.getId() != null) {
            return usuario.getId().equals(that.usuario != null ? that.usuario.getId() : null);
        }
        return id != null ? id.equals(that.id) : super.equals(obj);
    }
}