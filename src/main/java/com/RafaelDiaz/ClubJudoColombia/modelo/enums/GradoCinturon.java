package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Enumeración para los grados (Kyu y Dan) del Judo.
 */
public enum GradoCinturon {
    // Kyu (Grados de estudiante)
    BLANCO("6º Kyu", "Blanco"),
    AMARILLO("5º Kyu", "Amarillo"),
    NARANJA("4º Kyu", "Naranja"),
    VERDE("3º Kyu", "Verde"),
    AZUL("2º Kyu", "Azul"),
    MARRON("1º Kyu", "Marrón"),

    // Dan (Grados de maestría)
    NEGRO_1_DAN("1º Dan", "Negro"),
    NEGRO_2_DAN("2º Dan", "Negro"),
    NEGRO_3_DAN("3º Dan", "Negro"),
    NEGRO_4_DAN("4º Dan", "Negro"),
    NEGRO_5_DAN("5º Dan", "Negro"),
    NEGRO_6_DAN("6º Dan", "Rojo y Blanco"),
    NEGRO_7_DAN("7º Dan", "Rojo y Blanco"),
    NEGRO_8_DAN("8º Dan", "Rojo y Blanco"),
    NEGRO_9_DAN("9º Dan", "Rojo"),
    NEGRO_10_DAN("10º Dan", "Rojo");

    private final String grado;
    private final String color;

    GradoCinturon(String grado, String color) {
        this.grado = grado;
        this.color = color;
    }

    public String getGrado() {
        return grado;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return grado + " (" + color + ")";
    }
}