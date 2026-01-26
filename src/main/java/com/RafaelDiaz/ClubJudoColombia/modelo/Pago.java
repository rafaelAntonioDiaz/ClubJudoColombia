package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago; // <-- Importante
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "monto_cop", nullable = false)
    private Double monto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_pago_exitoso")
    private LocalDateTime fechaPagoExitoso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado;

    // --- MODELO DE PAGOS: EFECTIVO / NEQUI ---
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    /**
     * URL del pantallazo de Nequi guardado en Cloudflare R2.
     * Si es en Efectivo, este campo quedará nulo.
     */
    @Column(name = "url_comprobante", length = 500)
    private String urlComprobante;

    public Pago() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
    }

    // --- Getters y Setters Omitidos por brevedad (Recuerde generarlos en su IDE) ---

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public Double getMonto() {
        return monto;
    }

    public LocalDateTime getFechaPagoExitoso() {
        return fechaPagoExitoso;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getUrlComprobante() {
        return urlComprobante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public void setFechaPagoExitoso(LocalDateTime fechaPagoExitoso) {
        this.fechaPagoExitoso = fechaPagoExitoso;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setUrlComprobante(String urlComprobante) {
        this.urlComprobante = urlComprobante;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}