package com.RafaelDiaz.ClubJudoColombia.servicio.calculadores;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class CalculadorSJFT implements CalculadorIndice {
    @Override
    public boolean aplicaPara(FormulaCalculo formula) { return formula == FormulaCalculo.SJFT; }

    @Override
    public Map<Metrica, Double> calcular(Map<Metrica, Double> valores, PruebaEstandar prueba) {
        Map<Metrica, Double> resultados = new HashMap<>();

        Double proy = 0.0; Double fcFinal = 0.0; Double fc1min = 0.0;
        Metrica mIndice = null;

        for (Metrica m : prueba.getMetricas()) {
            String nk = m.getNombreKey().toLowerCase();
            if (nk.contains("proyecciones")) proy = valores.getOrDefault(m, 0.0);
            if (nk.contains("fc_final")) fcFinal = valores.getOrDefault(m, 0.0);
            if (nk.contains("fc_1min") || nk.contains("minuto")) fc1min = valores.getOrDefault(m, 0.0);
            if (nk.contains("indice")) mIndice = m;
        }

        if (proy > 0 && mIndice != null) {
            double indice = (fcFinal + fc1min) / proy;
            resultados.put(mIndice, redondear(indice, 2));
        }
        return resultados;
    }
    private double redondear(double v, int d) { return BigDecimal.valueOf(v).setScale(d, RoundingMode.HALF_UP).doubleValue(); }
}