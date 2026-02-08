package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraduccionRepository extends JpaRepository<Traduccion, Long> {

    // Método para buscar un texto específico
    Optional<Traduccion> findByClaveAndIdioma(String clave, String idioma);
    // Permite buscar "sabiduria.suntzu%" para obtener todas las de ese autor
    @Query("SELECT t FROM Traduccion t WHERE t.clave LIKE CONCAT(:prefix, '%') AND t.idioma = :idioma")
    List<Traduccion> findByClaveStartingWithAndIdioma(@Param("prefix") String prefix, @Param("idioma") String idioma);
}