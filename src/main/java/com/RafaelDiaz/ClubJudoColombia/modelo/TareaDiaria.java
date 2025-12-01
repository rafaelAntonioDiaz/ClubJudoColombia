package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * NUEVA ENTIDAD
 * Representa un ejercicio dinámico (Acondicionamiento)
 * creado por un Sensei (ej. "4x15 Flexiones").
 */
@Entity
@Table(name = "tareas_diarias")
public class TareaDiaria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarea")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Lob
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "meta_texto") // Ej. "4x15 reps"
    private String metaTexto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei_creador")
    private Sensei senseiCreador;

    public TareaDiaria(String nombre, String metaTexto, Sensei sensei) {
        this.nombre = nombre;
        this.metaTexto = metaTexto;
        this.senseiCreador = sensei;
    }

    public TareaDiaria() {

    }

    // Getters, Setters, hashCode, equals...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getMetaTexto() { return metaTexto; }
    public void setMetaTexto(String metaTexto) { this.metaTexto = metaTexto; }
    public Sensei getSenseiCreador() { return senseiCreador; }
    public void setSenseiCreador(Sensei senseiCreador) { this.senseiCreador = senseiCreador; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TareaDiaria that = (TareaDiaria) obj;
        return id != null && id.equals(that.id);
    }
}