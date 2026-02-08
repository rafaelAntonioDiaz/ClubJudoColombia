package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPago;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una obligación de pago (Factura/Deuda).
 * Se genera el día 1 del mes para Acudientes y/o Mecenas.
 */
@Entity
@Table(name = "cuentas_cobro")
public class CuentaCobro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judokaBeneficiario; // ¿Por quién se cobra?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsable", nullable = false)
    private Usuario responsablePago; // ¿A quién se le cobra? (Acudiente o Mecenas)

    @Column(nullable = false)
    private String concepto; // Ej. "Mensualidad Febrero", "Alquiler Judogi"

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "valor_comision_sensei")
    private BigDecimal valorComisionSensei; // Los $5.000 (se separan al pagar)

    @Column(nullable = false)
    private LocalDate fechaGeneracion;

    @Column(nullable = false)
    private LocalDate fechaVencimiento; // Día 5 del mes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    // Relación con el pago real (cuando suceda)
    @OneToOne(mappedBy = "cuentaCobro", fetch = FetchType.LAZY)
    private Pago pagoAsociado;

    public CuentaCobro() {
        this.fechaGeneracion = LocalDate.now();
    }

    // Constructores de conveniencia
    public CuentaCobro(Judoka judoka, Usuario responsable, String concepto, BigDecimal valor, BigDecimal comision, LocalDate vencimiento) {
        this.judokaBeneficiario = judoka;
        this.responsablePago = responsable;
        this.concepto = concepto;
        this.valorTotal = valor;
        this.valorComisionSensei = comision;
        this.fechaVencimiento = vencimiento;
        this.fechaGeneracion = LocalDate.now();
        this.estado = EstadoPago.PENDIENTE;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudokaBeneficiario() { return judokaBeneficiario; }
    public void setJudokaBeneficiario(Judoka judokaBeneficiario) { this.judokaBeneficiario = judokaBeneficiario; }
    public Usuario getResponsablePago() { return responsablePago; }
    public void setResponsablePago(Usuario responsablePago) { this.responsablePago = responsablePago; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public BigDecimal getValorComisionSensei() { return valorComisionSensei; }
    public void setValorComisionSensei(BigDecimal valorComisionSensei) { this.valorComisionSensei = valorComisionSensei; }
    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDate fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
    public Pago getPagoAsociado() { return pagoAsociado; }
    public void setPagoAsociado(Pago pagoAsociado) { this.pagoAsociado = pagoAsociado; }
}