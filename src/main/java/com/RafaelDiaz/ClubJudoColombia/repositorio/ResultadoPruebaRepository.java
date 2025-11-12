package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.ResultadoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoPruebaRepository extends JpaRepository<ResultadoPrueba, Long> {

    // Busca todos los resultados de un Judoka para una Prueba Estandar específica
    List<ResultadoPrueba> findByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroAsc(
            Judoka judoka,
            PruebaEstandar prueba
    );
}