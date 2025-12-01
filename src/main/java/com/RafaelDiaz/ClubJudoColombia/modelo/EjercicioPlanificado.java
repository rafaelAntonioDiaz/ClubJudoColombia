package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ENTIDAD REFACTORIZADA
 * Ahora vincula un Plan a una Prueba Estandar O a una Tarea Diaria.
 */
@Entity
@Table(name = "ejercicios_planificados")
public class EjercicioPlanificado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejercicio_plan")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanEntrenamiento planEntrenamiento;

    /**
     * --- LÓGICA REFACTORIZADA ---
     * Un plan puede ser una Prueba (evaluación)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prueba_estandar")
    private PruebaEstandar pruebaEstandar; // El "qué" (si es prueba)

    /**
     * O un plan puede ser una Tarea (acondicionamiento)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea_diaria")
    private TareaDiaria tareaDiaria; // El "qué" (si es tarea)

    @Column(name = "notas_sensei")
    private String notasSensei;

    @Column(name = "orden")
    private Integer orden;

    // Días de la semana que se hace el ejercicio
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_tarea_dias", joinColumns = @JoinColumn(name = "id_ejercicio_plan"))
    @Column(name = "dia_semana",length = 20)
    @Enumerated(EnumType.STRING) // <--- ESTO ES CLAVE: Guarda "MONDAY", "TUESDAY", etc.
    private Set<DayOfWeek> diasAsignados = new HashSet<>();
    /**
     * Un EjercicioPlanificado (tipo Prueba) tendrá MUCHOS ResultadosPrueba.
     */
    @OneToMany(
            mappedBy = "ejercicioPlanificado",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ResultadoPrueba> resultadosPrueba = new ArrayList<>();

    /**
     * Un EjercicioPlanificado (tipo Tarea) tendrá MUCHAS EjecucionesTarea.
     */
    @OneToMany(
            mappedBy = "ejercicioPlanificado",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EjecucionTarea> ejecucionesTarea = new ArrayList<>();

    // Getters, Setters, etc...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlanEntrenamiento getPlanEntrenamiento() { return planEntrenamiento; }
    public void setPlanEntrenamiento(PlanEntrenamiento planEntrenamiento) { this.planEntrenamiento = planEntrenamiento; }
    public PruebaEstandar getPruebaEstandar() { return pruebaEstandar; }
    public void setPruebaEstandar(PruebaEstandar pruebaEstandar) { this.pruebaEstandar = pruebaEstandar; }
    public TareaDiaria getTareaDiaria() { return tareaDiaria; }
    public void setTareaDiaria(TareaDiaria tareaDiaria) { this.tareaDiaria = tareaDiaria; }
    public String getNotasSensei() { return notasSensei; }
    public void setNotasSensei(String notasSensei) { this.notasSensei = notasSensei; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public List<ResultadoPrueba> getResultadosPrueba() { return resultadosPrueba; }
    public void setResultadosPrueba(List<ResultadoPrueba> resultadosPrueba) { this.resultadosPrueba = resultadosPrueba; }
    public List<EjecucionTarea> getEjecucionesTarea() { return ejecucionesTarea; }
    public void setEjecucionesTarea(List<EjecucionTarea> ejecucionesTarea) { this.ejecucionesTarea = ejecucionesTarea; }
    // --- GETTER/SETTER NUEVO ---
    public Set<DayOfWeek> getDiasAsignados() {
        return diasAsignados;
    }

    public void setDiasAsignados(Set<DayOfWeek> diasAsignados) {
        this.diasAsignados = diasAsignados;
    }
    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EjercicioPlanificado that = (EjercicioPlanificado) obj;
        return id != null && id.equals(that.id);
    }}