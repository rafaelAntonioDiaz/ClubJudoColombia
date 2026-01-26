package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoEntrenamientoRepository extends JpaRepository<GrupoEntrenamiento, Long> {

    // --- MÉTODOS SAAS: Filtros obligatorios por Sensei ---

    // 1. Buscar todos los grupos de UN Sensei
    Page<GrupoEntrenamiento> findBySenseiId(Long senseiId, Pageable pageable);

    // 2. Buscar grupos de UN Sensei filtrados por nombre (para el buscador de la vista)
    Page<GrupoEntrenamiento> findBySenseiIdAndNombreContainingIgnoreCase(Long senseiId, String nombre, Pageable pageable);

    // 3. Contar para la paginación
    long countBySenseiId(Long senseiId);
    long countBySenseiIdAndNombreContainingIgnoreCase(Long senseiId, String nombre);
    // Agrega esto en GrupoEntrenamientoRepository.java
    Optional<GrupoEntrenamiento> findBySenseiAndNombre(Sensei sensei, String nombre);

    List<GrupoEntrenamiento> findAllByJudokasContains(Judoka judoka);
}