package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Define los grupos de entrenamiento basados en la edad.
 */
public enum Grupo {
    // De 6 a 7:30 pm
    MENORES_12_ANOS("Menores de 12 años (5-11)"),

    // De 4 a 5:30 pm
    MAYORES_12_ANOS("Mayores de 12 años");

    private final String descripcion;

    Grupo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}