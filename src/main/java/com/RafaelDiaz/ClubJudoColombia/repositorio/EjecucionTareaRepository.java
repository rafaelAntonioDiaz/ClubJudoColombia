package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjecucionTareaRepository extends JpaRepository<EjecucionTarea, Long> {
    List<EjecucionTarea> findAllByOrderByFechaRegistroDesc();
}