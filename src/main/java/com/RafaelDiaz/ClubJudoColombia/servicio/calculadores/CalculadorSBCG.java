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
public class CalculadorSBCG implements CalculadorIndice {
    @Override
    public boolean aplicaPara(FormulaCalculo formula) { return formula == FormulaCalculo.SBCG; }

    @Override
    public Map<Metrica, Double> calcular(Map<Metrica, Double> valores, PruebaEstandar prueba) {
        Map<Metrica, Double> resultados = new HashMap<>();

        Double pesoKg = null; Double tiempoIso = null; Double repsDin = null;
        Metrica mIsoKg = null; Metrica mDinKg = null;

        for (Metrica m : prueba.getMetricas()) {
            String nk = m.getNombreKey().toLowerCase();
            if (nk.contains("peso") || nk.contains("masa")) pesoKg = valores.get(m);
            if (nk.contains("tiempo")) tiempoIso = valores.get(m);
            if (nk.contains("repeticiones") || nk.contains("dinamica")) repsDin = valores.get(m);
            if (nk.contains("iso_kg") || nk.contains("isometrico_kg")) mIsoKg = m;
            if (nk.contains("rep_din") || nk.contains("dinamicas_kg")) mDinKg = m;
        }

        if (pesoKg != null && pesoKg > 0) {
            if (tiempoIso != null && tiempoIso > 0 && mIsoKg != null) {
                resultados.put(mIsoKg, redondear(tiempoIso * pesoKg, 1));
            }
            if (repsDin != null && repsDin > 0 && mDinKg != null) {
                resultados.put(mDinKg, redondear(repsDin * pesoKg, 1));
            }
        }
        return resultados;
    }
    private double redondear(double v, int d) { return BigDecimal.valueOf(v).setScale(d, RoundingMode.HALF_UP).doubleValue(); }
}