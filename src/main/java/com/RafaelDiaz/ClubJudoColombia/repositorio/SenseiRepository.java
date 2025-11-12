package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SenseiRepository extends JpaRepository<Sensei, Long> {

    // Método para buscar un perfil de Sensei usando su cuenta de Usuario
    Optional<Sensei> findByUsuario(Usuario usuario);
}