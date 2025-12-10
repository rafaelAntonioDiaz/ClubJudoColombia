package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipacionCompetenciaRepository extends JpaRepository<ParticipacionCompetencia, Long> {

    // Para el Perfil del Judoka (Ver sus medallas)
    List<ParticipacionCompetencia> findByJudokaOrderByFechaDesc(Judoka judoka);

    // Para la Gestión del Sensei (Ver todos los torneos)
    // Usamos JOIN FETCH para optimizar la carga del Judoka y Usuario
    @Query("SELECT p FROM ParticipacionCompetencia p JOIN FETCH p.judoka j JOIN FETCH j.usuario ORDER BY p.fecha DESC")
    List<ParticipacionCompetencia> findAllWithDetails();
}