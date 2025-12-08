package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipacionCompetenciaRepository extends JpaRepository<ParticipacionCompetencia, Long> {
    // Ordenado por fecha descendente (lo más reciente arriba)
    List<ParticipacionCompetencia> findByJudokaOrderByFechaDesc(Judoka judoka);
}