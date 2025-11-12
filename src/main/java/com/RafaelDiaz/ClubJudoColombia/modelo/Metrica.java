package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "metricas")
public class Metrica implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metrica")
    private Long id;

    /**
     * --- CAMBIO I18N ---
     * Clave única de internacionalización (i18n) para el nombre.
     * Ej. "metrica.distancia"
     * El texto "Distancia (cm)" estará en los archivos .properties.
     */
    @Column(name = "nombre_key", nullable = false, unique = true)
    private String nombreKey;

    /**
     * La unidad (ej. "cm", "s", "reps", "lpm")
     * Esto no necesita traducción, es un dato técnico.
     */
    @Column(name = "unidad", nullable = false, length = 10)
    private String unidad;

    // --- Constructores ---
    public Metrica() {}

    public Metrica(String nombreKey, String unidad) {
        this.nombreKey = nombreKey;
        this.unidad = unidad;
    }

    // --- Getters y Setters (Actualizados) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreKey() { return nombreKey; }
    public void setNombreKey(String nombreKey) { this.nombreKey = nombreKey; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    // --- hashCode y equals (Actualizados) ---
    @Override
    public int hashCode() {
        return nombreKey != null ? nombreKey.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Metrica that = (Metrica) obj;
        return nombreKey != null && nombreKey.equals(that.nombreKey);
    }
}