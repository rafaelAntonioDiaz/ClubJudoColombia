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
    // 1. Buscamos por el nombre del atributo actual
    List<Judoka> findByAcudiente(Usuario acudiente);

    // 2. Armonizamos nombre del método y Query (Cambiamos Usuario -> Acudiente)
    @Query("SELECT j FROM GrupoEntrenamiento g JOIN g.judokas j JOIN FETCH j.acudiente WHERE g.id = :grupoId")
    List<Judoka> findByGrupoIdWithAcudiente(@Param("grupoId") Long grupoId);

    // 3. Ajustamos el fetch para el Sensei
    @Query("SELECT j FROM Judoka j JOIN FETCH j.acudiente WHERE j.sensei.id = :senseiId")
    List<Judoka> findBySenseiIdWithAcudiente(@Param("senseiId") Long senseiId);

    // 4. Ajustamos la vista de admisiones
    @Query("SELECT DISTINCT j FROM Judoka j JOIN FETCH j.acudiente LEFT JOIN FETCH j.documentos WHERE j.estado = :estado")
    List<Judoka> findByEstadoWithDetails(@Param("estado") EstadoJudoka estado);
    // Mantenemos compatibilidad con nombres genéricos si es necesario
    List<Judoka> findByEstado(EstadoJudoka estadoJudoka);
    Optional<Judoka> findByTokenAccesoDirecto(String token);
    // 1. SEGURIDAD: Buscar judokas de un Sensei específico
    List<Judoka> findBySensei(Sensei sensei);
    // Solo contar los judokas de su propio dojo (Para el Dashboard del Sensei)
    long countBySenseiId(Long senseiId);
    // Para limpiar tokens/aspirantes caducados en toda la plataforma
    List<Judoka> findByEstadoAndFechaGeneracionTokenBefore(EstadoJudoka estado, LocalDateTime fechaLimite);

}