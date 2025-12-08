package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum NivelCompetencia {
    INTERNACIONAL("Internacional", 100),
    NACIONAL("Nacional", 50),
    REGIONAL("Regional", 30),
    DEPARTAMENTAL("Departamental", 20),
    LOCAL("Local", 10);

    private final String nombre;
    private final int puntosBase;

    NivelCompetencia(String nombre, int puntosBase) {
        this.nombre = nombre;
        this.puntosBase = puntosBase;
    }

    public String getNombre() { return nombre; }
    public int getPuntosBase() { return puntosBase; }
}