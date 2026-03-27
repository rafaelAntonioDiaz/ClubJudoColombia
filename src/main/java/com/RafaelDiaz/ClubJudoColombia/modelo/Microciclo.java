package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MesocicloATC;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMicrociclo;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa una semana o bloque corto de entrenamiento estructurado.
 * Reemplaza al antiguo "PlanEntrenamiento" para cumplir con la teoría de Agudelo.
 */
@Entity
@Table(name = "microciclos") // <-- Nuevo nombre en la base de datos
public class Microciclo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_microciclo")
    private Long id;

    @Column(name = "nombre_microciclo", nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "microciclo_grupos",
            joinColumns = @JoinColumn(name = "id_microciclo"),
            inverseJoinColumns = @JoinColumn(name = "id_grupo")
    )
    private Set<GrupoEntrenamiento> gruposAsignados = new HashSet<>();

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "mesociclo_atc")
    private MesocicloATC mesocicloATC;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_microciclo")
    private TipoMicrociclo tipoMicrociclo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoMicrociclo estado;

    // --- RELACIÓN CON LOS EJERCICIOS ---
    @OneToMany(mappedBy = "microciclo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EjercicioPlanificado> ejerciciosPlanificados = new ArrayList<>();

    // --- RELACIÓN CON LAS PRUEBAS (EVALUACIONES) ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "microciclo_pruebas",
            joinColumns = @JoinColumn(name = "id_microciclo"),
            inverseJoinColumns = @JoinColumn(name = "id_prueba")
    )
    private Set<PruebaEstandar> pruebas = new HashSet<>();
    // --- RELACIÓN OPCIONAL HACIA EL MACROCICLO ---
    // Es nullable = true porque el Sensei puede crear un Microciclo suelto sin Macrociclo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_macrociclo", nullable = true)
    private Macrociclo macrociclo;

    public Microciclo() {}

    // --- Métodos de Conveniencia ---
    public void addEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.add(ejercicio);
        ejercicio.setMicrociclo(this);
    }

    public void removeEjercicio(EjercicioPlanificado ejercicio) {
        ejerciciosPlanificados.remove(ejercicio);
        ejercicio.setMicrociclo(null);
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    public Set<GrupoEntrenamiento> getGruposAsignados() { return gruposAsignados; }
    public void setGruposAsignados(Set<GrupoEntrenamiento> gruposAsignados) { this.gruposAsignados = gruposAsignados; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public MesocicloATC getMesocicloATC() { return mesocicloATC; }
    public void setMesocicloATC(MesocicloATC mesocicloATC) { this.mesocicloATC = mesocicloATC; }
    public TipoMicrociclo getTipoMicrociclo() { return tipoMicrociclo; }
    public void setTipoMicrociclo(TipoMicrociclo tipoMicrociclo) { this.tipoMicrociclo = tipoMicrociclo; }
    public EstadoMicrociclo getEstado() { return estado; }
    public void setEstado(EstadoMicrociclo estado) { this.estado = estado; }
    public List<EjercicioPlanificado> getEjerciciosPlanificados() { return ejerciciosPlanificados; }
    public void setEjerciciosPlanificados(List<EjercicioPlanificado> ejerciciosPlanificados) { this.ejerciciosPlanificados = ejerciciosPlanificados; }

    public Macrociclo getMacrociclo() {
        return macrociclo;
    }

    public void setMacrociclo(Macrociclo macrociclo) {
        this.macrociclo = macrociclo;
    }

    public Set<PruebaEstandar> getPruebas() { return pruebas; }

    public void setPruebas(Set<PruebaEstandar> pruebas) { this.pruebas = pruebas; }
    // --- Métodos de Conveniencia Pruebas ---
    public void addPrueba(PruebaEstandar prueba) {
        pruebas.add(prueba);
    }

    public void removePrueba(PruebaEstandar prueba) {
        pruebas.remove(prueba);
    }
    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Microciclo that = (Microciclo) obj;
        return id != null && id.equals(that.id);
    }


    public void clear() {

    }
}