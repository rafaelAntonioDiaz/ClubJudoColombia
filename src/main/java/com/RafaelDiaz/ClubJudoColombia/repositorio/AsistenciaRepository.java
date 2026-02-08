package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByJudokaAndSesion(Judoka judoka, SesionProgramada sesion);

    boolean existsByJudokaIdAndSesionId(Long judokaId, Long sesionId);

    /**
     * Busca la asistencia de una sesión cargando los datos del Judoka y su Acudiente.
     * ARMONIZADO: Se cambia 'j.usuario' por 'j.acudiente' y el método a 'WithAcudiente'.
     */
    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.judoka j " +
            "JOIN FETCH j.acudiente " + // <--- CAMBIO CLAVE: Referencia al nuevo campo
            "WHERE a.sesion.id = :sesionId")
    List<Asistencia> findBySesionIdWithAcudiente(@Param("sesionId") Long sesionId);

    @Query(value = "SELECT " +
            "COUNT(CASE WHEN a.presente = true THEN 1 END) as asistencias, " +
            "COUNT(*) as totales " +
            "FROM asistencias a " +
            "JOIN sesiones_programadas sp ON a.id_sesion = sp.id_sesion " +
            "WHERE sp.id_sensei = :id_sensei " +
            "AND a.fecha_hora_marcacion >= :inicio", nativeQuery = true)
    List<Object[]> countAsistenciasUltimos30Dias(
            @Param("id_sensei") Long senseiId, @Param("inicio") LocalDateTime inicio);

    long countByJudoka(Judoka judoka);

    /**
     * Listado completo de asistencia con detalles del acudiente.
     * ARMONIZADO: Cambio de j.usuario a j.acudiente.
     */
    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.judoka j " +
            "JOIN FETCH j.acudiente " +
            "ORDER BY a.fechaHoraMarcacion DESC")
    List<Asistencia> findAllWithDetails();
}