package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.vaadin.hilla.mappedtypes.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TareaDiariaRepository extends JpaRepository<TareaDiaria, Long> {

    List<TareaDiaria> findByCategoriaIn(List<CategoriaEjercicio> categorias);
    long countByCategoriaIn(List<CategoriaEjercicio> categorias);

    TareaDiaria findByNombreContainingIgnoreCase(String nombre);
    // Necesario para el DataInitializer
    Optional<TareaDiaria> findByNombreIgnoreCase(String nombre);
}