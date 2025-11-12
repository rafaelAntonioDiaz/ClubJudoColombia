package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.NormaEvaluacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository; // --- REFACTORIZADO ---
import com.RafaelDiaz.ClubJudoColombia.repositorio.ResultadoPruebaRepository; // --- REFACTORIZADO ---
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * --- SERVICIO REFACTORIZADO ---
 * (Antes ResultadoEjercicioService)
 * Maneja la lógica de las Pruebas Estándar (SJFT, Salto, etc.)
 * y calcula el "Poder de Combate".
 */
@Service
public class ResultadoPruebaService {

    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final NormaEvaluacionRepository normaRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final TraduccionService traduccionService;

    public ResultadoPruebaService(ResultadoPruebaRepository resultadoPruebaRepository,
                                  NormaEvaluacionRepository normaRepository,
                                  PruebaEstandarRepository pruebaEstandarRepository,
                                  TraduccionService traduccionService) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.normaRepository = normaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.traduccionService = traduccionService;
    }

    /**
     * Guarda un nuevo resultado de prueba (registrado por el Sensei).
     */
    @Transactional
    public ResultadoPrueba registrarResultado(ResultadoPrueba resultado) {
        return resultadoPruebaRepository.save(resultado);
    }

    /**
     * Obtiene la clasificación (EXCELENTE, BUENO...) para un resultado.
     */
    public Optional<ClasificacionRendimiento> getClasificacionParaResultado(ResultadoPrueba resultado) {

        Judoka judoka = resultado.getJudoka();
        PruebaEstandar prueba = resultado.getEjercicioPlanificado().getPruebaEstandar();
        Metrica metrica = resultado.getMetrica();
        double valorObtenido = resultado.getValor();

        if (judoka == null || prueba == null || metrica == null) {
            return Optional.empty();
        }

        Sexo sexo = judoka.getSexo();
        int edad = judoka.getEdad();

        List<NormaEvaluacion> normas = normaRepository.findNormasPorCriterios(
                prueba,
                metrica,
                sexo,
                edad
        );

        if (normas.isEmpty()) {
            return Optional.empty();
        }

        // (Lógica de comparación de rangos)
        for (NormaEvaluacion norma : normas) {
            Double min = norma.getValorMin();
            Double max = norma.getValorMax();
            if (min != null && max != null) {
                if (valorObtenido >= min && valorObtenido <= max) {
                    return Optional.of(norma.getClasificacion());
                }
            } else if (min != null && max == null) {
                if (valorObtenido >= min) {
                    return Optional.of(norma.getClasificacion());
                }
            } else if (min == null && max != null) {
                if (valorObtenido <= max) {
                    return Optional.of(norma.getClasificacion());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Obtiene el historial de resultados para un Judoka y una Prueba Estandar.
     */
    @Transactional(readOnly = true)
    public List<ResultadoPrueba> getHistorialDeResultados(Judoka judoka, PruebaEstandar prueba) {
        List<ResultadoPrueba> resultados =
                resultadoPruebaRepository.findByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroAsc(judoka, prueba);

        // Forzar inicialización
        resultados.forEach(r -> r.getMetrica().getNombreKey());

        return resultados;
    }

    /**
     * Obtiene los componentes del Poder de Combate (los puntos 1-5).
     */
    @Transactional(readOnly = true)
    public Map<String, Double> getPoderDeCombateComponentes(Judoka judoka) {
        Map<String, Double> componentes = new LinkedHashMap<>();
        List<String> clavesPruebasClave = List.of(
                "ejercicio.salto_horizontal_proesp.nombre",
                "ejercicio.lanzamiento_balon.nombre",
                "ejercicio.abdominales_1min.nombre",
                "ejercicio.carrera_6min.nombre",
                "ejercicio.agilidad_4x4.nombre",
                "ejercicio.carrera_20m.nombre",
                "ejercicio.sjft.nombre"
        );

        for (String clavePrueba : clavesPruebasClave) {
            PruebaEstandar prueba = pruebaEstandarRepository.findByNombreKey(clavePrueba).orElse(null);
            if (prueba == null) continue;

            String nombreTraducido = traduccionService.get(clavePrueba);
            if (nombreTraducido.contains("(")) {
                nombreTraducido = nombreTraducido.substring(0, nombreTraducido.indexOf(" ("));
            }

            List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);
            double puntos = 1.0; // Puntaje mínimo

            if (!historial.isEmpty()) {
                ResultadoPrueba ultimoResultado = historial.get(historial.size() - 1);
                ClasificacionRendimiento clasificacion = getClasificacionParaResultado(ultimoResultado)
                        .orElse(ClasificacionRendimiento.DEBIL);

                switch (clasificacion) {
                    case EXCELENTE -> puntos = 5.0;
                    case MUY_BIEN -> puntos = 4.0;
                    case BUENO -> puntos = 3.0;
                    case REGULAR, RAZONABLE -> puntos = 2.0;
                    default -> puntos = 1.0;
                }
            }
            componentes.put(nombreTraducido, puntos);
        }
        return componentes;
    }

    /**
     * Calcula el "Poder de Combate" (PC) final y escalado.
     */
    @Transactional(readOnly = true)
    public Double calcularPoderDeCombate(Judoka judoka) {
        Map<String, Double> componentes = getPoderDeCombateComponentes(judoka);
        double puntajeTotal = 0.0;
        double pesoMaximo = 0.0;

        for (Map.Entry<String, Double> entry : componentes.entrySet()) {
            String nombrePrueba = entry.getKey();
            Double puntos = entry.getValue();

            // Ponderación (SJFT pesa más)
            double pesoPonderado = (nombrePrueba.contains("Special Judo Fitness Test")) ? 1.5 : 1.0;
            pesoMaximo += (5.0 * pesoPonderado);
            puntajeTotal += (puntos * pesoPonderado);
        }

        double puntajeBase = 1000.0;
        double rango = 4000.0;
        if (pesoMaximo == 0) return puntajeBase;
        return puntajeBase + ((puntajeTotal / pesoMaximo) * rango);
    }
}