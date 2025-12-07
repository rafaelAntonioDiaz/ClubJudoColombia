package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JudokaInsigniaRepository extends JpaRepository<JudokaInsignia, Long> {
    // Busca todos los logros ganados por este judoka
    List<JudokaInsignia> findByJudoka(Judoka judoka);

    boolean existsByJudokaAndInsignia_Clave(Judoka judoka, String claveInsignia);
}