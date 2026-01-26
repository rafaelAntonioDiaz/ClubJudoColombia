package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JudokaRepository extends JpaRepository<Judoka, Long> {

    Optional<Judoka> findByUsuario(Usuario usuario);

    // --- 1. MÉTODOS PARA EL SENSEI (Aislamiento de Datos) ---

    // REEMPLAZO DE findAllWithUsuario(): El Sensei SOLO ve sus propios Judokas
    @Query("SELECT DISTINCT j FROM Judoka j JOIN FETCH j.usuario WHERE j.sensei.id = :senseiId")
    List<Judoka> findBySenseiIdWithUsuario(@Param("senseiId") Long senseiId);

    // Solo contar los judokas de su propio dojo (Para el Dashboard del Sensei)
    long countBySenseiId(Long senseiId);


    // --- 2. MÉTODOS PARA EL MASTER (Vista Global) ---

    // Para ValidacionIngresoView (El Master ve a todos los pendientes de la plataforma)
    // CAMBIO: Añadido "JOIN FETCH j.sensei" para que el Master sepa de quién es el judoka.
    @Query("SELECT DISTINCT j FROM Judoka j " +
            "JOIN FETCH j.usuario " +           // Datos del aspirante
            "JOIN FETCH j.sensei " +            // <--- VITAL: Saber qué Sensei lo invitó
            "LEFT JOIN FETCH j.documentos " +   // Documentos para revisión
            "WHERE j.estado = :estado")
    List<Judoka> findByEstadoWithDetails(@Param("estado") EstadoJudoka estado);


    // --- 3. MÉTODOS DE MANTENIMIENTO (Cleanup Service) ---

    // Para limpiar tokens/aspirantes caducados en toda la plataforma
    List<Judoka> findByEstadoAndFechaPreRegistroBefore(EstadoJudoka estado, LocalDateTime fechaLimite);


    // --- 4. MÉTODOS ESPECÍFICOS (Grupos) ---

    // Este se mantiene igual, asumiendo que el Sensei solo puede consultar sus propios grupos.
    @Query("SELECT DISTINCT j FROM GrupoEntrenamiento g JOIN g.judokas j JOIN FETCH j.usuario WHERE g.id = :grupoId")
    List<Judoka> findByGrupoIdWithUsuario(@Param("grupoId") Long grupoId);
}