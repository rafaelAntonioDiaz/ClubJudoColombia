package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface GrupoEntrenamientoRepository extends JpaRepository<GrupoEntrenamiento, Long> {
    // --- AÑADIR ESTE MÉTODO ---
    // Spring Data JPA creará la consulta para encontrar todos los grupos
    // que contienen a un judoka específico en su lista 'judokas'.
    List<GrupoEntrenamiento> findAllByJudokasContains(Judoka judoka);
    // --- MÉTODO AÑADIDO (para el DataInitializer) ---
    // Para buscar un grupo por su nombre único
    Optional<GrupoEntrenamiento> findByNombre(String nombre);
}