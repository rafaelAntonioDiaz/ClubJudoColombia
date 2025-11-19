package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa una única sesión de entrenamiento programada.
 * (Ej. "Lunes 3 de Nov, 4:00pm").
 *
 * La lógica de "recurrencia" (L, M, V) la manejaremos en la
 * capa de Servicio/Inicialización, que creará múltiples
 * instancias de esta clase.
 */
@Entity
@Table(name = "sesiones_programadas")
public class SesionProgramada implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long id;

    /**
     * El Sensei que dirige esta sesión.
     * Una sesión solo tiene un Sensei (ManyToOne).
     * Un Sensei puede tener muchas sesiones (OneToMany).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    /**
     * El grupo de entrenamiento al que va dirigida la sesión.
     */
    @Column(name = "grupo", nullable = false)
    private GrupoEntrenamiento grupo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * --- CAMPO CLAVE (Regla de Negocio) ---
     * Si es 'true', esta sesión se permite aunque
     * caiga en domingo o festivo.
     * Si es 'false' (por defecto), el servicio validará
     * contra FestivosColombia.java y domingos.
     */
    @Column(name = "es_excepcion", nullable = false)
    private boolean esExcepcion = false;

    // --- Constructores ---
    public SesionProgramada() {}

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    public GrupoEntrenamiento getGrupo() { return grupo; }
    public void setGrupo(GrupoEntrenamiento grupo) { this.grupo = grupo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public boolean isEsExcepcion() { return esExcepcion; }
    public void setEsExcepcion(boolean esExcepcion) { this.esExcepcion = esExcepcion; }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SesionProgramada that = (SesionProgramada) obj;
        return id != null && id.equals(that.id);
    }
}