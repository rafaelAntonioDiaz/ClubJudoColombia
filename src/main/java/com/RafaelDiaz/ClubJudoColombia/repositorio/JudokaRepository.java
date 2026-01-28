package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
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
    // 1. SEGURIDAD: Buscar judokas de un Sensei específico
    List<Judoka> findBySensei(Sensei sensei);
    // Solo contar los judokas de su propio dojo (Para el Dashboard del Sensei)
    long countBySenseiId(Long senseiId);

    // Para limpiar tokens/aspirantes caducados en toda la plataforma
    List<Judoka> findByEstadoAndFechaPreRegistroBefore(EstadoJudoka estado, LocalDateTime fechaLimite);
    // 4. NUEVO: Buscar Judokas por Grupo (con fetch del usuario para rendimiento)
    // Asumimos que GrupoEntrenamiento tiene una colección 'judokas'
    @Query("SELECT j FROM GrupoEntrenamiento g JOIN g.judokas j JOIN FETCH j.usuario WHERE g.id = :grupoId")
    List<Judoka> findByGrupoIdWithUsuario(@Param("grupoId") Long grupoId);
    // 5. NUEVO: Buscar por ID de Sensei optimizado para Vistas (ComboBox)
    @Query("SELECT j FROM Judoka j JOIN FETCH j.usuario WHERE j.sensei.id = :senseiId")
    List<Judoka> findBySenseiIdWithUsuario(@Param("senseiId") Long senseiId);
    // 6. ADMISIONES (NUEVO): Buscar por Estado trayendo Usuario y Documentos
    // Usamos DISTINCT porque el LEFT JOIN con documentos podría duplicar filas
    @Query("SELECT DISTINCT j FROM Judoka j JOIN FETCH j.usuario LEFT JOIN FETCH j.documentos WHERE j.estado = :estado")
    List<Judoka> findByEstadoWithDetails(@Param("estado") EstadoJudoka estado);
}