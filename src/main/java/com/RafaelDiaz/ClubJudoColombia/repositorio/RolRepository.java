package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Método para buscar un Rol por su nombre.
     * "findByNombre" se traduce a: "SELECT * FROM roles WHERE nombre = ?"
     */
    Optional<Rol> findByNombre(String nombre);
}