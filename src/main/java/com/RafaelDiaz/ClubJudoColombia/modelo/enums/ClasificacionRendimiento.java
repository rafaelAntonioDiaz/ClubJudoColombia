package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Unifica todas las clasificaciones de rendimiento
 * de los manuales (CBJ, PROESP), traducidas al español.
 */
public enum ClasificacionRendimiento {

    // --- Niveles de Rendimiento (Unificados) ---

    /**
     * Cubre "Excelente" y "Excelencia"
     */
    EXCELENTE,

    /**
     * Cubre "Muy Bien"
     */
    MUY_BIEN,

    /**
     * Cubre "Bom" (Portugués) y "Bien"
     */
    BUENO,

    /**
     * Cubre "Regular"
     */
    REGULAR,

    /**
     * Cubre "Razonable"
     */
    RAZONABLE,

    /**
     * Cubre "Fraco" (Portugués) y "Debil"
     */
    DEBIL,

    /**
     * Cubre "Muito Fraco" (Portugués)
     */
    MUY_DEBIL,


    // --- Criterios de Salud (PROESP) ---

    /**
     * Cubre "Zona de Risco"
     */
    ZONA_DE_RIESGO,

    /**
     * Cubre la zona saludable (implícita)
     */
    ZONA_SALUDABLE;

    public String getTraduccionKey() {
        return "clasificacion." + this.name().toLowerCase();
    }
}