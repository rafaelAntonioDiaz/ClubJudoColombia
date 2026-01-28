package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SenseiRepository extends JpaRepository<Sensei, Long> {

    // Método para buscar un perfil de Sensei usando su objeto Usuario
    Optional<Sensei> findByUsuario(Usuario usuario);

    // Método con guion bajo (JPA explícito) - YA LO TENÍAS
    Optional<Sensei> findByUsuario_Username(String username);

    // --- NUEVO: Método CamelCase (El que pide tu Vista) ---
    // Spring Data es inteligente y entiende que buscas el username dentro del usuario
    Optional<Sensei> findByUsuarioUsername(String username);
}