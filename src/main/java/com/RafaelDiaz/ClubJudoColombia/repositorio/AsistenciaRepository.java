package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionEjecutada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByJudokaAndSesion(Judoka judoka, SesionEjecutada sesion);

    boolean existsByJudokaIdAndSesionId(Long judokaId, Long sesionId);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.judoka j JOIN FETCH j.acudiente WHERE a.sesion.id = :sesionId")
    List<Asistencia> findBySesionIdWithAcudiente(@Param("sesionId") Long sesionId);

    long countByJudoka(Judoka judoka);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.judoka j JOIN FETCH j.acudiente JOIN FETCH a.sesion s ORDER BY s.fechaHoraEjecucion DESC")
    List<Asistencia> findAllWithDetails();

    // ====================================================================
    // SOLUCIÓN DE FONDO: JPQL PURO EN VEZ DE SQL NATIVO
    // ====================================================================

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.sesion.sensei.id = :senseiId AND a.sesion.fechaHoraEjecucion >= :inicio")
    long contarTotalesUltimos30Dias(@Param("senseiId") Long senseiId, @Param("inicio") LocalDateTime inicio);

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.sesion.sensei.id = :senseiId AND a.sesion.fechaHoraEjecucion >= :inicio AND a.estado = :estado")
    long contarPorEstadoUltimos30Dias(@Param("senseiId") Long senseiId, @Param("inicio") LocalDateTime inicio, @Param("estado") EstadoAsistencia estado);

    // ====================================================================
    // EL ESPEJO DEL ALUMNO: HISTORIAL
    // ====================================================================

    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.sesion s " +
            "JOIN FETCH s.microciclo m " +
            "JOIN FETCH s.sensei sen " +
            "JOIN FETCH sen.usuario " + // <-- CLAVE: Traemos al Usuario para sacar su nombre sin fallos
            "WHERE a.judoka.id = :judokaId " +
            "ORDER BY s.fechaHoraEjecucion DESC")
    List<Asistencia> findHistorialByJudokaId(@Param("judokaId") Long judokaId);
    long countByJudokaAndEstado(Judoka judoka, EstadoAsistencia estado);
}