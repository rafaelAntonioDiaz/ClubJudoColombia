package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "campos_entrenamiento")
public class CampoEntrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    @Column(nullable = false)
    private String nombre; // Ej: "Campo de Altura Paipa 2025"

    private String ubicacion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    private String objetivo; // Ej: "Perfeccionamiento Ne-Waza"

    private boolean completado = false; // ¿Asistió y cumplió?

    private int puntosAscenso = 0; // La recompensa para el grado

    public CampoEntrenamiento() {}

    public CampoEntrenamiento(Judoka judoka, String nombre, String ubicacion, LocalDate fechaInicio, LocalDate fechaFin, String objetivo) {
        this.judoka = judoka;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.objetivo = objetivo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
    public int getPuntosAscenso() { return puntosAscenso; }
    public void setPuntosAscenso(int puntosAscenso) { this.puntosAscenso = puntosAscenso; }
}