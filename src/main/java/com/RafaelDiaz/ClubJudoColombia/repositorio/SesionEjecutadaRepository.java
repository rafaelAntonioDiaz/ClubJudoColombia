package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.SesionEjecutada;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SesionEjecutadaRepository extends JpaRepository<SesionEjecutada, Long> {


    /**
     * HISTORIAL TÁCTICO: Trae las últimas sesiones de un Sensei.
     * Usamos DISTINCT y múltiples LEFT JOIN FETCH para traer la sesión,
     * el grupo, el plan, las asistencias y a los alumnos en un solo viaje
     * a la base de datos.
     */
    @Query("SELECT DISTINCT s FROM SesionEjecutada s " +
            "JOIN FETCH s.microciclo " +
            "JOIN FETCH s.grupo " +
            "LEFT JOIN FETCH s.listaAsistencia a " +
            "LEFT JOIN FETCH a.judoka " +
            "WHERE s.sensei = :sensei " +
            "ORDER BY s.fechaHoraEjecucion DESC")
    List<SesionEjecutada> findBySenseiOrderByFechaHoraEjecucionDesc(@Param("sensei") Sensei sensei);
    /**
     * ANALÍTICA DE ASISTENCIA: Calcula presentes vs total del grupo.
     * Ya está armonizado con el campo 'grupo' (singular) de Judoka.java.
     */
    @Query("SELECT COUNT(a), (SELECT COUNT(j) FROM Judoka j WHERE j.grupo = s.grupo) " +
            "FROM SesionEjecutada s JOIN s.listaAsistencia a " +
            "WHERE s.id = :sesionId AND a.estado = 'PRESENTE' " +
            "GROUP BY s.id")
    Object[] obtenerDatosAsistencia(@Param("sesionId") Long sesionId);
    /**
     * CONTROL DE VOLUMEN (AGUDELO): Suma cuántas sesiones se han ejecutado
     * realmente en un rango de fechas para un grupo.
     * Ayuda a comparar "Lo planeado vs Lo ejecutado".
     */
    long countByGrupoAndFechaHoraEjecucionBetween(GrupoEntrenamiento grupo, LocalDateTime inicio, LocalDateTime fin);

    /**
     * BUSCADOR DE RETROALIMENTACIÓN: Busca palabras clave en las notas de voz
     * dictadas por el Sensei (Ej: "lesión", "agarre", "falta fuerza").
     */
    List<SesionEjecutada> findByNotasRetroalimentacionContainingIgnoreCaseAndSensei(String palabraClave, Sensei sensei);
}