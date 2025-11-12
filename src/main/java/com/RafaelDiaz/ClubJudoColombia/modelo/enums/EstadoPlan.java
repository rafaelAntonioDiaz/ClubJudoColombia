package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Define el estado de un Plan de Entrenamiento.
 */
public enum EstadoPlan {
    PENDIENTE("Pendiente"),
    EN_PROGRESO("En Progreso"),
    COMPLETADO("Completado");

    private final String descripcion;

    EstadoPlan(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}