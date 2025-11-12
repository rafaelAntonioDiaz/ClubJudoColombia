package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Mecenas;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MecenasRepository extends JpaRepository<Mecenas, Long> {

    // Método para buscar un perfil de Mecenas usando su cuenta de Usuario
    Optional<Mecenas> findByUsuario(Usuario usuario);
}