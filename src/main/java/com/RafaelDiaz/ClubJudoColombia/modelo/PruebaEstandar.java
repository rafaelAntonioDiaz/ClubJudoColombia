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
    @Column(name = "id_ejercicio")
    private Long id;

    @Column(name = "nombre_key", unique = true, length = 200)
    private String nombreKey;

    @Lob
    @Column(name = "objetivo_key", columnDefinition = "TEXT")
    private String objetivoKey;

    @Lob
    @Column(name = "descripcion_key", columnDefinition = "TEXT")
    private String descripcionKey;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaEjercicio categoria;

    @Column(name = "es_global", nullable = false)
    private boolean esGlobal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei_creador")
    private Sensei senseiCreador;

    @Column(name = "nombre_personalizado", length = 200)
    private String nombrePersonalizado;

    @Lob
    @Column(name = "objetivo_personalizado", columnDefinition = "TEXT")
    private String objetivoPersonalizado;

    @Lob
    @Column(name = "descripcion_personalizada", columnDefinition = "TEXT")
    private String descripcionPersonalizada;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "prueba_estandar_metricas", // Tabla de unión corregida
            joinColumns = @JoinColumn(name = "id_ejercicio"),
            inverseJoinColumns = @JoinColumn(name = "id_metrica")
    )
    private Set<Metrica> metricas = new HashSet<>();

    public void setEsGlobal(boolean esGlobal) {
        this.esGlobal = esGlobal;
    }

    public boolean isEsGlobal() {
        return esGlobal;
    }

    public String getDescripcionPersonalizada() {
        return descripcionPersonalizada;
    }

    public void setDescripcionPersonalizada(String descripcionPersonalizada) {
        this.descripcionPersonalizada = descripcionPersonalizada;
    }

    public String getObjetivoPersonalizado() {
        return objetivoPersonalizado;
    }

    public void setObjetivoPersonalizado(String objetivoPersonalizado) {
        this.objetivoPersonalizado = objetivoPersonalizado;
    }

    public Sensei getSenseiCreador() {
        return senseiCreador;
    }

    public void setNombrePersonalizado(String nombrePersonalizado) {
        this.nombrePersonalizado = nombrePersonalizado;
    }

    public String getNombrePersonalizado() {
        return nombrePersonalizado;
    }

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

    public void setSenseiCreador(Sensei senseiCreador) {
        this.senseiCreador = senseiCreador;
    }

    /**
     * Devuelve el nombre correcto dependiendo del origen de la prueba.
     */
    public String getNombreMostrar(com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService traduccionService) {
        if (this.esGlobal && this.nombreKey != null) {
            return traduccionService.get(this.nombreKey);
        }
        return this.nombrePersonalizado != null ? this.nombrePersonalizado : "Prueba sin nombre";
    }

    /**
     * Devuelve la descripción correcta dependiendo del origen de la prueba.
     */
    public String getDescripcionMostrar(com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService traduccionService) {
        if (this.esGlobal && this.descripcionKey != null) {
            return traduccionService.get(this.descripcionKey);
        }
        return this.descripcionPersonalizada != null ? this.descripcionPersonalizada : "Sin descripción";
    }

    /**
     * Devuelve el objetivo correcto dependiendo del origen de la prueba.
     */
    public String getObjetivoMostrar(com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService traduccionService) {
        if (this.esGlobal && this.objetivoKey != null) {
            return traduccionService.get(this.objetivoKey);
        }
        return this.objetivoPersonalizado != null ? this.objetivoPersonalizado : "Sin objetivo definido";
    }
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