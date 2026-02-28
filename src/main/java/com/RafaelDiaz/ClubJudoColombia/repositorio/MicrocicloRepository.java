package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento; // --- IMPORTAR ---
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Microciclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MicrocicloRepository extends JpaRepository<Microciclo, Long> {

    // Busca todos los planes que están asignados a un grupo específico.
    List<Microciclo> findAllByGruposAsignadosContains(GrupoEntrenamiento grupo);
    //long countByJudokasContainsAndEstadoIn(Judoka judoka, List<EstadoPlan> estados);
    @Query("""
        SELECT COUNT(DISTINCT p) FROM Microciclo p
        JOIN p.gruposAsignados g
        JOIN g.judokas j
        WHERE j = :judoka
          AND p.estado IN :estados
        """)
    long contarPlanesActivosParaJudoka(
            @Param("judoka") Judoka judoka,
            @Param("estados") List<EstadoMicrociclo> estados
    );
    List<Microciclo> findBySenseiOrderByFechaInicioDesc(Sensei sensei);
}