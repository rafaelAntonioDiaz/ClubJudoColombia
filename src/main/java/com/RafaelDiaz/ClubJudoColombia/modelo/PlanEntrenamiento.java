package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet; // --- NUEVO ---
import java.util.List;
import java.util.Set; // --- NUEVO ---

/**
 * --- ACTUALIZADO ---
 * Entidad que representa el "contenedor" de un plan de entrenamiento.
 * Creado por un Sensei y asignado a uno o más Grupos de Entrenamiento.
 */
@Entity
@Table(name = "planes_entrenamiento")
public class PlanEntrenamiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long id;

    /**
     * El Sensei que crea y asigna este plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei_creador", nullable = false)
    private Sensei sensei;

    /**
     * --- CAMPO ELIMINADO ---
     * Ya no hay un 'id_judoka_asignado'.
     */
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "id_judoka_asignado", nullable = false)
    // private Judoka judoka;

    @Column(name = "nombre_plan", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPlan estado;

    /**
     * --- RELACIÓN NUEVA (Muchos-a-Muchos con Grupo) ---
     * Un plan se puede asignar a muchos grupos.
     * Un grupo puede recibir muchos planes.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "plan_grupos", // Tabla de unión
            joinColumns = @JoinColumn(name = "id_plan"),
            inverseJoinColumns = @JoinColumn(name = "id_grupo")
    )
    private Set<GrupoEntrenamiento> gruposAsignados = new HashSet<>();

    /**
     * --- RELACIÓN (One-to-Many) (Sin cambios) ---
     * Un Plan de Entrenamiento tiene MUCHOS Ejercicios Planificados.
     */
    @OneToMany(
            mappedBy = "planEntrenamiento",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EjercicioPlanificado> ejerciciosPlanificados = new ArrayList<>();

    // --- Constructores ---
    public PlanEntrenamiento() {
        this.fechaAsignacion = LocalDate.now();
        this.estado = EstadoPlan.PENDIENTE;
    }

    // --- Getters y Setters (Actualizados) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    // public Judoka getJudoka() { return judoka; } // --- ELIMINADO ---
    // public void setJudoka(Judoka judoka) { this.judoka = judoka; } // --- ELIMINADO ---
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public EstadoPlan getEstado() { return estado; }
    public void setEstado(EstadoPlan estado) { this.estado = estado; }
    public List<EjercicioPlanificado> getEjerciciosPlanificados() { return ejerciciosPlanificados; }
    public void setEjerciciosPlanificados(List<EjercicioPlanificado> ejerciciosPlanificados) { this.ejerciciosPlanificados = ejerciciosPlanificados; }

    // --- Getter/Setter NUEVO ---
    public Set<GrupoEntrenamiento> getGruposAsignados() { return gruposAsignados; }
    public void setGruposAsignados(Set<GrupoEntrenamiento> gruposAsignados) { this.gruposAsignados = gruposAsignados; }

    // --- Métodos Helper (Sin cambios) ---
    public void addEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.add(ejercicio);
        ejercicio.setPlanEntrenamiento(this);
    }

    public void removeEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.remove(ejercicio);
        ejercicio.setPlanEntrenamiento(null);
    }

    // hashCode y equals (Sin cambios)
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlanEntrenamiento that = (PlanEntrenamiento) obj;
        return id != null && id.equals(that.id);
    }
}