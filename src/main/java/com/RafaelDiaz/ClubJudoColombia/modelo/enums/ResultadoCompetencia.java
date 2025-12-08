package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum ResultadoCompetencia {
    ORO("Oro", 1.0, "#FFD700"),      // 100% de puntos
    PLATA("Plata", 0.7, "#C0C0C0"),  // 70%
    BRONCE("Bronce", 0.5, "#CD7F32"),// 50%
    QUINTO("5to Lugar", 0.3, "#3498DB"),
    PARTICIPACION("Participación", 0.1, "#95A5A6");

    private final String nombre;
    private final double multiplicador;
    private final String colorHex;

    ResultadoCompetencia(String nombre, double multiplicador, String colorHex) {
        this.nombre = nombre;
        this.multiplicador = multiplicador;
        this.colorHex = colorHex;
    }

    public String getNombre() { return nombre; }
    public double getMultiplicador() { return multiplicador; }
    public String getColorHex() { return colorHex; }
}