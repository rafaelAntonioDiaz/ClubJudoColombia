package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    // (Podemos añadir métodos de búsqueda personalizados aquí más adelante)
}