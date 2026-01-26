package com.RafaelDiaz.ClubJudoColombia.modelo.enums;


/**
 * Enum simplificado para la realidad del Club de Judo en Colombia.
 */
public enum MetodoPago {
    EFECTIVO("enum.metodo_pago.efectivo"),
    NEQUI("enum.metodo_pago.nequi");

    private final String clave;

    MetodoPago(String clave) {
        this.clave = clave;
    }

    public String getDescripcion() {
        return clave;
    }
}