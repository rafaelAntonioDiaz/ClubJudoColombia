package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad para almacenar traducciones (i18n) en la base de datos.
 * Esto reemplaza la necesidad de archivos .properties estáticos
 * y permite a los administradores añadir/editar textos.
 */
@Entity
@Table(name = "traducciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"clave", "idioma"}) // Una clave solo puede tener una traducción por idioma
)
public class Traduccion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La clave de i18n. Ej. "ejercicio.sjft.nombre"
     */
    @Column(name = "clave", nullable = false, length = 255)
    private String clave;

    /**
     * El código del idioma. Ej. "es", "en", "pt"
     */
    @Column(name = "idioma", nullable = false, length = 5)
    private String idioma;

    /**
     * El texto traducido.
     */
    @Lob // Usamos TEXT para textos largos
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    // --- Constructores, Getters/Setters, hashCode/equals ---

    public Traduccion() {}

    public Traduccion(String clave, String idioma, String texto) {
        this.clave = clave;
        this.idioma = idioma;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    @Override
    public int hashCode() {
        return clave != null ? clave.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Traduccion that = (Traduccion) obj;
        if (clave != null ? !clave.equals(that.clave) : that.clave != null) return false;
        return idioma != null ? idioma.equals(that.idioma) : that.idioma == null;
    }
}