package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDAD REFACTORIZADA
 *
 * Representa una Prueba Estandarizada (SJFT, Salto, etc.)
 * que es cargada por Flyway y evaluada por un Sensei.
 */
@Entity
@Table(name = "pruebas_estandar")
public class PruebaEstandar implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejercicio") // Mantenemos el nombre de columna por consistencia de FKs
    private Long id;

    @Column(name = "nombre_key", nullable = false, unique = true, length = 200)
    private String nombreKey;

    @Lob
    @Column(name = "objetivo_key", nullable = false, columnDefinition = "TEXT")
    private String objetivoKey;

    @Lob
    @Column(name = "descripcion_key", nullable = false, columnDefinition = "TEXT")
    private String descripcionKey;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaEjercicio categoria;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "prueba_estandar_metricas", // Tabla de unión corregida
            joinColumns = @JoinColumn(name = "id_ejercicio"),
            inverseJoinColumns = @JoinColumn(name = "id_metrica")
    )
    private Set<Metrica> metricas = new HashSet<>();

    // Getters, Setters, hashCode, equals...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreKey() { return nombreKey; }
    public void setNombreKey(String nombreKey) { this.nombreKey = nombreKey; }
    public String getObjetivoKey() { return objetivoKey; }
    public void setObjetivoKey(String objetivoKey) { this.objetivoKey = objetivoKey; }
    public String getDescripcionKey() { return descripcionKey; }
    public void setDescripcionKey(String descripcionKey) { this.descripcionKey = descripcionKey; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public CategoriaEjercicio getCategoria() { return categoria; }
    public void setCategoria(CategoriaEjercicio categoria) { this.categoria = categoria; }
    public Set<Metrica> getMetricas() { return metricas; }
    public void setMetricas(Set<Metrica> metricas) { this.metricas = metricas; }

    @Override
    public int hashCode() { return nombreKey != null ? nombreKey.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PruebaEstandar that = (PruebaEstandar) obj;
        return nombreKey != null && nombreKey.equals(that.nombreKey);
    }
}