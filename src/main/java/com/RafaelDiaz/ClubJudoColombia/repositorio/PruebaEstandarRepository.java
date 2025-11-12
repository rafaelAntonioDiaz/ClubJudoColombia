package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PruebaEstandarRepository extends JpaRepository<PruebaEstandar, Long> {

    Optional<PruebaEstandar> findByNombreKey(String nombreKey);
}