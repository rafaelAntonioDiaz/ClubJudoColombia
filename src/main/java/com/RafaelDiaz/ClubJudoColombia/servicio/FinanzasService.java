package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MovimientoCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class FinanzasService {

    private final MovimientoCajaRepository movimientoRepo;
    private final ConceptoFinancieroRepository conceptoRepo;
    private final SecurityService securityService;
    private final TraduccionService traduccionService; // <--- INYECCIÓN (Futuro)

    public FinanzasService(MovimientoCajaRepository movimientoRepo,
                           ConceptoFinancieroRepository conceptoRepo,
                           SecurityService securityService,
                           TraduccionService traduccionService) { // <--- ACTUALIZADO
        this.movimientoRepo = movimientoRepo;
        this.conceptoRepo = conceptoRepo;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
    }

    // --- CONCEPTOS ---
    public List<ConceptoFinanciero> obtenerConceptosPorTipo(TipoTransaccion tipo) {
        return conceptoRepo.findByTipo(tipo);
    }

    public ConceptoFinanciero guardarConcepto(ConceptoFinanciero concepto) {
        return conceptoRepo.save(concepto);
    }

    // --- MOVIMIENTOS ---
    @Transactional
    public MovimientoCaja registrarMovimiento(MovimientoCaja movimiento) {
        // Auditoría automática
        securityService.getAuthenticatedUserDetails().ifPresent(u ->
                movimiento.setRegistradoPor(u.getUsername())
        );
        return movimientoRepo.save(movimiento);
    }

    public List<MovimientoCaja> obtenerMovimientosDelMes() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        return movimientoRepo.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    // --- DASHBOARD DE BALANCE ---
    public BigDecimal calcularTotalIngresosMes() {
        return calcularTotal(TipoTransaccion.INGRESO);
    }

    public BigDecimal calcularTotalEgresosMes() {
        return calcularTotal(TipoTransaccion.EGRESO);
    }

    public BigDecimal calcularBalanceMes() {
        BigDecimal ingresos = calcularTotalIngresosMes();
        BigDecimal egresos = calcularTotalEgresosMes();
        return ingresos.subtract(egresos);
    }

    private BigDecimal calcularTotal(TipoTransaccion tipo) {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        BigDecimal total = movimientoRepo.sumarTotalPorTipoYFecha(tipo, inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }
}