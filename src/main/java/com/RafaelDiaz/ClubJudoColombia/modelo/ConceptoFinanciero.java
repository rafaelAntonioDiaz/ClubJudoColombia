package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "conceptos_financieros")
public class ConceptoFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo; // ¿Es un concepto de cobro o de gasto?

    private BigDecimal valorSugerido; // Opcional, para autocompletar

    public ConceptoFinanciero() {}

    public ConceptoFinanciero(String nombre, TipoTransaccion tipo, BigDecimal valorSugerido) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valorSugerido = valorSugerido;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public BigDecimal getValorSugerido() { return valorSugerido; }
    public void setValorSugerido(BigDecimal valorSugerido) { this.valorSugerido = valorSugerido; }
}