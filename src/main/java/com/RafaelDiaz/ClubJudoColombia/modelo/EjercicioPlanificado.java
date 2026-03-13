package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.*;

@Entity
@Table(name = "ejercicios_planificados")
public class EjercicioPlanificado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejercicio_plan")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_microciclo", nullable = false)
    private Microciclo microciclo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prueba_estandar")
    private PruebaEstandar pruebaEstandar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea_diaria")
    private TareaDiaria tareaDiaria;

    @Column(name = "notas_sensei")
    private String notasSensei;

    @Column(name = "orden_ejecucion")
    private Integer orden;

    @Column(name = "nota_ajuste")
    private String notaAjuste;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_tarea_dias", joinColumns = @JoinColumn(name = "id_ejercicio_plan"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Set<DayOfWeek> diasAsignados = new HashSet<>();

    @OneToMany(mappedBy = "ejercicioPlanificado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoPrueba> resultadosPrueba = new ArrayList<>();

    @OneToMany(mappedBy = "ejercicioPlanificado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EjecucionTarea> ejecucionesTarea = new ArrayList<>();

    @Column(name = "requiere_supervision", nullable = false)
    private boolean requiereSupervision = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka_asignado")
    private Judoka judokaAsignado;

    @Column(name = "intensidad")
    private Integer intensidad; // 1 a 5


    // ---------------------

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Microciclo getMicrociclo() { return microciclo; }
    public void setMicrociclo(Microciclo microciclo) { this.microciclo = microciclo; }

    public PruebaEstandar getPruebaEstandar() { return pruebaEstandar; }
    public void setPruebaEstandar(PruebaEstandar pruebaEstandar) { this.pruebaEstandar = pruebaEstandar; }

    public TareaDiaria getTareaDiaria() { return tareaDiaria; }
    public void setTareaDiaria(TareaDiaria tareaDiaria) { this.tareaDiaria = tareaDiaria; }

    public String getNotasSensei() { return notasSensei; }
    public void setNotasSensei(String notasSensei) { this.notasSensei = notasSensei; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public String getNotaAjuste() { return notaAjuste; }
    public void setNotaAjuste(String notaAjuste) { this.notaAjuste = notaAjuste; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public Set<DayOfWeek> getDiasAsignados() { return diasAsignados; }
    public void setDiasAsignados(Set<DayOfWeek> diasAsignados) { this.diasAsignados = diasAsignados; }

    public List<ResultadoPrueba> getResultadosPrueba() { return resultadosPrueba; }
    public void setResultadosPrueba(List<ResultadoPrueba> resultadosPrueba) { this.resultadosPrueba = resultadosPrueba; }

    public List<EjecucionTarea> getEjecucionesTarea() { return ejecucionesTarea; }
    public void setEjecucionesTarea(List<EjecucionTarea> ejecucionesTarea) { this.ejecucionesTarea = ejecucionesTarea; }

    // Nuevos getters/setters
    public boolean isRequiereSupervision() {
        return requiereSupervision;
    }

    public void setRequiereSupervision(boolean requiereSupervision) {
        this.requiereSupervision = requiereSupervision;
    }

    public Judoka getJudokaAsignado() {
        return judokaAsignado;
    }

    public void setJudokaAsignado(Judoka judokaAsignado) {
        this.judokaAsignado = judokaAsignado;
    }

    public Integer getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(Integer intensidad) {
        this.intensidad = intensidad;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EjercicioPlanificado that = (EjercicioPlanificado) obj;
        return id != null && id.equals(that.id);
    }
}