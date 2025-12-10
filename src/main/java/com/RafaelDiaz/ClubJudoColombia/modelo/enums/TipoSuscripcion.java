package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Define la recurrencia de un producto o plan con soporte I18n.
 */
public enum TipoSuscripcion {
    PAGO_UNICO("enum.tipo_suscripcion.pago_unico"),
    MENSUAL("enum.tipo_suscripcion.mensual"),
    BIMENSUAL("enum.tipo_suscripcion.bimensual"),
    TRIMESTRAL("enum.tipo_suscripcion.trimestral"),
    SEMESTRAL("enum.tipo_suscripcion.semestral"),
    ANUAL("enum.tipo_suscripcion.anual");

    private final String clave;

    TipoSuscripcion(String clave) {
        this.clave = clave;
    }

    public String getDescripcion() {
        return clave;
    }
}