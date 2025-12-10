package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MovimientoCajaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito
class FinanzasServiceTest {

    @Mock
    private MovimientoCajaRepository movimientoRepo;

    @Mock
    private ConceptoFinancieroRepository conceptoRepo;

    @Mock
    private SecurityService securityService; // Para simular el usuario logueado

    @InjectMocks
    private FinanzasService finanzasService; // El servicio que vamos a probar

    private ConceptoFinanciero conceptoMensualidad;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba comunes
        conceptoMensualidad = new ConceptoFinanciero("Mensualidad", TipoTransaccion.INGRESO, new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Debe registrar un ingreso correctamente y asignar el usuario auditor")
    void registrarMovimiento_Ingreso_Exitoso() {
        // 1. PREPARAR (Arrange)
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setMonto(new BigDecimal("50000"));
        movimiento.setTipo(TipoTransaccion.INGRESO);
        movimiento.setConcepto(conceptoMensualidad);

        Usuario senseiMock = new Usuario();
        senseiMock.setUsername("sensei_rafael");

        // Simulamos que el repositorio devuelve el mismo objeto al guardar
        when(movimientoRepo.save(any(MovimientoCaja.class))).thenReturn(movimiento);
        // Simulamos que hay un usuario logueado
        when(securityService.getAuthenticatedUsuario()).thenReturn(Optional.of(senseiMock));

        // 2. ACTUAR (Act)
        MovimientoCaja resultado = finanzasService.registrarMovimiento(movimiento);

        // 3. VERIFICAR (Assert)
        assertNotNull(resultado);
        assertEquals(new BigDecimal("50000"), resultado.getMonto());
        assertEquals("sensei_rafael", resultado.getRegistradoPor(), "Debe auditar quién registró");

        // Verificar que se llamó al repositorio 1 vez
        verify(movimientoRepo, times(1)).save(movimiento);
    }

    @Test
    @DisplayName("Debe calcular el balance correctamente (Ingresos - Egresos)")
    void calcularBalanceMes_CalculoCorrecto() {
        // 1. PREPARAR
        // Simulamos que el Repo devuelve totales
        when(movimientoRepo.sumarTotalPorTipoYFecha(eq(TipoTransaccion.INGRESO), any(), any()))
                .thenReturn(new BigDecimal("100000")); // Entraron 100k

        when(movimientoRepo.sumarTotalPorTipoYFecha(eq(TipoTransaccion.EGRESO), any(), any()))
                .thenReturn(new BigDecimal("30000")); // Salieron 30k

        // 2. ACTUAR
        BigDecimal balance = finanzasService.calcularBalanceMes();

        // 3. VERIFICAR
        // 100.000 - 30.000 = 70.000
        assertEquals(new BigDecimal("70000"), balance);
    }

    @Test
    @DisplayName("Debe manejar balance cero si no hay movimientos")
    void calcularBalanceMes_SinMovimientos() {
        // Simulamos que devuelve NULL (como pasa en SQL real si no hay filas)
        when(movimientoRepo.sumarTotalPorTipoYFecha(any(), any(), any())).thenReturn(null);

        BigDecimal balance = finanzasService.calcularBalanceMes();

        assertEquals(BigDecimal.ZERO, balance, "El balance debe ser 0, no null");
    }
}