package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Reflexion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReflexionRepository extends JpaRepository<Reflexion, Long> {
    // Traer las más recientes primero (estilo Blog/Timeline)
    List<Reflexion> findByJudokaOrderByFechaCreacionDesc(Judoka judoka);
}