package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que registra la asistencia (o ausencia) de un
 * Judoka a una SesionProgramada específica.
 */
@Entity
@Table(name = "asistencias",
        // Creamos un índice único para evitar que un judoka
        // sea marcado dos veces en la misma sesión.
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_judoka", "id_sesion"})
)
public class Asistencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long id;

    /**
     * El Judoka que asiste.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    /**
     * La Sesión a la que se asiste.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion", nullable = false)
    private SesionProgramada sesion;

    /**
     * 'true' si asistió, 'false' si fue una ausencia justificada o no.
     */
    @Column(name = "presente", nullable = false)
    private boolean presente;

    /**
     * La fecha y hora exactas en que se marcó esta asistencia
     * (útil para auditoría).
     */
    @Column(name = "fecha_hora_marcacion", nullable = false)
    private LocalDateTime fechaHoraMarcacion;

    /**
     * (Opcional) Notas del sensei, ej. "Llegó tarde", "Justificado".
     */
    @Column(name = "notas")
    private String notas;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    // --- Constructores ---
    public Asistencia() {}

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public SesionProgramada getSesion() { return sesion; }
    public void setSesion(SesionProgramada sesion) { this.sesion = sesion; }
    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }
    public LocalDateTime getFechaHoraMarcacion() { return fechaHoraMarcacion; }
    public void setFechaHoraMarcacion(LocalDateTime fechaHoraMarcacion) { this.fechaHoraMarcacion = fechaHoraMarcacion; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Asistencia that = (Asistencia) obj;
        return id != null && id.equals(that.id);
    }
}