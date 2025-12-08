package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reflexiones")
public class Reflexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaEdicion;

    @Lob // Texto largo
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    public Reflexion() {}

    public Reflexion(Judoka judoka, String contenido) {
        this.judoka = judoka;
        this.contenido = contenido;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaEdicion = LocalDateTime.now();
    }

    // Lógica de Negocio: ¿Es editable?
    public boolean esEditable() {
        return LocalDateTime.now().isBefore(fechaCreacion.plusHours(24));
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaUltimaEdicion() { return fechaUltimaEdicion; }
    public void setFechaUltimaEdicion(LocalDateTime fechaUltimaEdicion) { this.fechaUltimaEdicion = fechaUltimaEdicion; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}