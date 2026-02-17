package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Categoriza el tipo de Ejercicio o Prueba de Rendimiento.
 * Agrupadas por bloques según Agudelo.
 */
public enum CategoriaEjercicio {
    // Biometría
    MEDICION_ANTROPOMETRICA ("Medición Antropométrica"),

    // Bloque Definitorio de Aptitudes Físicas
    POTENCIA ("Potencia"),
    VELOCIDAD ("Velocidad"),

    // Bloque de sustento
    RESISTENCIA_DINAMICA ("Resistencia Dinámica"),
    RESISTENCIA_MUSCULAR_LOCALIZADA ("Resistencia Muscular Localizada"),
    RESISTENCIA_ISOMETRICA ("Resistencia Isométrica"),

   // Bloque de Eficiencia
    APTITUD_ANAEROBICA ("Aptitud Anaeróbica"),
    APTITUD_AEROBICA ("Aptitud Aeróbica"),

    // Bloque de Protección
    FLEXIBILIDAD ("Flexibilidad"),

    // Bloque Técnico-Coordinativo eje transversal
    AGILIDAD ("Agilidad"),
    TECNICA ("Técnica"),
    ANTICIPACION ("Anticipación");


    private final String descripcion;

    CategoriaEjercicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}