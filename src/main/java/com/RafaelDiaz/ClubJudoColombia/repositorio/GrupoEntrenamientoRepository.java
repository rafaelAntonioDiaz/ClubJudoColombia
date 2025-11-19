package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

// GrupoEntrenamientoRepository.java
@Repository
public interface GrupoEntrenamientoRepository extends JpaRepository<GrupoEntrenamiento, Long> {

    /**
     * Busca grupos con JOIN FETCH a judokas y planes (evita LazyInitializationException).
     */
    @EntityGraph(attributePaths = {"judokas", "planesAsignados"})
    Page<GrupoEntrenamiento> findAll(Pageable pageable);

    /**
     * Busca grupos por nombre con JOIN FETCH a judokas y planes.
     */
    @EntityGraph(attributePaths = {"judokas", "planesAsignados"})
    Page<GrupoEntrenamiento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByNombreContainingIgnoreCase(String nombre);

    List<GrupoEntrenamiento> findAllByJudokasContains(Judoka judoka);

    Optional<GrupoEntrenamiento> findByNombre(String nombre);
}