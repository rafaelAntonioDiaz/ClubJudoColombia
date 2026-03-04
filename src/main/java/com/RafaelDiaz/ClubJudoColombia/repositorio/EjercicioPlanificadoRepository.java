package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjercicioPlanificado;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EjercicioPlanificadoRepository extends JpaRepository<EjercicioPlanificado, Long> {
    List<EjercicioPlanificado> findByPruebaEstandar(PruebaEstandar prueba);
}