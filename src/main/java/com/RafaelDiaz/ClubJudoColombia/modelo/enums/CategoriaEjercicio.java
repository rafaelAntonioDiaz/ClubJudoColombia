package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Categoriza el tipo de Ejercicio o Prueba de Rendimiento.
 * (Versión actualizada para incluir pruebas de aptitud física general)
 */
public enum CategoriaEjercicio {

    // De la primera tabla (Judo)
    MEDICION_ANTROPOMETRICA ("Medición Antropométrica"),
    POTENCIA ("Potencia"),
    RESISTENCIA_ISOMETRICA ("Resistencia Isométrica"),
    RESISTENCIA_DINAMICA ("Resistencia Dinámica"),
    APTITUD_ANAEROBICA ("Aptitud Anaeróbica"),
    APTITUD_AEROBICA ("Aptitud Aeróbica"),
    TECNICA ("Técnica"),

    // De la segunda tabla (General)
    FLEXIBILIDAD ("Flexibilidad"),
    RESISTENCIA_MUSCULAR_LOCALIZADA ("Resistencia Muscular Localizada"),
    AGILIDAD ("Agilidad"),
    VELOCIDAD ("Velocidad");

    private final String descripcion;

    CategoriaEjercicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}