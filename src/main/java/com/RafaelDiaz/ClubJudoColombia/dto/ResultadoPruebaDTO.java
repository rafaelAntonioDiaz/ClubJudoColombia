package com.RafaelDiaz.ClubJudoColombia.dto;

import java.time.LocalDate;

public class ResultadoPruebaDTO {
    private Long id;
    private LocalDate fecha;
    private String pruebaNombre;
    private String metricaNombre;
    private Double valor;
    private String clasificacion;
    private Double puntos;

    public ResultadoPruebaDTO() {}

    public ResultadoPruebaDTO(Long id, LocalDate fecha, String pruebaNombre, String metricaNombre,
                              Double valor, String clasificacion, Double puntos) {
        this.id = id;
        this.fecha = fecha;
        this.pruebaNombre = pruebaNombre;
        this.metricaNombre = metricaNombre;
        this.valor = valor;
        this.clasificacion = clasificacion;
        this.puntos = puntos;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getPruebaNombre() { return pruebaNombre; }
    public void setPruebaNombre(String pruebaNombre) { this.pruebaNombre = pruebaNombre; }
    public String getMetricaNombre() { return metricaNombre; }
    public void setMetricaNombre(String metricaNombre) { this.metricaNombre = metricaNombre; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public Double getPuntos() { return puntos; }
    public void setPuntos(Double puntos) { this.puntos = puntos; }
}