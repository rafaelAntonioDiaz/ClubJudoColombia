package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalmaresRepository extends JpaRepository<ParticipacionCompetencia, Long> {
    List<ParticipacionCompetencia> findByJudokaOrderByFechaDesc(Judoka judoka);
}