package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    // Para el historial del mes
    List<MovimientoCaja> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    // Para calcular totales rápidos (Dashboard)
    @Query("SELECT SUM(m.monto) FROM MovimientoCaja m WHERE m.tipo = :tipo AND m.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumarTotalPorTipoYFecha(@Param("tipo") TipoTransaccion tipo,
                                       @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin);

    @Query("SELECT m FROM MovimientoCaja m " +
                "LEFT JOIN FETCH m.judoka j " +
                "LEFT JOIN FETCH j.acudiente a " +
                "LEFT JOIN FETCH m.concepto c " +
                "WHERE m.fecha BETWEEN :inicio AND :fin " +
                "ORDER BY m.fecha DESC")
    List<MovimientoCaja> findByFechaBetweenWithDetails(@Param("inicio") LocalDateTime inicio,
                                                       @Param("fin") LocalDateTime fin);
}