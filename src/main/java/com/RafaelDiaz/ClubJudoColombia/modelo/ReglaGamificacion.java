package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.OperadorComparacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoEventoGamificacion;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reglas_gamificacion")
public class ReglaGamificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensei_id", nullable = false)
    private Sensei sensei;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insignia_id", nullable = false)
    private Insignia insignia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metrica_id")
    private Metrica metrica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEventoGamificacion tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "operador", nullable = false)
    private OperadorComparacion operador;

    @Column(name = "valor_objetivo", precision = 10, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    // Constructores
    public ReglaGamificacion() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }

    public Insignia getInsignia() { return insignia; }
    public void setInsignia(Insignia insignia) { this.insignia = insignia; }

    public Metrica getMetrica() { return metrica; }
    public void setMetrica(Metrica metrica) { this.metrica = metrica; }

    public TipoEventoGamificacion getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEventoGamificacion tipoEvento) { this.tipoEvento = tipoEvento; }

    public OperadorComparacion getOperador() { return operador; }
    public void setOperador(OperadorComparacion operador) { this.operador = operador; }

    public BigDecimal getValorObjetivo() { return valorObjetivo; }
    public void setValorObjetivo(BigDecimal valorObjetivo) { this.valorObjetivo = valorObjetivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}