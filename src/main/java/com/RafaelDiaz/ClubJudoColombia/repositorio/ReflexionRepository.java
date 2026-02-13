package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Reflexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReflexionRepository extends JpaRepository<Reflexion, Long> {
    // Traer las más recientes primero (estilo Blog/Timeline)
    List<Reflexion> findByJudokaOrderByFechaCreacionDesc(Judoka judoka);
}