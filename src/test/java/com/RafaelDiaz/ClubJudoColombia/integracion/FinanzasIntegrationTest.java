package com.RafaelDiaz.ClubJudoColombia.integracion;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MovimientoCajaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanzasIntegrationTest {

    @Autowired
    private FinanzasService finanzasService;

    @Autowired
    private ConceptoFinancieroRepository conceptoRepo;

    @Autowired
    private MovimientoCajaRepository movimientoRepo;

    @Test
    @WithMockUser(username = "sensei_test")
    @DisplayName("INTEGRACIÓN: El balance debe filtrar correctamente por fecha y tipo")
    void testCalculosFinancierosIntegrales() {
        // 1. PREPARAR: Escenario con ruidos
        // Movimiento del mes PASADO (No debe sumarse)
        registrarMovimientoManual(new BigDecimal("100000"), TipoTransaccion.INGRESO, LocalDateTime.now().minusMonths(1));

        // Movimiento tipo EGRESO de este mes
        registrarMovimientoManual(new BigDecimal("30000"), TipoTransaccion.EGRESO, LocalDateTime.now());

        // 2. ACTUAR: Registrar movimientos válidos usando el SERVICIO
        finanzasService.registrarMovimiento(
                new BigDecimal("50000"),
                TipoTransaccion.INGRESO,
                MetodoPago.EFECTIVO,
                "MENSUALIDAD",
                null, "Pago 1", null
        );

        finanzasService.registrarMovimiento(
                new BigDecimal("20000"),
                TipoTransaccion.INGRESO,
                MetodoPago.NEQUI,
                null, // Probará la creación automática de "OTROS"
                null, "Pago 2", null
        );

        // 3. VERIFICAR
        BigDecimal totalIngresos = finanzasService.calcularTotalIngresosMes();
        BigDecimal balance = finanzasService.calcularBalanceMes();

        // Ingresos: 50.000 + 20.000 = 70.000
        assertEquals(0, new BigDecimal("70000").compareTo(totalIngresos),
                "Debe sumar solo ingresos del mes actual");

        // Balance: 70.000 (Ingresos) - 30.000 (Egreso hoy) = 40.000
        assertEquals(0, new BigDecimal("40000").compareTo(balance),
                "El balance debe restar los egresos del mes actual");
    }

    @Test
    @DisplayName("INTEGRACIÓN: Debe evitar duplicidad de conceptos automáticos")
    void testConceptosAutomaticosNoDuplicados() {
        // Registrar dos movimientos con concepto null (usando el servicio)
        finanzasService.registrarMovimiento(new BigDecimal("10"), TipoTransaccion.INGRESO, MetodoPago.EFECTIVO, null, null, "1", null);
        finanzasService.registrarMovimiento(new BigDecimal("20"), TipoTransaccion.INGRESO, MetodoPago.EFECTIVO, null, null, "2", null);

        // Verificar que en la BD solo exista UN concepto llamado "OTROS"
        long count = conceptoRepo.findAll().stream()
                .filter(c -> c.getNombre().equals("OTROS"))
                .count();

        assertEquals(1, count, "No deben crearse múltiples conceptos 'OTROS' en la base de datos");
    }

    /**
     * Helper corregido para evitar DataIntegrityViolationException.
     * Busca el concepto por nombre antes de intentar guardarlo.
     */
    private void registrarMovimientoManual(BigDecimal monto, TipoTransaccion tipo, LocalDateTime fecha) {
        // CAMBIO CLAVE: Usar findByNombre para no duplicar el concepto "TEST"
        ConceptoFinanciero cf = conceptoRepo.findByNombre("TEST")
                .orElseGet(() -> conceptoRepo.save(new ConceptoFinanciero("TEST", tipo, BigDecimal.ZERO)));

        MovimientoCaja mov = new MovimientoCaja();
        mov.setMonto(monto);
        mov.setTipo(tipo);
        mov.setFecha(fecha);
        mov.setMetodoPago(MetodoPago.EFECTIVO);
        mov.setConcepto(cf);
        mov.setRegistradoPor("sistema_test");

        movimientoRepo.save(mov);
    }
}