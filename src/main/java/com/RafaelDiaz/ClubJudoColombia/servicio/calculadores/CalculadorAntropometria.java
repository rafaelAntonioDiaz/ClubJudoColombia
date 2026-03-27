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
public class CalculadorAntropometria implements CalculadorIndice {

    @Override
    public boolean aplicaPara(FormulaCalculo formula) {
        return formula == FormulaCalculo.ANTROPOMETRIA;
    }

    @Override
    public Map<Metrica, Double> calcular(Map<Metrica, Double> valores, PruebaEstandar prueba) {
        Map<Metrica, Double> resultados = new HashMap<>();

        Double estaturaCm = null; Double pesoKg = null; Double cinturaCm = null;
        Metrica mImc = null; Metrica mRcc = null;

        // Buscamos las métricas dinámicamente sin importar cómo se llamen exactamente en la BD
        for (Metrica m : prueba.getMetricas()) {
            String nk = m.getNombreKey().toLowerCase();
            if (nk.contains("estatura") || nk.contains("altura")) estaturaCm = valores.get(m);
            if (nk.contains("masa") || nk.contains("peso")) pesoKg = valores.get(m);
            if (nk.contains("cintura") || nk.contains("perimetro")) cinturaCm = valores.get(m);
            if (nk.contains("imc")) mImc = m;
            if (nk.contains("rcc") || nk.contains("whtr")) mRcc = m;
        }

        if (estaturaCm != null && estaturaCm > 0) {
            if (pesoKg != null && mImc != null) {
                double imc = pesoKg / Math.pow(estaturaCm / 100.0, 2);
                resultados.put(mImc, redondear(imc, 1));
            }
            if (cinturaCm != null && mRcc != null) {
                double rcc = cinturaCm / estaturaCm;
                resultados.put(mRcc, redondear(rcc, 2));
            }
        }
        return resultados;
    }

    private double redondear(double valor, int decimales) {
        return BigDecimal.valueOf(valor).setScale(decimales, RoundingMode.HALF_UP).doubleValue();
    }
}