package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un "equipo" o "grupo de entrenamiento".
 * Ej. "Equipo Masculino Sub-13", "Equipo Femenino Mayores".
 */
@Entity
@Table(name = "grupos_entrenamiento")
public class GrupoEntrenamiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /**
     * --- RELACIÓN (Muchos-a-Muchos con Judoka) ---
     * Un grupo tiene muchos Judokas.
     * Un Judoka puede pertenecer a varios grupos
     * (ej. "Sub-13" y "Equipo de Competencia").
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "judoka_grupos", // Tabla de unión
            joinColumns = @JoinColumn(name = "id_grupo"),
            inverseJoinColumns = @JoinColumn(name = "id_judoka")
    )
    private Set<Judoka> judokas = new HashSet<>();

    /**
     * --- RELACIÓN (Muchos-a-Muchos con Plan) ---
     * Un grupo puede tener asignados muchos planes.
     * Un plan puede ser asignado a muchos grupos.
     */
    @ManyToMany(mappedBy = "gruposAsignados", fetch = FetchType.LAZY)
    private Set<PlanEntrenamiento> planesAsignados = new HashSet<>();


    // --- Constructores ---
    public GrupoEntrenamiento() {}

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Set<Judoka> getJudokas() { return judokas; }
    public void setJudokas(Set<Judoka> judokas) { this.judokas = judokas; }
    public Set<PlanEntrenamiento> getPlanesAsignados() { return planesAsignados; }
    public void setPlanesAsignados(Set<PlanEntrenamiento> planesAsignados) { this.planesAsignados = planesAsignados; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return nombre != null ? nombre.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GrupoEntrenamiento that = (GrupoEntrenamiento) obj;
        return nombre != null && nombre.equals(that.nombre);
    }
}