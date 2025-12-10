package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventario_articulos")
public class ArticuloInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private int cantidadStock;

    private int stockMinimoAlerta = 2; // Por defecto avisa si quedan 2

    @Column(nullable = false)
    private BigDecimal precioCosto; // Para saber cuánto invertiste

    @Column(nullable = false)
    private BigDecimal precioVenta; // A cómo lo vendes

    public ArticuloInventario() {}

    public ArticuloInventario(String nombre, int cantidadStock, BigDecimal precioVenta) {
        this.nombre = nombre;
        this.cantidadStock = cantidadStock;
        this.precioVenta = precioVenta;
    }

    // Lógica de Semáforo
    public boolean esStockCritico() {
        return cantidadStock <= stockMinimoAlerta;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCantidadStock() { return cantidadStock; }
    public void setCantidadStock(int cantidadStock) { this.cantidadStock = cantidadStock; }
    public int getStockMinimoAlerta() { return stockMinimoAlerta; }
    public void setStockMinimoAlerta(int stockMinimoAlerta) { this.stockMinimoAlerta = stockMinimoAlerta; }
    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
}