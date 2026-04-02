package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.CuentaCobro;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CuentaCobroRepository extends JpaRepository<CuentaCobro, Long> {

    // Buscar deudas pendientes de un usuario responsable (Padre o Mecenas)
    List<CuentaCobro> findByResponsablePagoAndEstado(Usuario responsable, EstadoPago estado);
    @Query("SELECT c FROM CuentaCobro c " +
            "JOIN FETCH c.judokaBeneficiario " +
            "WHERE c.responsablePago = :responsable AND c.estado = :estado")
    List<CuentaCobro> findByResponsablePagoAndEstadoWithJudoka(@Param("responsable") Usuario responsable, @Param("estado") EstadoPago estado);
    // Para evitar cobrar doble el mismo mes (Validación)
    boolean existsByJudokaBeneficiarioAndConceptoLikeAndFechaGeneracionAfter(
            Judoka judoka, String concepto, LocalDate fechaGeneracion
    );

    // Validar facturas vencidas
    List<CuentaCobro> findByEstadoAndFechaVencimientoBefore(EstadoPago estado, LocalDate fecha);
}
