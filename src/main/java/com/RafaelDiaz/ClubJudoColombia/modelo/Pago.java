package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPago;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que registra una transacción de pago (un intento o éxito).
 */
@Entity
@Table(name = "pagos")
public class Pago implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    /**
     * El Usuario (Judoka, Sensei, o Mecenas) que realiza el pago.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * El Producto por el cual se está pagando.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "monto_cop", nullable = false)
    private Double monto; // Monto pagado (en COP)

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_pago_exitoso") // Nulo si está pendiente o fallido
    private LocalDateTime fechaPagoExitoso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado;

    /**
     * --- CAMPO CLAVE (Integración con Stripe) ---
     * El ID de la transacción o "Payment Intent" de Stripe.
     * Ej. "pi_3P8..."
     * Lo usamos para verificar el estado del pago con Stripe.
     */
    @Column(name = "stripe_payment_intent_id", unique = true, length = 100)
    private String stripePaymentIntentId;

    // --- Constructores ---
    public Pago() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
    }

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaPagoExitoso() { return fechaPagoExitoso; }
    public void setFechaPagoExitoso(LocalDateTime fechaPagoExitoso) { this.fechaPagoExitoso = fechaPagoExitoso; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pago that = (Pago) obj;
        return id != null && id.equals(that.id);
    }
}