package com.RafaelDiaz.ClubJudoColombia.servicio.calculadores;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo;
import java.util.Map;

public interface CalculadorIndice {
    boolean aplicaPara(FormulaCalculo formula);

    // Ahora recibe y devuelve Objetos Metrica reales de la base de datos
    Map<Metrica, Double> calcular(Map<Metrica, Double> valoresCrudos, PruebaEstandar prueba);
}