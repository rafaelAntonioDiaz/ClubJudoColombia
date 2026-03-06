package com.RafaelDiaz.ClubJudoColombia.dto;

import java.time.LocalDate;

public class DatosAntropometricosDTO {
    private LocalDate fecha;
    private Double peso;
    private Double estatura;
    private Double imc;

    public DatosAntropometricosDTO() {}

    public DatosAntropometricosDTO(LocalDate fecha, Double peso, Double estatura, Double imc) {
        this.fecha = fecha;
        this.peso = peso;
        this.estatura = estatura;
        this.imc = imc;
    }

    // Getters y Setters
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public Double getEstatura() { return estatura; }
    public void setEstatura(Double estatura) { this.estatura = estatura; }
    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }
}