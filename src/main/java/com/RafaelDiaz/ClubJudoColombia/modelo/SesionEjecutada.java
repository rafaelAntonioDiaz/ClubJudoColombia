package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una clase que ya fue dada en la vida real.
 * Es el puente entre la Planeación y el Control (Retroalimentación).
 */
@Entity
@Table(name = "sesiones_ejecutadas")
public class SesionEjecutada implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion_ejecutada")
    private Long id;

    // ¿Quién dio la clase?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    // ¿A qué grupo se le dio la clase?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoEntrenamiento grupo;

    // ¿Qué se planeó hacer ese día?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_microciclo", nullable = false)
    private Microciclo microciclo;

    // ¿Cuándo se ejecutó realmente?
    @Column(name = "fecha_hora_ejecucion", nullable = false)
    private LocalDateTime fechaHoraEjecucion;

    // La bitácora (La Fase 'R' de Agudelo)
    @Column(name = "notas_retroalimentacion", length = 1000)
    private String notasRetroalimentacion;

    // La lista de quién vino y quién no
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asistencia> listaAsistencia = new ArrayList<>();
    private Double latitud;
    private Double longitud;
    private Integer radioPermitidoMetros = 100;

    public SesionEjecutada() {
        this.fechaHoraEjecucion = LocalDateTime.now(); // Por defecto, marca el momento actual
    }



    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    public GrupoEntrenamiento getGrupo() { return grupo; }
    public void setGrupo(GrupoEntrenamiento grupo) { this.grupo = grupo; }
    public Microciclo getMicrociclo() { return microciclo; }
    public void setMicrociclo(Microciclo microciclo) { this.microciclo = microciclo; }
    public LocalDateTime getFechaHoraEjecucion() { return fechaHoraEjecucion; }
    public void setFechaHoraEjecucion(LocalDateTime fechaHoraEjecucion) { this.fechaHoraEjecucion = fechaHoraEjecucion; }
    public String getNotasRetroalimentacion() { return notasRetroalimentacion; }
    public void setNotasRetroalimentacion(String notasRetroalimentacion) { this.notasRetroalimentacion = notasRetroalimentacion; }
    public List<Asistencia> getListaAsistencia() { return listaAsistencia; }
    public void setListaAsistencia(List<Asistencia> listaAsistencia) { this.listaAsistencia = listaAsistencia; }
    public void addAsistencia(Asistencia asistencia) {
        listaAsistencia.add(asistencia);
        asistencia.setSesion(this);
    }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public Integer getRadioPermitidoMetros() { return radioPermitidoMetros; }
    public void setRadioPermitidoMetros(Integer radioPermitidoMetros) { this.radioPermitidoMetros = radioPermitidoMetros; }
}