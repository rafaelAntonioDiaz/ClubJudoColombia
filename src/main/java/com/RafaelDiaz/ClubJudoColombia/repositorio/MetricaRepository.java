package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricaRepository extends JpaRepository<Metrica, Long> {

    // Spring Data JPA creará automáticamente la consulta para buscar
    // una métrica por su nombre único (que ahora es 'nombreKey').
    //
    // --- MÉTODO ACTUALIZADO ---
    Optional<Metrica> findByNombreKey(String nombreKey);
}