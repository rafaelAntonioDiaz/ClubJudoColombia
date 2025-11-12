package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMecenas;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad de Perfil que representa a un Mecenas (Patrocinador).
 * Se vincula a una entidad 'Usuario' para el login y, si el Mecenas
 * es una persona natural, para sus datos básicos (nombre, apellido).
 */
@Entity
@Table(name = "mecenas")
public class Mecenas implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mecenas")
    private Long id;

    /**
     * --- RELACIÓN CLAVE (One-to-One) ---
     * Conexión a la entidad Usuario.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;

    /**
     * Define si este patrocinador es una Persona Natural o una Empresa,
     * usando el Enum que creamos.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mecenas", nullable = false)
    private TipoMecenas tipo;

    // --- Campos Específicos para EMPRESA ---
    // Estos campos serán 'null' si el tipo_mecenas es 'PERSONA_NATURAL'.

    /**
     * Razón Social o nombre comercial de la empresa.
     */
    @Column(name = "nombre_empresa", length = 255)
    private String nombreEmpresa;

    /**
     * NIT o identificador fiscal de la empresa.
     */
    @Column(name = "nit_empresa", length = 50)
    private String nitEmpresa;


    /**
     * Descripción del patrocinio (ej. "Patrocinio mensual",
     * "Apoyo a competidores juveniles", etc.)
     */
    @Lob
    @Column(name = "descripcion_patrocinio", columnDefinition = "TEXT")
    private String descripcionPatrocinio;

    // --- Constructores ---

    public Mecenas() {
        // Constructor vacío requerido por JPA
    }

    // --- Getters y Setters ---

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

    public TipoMecenas getTipo() {
        return tipo;
    }

    public void setTipo(TipoMecenas tipo) {
        this.tipo = tipo;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNitEmpresa() {
        return nitEmpresa;
    }

    public void setNitEmpresa(String nitEmpresa) {
        this.nitEmpresa = nitEmpresa;
    }

    public String getDescripcionPatrocinio() {
        return descripcionPatrocinio;
    }

    public void setDescripcionPatrocinio(String descripcionPatrocinio) {
        this.descripcionPatrocinio = descripcionPatrocinio;
    }

    // --- hashCode y equals ---
    // Basado en el ID del Usuario.

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

        Mecenas that = (Mecenas) obj;

        if (usuario != null && usuario.getId() != null) {
            return usuario.getId().equals(that.usuario != null ? that.usuario.getId() : null);
        }
        return id != null ? id.equals(that.id) : super.equals(obj);
    }
}