package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionProgramadaRepository extends JpaRepository<SesionProgramada, Long> {

    // --- 1. MÉTODOS SOLICITADOS ---

    // Para listar todo ordenado (Admin/Sensei general)
    List<SesionProgramada> findAllByOrderByFechaHoraInicioDesc();

    // Para validar solapamientos (Evitar clases dobles en el mismo grupo)
    // Busca sesiones de un grupo que EMPIECEN dentro de un rango de tiempo.
    @Query("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio BETWEEN :inicio AND :fin")
    List<SesionProgramada> findByGrupoIdAndFechaHoraInicioBetween(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // --- 2. FILTROS BÁSICOS ---

    List<SesionProgramada> findBySenseiOrderByFechaHoraInicioDesc(Sensei sensei);

    // --- 3. CONSULTAS OPTIMIZADAS (Dashboard Judoka) ---

    @EntityGraph(value = "SesionProgramada.completo", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT s FROM SesionProgramada s WHERE s.grupo.id = :grupoId AND s.fechaHoraInicio > :ahora ORDER BY s.fechaHoraInicio ASC")
    List<SesionProgramada> findActivasByGrupo(@Param("grupoId") Long grupoId, @Param("ahora") LocalDateTime ahora);

    // Búsqueda eficiente para el calendario mensual
    @Query("SELECT s FROM SesionProgramada s " +
            "JOIN FETCH s.sensei sen " +
            "JOIN FETCH sen.usuario u " +
            "WHERE s.grupo IN :grupos " +
            "AND s.fechaHoraInicio BETWEEN :inicio AND :fin " +
            "ORDER BY s.fechaHoraInicio ASC")
    List<SesionProgramada> findByGruposAndFechaBetween(
            @Param("grupos") Collection<GrupoEntrenamiento> grupos,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // --- 4. CHECK-IN GPS & KPIs ---

    @Query("SELECT s FROM SesionProgramada s " +
            "WHERE s.grupo = :grupo " +
            "AND s.fechaHoraInicio <= :margenInicio " +
            "AND s.fechaHoraFin >= :ahora")
    Optional<SesionProgramada> findSesionActiva(
            @Param("grupo") GrupoEntrenamiento grupo,
            @Param("margenInicio") LocalDateTime margenInicio,
            @Param("ahora") LocalDateTime ahora);

    @Query("SELECT COUNT(s) FROM SesionProgramada s " +
            "WHERE s.tipoSesion = :tipoSesion " +
            "AND s.sensei.id = :senseiId " +
            "AND s.fechaHoraInicio BETWEEN :inicio AND :fin")
    long contarPruebasDelSenseiHoy(
            @Param("tipoSesion") TipoSesion tipoSesion,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("senseiId") Long senseiId);

    @Override
    @EntityGraph("SesionProgramada.completo")
    Optional<SesionProgramada> findById(Long id);
    // CORRECCIÓN LAZY: Traer Sesión con sus relaciones clave (Grupo, Sensei)
    @Query("SELECT s FROM SesionProgramada s " +
            "JOIN FETCH s.grupo " +
            "LEFT JOIN FETCH s.sensei " +
            "ORDER BY s.fechaHoraInicio DESC")
    List<SesionProgramada> findAllWithDetails();
}