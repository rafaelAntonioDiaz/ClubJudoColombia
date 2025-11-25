package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio completo para resultados de pruebas y dashboard del Judoka.
 * Incluye cálculo de Poder de Combate + métricas para el dashboard moderno.
 *
 * @author RafaelDiaz – Versión FINAL Dashboard 2025-11-20
 */
@Service
public class ResultadoPruebaService {

    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final NormaEvaluacionRepository normaRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final TraduccionService traduccionService;
    private final PlanEntrenamientoRepository planEntrenamientoRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;

    public ResultadoPruebaService(ResultadoPruebaRepository resultadoPruebaRepository,
                                  NormaEvaluacionRepository normaRepository,
                                  PruebaEstandarRepository pruebaEstandarRepository,
                                  TraduccionService traduccionService,
                                  PlanEntrenamientoRepository planEntrenamientoRepository,
                                  EjecucionTareaRepository ejecucionTareaRepository) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.normaRepository = normaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.traduccionService = traduccionService;
        this.planEntrenamientoRepository = planEntrenamientoRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
    }

    // ==================================================================
    // MÉTODOS EXISTENTES (ya los tenías – los mantengo limpios)
    // ==================================================================

    @Transactional
    public ResultadoPrueba registrarResultado(ResultadoPrueba resultado) {
        return resultadoPruebaRepository.save(resultado);
    }

    public Optional<ClasificacionRendimiento> getClasificacionParaResultado(ResultadoPrueba resultado) {
        Judoka judoka = resultado.getJudoka();
        PruebaEstandar prueba = resultado.getEjercicioPlanificado().getPruebaEstandar();
        Metrica metrica = resultado.getMetrica();
        double valorObtenido = resultado.getValor();

        if (judoka == null || prueba == null || metrica == null) return Optional.empty();

        List<NormaEvaluacion> normas = normaRepository.findNormasPorCriterios(
                prueba, metrica, judoka.getSexo(), judoka.getEdad());

        if (normas.isEmpty()) return Optional.empty();

        return normas.stream()
                .filter(norma -> {
                    Double min = norma.getValorMin();
                    Double max = norma.getValorMax();
                    if (min != null && max != null) return valorObtenido >= min && valorObtenido <= max;
                    if (min != null) return valorObtenido >= min;
                    if (max != null) return valorObtenido <= max;
                    return false;
                })
                .findFirst()
                .map(NormaEvaluacion::getClasificacion);
    }

    @Transactional(readOnly = true)
    public List<ResultadoPrueba> getHistorialDeResultados(Judoka judoka, PruebaEstandar prueba) {
        return resultadoPruebaRepository
                .findByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroAsc(judoka, prueba);
    }

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

        for (String clave : clavesPruebasClave) {
            PruebaEstandar prueba = pruebaEstandarRepository.findByNombreKey(clave).orElse(null);
            if (prueba == null) continue;

            String nombre = traduccionService.get(clave);
            if (nombre.contains(" (")) nombre = nombre.substring(0, nombre.indexOf(" ("));

            double puntos = 1.0;
            List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);
            if (!historial.isEmpty()) {
                ClasificacionRendimiento clas = getClasificacionParaResultado(historial.get(historial.size() - 1))
                        .orElse(ClasificacionRendimiento.DEBIL);

                puntos = switch (clas) {
                    case EXCELENTE -> 5.0;
                    case MUY_BIEN -> 4.0;
                    case BUENO -> 3.0;
                    case REGULAR, RAZONABLE -> 2.0;
                    default -> 1.0;
                };
            }
            componentes.put(nombre, puntos);
        }
        return componentes;
    }

    @Transactional(readOnly = true)
    public Double calcularPoderDeCombate(Judoka judoka) {
        Map<String, Double> componentes = getPoderDeCombateComponentes(judoka);
        double puntajeTotal = 0.0;
        double pesoMaximo = 0.0;

        for (Map.Entry<String, Double> e : componentes.entrySet()) {
            double peso = e.getKey().contains("Special Judo Fitness Test") ? 1.5 : 1.0;
            pesoMaximo += 5.0 * peso;
            puntajeTotal += e.getValue() * peso;
        }

        return pesoMaximo == 0 ? 1000.0 : 1000.0 + (puntajeTotal / pesoMaximo) * 4000.0;
    }

    // ==================================================================
    // NUEVOS MÉTODOS PARA EL DASHBOARD DEL JUDOKA (2025-11-20)
    // ==================================================================

    /** KPI: Planes activos (PENDIENTE o EN_PROGRESO) */
    @Transactional(readOnly = true)
    public long contarPlanesActivos(Judoka judoka) {
        return planEntrenamientoRepository.contarPlanesActivosParaJudoka(
                judoka,
                List.of(EstadoPlan.PENDIENTE, EstadoPlan.EN_PROGRESO)
        );
    }


    /** KPI: Tareas de acondicionamiento completadas hoy */
    @Transactional(readOnly = true)
    public long contarEjecucionesTareaHoy(Judoka judoka) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();

        return ejecucionTareaRepository.countByJudokaAndFechaRegistroBetween(
                judoka, inicioDia, finDia);
    }

    /** KPI: Días hasta la próxima evaluación física programada */
    @Transactional(readOnly = true)
    public Optional<Long> proximaEvaluacionEnDias(Judoka judoka) {
        return resultadoPruebaRepository.findTopByJudokaOrderByFechaRegistroDesc(judoka)
                .map(ultima -> {
                    LocalDate ultimaFecha = ultima.getFechaRegistro().toLocalDate();
                    LocalDate proxima = ultimaFecha.plusMonths(3); // Cada 3 meses (ajustable)
                    return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), proxima);
                });
    }
    /**
     * Devuelve datos listos para ApexCharts: lista de mapas con fecha, métrica y valor.
     * CORREGIDO: Ya no asume submetricas. Usa directamente el nombre de la métrica principal.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistorialParaGrafico(Judoka judoka, PruebaEstandar prueba) {
        List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);

        if (historial.isEmpty()) {
            return Collections.emptyList();
        }

        return historial.stream()
                .map(r -> {
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("fecha", r.getFechaRegistro().toLocalDate().toString());
                    mapa.put("metrica", traduccionService.get(r.getMetrica().getNombreKey()));
                    mapa.put("valor", r.getValor());
                    return mapa;
                })
                .collect(Collectors.toList());
    }}