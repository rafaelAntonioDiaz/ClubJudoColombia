package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSuscripcion;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad que define un producto o plan de pago.
 * Ej. "Mensualidad Judoka", "Patrocinio Bimensual Mecenas".
 */
@Entity
@Table(name = "productos")
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio_cop", nullable = false)
    private Double precio; // Precio en Pesos Colombianos (COP)

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_subscripcion", nullable = false)
    private TipoSuscripcion tipo;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /**
     * --- CAMPO CLAVE (Integración con Stripe) ---
     * Este es el ID del "Price" (precio) en Stripe.
     * Ej. "price_1P8g..."
     * Cuando creemos un producto en nuestra BD, también lo crearemos
     * en el dashboard de Stripe y copiaremos el ID de Stripe aquí.
     * Es la llave para iniciar un checkout.
     */
    @Column(name = "stripe_price_id", unique = true, length = 100)
    private String stripePriceId;

    // --- Constructores ---
    public Producto() {}

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public TipoSuscripcion getTipo() { return tipo; }
    public void setTipo(TipoSuscripcion tipo) { this.tipo = tipo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getStripePriceId() { return stripePriceId; }
    public void setStripePriceId(String stripePriceId) { this.stripePriceId = stripePriceId; }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto that = (Producto) obj;
        return id != null && id.equals(that.id);
    }
}