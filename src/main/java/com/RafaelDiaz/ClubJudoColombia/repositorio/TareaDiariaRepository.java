package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TareaDiariaRepository extends JpaRepository<TareaDiaria, Long> {

    TareaDiaria findByNombreContainingIgnoreCase(String nombre);
    // Necesario para el DataInitializer
    Optional<TareaDiaria> findByNombreIgnoreCase(String nombre);
}