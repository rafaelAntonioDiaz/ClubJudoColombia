package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Enum para métodos de pago con soporte i18n.
 */
public enum MetodoPago {
    EFECTIVO("enum.metodo_pago.efectivo"),
    TRANSFERENCIA("enum.metodo_pago.transferencia"),
    TARJETA("enum.metodo_pago.tarjeta");

    private final String clave;

    MetodoPago(String clave) {
        this.clave = clave;
    }

    /**
     * Retorna la clave para que TraduccionService.get(this) funcione.
     */
    public String getDescripcion() {
        return clave;
    }

    // Mantenemos getNombre por compatibilidad si alguna vista vieja lo usa,
    // pero idealmente deberíamos usar getDescripcion o el servicio de traducción.
    public String getNombre() {
        return clave;
    }
}