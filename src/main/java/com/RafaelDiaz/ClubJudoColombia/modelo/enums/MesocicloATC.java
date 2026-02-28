package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum MesocicloATC {
    NIVELACION("Nivelación"),
    ADQUISICION("Adquisición (A)"),
    TRANSFERENCIA("Transferencia (T)"),
    COMPETENCIA("Competencia (C)"),
    REFUERZO("Refuerzo / Retroalimentación (R)"),
    RECUPERACION("Recuperación (R)");

    private final String descripcion;

    MesocicloATC(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}