package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period; // --- NUEVO IMPORT ---

/**
 * --- RENOMBRADO ---
 * Entidad de Perfil que representa a un Judoka (antes Practicante).
 * Almacena los datos de negocio y se vincula a 'Usuario'.
 */
@Entity
@Table(name = "judokas") // --- RENOMBRADO ---
public class Judoka implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_judoka")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;

    @Column(name = "peso_kg")
    private Double peso;

    @Column(name = "estatura_cm")
    private Double estatura;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

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

    // --- NUEVOS CAMPOS (Acudiente y Waiver) ---

    /**
     * Nombre completo del acudiente o contacto de emergencia.
     * Esencial si el judoka es menor de edad.
     */
    @Column(name = "nombre_acudiente", length = 255)
    private String nombreAcudiente;

    /**
     * Teléfono del acudiente o contacto de emergencia.
     */
    @Column(name = "telefono_acudiente", length = 20)
    private String telefonoAcudiente;

    /**
     * Ruta al archivo (PDF/PNG) de la autorización firmada
     * y la exoneración de responsabilidad (waiver).
     * (Ej. "/almacenamiento/waivers/judoka_12.pdf")
     * Este campo será 'null' si el judoka es mayor de edad.
     */
    @Column(name = "ruta_autorizacion_waiver")
    private String rutaAutorizacionWaiver;

    // --- Constructores ---

    public Judoka() {
        // Constructor vacío requerido por JPA
    }

    // --- Getters y Setters (Incluyendo los nuevos) ---

    // (Getters/Setters de campos antiguos...)

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
    public void setGrado(GradoCinturon grado) { this.grado = grado; }
    public String getPalmares() { return palmares; }
    public void setPalmares(String palmares) { this.palmares = palmares; }
    public String getOcupacionPrincipal() { return ocupacionPrincipal; }
    public void setOcupacionPrincipal(String ocupacionPrincipal) { this.ocupacionPrincipal = ocupacionPrincipal; }
    public boolean isEsCompetidorActivo() { return esCompetidorActivo; }
    public void setEsCompetidorActivo(boolean esCompetidorActivo) { this.esCompetidorActivo = esCompetidorActivo; }


    // --- Getters/Setters para los NUEVOS campos ---

    public String getNombreAcudiente() {
        return nombreAcudiente;
    }

    public void setNombreAcudiente(String nombreAcudiente) {
        this.nombreAcudiente = nombreAcudiente;
    }

    public String getTelefonoAcudiente() {
        return telefonoAcudiente;
    }

    public void setTelefonoAcudiente(String telefonoAcudiente) {
        this.telefonoAcudiente = telefonoAcudiente;
    }

    public String getRutaAutorizacionWaiver() {
        return rutaAutorizacionWaiver;
    }

    public void setRutaAutorizacionWaiver(String rutaAutorizacionWaiver) {
        this.rutaAutorizacionWaiver = rutaAutorizacionWaiver;
    }

    // --- Lógica de Negocio (Helpers) ---

    /**
     * --- NUEVO MÉTODO (Lógica de Negocio) ---
     * Calcula dinámicamente si el judoka es menor de edad.
     * En Colombia, la mayoría de edad es a los 18 años.
     * No guardamos 'edad' en la BD, la calculamos.
     *
     * @return true si el judoka tiene menos de 18 años.
     */
    @Transient // Le dice a JPA que NO intente guardar esto en la BD.
    public boolean esMenorDeEdad() {
        if (this.fechaNacimiento == null) {
            return false; // O lanzar excepción, según se prefiera
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears() < 18;
    }
    /**
     * --- NUEVO MÉTODO (Lógica de Negocio) ---
     * Calcula y devuelve la edad actual del judoka en años.
     *
     * @return la edad en años.
     */
    @Transient // Le dice a JPA que NO intente guardar esto en la BD.
    public int getEdad() {
        if (this.fechaNacimiento == null) {
            return 0; // O lanzar una excepción
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }

    // --- hashCode y equals (Renombrados) ---

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

        Judoka that = (Judoka) obj; // --- RENOMBRADO ---

        if (usuario != null && usuario.getId() != null) {
            return usuario.getId().equals(that.usuario != null ? that.usuario.getId() : null);
        }
        return id != null ? id.equals(that.id) : super.equals(obj);
    }
}