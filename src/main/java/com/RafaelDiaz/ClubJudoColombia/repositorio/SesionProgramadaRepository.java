package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para SesionProgramada con FETCH optimizado y queries nativas.
 *
 * @author RafaelDiaz
 * @version 1.2 (Corregida)
 * @since 2025-11-20
 */
@Repository
public interface SesionProgramadaRepository extends JpaRepository<SesionProgramada, Long> {

    @EntityGraph(value = "SesionProgramada.completo", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio > :ahora ORDER BY s.fechaHoraInicio ASC")
    List<SesionProgramada> findActivasByGrupo(@Param("grupoId") Long grupoId, @Param("ahora") LocalDateTime ahora);

    // ✅ MÉTODO CORRECTO PARA PRUEBAS HOY (con filtro por sensei)
    @Query("SELECT COUNT(s) FROM SesionProgramada s " +
            "WHERE s.tipoSesion = :tipoSesion " +
            "AND s.sensei.id = :senseiId " +
            "AND s.fechaHoraInicio BETWEEN :inicio AND :fin")
    long contarPruebasDelSenseiHoy(
            @Param("tipoSesion") TipoSesion tipoSesion,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("senseiId") Long senseiId);

    // ✅ MÉTODO CORRECTO PARA EL DASHBOARD (lista de sesiones del sensei)
    @Query("SELECT s FROM SesionProgramada s " +
            "WHERE s.sensei.id = :senseiId " +
            "AND s.fechaHoraInicio BETWEEN :inicio AND :fin " +
            "ORDER BY s.fechaHoraInicio ASC")
    List<SesionProgramada> findBySenseiIdAndFechaHoraInicioBetween(
            @Param("senseiId") Long senseiId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // ✅ Método para validación de solapamiento
    @Query("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio BETWEEN :inicio AND :fin")
    List<SesionProgramada> findByGrupoIdAndFechaHoraInicioBetween(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Override
    @EntityGraph("SesionProgramada.completo")
    Optional<SesionProgramada> findById(Long id);

    // Búsqueda eficiente para el calendario: Sesiones de una lista de grupos en un rango de fechas
    // CORRECCIÓN: JOIN FETCH para traer Sensei y Usuario de una vez y evitar LazyInitException
    @Query("SELECT s FROM SesionProgramada s " +
            "JOIN FETCH s.sensei sen " +     // Cargar Sensei
            "JOIN FETCH sen.usuario u " +    // Cargar Usuario del Sensei
            "WHERE s.grupo IN :grupos " +
            "AND s.fechaHoraInicio BETWEEN :inicio AND :fin " +
            "ORDER BY s.fechaHoraInicio ASC")
    List<SesionProgramada> findByGruposAndFechaBetween(
            @Param("grupos")
            java.util.Collection
                    <com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento> grupos,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fin") java.time.LocalDateTime fin);
}