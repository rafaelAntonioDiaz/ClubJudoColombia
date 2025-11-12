package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SesionProgramadaRepository extends JpaRepository<SesionProgramada, Long> {

    // Método para buscar sesiones en un rango de fechas (útil para calendarios)
    List<SesionProgramada> findByFechaBetween(LocalDate inicio, LocalDate fin);
}