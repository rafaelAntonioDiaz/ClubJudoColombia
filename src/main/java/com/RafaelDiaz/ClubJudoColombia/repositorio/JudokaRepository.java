package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JudokaRepository extends JpaRepository<Judoka, Long> {

    // Método para buscar un perfil de Judoka usando su cuenta de Usuario
    Optional<Judoka> findByUsuario(Usuario usuario);
}