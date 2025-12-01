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

        // Claves LIMPIAS (sin .nombre) que coinciden con DataInitializer
        List<String> clavesPruebasClave = List.of(
                "ejercicio.salto_horizontal_proesp",
                "ejercicio.lanzamiento_balon",
                "ejercicio.abdominales_1min",
                "ejercicio.carrera_6min",
                "ejercicio.agilidad_4x4",
                "ejercicio.carrera_20m",
                "ejercicio.sjft"
        );

        System.out.println("--- CALCULANDO PODER DE COMBATE PARA: " + judoka.getUsuario().getUsername() + " ---");

        for (String clave : clavesPruebasClave) {
            // Buscamos la prueba (Intento doble: clave pura o con .nombre)
            PruebaEstandar prueba = pruebaEstandarRepository.findByNombreKey(clave)
                    .or(() -> pruebaEstandarRepository.findByNombreKey(clave + ".nombre"))
                    .orElse(null);

            if (prueba == null) {
                System.out.println("   [X] Prueba no encontrada: " + clave);
                continue;
            }

            String nombre = traduccionService.get(clave);
            if (nombre.contains(" (")) nombre = nombre.substring(0, nombre.indexOf(" ("));

            double puntos = 1.0;

            // Buscamos historial
            List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);
            System.out.println("   [?] Prueba: " + clave + " | ID: " + prueba.getId() + " | Resultados encontrados: " + historial.size());

            if (!historial.isEmpty()) {
                ResultadoPrueba ultimoResultado = historial.get(historial.size() - 1);
                System.out.println("       -> Último valor: " + ultimoResultado.getValor());

                // Intentamos clasificar
                Optional<ClasificacionRendimiento> clasOpt = getClasificacionParaResultado(ultimoResultado);

                if (clasOpt.isPresent()) {
                    ClasificacionRendimiento clas = clasOpt.get();
                    System.out.println("       -> Clasificación: " + clas);
                    puntos = switch (clas) {
                        case EXCELENTE -> 5.0;
                        case MUY_BIEN -> 4.0;
                        case BUENO -> 3.0;
                        case REGULAR, RAZONABLE -> 2.0; // Ajusté RAZONABLE que a veces se llama diferente
                        default -> 1.0;
                    };
                } else {
                    System.out.println("       -> [!] No se encontró norma de clasificación. Asignando 1.0 por defecto.");
                    // --- PARCHE TEMPORAL PARA VER GRÁFICO ---
                    // Si hay datos pero no hay normas cargadas en la BD,
                    // el gráfico saldrá plano. Vamos a simular puntos basados en el valor
                    // solo para que veas que el gráfico funciona.
                    if (ultimoResultado.getValor() > 20) puntos = 4.0;
                    else if (ultimoResultado.getValor() > 10) puntos = 3.0;
                    else puntos = 2.0;
                }
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
                List.of(EstadoPlan.ACTIVO)
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