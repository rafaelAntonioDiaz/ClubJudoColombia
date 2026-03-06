package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.dto.BloqueConPruebasDTO;
import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.BloqueAgudelo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JudokaDashboardService {

    private final ResultadoPruebaService resultadoPruebaService;
    private final InsigniaRepository insigniaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final ParticipacionCompetenciaRepository palmaresRepo;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final NormaEvaluacionRepository normaEvaluacionRepository;
    public JudokaDashboardService(ResultadoPruebaService resultadoPruebaService,
                                  InsigniaRepository insigniaRepository,
                                  JudokaInsigniaRepository judokaInsigniaRepository,
                                  PruebaEstandarRepository pruebaEstandarRepository, ParticipacionCompetenciaRepository palmaresRepo, ResultadoPruebaRepository resultadoPruebaRepository, NormaEvaluacionRepository normaEvaluacionRepository) {
        this.resultadoPruebaService = resultadoPruebaService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.palmaresRepo = palmaresRepo;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.normaEvaluacionRepository = normaEvaluacionRepository;
    }

    // --- SECCIÓN HERO (Poder y Radar) ---

    public Double getPoderDeCombate(Judoka judoka) {
        return resultadoPruebaService.calcularPoderDeCombate(judoka);
    }

    public Map<String, Double> getDatosRadar(Judoka judoka) {
        return resultadoPruebaService.getPoderDeCombateComponentes(judoka);
    }

    // --- SECCIÓN AGENDA (KPIs) ---

    public long getPlanesActivos(Judoka judoka) {
        return resultadoPruebaService.contarPlanesActivos(judoka);
    }

    public long getTareasHoy(Judoka judoka) {
        return resultadoPruebaService.contarEjecucionesTareaHoy(judoka);
    }

    public Optional<Long> getDiasProximaEvaluacion(Judoka judoka) {
        return resultadoPruebaService.proximaEvaluacionEnDias(judoka);
    }

    // --- SECCIÓN GAMIFICACIÓN (Insignias) ---

    public List<Insignia> getCatalogoInsignias() {
        return insigniaRepository.findAll();
    }

    public List<JudokaInsignia> getInsigniasGanadas(Judoka judoka) {
        return judokaInsigniaRepository.findByJudoka(judoka);
    }

    // --- SECCIÓN DETALLE (Gráficos Históricos) ---

    public Optional<PruebaEstandar> buscarPrueba(String nombreKey) {
        return pruebaEstandarRepository.findByNombreKey(nombreKey);
    }

    public List<Map<String, Object>> getHistorialPrueba(Judoka judoka, PruebaEstandar prueba) {
        return resultadoPruebaService.getHistorialParaGrafico(judoka, prueba);
    }

    public List<Map<String, Object>> getMetaPrueba(Judoka judoka, PruebaEstandar prueba) {
        return resultadoPruebaService.getNormasParaGrafico(judoka, prueba);
    }
    // --- SECCIÓN PALMARÉS ---
    public List<ParticipacionCompetencia> getPalmares(Judoka judoka) {
        return palmaresRepo.findByJudokaOrderByFechaDesc(judoka);
    }

    public int calcularPuntosAscenso(Judoka judoka) {
        return getPalmares(judoka).stream()
                .mapToInt(ParticipacionCompetencia::getPuntosCalculados)
                .sum();
    }
    // ==========================================================
    // FASE 2: MOTOR DEL MOTIVADOR (PROESP & FALLBACK EXPERIMENTAL)
    // ==========================================================

    // ==========================================================
    // FASE 2 y 3: MOTOR DEL MOTIVADOR (PROESP & RÉCORD DOJO)
    // ==========================================================

    public Double calcularMotivador(Judoka judoka, PruebaEstandar prueba) {
        List<Map<String, Object>> historial = getHistorialPrueba(judoka, prueba);
        if (historial == null || historial.isEmpty()) {
            return null; // Sin datos, no hay meta
        }

        // Extraemos el último valor de la gráfica
        Double mejorValorActual = ((Number) historial.get(historial.size() - 1).get("valor")).doubleValue();

        // Calculamos la edad exacta hoy
        int edad = java.time.Period.between(judoka.getFechaNacimiento(), java.time.LocalDate.now()).getYears();
        com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo sexo = judoka.getSexo();

        // 1. Intentamos buscar en la ciencia (PROESP / CBJ) usando tu método exacto
        List<NormaEvaluacion> normas = normaEvaluacionRepository.findNormasPorCriteriosBasicos(prueba.getId(), sexo, edad);

        boolean esPruebaDeTiempo = prueba.getNombreKey().contains("velocidad") || prueba.getNombreKey().contains("carrera");

        if (normas != null && !normas.isEmpty()) {
            // LÓGICA PROESP: Buscar el umbral del siguiente escalón real
            return calcularEscalonCientifico(mejorValorActual, normas, esPruebaDeTiempo);
        }

        // 2. FALLBACK EXPERIMENTAL: Si no hay norma, el motivador es el Récord del Dojo
        Double recordDojo = esPruebaDeTiempo ?
                resultadoPruebaRepository.findRecordMinimoDojo(prueba) :
                resultadoPruebaRepository.findRecordMaximoDojo(prueba);

        // Si él mismo tiene el récord del dojo o no hay récord, su motivador es superarse a sí mismo
        if (recordDojo == null || recordDojo.equals(mejorValorActual)) {
            return esPruebaDeTiempo ? mejorValorActual * 0.95 : mejorValorActual * 1.05;
        }

        return recordDojo;
    }

    private Double calcularEscalonCientifico(Double valorActual, List<NormaEvaluacion> normas, boolean esPruebaDeTiempo) {
        // Orden de la escalera de peor a mejor (Asegúrate de que estos nombres coincidan con tu Enum ClasificacionRendimiento)
        List<String> jerarquia = java.util.Arrays.asList("ZONA_DE_RIESGO", "DEBIL", "RAZONABLE", "BUENO", "MUY_BIEN", "EXCELENTE");

        String nivelActual = "ZONA_DE_RIESGO"; // Por defecto

        // Descubrir en qué nivel está hoy
        for (NormaEvaluacion norma : normas) {
            Double min = norma.getValorMin();
            Double max = norma.getValorMax();

            boolean encaja = false;
            if (min != null && max != null) {
                encaja = (valorActual >= min && valorActual <= max);
            } else if (min != null) {
                encaja = (valorActual >= min);
            } else if (max != null) {
                encaja = (valorActual <= max);
            }

            if (encaja) {
                nivelActual = norma.getClasificacion().name(); // Obtenemos el String seguro del Enum
                break;
            }
        }

        // Buscar la norma del siguiente nivel
        int indiceActual = jerarquia.indexOf(nivelActual);
        if (indiceActual >= 0 && indiceActual < jerarquia.size() - 1) {
            String nivelObjetivo = jerarquia.get(indiceActual + 1);
            for (NormaEvaluacion norma : normas) {
                if (norma.getClasificacion().name().equals(nivelObjetivo)) {
                    // Si es tiempo (ej. Velocidad), la meta es bajar del valor MÁXIMO de la categoría destino
                    // Si es fuerza (ej. Abdominales), la meta es superar el valor MÍNIMO de la categoría destino
                    return esPruebaDeTiempo ? norma.getValorMax() : norma.getValorMin();
                }
            }
        }

        // Si ya es "EXCELENTE", su motivador es mejorar un 2% su propia marca
        return esPruebaDeTiempo ? valorActual * 0.98 : valorActual * 1.02;
    }
    public List<BloqueConPruebasDTO> getPruebasPorBloque(Judoka judoka) {
        return resultadoPruebaService.getPruebasPorBloque(judoka);
    }

    public Map<String, Double> getPuntosRadar(Judoka judoka, Map<BloqueAgudelo, Long> seleccion) {
        return resultadoPruebaService.getPuntosRadar(judoka, seleccion);
    }
}