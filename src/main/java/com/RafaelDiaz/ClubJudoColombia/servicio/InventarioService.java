package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ArticuloInventario;
import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ArticuloInventarioRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConceptoFinancieroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventarioService {

    private final ArticuloInventarioRepository articuloRepo;
    private final FinanzasService finanzasService; // <--- Integración Clave
    private final ConceptoFinancieroRepository conceptoRepo;

    public InventarioService(ArticuloInventarioRepository articuloRepo,
                             FinanzasService finanzasService,
                             ConceptoFinancieroRepository conceptoRepo) {
        this.articuloRepo = articuloRepo;
        this.finanzasService = finanzasService;
        this.conceptoRepo = conceptoRepo;
    }

    public List<ArticuloInventario> obtenerTodo() {
        return articuloRepo.findAll();
    }

    public ArticuloInventario guardar(ArticuloInventario articulo) {
        return articuloRepo.save(articulo);
    }

    @Transactional
    public void registrarVenta(ArticuloInventario articulo, int cantidad, MetodoPago metodoPago) {
        if (articulo.getCantidadStock() < cantidad) {
            throw new RuntimeException("No hay suficiente stock. Quedan: " + articulo.getCantidadStock());
        }

        // 1. Descontar Stock
        articulo.setCantidadStock(articulo.getCantidadStock() - cantidad);
        articuloRepo.save(articulo);

        // 2. Buscar o Crear Concepto Financiero
        ConceptoFinanciero conceptoVenta = conceptoRepo.findByTipo(TipoTransaccion.INGRESO).stream()
                .filter(c -> c.getNombre().equalsIgnoreCase("Venta Tienda"))
                .findFirst()
                .orElseGet(() -> {
                    // Si no existe, lo creamos usando el servicio de Finanzas (o el repo directamente)
                    return conceptoRepo.save(new ConceptoFinanciero("Venta Tienda", TipoTransaccion.INGRESO, BigDecimal.ZERO));
                });

        // 3. Registrar Ingreso en Caja (USANDO EL NUEVO MÉTODO DE 7 ARGUMENTOS)
        finanzasService.registrarMovimiento(
                articulo.getPrecioVenta().multiply(new BigDecimal(cantidad)), // 1. Monto
                TipoTransaccion.INGRESO,                                      // 2. Tipo
                metodoPago,                                                   // 3. Método
                String.valueOf(conceptoVenta),                                                // 4. Concepto
                null,                                                         // 5. Judoka (Null porque es venta anónima de tienda)
                "Venta de " + cantidad + "x " + articulo.getNombre(),         // 6. Observación
                null                                                          // 7. URL Soporte (Null por ahora)
        );
    }

    @Transactional
    public void agregarStock(ArticuloInventario articulo, int cantidadNueva) {
        articulo.setCantidadStock(articulo.getCantidadStock() + cantidadNueva);
        articuloRepo.save(articulo);
    }
}