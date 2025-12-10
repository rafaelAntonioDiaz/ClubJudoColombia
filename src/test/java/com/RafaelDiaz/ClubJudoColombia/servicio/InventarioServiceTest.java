package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ArticuloInventario;
import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ArticuloInventarioRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ArticuloInventarioRepository articuloRepo;

    @Mock
    private FinanzasService finanzasService; // Mockeamos Finanzas para no tocar caja real

    @Mock
    private ConceptoFinancieroRepository conceptoRepo;

    @InjectMocks
    private InventarioService inventarioService;

    private ArticuloInventario judogi;

    @BeforeEach
    void setUp() {
        // Datos de prueba: Un Judogi con 5 unidades en stock
        judogi = new ArticuloInventario("Judogi Azul Talla M", 5, new BigDecimal("150000"));
        judogi.setPrecioCosto(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("Debe registrar venta: descontar stock y sumar dinero a caja")
    void registrarVenta_Exito() {
        // 1. PREPARAR (Arrange)
        int cantidadVenta = 2;

        // Simulamos que existe el concepto "Venta Tienda"
        ConceptoFinanciero conceptoVenta = new ConceptoFinanciero("Venta Tienda", TipoTransaccion.INGRESO, null);
        when(conceptoRepo.findByTipo(TipoTransaccion.INGRESO)).thenReturn(List.of(conceptoVenta));

        // 2. ACTUAR (Act)
        inventarioService.registrarVenta(judogi, cantidadVenta, MetodoPago.EFECTIVO);

        // 3. VERIFICAR (Assert)
        // A. Verificar que el stock bajó en memoria (5 - 2 = 3)
        assertEquals(3, judogi.getCantidadStock(), "El stock debe reducirse en 2");

        // B. Verificar que se guardó el artículo actualizado en la BD
        verify(articuloRepo).save(judogi);

        // C. Verificar que se llamó a Finanzas para guardar la plata
        // Usamos 'ArgumentCaptor' si quisiéramos ser muy estrictos, o verify simple:
        verify(finanzasService, times(1)).registrarMovimiento(any(MovimientoCaja.class));
    }

    @Test
    @DisplayName("Debe lanzar error si no hay suficiente stock")
    void registrarVenta_SinStock() {
        // 1. PREPARAR
        int cantidadVentaExcesiva = 10; // Solo hay 5

        // 2. & 3. ACTUAR Y VERIFICAR EXCEPCIÓN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventarioService.registrarVenta(judogi, cantidadVentaExcesiva, MetodoPago.EFECTIVO);
        });

        assertEquals("No hay suficiente stock. Quedan: 5", exception.getMessage());

        // Verificar que NUNCA se guardó nada ni se tocó la caja
        verify(articuloRepo, never()).save(any());
        verify(finanzasService, never()).registrarMovimiento(any());
    }
}