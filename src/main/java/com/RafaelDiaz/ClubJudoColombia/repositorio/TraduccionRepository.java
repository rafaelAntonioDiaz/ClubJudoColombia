package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraduccionRepository extends JpaRepository<Traduccion, Long> {

    // Método para buscar un texto específico
    Optional<Traduccion> findByClaveAndIdioma(String clave, String idioma);
}