package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Para buscar un producto por su ID de Stripe
    Optional<Producto> findByStripePriceId(String stripePriceId);
}