package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Define el estado de un Plan de Entrenamiento.
 */
public enum EstadoMicrociclo {
    BORRADOR("Borrador"),
    ACTIVO("Activo"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoMicrociclo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}