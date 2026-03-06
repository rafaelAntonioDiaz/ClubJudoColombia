package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.dto.BloqueConPruebasDTO;
import com.RafaelDiaz.ClubJudoColombia.dto.PruebaResumenDTO;
import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.BloqueAgudelo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
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
    private final MicrocicloRepository microcicloRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;
    private final GamificationService gamificationService;
    private final MapeoBloquesService mapeoBloquesService;

    public ResultadoPruebaService(ResultadoPruebaRepository resultadoPruebaRepository,
                                  NormaEvaluacionRepository normaRepository,
                                  PruebaEstandarRepository pruebaEstandarRepository,
                                  TraduccionService traduccionService,
                                  MicrocicloRepository microcicloRepository,
                                  EjecucionTareaRepository ejecucionTareaRepository, GamificationService gamificationService, MapeoBloquesService mapeoBloquesService) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.normaRepository = normaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.traduccionService = traduccionService;
        this.microcicloRepository = microcicloRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
        this.gamificationService = gamificationService;
        this.mapeoBloquesService = mapeoBloquesService;
    }

    @Transactional
    public ResultadoPrueba registrarResultado(ResultadoPrueba resultado) {
        ResultadoPrueba guardado = resultadoPruebaRepository.save(resultado);
        // 🎮 GAMIFICATION: Verificar logros físicos
        gamificationService.verificarLogrosFisicos(guardado.getJudoka(), guardado);
        return guardado;
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

        Sensei sensei = judoka.getSensei();
        // Obtener todas las pruebas (globales + del sensei) que tengan categoría
        List<PruebaEstandar> todas = pruebaEstandarRepository.findGlobalesYDelSensei(sensei);

        // Agrupar por bloque usando el servicio de mapeo
        Map<BloqueAgudelo, List<PruebaEstandar>> pruebasPorBloque = new EnumMap<>(BloqueAgudelo.class);
        for (PruebaEstandar p : todas) {
            BloqueAgudelo bloque = mapeoBloquesService.getBloque(p.getCategoria());
            if (bloque != null) {
                pruebasPorBloque.computeIfAbsent(bloque, k -> new ArrayList<>()).add(p);
            }
        }

        // Para cada bloque, calcular el promedio de puntos de los últimos resultados
        for (BloqueAgudelo bloque : BloqueAgudelo.values()) {
            List<PruebaEstandar> pruebas = pruebasPorBloque.getOrDefault(bloque, Collections.emptyList());
            double sumaPuntos = 0.0;
            int pruebasConDatos = 0;

            for (PruebaEstandar prueba : pruebas) {
                List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);
                if (!historial.isEmpty()) {
                    ResultadoPrueba ultimo = historial.get(historial.size() - 1);
                    Optional<ClasificacionRendimiento> clasOpt = getClasificacionParaResultado(ultimo);
                    double puntos = clasOpt.map(this::puntosDeClasificacion).orElse(1.0);
                    sumaPuntos += puntos;
                    pruebasConDatos++;
                }
            }

            double valorBloque = (pruebasConDatos > 0) ? sumaPuntos / pruebasConDatos : 1.0;
            String nombreBloque = traduccionService.get("bloque." + bloque.name().toLowerCase());
            componentes.put(nombreBloque, valorBloque);
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

    /** KPI: Planes activos (PENDIENTE o EN_PROGRESO) */
    @Transactional(readOnly = true)
    public long contarPlanesActivos(Judoka judoka) {
        return microcicloRepository.contarPlanesActivosParaJudoka(
                judoka,
                List.of(EstadoMicrociclo.ACTIVO)
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
    }

    /**
     * NUEVO: Obtiene las normas de evaluación para un judoka y prueba específicos.
     * Devuelve una lista de mapas para usar en las anotaciones del gráfico.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNormasParaGrafico(Judoka judoka, PruebaEstandar prueba) {
        Optional<ResultadoPrueba> ultimo = resultadoPruebaRepository
                .findTopByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroDesc(judoka, prueba);

        if (ultimo.isEmpty()) return Collections.emptyList();

        Metrica metrica = ultimo.get().getMetrica();

        // Buscamos todas las normas para esa métrica/edad/sexo
        List<NormaEvaluacion> todasLasNormas = normaRepository.findNormasPorCriterios(
                prueba, metrica, judoka.getSexo(), judoka.getEdad());

        // FILTRO QUIRÚRGICO: Nos quedamos solo con la MEJOR norma
        // Asumimos que la clasificación tiene un orden o buscamos "EXCELENTE"
        return todasLasNormas.stream()
                .filter(n -> n.getClasificacion() == ClasificacionRendimiento.EXCELENTE) // Solo la meta dorada
                .findFirst()
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", "Meta (Excelente)"); // Texto fijo o traducible
                    // Si es tiempo (carrera), el "mejor" es el valor mínimo (o máximo del rango excelente)
                    // Ajusta esta lógica según si tu métrica es ascendente o descendente
                    Double valorTarget = (n.getValorMin() != null) ? n.getValorMin() : n.getValorMax();
                    map.put("valor", valorTarget);
                    return map;
                })
                .map(List::of) // Devolvemos lista de 1 elemento
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<BloqueConPruebasDTO> getPruebasPorBloque(Judoka judoka) {
        Sensei sensei = judoka.getSensei();
        // Obtener todas las pruebas (globales + del sensei)
        List<PruebaEstandar> todas = pruebaEstandarRepository.findGlobalesYDelSensei(sensei);

        // Agrupar por bloque
        Map<BloqueAgudelo, List<PruebaEstandar>> porBloque = new EnumMap<>(BloqueAgudelo.class);
        for (PruebaEstandar p : todas) {
            BloqueAgudelo bloque = mapeoBloquesService.getBloque(p.getCategoria());
            if (bloque != null) {
                porBloque.computeIfAbsent(bloque, k -> new ArrayList<>()).add(p);
            }
        }

        List<BloqueConPruebasDTO> resultado = new ArrayList<>();
        for (Map.Entry<BloqueAgudelo, List<PruebaEstandar>> entry : porBloque.entrySet()) {
            BloqueAgudelo bloque = entry.getKey();
            List<PruebaEstandar> pruebas = entry.getValue();

            List<PruebaResumenDTO> resumenes = new ArrayList<>();
            for (PruebaEstandar p : pruebas) {
                Optional<ResultadoPrueba> ultimo = resultadoPruebaRepository
                        .findTopByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroDesc(judoka, p);
                if (ultimo.isPresent()) {
                    ResultadoPrueba r = ultimo.get();
                    Optional<ClasificacionRendimiento> clasif = getClasificacionParaResultado(r);
                    double puntos = clasif.map(this::puntosDeClasificacion).orElse(1.0);
                    String clasificacionTexto = clasif.map(c -> traduccionService.get(c)).orElse("");
                    PruebaResumenDTO dto = new PruebaResumenDTO(
                            p.getId(),
                            p.getNombreMostrar(traduccionService),
                            r.getValor(),
                            r.getFechaRegistro().toLocalDate(),
                            clasificacionTexto,
                            puntos
                    );
                    resumenes.add(dto);
                }
            }
            // Ordenar por fecha descendente (más reciente primero)
            resumenes.sort((a, b) -> b.getFechaUltimo().compareTo(a.getFechaUltimo()));

            // Determinar prueba seleccionada por defecto: la más reciente (si hay)
            Long seleccionada = resumenes.isEmpty() ? null : resumenes.get(0).getId();

            resultado.add(new BloqueConPruebasDTO(
                    bloque,
                    traduccionService.get("bloque." + bloque.name().toLowerCase()),
                    resumenes,
                    seleccionada
            ));
        }
        // Ordenar bloques según el orden natural del enum
        resultado.sort(Comparator.comparing(b -> b.getBloque().ordinal()));
        return resultado;
    }
    // Método para obtener los puntos del radar basado en las selecciones actuales
    @Transactional(readOnly = true)
    public Map<String, Double> getPuntosRadar(Judoka judoka, Map<BloqueAgudelo, Long> seleccion) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Map.Entry<BloqueAgudelo, Long> entry : seleccion.entrySet()) {
            BloqueAgudelo bloque = entry.getKey();
            Long pruebaId = entry.getValue();
            if (pruebaId == null) {
                resultado.put(traduccionService.get("bloque." + bloque.name().toLowerCase()), 1.0);
                continue;
            }
            PruebaEstandar prueba = pruebaEstandarRepository.findById(pruebaId).orElse(null);
            if (prueba == null) {
                resultado.put(traduccionService.get("bloque." + bloque.name().toLowerCase()), 1.0);
                continue;
            }
            List<ResultadoPrueba> historial = getHistorialDeResultados(judoka, prueba);
            if (historial.isEmpty()) {
                resultado.put(traduccionService.get("bloque." + bloque.name().toLowerCase()), 1.0);
            } else {
                ResultadoPrueba ultimo = historial.get(historial.size() - 1);
                Optional<ClasificacionRendimiento> clasif = getClasificacionParaResultado(ultimo);
                double puntos = clasif.map(this::puntosDeClasificacion).orElse(1.0);
                resultado.put(traduccionService.get("bloque." + bloque.name().toLowerCase()), puntos);
            }
        }
        return resultado;
    }


    // Método auxiliar
    private double puntosDeClasificacion(ClasificacionRendimiento clas) {
        return switch (clas) {
            case EXCELENTE -> 5.0;
            case MUY_BIEN -> 4.0;
            case BUENO -> 3.0;
            case REGULAR, RAZONABLE -> 2.0;
            default -> 1.0;
        };
    }
}