package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad que representa el "contenedor" de un plan de entrenamiento.
 * Creado por un Sensei y asignado a uno o más Grupos de Entrenamiento.
 *
 * @author RafaelDiaz
 * @version 1.1 (Corregida)
 * @since 2025-11-20
 */
@Entity
@Table(name = "planes_entrenamiento")
public class PlanEntrenamiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei_creador", nullable = false)
    private Sensei sensei;

    @Column(name = "nombre_plan", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPlan estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sesion", nullable = false, length = 50)
    private TipoSesion tipoSesion = TipoSesion.ENTRENAMIENTO;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "plan_grupos",
            joinColumns = @JoinColumn(name = "id_plan"),
            inverseJoinColumns = @JoinColumn(name = "id_grupo")
    )
    private Set<GrupoEntrenamiento> gruposAsignados = new HashSet<>();

    @OneToMany(
            mappedBy = "planEntrenamiento",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EjercicioPlanificado> ejerciciosPlanificados = new ArrayList<>();

    public PlanEntrenamiento() {
        this.fechaAsignacion = LocalDate.now();
        this.estado = EstadoPlan.ACTIVO;
    }

    // ✅ CORREGIDO: Getters y Setters completos incluyendo tipoSesion
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public EstadoPlan getEstado() { return estado; }
    public void setEstado(EstadoPlan estado) { this.estado = estado; }

    // ✅ NUEVO: Getter y Setter para tipoSesion
    public TipoSesion getTipoSesion() { return tipoSesion; }
    public void setTipoSesion(TipoSesion tipoSesion) { this.tipoSesion = tipoSesion; }

    public List<EjercicioPlanificado> getEjerciciosPlanificados() { return ejerciciosPlanificados; }
    public void setEjerciciosPlanificados(List<EjercicioPlanificado> ejerciciosPlanificados) { this.ejerciciosPlanificados = ejerciciosPlanificados; }

    public Set<GrupoEntrenamiento> getGruposAsignados() { return gruposAsignados; }
    public void setGruposAsignados(Set<GrupoEntrenamiento> gruposAsignados) { this.gruposAsignados = gruposAsignados; }

    public void addEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.add(ejercicio);
        ejercicio.setPlanEntrenamiento(this);
    }

    public void removeEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.remove(ejercicio);
        ejercicio.setPlanEntrenamiento(null);
    }

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