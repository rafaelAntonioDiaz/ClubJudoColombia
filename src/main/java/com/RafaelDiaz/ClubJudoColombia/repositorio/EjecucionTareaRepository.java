package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.modelo.EjercicioPlanificado;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EjecucionTareaRepository extends JpaRepository<EjecucionTarea, Long> {

    List<EjecucionTarea> findAllByOrderByFechaRegistroDesc();

    long countByJudokaAndFechaRegistroBetween(Judoka judoka, LocalDateTime inicio, LocalDateTime fin);

    long countByJudokaAndEjercicioPlanificadoAndFechaRegistroBetween(
            Judoka judoka,
            EjercicioPlanificado ejercicio,
            LocalDateTime inicio,
            LocalDateTime fin);

    List<EjecucionTarea> findTop10ByJudokaOrderByFechaRegistroDesc(Judoka judoka);

    @Query("SELECT e FROM EjecucionTarea e WHERE e.judoka = :judoka AND e.ejercicioPlanificado = :ejercicio AND e.fechaRegistro BETWEEN :inicio AND :fin")
    Optional<EjecucionTarea> findByJudokaAndEjercicioAndFechaBetween(
            @Param("judoka") Judoka judoka,
            @Param("ejercicio") EjercicioPlanificado ejercicio,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
    boolean existsByJudokaAndEjercicioPlanificadoAndFechaRegistroBetween(
            Judoka judoka, EjercicioPlanificado ejercicio, LocalDateTime inicio, LocalDateTime fin);
}