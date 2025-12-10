package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Enum EstadoJudoka Internacionalizado.
 * [cite_start]Ahora la 'descripcion' almacena la CLAVE de traducción, no el texto final[cite: 7].
 */
public enum EstadoJudoka {
    PENDIENTE("enum.estado_judoka.pendiente"),
    EN_REVISION("enum.estado_judoka.en_revision"),
    ACTIVO("enum.estado_judoka.activo"),
    INACTIVO("enum.estado_judoka.inactivo"),
    RECHAZADO("enum.estado_judoka.rechazado");

    private final String clave;

    EstadoJudoka(String clave) {
        this.clave = clave;
    }

    /**
     * Retorna la clave de traducción (ej. "enum.estado_judoka.activo").
     * Usar con TraduccionService.get() para obtener el texto real.
     */
    public String getDescripcion() {
        return clave;
    }
}