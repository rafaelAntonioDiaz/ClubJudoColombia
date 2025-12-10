package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum NivelOrganizacional {

    CLUB(
            "enum.nivel_organizacional.club.nombre",
            "enum.nivel_organizacional.club.desc"
    ),
    LIGA(
            "enum.nivel_organizacional.liga.nombre",
            "enum.nivel_organizacional.liga.desc"
    ),
    FEDERACION(
            "enum.nivel_organizacional.federacion.nombre",
            "enum.nivel_organizacional.federacion.desc"
    );

    private final String claveNombre;
    private final String claveDescripcion;

    NivelOrganizacional(String claveNombre, String claveDescripcion) {
        this.claveNombre = claveNombre;
        this.claveDescripcion = claveDescripcion;
    }

    /**
     * Método principal para I18n.
     * Devuelve la clave del NOMBRE (ej: "Club") para usar en ComboBoxes.
     */
    public String getDescripcion() {
        return claveNombre;
    }

    /**
     * Devuelve la clave de la descripción larga.
     * Usar: traduccionService.get(nivel.getClaveDescripcionLarga())
     */
    public String getClaveDescripcionLarga() {
        return claveDescripcion;
    }

    // Getter por compatibilidad, apunta a la clave del nombre
    public String getNombre() {
        return claveNombre;
    }
}