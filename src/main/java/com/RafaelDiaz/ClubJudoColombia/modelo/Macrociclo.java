package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la estructura mayor de planificación (Ej. Un año completo o una temporada).
 * Contiene múltiples Microciclos en su interior.
 */
@Entity
@Table(name = "macrociclos")
public class Macrociclo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_macrociclo")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "objetivo_principal", length = 255)
    private String objetivoPrincipal;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // Relacionamos el Macrociclo con el Sensei que lo creó
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    // --- LA MAGIA: UN MACROCICLO TIENE MUCHOS MICROCICLOS ---
    @OneToMany(mappedBy = "macrociclo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio ASC") // Queremos que siempre nos los devuelva ordenados cronológicamente
    private List<Microciclo> microciclos = new ArrayList<>();

    public Macrociclo() {}

    // --- Métodos de Conveniencia (Buenas prácticas JPA) ---
    public void addMicrociclo(Microciclo microciclo) {
        microciclos.add(microciclo);
        microciclo.setMacrociclo(this);
    }

    public void removeMicrociclo(Microciclo microciclo) {
        microciclos.remove(microciclo);
        microciclo.setMacrociclo(null);
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getObjetivoPrincipal() { return objetivoPrincipal; }
    public void setObjetivoPrincipal(String objetivoPrincipal) { this.objetivoPrincipal = objetivoPrincipal; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    public List<Microciclo> getMicrociclos() { return microciclos; }
    public void setMicrociclos(List<Microciclo> microciclos) { this.microciclos = microciclos; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Macrociclo that = (Macrociclo) obj;
        return id != null && id.equals(that.id);
    }
}