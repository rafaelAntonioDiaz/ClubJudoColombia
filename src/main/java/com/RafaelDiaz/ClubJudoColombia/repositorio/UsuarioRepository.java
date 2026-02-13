package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 *
 * JpaRepository nos provee (sin necesidad de escribirlos) métodos CRUD estándar:
 * - save()
 * - findById()
 * - findAll()
 * - delete()
 * - ...y muchos más.
 *
 * Hereda de JpaRepository<TipoDeEntidad, TipoDeID>
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Spring Data JPA creará automáticamente la consulta SQL para este método
     * basándose en el nombre del método.
     *
     * "findByUsername" se traduce a: "SELECT * FROM usuarios WHERE username = ?"
     *
     * Usamos Optional<> porque es una buena práctica para manejar valores
     * que pueden ser nulos (si el usuario no se encuentra).
     */
    Optional<Usuario> findByUsername(String username);

}