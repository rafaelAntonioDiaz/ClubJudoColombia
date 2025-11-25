package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EjecucionTareaRepository extends JpaRepository<EjecucionTarea, Long> {
    List<EjecucionTarea> findAllByOrderByFechaRegistroDesc();
    long countByJudokaAndFechaRegistroBetween(Judoka judoka, LocalDateTime inicio, LocalDateTime fin);
}