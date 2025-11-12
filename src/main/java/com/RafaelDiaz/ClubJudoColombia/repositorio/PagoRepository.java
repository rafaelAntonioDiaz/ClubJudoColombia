package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Pago;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Para buscar un pago usando el ID de la transacción de Stripe
    Optional<Pago> findByStripePaymentIntentId(String paymentIntentId);

    // Para buscar el historial de pagos de un usuario
    List<Pago> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
}