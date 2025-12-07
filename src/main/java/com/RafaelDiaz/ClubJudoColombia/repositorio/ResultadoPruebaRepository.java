package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.ResultadoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoPruebaRepository extends JpaRepository<ResultadoPrueba, Long> {

    // Historial completo de una prueba estándar
    List<ResultadoPrueba> findByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroAsc(
            Judoka judoka,
            PruebaEstandar prueba
    );

    // ÚLTIMA evaluación física del judoka (para calcular próxima)
    Optional<ResultadoPrueba> findTopByJudokaOrderByFechaRegistroDesc(Judoka judoka);

    // Para KPIs: contar evaluaciones en un rango de fechas (opcional, si quieres más precisión)
    @Query("SELECT COUNT(r) FROM ResultadoPrueba r WHERE r.judoka = :judoka AND r.fechaRegistro BETWEEN :inicio AND :fin")
    long countByJudokaAndFechaRegistroBetween(Judoka judoka, LocalDateTime inicio, LocalDateTime fin);

    Optional<ResultadoPrueba> findTopByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroDesc(Judoka judoka, PruebaEstandar prueba);
}