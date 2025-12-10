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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest // Levanta el contexto completo de Spring
@ActiveProfiles("test") // Usa application-test.properties (H2 DB)
@Transactional // Revierte cambios en BD después de cada test
class FinanzasIntegrationTest {

    @Autowired
    private FinanzasService finanzasService;

    @Autowired
    private ConceptoFinancieroRepository conceptoRepo;

    @Autowired
    private MovimientoCajaRepository movimientoRepo;

    @Test
    @DisplayName("INTEGRACIÓN: Debe sumar correctamente los ingresos en base de datos")
    void testSumarTotalesEnBaseDeDatos() {
        // 1. PREPARAR DATOS REALES EN BD H2
        ConceptoFinanciero mensualidad = conceptoRepo.save(
                new ConceptoFinanciero("Mensualidad Test", TipoTransaccion.INGRESO, BigDecimal.valueOf(50000))
        );

        // Crear Movimiento 1 (Hoy)
        MovimientoCaja mov1 = new MovimientoCaja();
        mov1.setFecha(LocalDateTime.now());
        mov1.setMonto(new BigDecimal("50000"));
        mov1.setTipo(TipoTransaccion.INGRESO);
        mov1.setMetodoPago(MetodoPago.EFECTIVO);
        mov1.setConcepto(mensualidad);
        movimientoRepo.save(mov1);

        // Crear Movimiento 2 (Hoy)
        MovimientoCaja mov2 = new MovimientoCaja();
        mov2.setFecha(LocalDateTime.now());
        mov2.setMonto(new BigDecimal("20000"));
        mov2.setTipo(TipoTransaccion.INGRESO);
        mov2.setMetodoPago(MetodoPago.TRANSFERENCIA);
        mov2.setConcepto(mensualidad);
        movimientoRepo.save(mov2);

        // 2. EJECUTAR EL SERVICIO
        BigDecimal totalIngresos = finanzasService.calcularTotalIngresosMes();

        // 3. VERIFICAR
        // 50.000 + 20.000 = 70.000
        assertEquals(0, new BigDecimal("70000").compareTo(totalIngresos), "La suma SQL debe ser correcta");
    }
}