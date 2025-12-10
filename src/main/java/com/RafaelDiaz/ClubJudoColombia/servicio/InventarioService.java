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

        // 2. Registrar Ingreso en Caja Automáticamente
        ConceptoFinanciero conceptoVenta = conceptoRepo.findByTipo(TipoTransaccion.INGRESO).stream()
                .filter(c -> c.getNombre().equalsIgnoreCase("Venta Tienda"))
                .findFirst()
                .orElseGet(() -> {
                    // Si no existe el concepto, lo creamos al vuelo
                    return finanzasService.guardarConcepto(new ConceptoFinanciero("Venta Tienda", TipoTransaccion.INGRESO, null));
                });

        MovimientoCaja venta = new MovimientoCaja();
        venta.setFecha(java.time.LocalDateTime.now());
        venta.setTipo(TipoTransaccion.INGRESO);
        venta.setConcepto(conceptoVenta);
        venta.setMonto(articulo.getPrecioVenta().multiply(new BigDecimal(cantidad)));
        venta.setMetodoPago(metodoPago);
        venta.setObservacion("Venta de " + cantidad + "x " + articulo.getNombre());

        finanzasService.registrarMovimiento(venta);
    }

    @Transactional
    public void agregarStock(ArticuloInventario articulo, int cantidadNueva) {
        articulo.setCantidadStock(articulo.getCantidadStock() + cantidadNueva);
        articuloRepo.save(articulo);
    }
}