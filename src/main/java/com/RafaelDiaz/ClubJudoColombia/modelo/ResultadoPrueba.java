package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ENTIDAD REFACTORIZADA
 * (Antes 'ResultadoEjercicio')
 * Almacena el resultado numérico (bruto) de una Prueba Estandar,
 * registrado por el Sensei.
 */
@Entity
@Table(name = "resultados_pruebas")
public class ResultadoPrueba implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resultado")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejercicio_plan", nullable = false)
    private EjercicioPlanificado ejercicioPlanificado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metrica", nullable = false)
    private Metrica metrica;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "numero_intento")
    private Integer numeroIntento;

    @Column(name = "notas_judoka")
    private String notasJudoka;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    // (Campos GPS eliminados)

    // Constructores, Getters, Setters...
    public ResultadoPrueba() {
        this.fechaRegistro = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public EjercicioPlanificado getEjercicioPlanificado() { return ejercicioPlanificado; }
    public void setEjercicioPlanificado(EjercicioPlanificado ejercicioPlanificado) { this.ejercicioPlanificado = ejercicioPlanificado; }
    public Metrica getMetrica() { return metrica; }
    public void setMetrica(Metrica metrica) { this.metrica = metrica; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public Integer getNumeroIntento() { return numeroIntento; }
    public void setNumeroIntento(Integer numeroIntento) { this.numeroIntento = numeroIntento; }
    public String getNotasJudoka() { return notasJudoka; }
    public void setNotasJudoka(String notasJudoka) { this.notasJudoka = notasJudoka; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // (hashCode y equals)
}