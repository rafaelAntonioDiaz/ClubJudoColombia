package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
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
    @Query("SELECT DISTINCT s FROM Sensei s LEFT JOIN FETCH s.usuario")
    List<Sensei> findAllWithUsuario();
    List<Sensei> findBySaldoWalletGreaterThan(BigDecimal saldo);
    @Query("SELECT s FROM Sensei s LEFT JOIN FETCH s.usuario WHERE s.id = :id")
    Optional<Sensei> findByIdWithUsuario(@Param("id") Long id);

}