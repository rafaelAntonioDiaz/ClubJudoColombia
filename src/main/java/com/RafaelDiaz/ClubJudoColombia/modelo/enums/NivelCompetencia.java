package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum NivelCompetencia {
    INTERNACIONAL("enum.nivel_competencia.internacional", 100),
    NACIONAL("enum.nivel_competencia.nacional", 50),
    REGIONAL("enum.nivel_competencia.regional", 30),
    DEPARTAMENTAL("enum.nivel_competencia.departamental", 20),
    LOCAL("enum.nivel_competencia.local", 10);

    private final String nombre;
    private final int puntosBase;

    NivelCompetencia(String nombre, int puntosBase) {
        this.nombre = nombre;
        this.puntosBase = puntosBase;
    }

    public String getNombre() { return nombre; }
    public int getPuntosBase() { return puntosBase; }
}