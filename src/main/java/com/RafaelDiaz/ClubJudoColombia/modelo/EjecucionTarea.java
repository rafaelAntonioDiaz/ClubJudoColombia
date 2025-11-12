package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * NUEVA ENTIDAD
 * Almacena el "check" (completado) de una Tarea Diaria,
 * registrado por el Judoka (con GPS).
 */
@Entity
@Table(name = "ejecuciones_tareas")
public class EjecucionTarea implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejecucion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    /**
     * Vinculado al EjercicioPlanificado (la tarea específica del plan).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejercicio_plan", nullable = false)
    private EjercicioPlanificado ejercicioPlanificado;

    @Column(name = "completado", nullable = false)
    private boolean completado = false;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    // Getters, Setters, etc...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public EjercicioPlanificado getEjercicioPlanificado() { return ejercicioPlanificado; }
    public void setEjercicioPlanificado(EjercicioPlanificado ejercicioPlanificado) { this.ejercicioPlanificado = ejercicioPlanificado; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EjecucionTarea that = (EjecucionTarea) obj;
        return id != null && id.equals(that.id);
    }
}