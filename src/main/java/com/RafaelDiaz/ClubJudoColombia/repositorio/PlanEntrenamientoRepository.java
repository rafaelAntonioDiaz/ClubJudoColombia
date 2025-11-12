package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento; // --- IMPORTAR ---
import com.RafaelDiaz.ClubJudoColombia.modelo.PlanEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanEntrenamientoRepository extends JpaRepository<PlanEntrenamiento, Long> {

    // (Este método sigue siendo válido)
    List<PlanEntrenamiento> findBySenseiOrderByFechaAsignacionDesc(Sensei sensei);

    // --- MÉTODO ACTUALIZADO ---
    // (Antes era findByJudoka...)
    // Ahora, busca todos los planes que están asignados a un grupo específico.
    List<PlanEntrenamiento> findAllByGruposAsignadosContains(GrupoEntrenamiento grupo);
}