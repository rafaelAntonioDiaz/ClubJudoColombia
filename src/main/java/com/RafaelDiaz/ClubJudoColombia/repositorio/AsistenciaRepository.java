package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    boolean existsByJudokaIdAndSesionId(Long judokaId, Long sesionId);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.judoka j JOIN FETCH j.usuario WHERE a.sesion.id = :sesionId")
    List<Asistencia> findBySesionIdWithDetails(@Param("sesionId") Long sesionId);

    // ✅ CORREGIDO: Para calcular asistencia promedio en el servicio
    @Query(value = "SELECT " +
            "COUNT(CASE WHEN a.presente = true THEN 1 END) as asistencias, " +
            "COUNT(*) as totales " +
            "FROM asistencias a " +
            "JOIN sesiones_programadas sp ON a.id_sesion = sp.id_sesion " +
            "WHERE sp.id_sensei = :id_sensei " +
            "AND a.fecha_hora_marcacion >= :inicio", nativeQuery = true)
    List<Object[]> countAsistenciasUltimos30Dias(@Param("id_sensei") Long senseiId, @Param("inicio") LocalDateTime inicio);
}