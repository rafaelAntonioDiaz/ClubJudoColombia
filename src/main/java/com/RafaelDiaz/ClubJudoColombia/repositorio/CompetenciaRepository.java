package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Competencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {

    // Buscar inscripciones de un judoka (Para la vista "Mis Torneos")
    @Query("SELECT p.competencia FROM ParticipacionCompetencia p WHERE p.judoka = :judoka")
    List<Competencia> findCompetenciasByJudoka(@Param("judoka") Judoka judoka);

    // Buscar participaciones específicas (Para el Sensei)
    @Query("SELECT p FROM ParticipacionCompetencia p WHERE p.competencia.id = :competenciaId")
    List<ParticipacionCompetencia> findParticipacionesByCompetenciaId(@Param("competenciaId") Long competenciaId);
}