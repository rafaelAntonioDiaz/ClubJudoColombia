package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Macrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MacrocicloRepository extends JpaRepository<Macrociclo, Long> {

    // Trae los Macrociclos de un Sensei ordenados del más reciente al más antiguo
    List<Macrociclo> findBySenseiOrderByFechaInicioDesc(Sensei sensei);
}