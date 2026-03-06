package com.RafaelDiaz.ClubJudoColombia.dto;

import java.time.LocalDateTime;

public class TareaEjecutadaDTO {
    private LocalDateTime fecha;
    private String tareaNombre;
    private boolean completada;

    public TareaEjecutadaDTO() {}

    public TareaEjecutadaDTO(LocalDateTime fecha, String tareaNombre, boolean completada) {
        this.fecha = fecha;
        this.tareaNombre = tareaNombre;
        this.completada = completada;
    }

    // Getters y Setters
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getTareaNombre() { return tareaNombre; }
    public void setTareaNombre(String tareaNombre) { this.tareaNombre = tareaNombre; }
    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}