package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MovimientoCajaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanzasServiceTest {

    @Mock
    private MovimientoCajaRepository movimientoRepo;

    @Mock
    private ConceptoFinancieroRepository conceptoRepo;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private FinanzasService finanzasService;

    private ConceptoFinanciero conceptoMensualidad;

    @BeforeEach
    void setUp() {
        conceptoMensualidad = new ConceptoFinanciero("Mensualidad Enero", TipoTransaccion.INGRESO, new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Debe registrar un movimiento correctamente con los 7 argumentos")
    void registrarMovimiento_FlujoExitoso() {
        // 1. PREPARAR (Mocks)

        // Simulamos un usuario logueado para la auditoría
        UserDetails userDetails = new User("sensei_rafael", "password", Collections.emptyList());
        when(securityService.getAuthenticatedUserDetails()).thenReturn(Optional.of(userDetails));

        // Cuando guarde, devolvemos un objeto cualquiera (no afecta la aserción del captor)
        when(movimientoRepo.save(any(MovimientoCaja.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. ACTUAR (Llamamos al NUEVO método de 7 argumentos)
        BigDecimal monto = new BigDecimal("50000");
        finanzasService.registrarMovimiento(
                monto,
                TipoTransaccion.INGRESO,
                MetodoPago.EFECTIVO,
                String.valueOf(conceptoMensualidad),
                null, // Judoka null
                "Pago mensualidad test",
                "http://url-fake.com"
        );

        // 3. VERIFICAR (Usamos Captor para ver qué construyó el servicio por dentro)
        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoRepo, times(1)).save(captor.capture());

        MovimientoCaja movimientoGuardado = captor.getValue();

        // Validaciones
        assertEquals(monto, movimientoGuardado.getMonto());
        assertEquals(TipoTransaccion.INGRESO, movimientoGuardado.getTipo());
        assertEquals(MetodoPago.EFECTIVO, movimientoGuardado.getMetodoPago());
        assertEquals("sensei_rafael", movimientoGuardado.getRegistradoPor(), "La auditoría debe capturar el usuario logueado");
        assertEquals("Pago mensualidad test", movimientoGuardado.getObservacion());
    }

    @Test
    @DisplayName("Debe autogenerar el concepto si se pasa null")
    void registrarMovimiento_ConceptoNull_GeneraAutomatico() {
        // 1. PREPARAR
        // Simulamos usuario
        UserDetails userDetails = new User("admin", "123", Collections.emptyList());
        when(securityService.getAuthenticatedUserDetails()).thenReturn(Optional.of(userDetails));

        // Simulamos que NO existe el concepto "OTROS INGRESO", así que el servicio debe crearlo
        // Nota: El servicio busca "OTROS INGRESO" si el concepto es null
        when(conceptoRepo.findByNombre(anyString())).thenReturn(Optional.empty());

        // Simulamos el guardado del nuevo concepto
        when(conceptoRepo.save(any(ConceptoFinanciero.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. ACTUAR
        finanzasService.registrarMovimiento(
                new BigDecimal("20000"),
                TipoTransaccion.INGRESO,
                MetodoPago.NEQUI,
                null, // <--- PROBAMOS EL NULL AQUÍ
                null,
                "Venta sin concepto",
                null
        );

        // 3. VERIFICAR
        ArgumentCaptor<MovimientoCaja> captor = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoRepo).save(captor.capture());

        MovimientoCaja mov = captor.getValue();
        assertNotNull(mov.getConcepto());
        assertTrue(mov.getConcepto().getNombre().contains("OTROS"), "Debe asignar un concepto por defecto tipo OTROS");
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
        // Nota: Asegúrate que FinanzasService tenga este método o uno similar
        // Si lo borraste en el refactor, este test fallará.
        // Asumo que mantuviste la lógica de reportes.
        BigDecimal balance = finanzasService.calcularBalanceMes();

        // 3. VERIFICAR
        // 100.000 - 30.000 = 70.000
        assertEquals(new BigDecimal("70000"), balance);
    }
}