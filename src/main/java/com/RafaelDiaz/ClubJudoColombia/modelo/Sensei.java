package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad de Perfil que representa a un Entrenador (Sensei).
 * (Actualizada para manejar la ruta de certificaciones)
 */
@Entity
@Table(name = "senseis")
public class Sensei implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sensei")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "grado_cinturon", nullable = false)
    private GradoCinturon grado;

    @Column(name = "anos_practica")
    private Integer anosPractica;

    /**
     * --- CAMPO ACTUALIZADO ---
     * Este campo ya NO guarda el texto de las certificaciones.
     * Ahora guarda la RUTA (path) al archivo (PDF o PNG)
     * que se subió al almacenamiento del servidor.
     *
     * Ej. "/almacenamiento/certificaciones/sensei_5.pdf"
     *
     * (La lógica para subir el archivo y generar esta ruta
     * la implementaremos en el 'Servicio' y el 'Formulario'
     * más adelante).
     */
    @Column(name = "ruta_certificaciones_archivo")
    private String rutaCertificaciones;

    /**
     * Campo de texto largo para la biografía del Sensei.
     */
    @Lob
    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    // --- Constructores ---

    public Sensei() {
        // Constructor vacío requerido por JPA
    }

    // --- Getters y Setters (Actualizados) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public GradoCinturon getGrado() {
        return grado;
    }

    public void setGrado(GradoCinturon grado) {
        this.grado = grado;
    }

    public Integer getAnosPractica() {
        return anosPractica;
    }

    public void setAnosPractica(Integer anosPractica) {
        this.anosPractica = anosPractica;
    }

    /**
     * Getter para la RUTA del archivo
     */
    public String getRutaCertificaciones() {
        return rutaCertificaciones;
    }

    /**
     * Setter para la RUTA del archivo
     */
    public void setRutaCertificaciones(String rutaCertificaciones) {
        this.rutaCertificaciones = rutaCertificaciones;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    // ... (hashCode y equals no cambian) ...

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

        Sensei that = (Sensei) obj;

        if (usuario != null && usuario.getId() != null) {
            return usuario.getId().equals(that.usuario != null ? that.usuario.getId() : null);
        }
        return id != null ? id.equals(that.id) : super.equals(obj);
    }
}