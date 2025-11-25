package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento; // --- IMPORTAR ---
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PlanEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    //long countByJudokasContainsAndEstadoIn(Judoka judoka, List<EstadoPlan> estados);
    @Query("""
        SELECT COUNT(DISTINCT p) FROM PlanEntrenamiento p
        JOIN p.gruposAsignados g
        JOIN g.judokas j
        WHERE j = :judoka
          AND p.estado IN :estados
        """)
    long contarPlanesActivosParaJudoka(
            @Param("judoka") Judoka judoka,
            @Param("estados") List<EstadoPlan> estados
    );
}