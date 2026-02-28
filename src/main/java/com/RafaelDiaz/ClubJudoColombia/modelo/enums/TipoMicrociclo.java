package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum TipoMicrociclo {
    CORRIENTE("Corriente / Carga"),
    CHOQUE("Choque / Impacto"),
    AJUSTE("Ajuste"),
    CONTROL("Control / Evaluación");

    private final String descripcion;

    TipoMicrociclo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}