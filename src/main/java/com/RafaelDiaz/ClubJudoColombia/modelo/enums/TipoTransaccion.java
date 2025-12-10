package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum TipoTransaccion {
    INGRESO("enum.tipo_transaccion.ingreso", "green"),
    EGRESO("enum.tipo_transaccion.egreso", "red");

    private final String clave;
    private final String color;

    TipoTransaccion(String clave, String color) {
        this.clave = clave;
        this.color = color;
    }

    /**
     * Retorna la clave de traducción para I18n.
     * Ej: "enum.tipo_transaccion.ingreso"
     */
    public String getDescripcion() {
        return clave;
    }

    /**
     * Mantenemos getNombre() por compatibilidad, pero retorna la clave.
     * Idealmente usar getDescripcion() o traduccionService.get(this).
     */
    public String getNombre() {
        return clave;
    }

    public String getColor() {
        return color;
    }
}