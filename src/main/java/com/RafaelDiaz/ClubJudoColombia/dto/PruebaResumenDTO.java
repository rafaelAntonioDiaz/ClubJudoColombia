package com.RafaelDiaz.ClubJudoColombia.dto;

import java.time.LocalDate;

public class PruebaResumenDTO {
    private Long id;
    private String nombre;
    private Double ultimoValor;
    private LocalDate fechaUltimo;
    private String clasificacion; // Texto traducido de la clasificación
    private Double puntos; // 1-5 según clasificación

    public PruebaResumenDTO() {}

    public PruebaResumenDTO(Long id, String nombre, Double ultimoValor, LocalDate fechaUltimo,
                            String clasificacion, Double puntos) {
        this.id = id;
        this.nombre = nombre;
        this.ultimoValor = ultimoValor;
        this.fechaUltimo = fechaUltimo;
        this.clasificacion = clasificacion;
        this.puntos = puntos;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getUltimoValor() { return ultimoValor; }
    public void setUltimoValor(Double ultimoValor) { this.ultimoValor = ultimoValor; }
    public LocalDate getFechaUltimo() { return fechaUltimo; }
    public void setFechaUltimo(LocalDate fechaUltimo) { this.fechaUltimo = fechaUltimo; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public Double getPuntos() { return puntos; }
    public void setPuntos(Double puntos) { this.puntos = puntos; }
}