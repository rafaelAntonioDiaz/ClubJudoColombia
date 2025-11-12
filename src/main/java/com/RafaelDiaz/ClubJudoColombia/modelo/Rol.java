package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad que representa un Rol en el sistema (ej. ENTRENADOR, COMPETIDOR).
 * Usamos una entidad en lugar de un Enum para permitir flexibilidad.
 * Se pueden añadir nuevos roles a la base de datos sin recompilar la aplicación.
 */
@Entity
@Table(name = "roles") // Nombre de la tabla en plural
public class Rol implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    /**
     * Nombre único del rol.
     * Es crucial que sea único (ej. "ROLE_ENTRENADOR", "ROLE_COMPETIDOR").
     * Usaremos el prefijo "ROLE_" por convención para la futura
     * integración con Spring Security.
     */
    @Column(name = "nombre", unique = true, nullable = false, length = 50)
    private String nombre;

    // --- Constructores ---

    /**
     * Constructor vacío requerido por JPA.
     */
    public Rol() {
    }

    /**
     * Constructor de conveniencia.
     */
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // --- hashCode y equals ---
    // Importante para que las relaciones @ManyToMany funcionen correctamente
    // en colecciones (Sets).

    @Override
    public int hashCode() {
        return nombre != null ? nombre.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Rol otroRol = (Rol) obj;
        // Comparamos por el nombre del rol, que debe ser único.
        return nombre != null && nombre.equals(otroRol.nombre);
    }
}