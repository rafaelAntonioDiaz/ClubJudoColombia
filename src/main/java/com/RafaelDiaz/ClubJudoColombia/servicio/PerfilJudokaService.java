package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.dto.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerfilJudokaService {

    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final EjecucionTareaRepository ejecucionTareaRepository;
    private final JudokaInsigniaRepository judokaInsigniaRepository;
    private final PalmaresRepository palmaresRepository;
    private final DocumentoRequisitoRepository documentoRepository;
    private final TraduccionService traduccionService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final MetricaRepository metricaRepository;

    public PerfilJudokaService(ResultadoPruebaRepository resultadoPruebaRepository,
                               EjecucionTareaRepository ejecucionTareaRepository,
                               JudokaInsigniaRepository judokaInsigniaRepository,
                               PalmaresRepository palmaresRepository,
                               DocumentoRequisitoRepository documentoRepository,
                               TraduccionService traduccionService,
                               ResultadoPruebaService resultadoPruebaService, PruebaEstandarRepository pruebaEstandarRepository, MetricaRepository metricaRepository) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.ejecucionTareaRepository = ejecucionTareaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
        this.palmaresRepository = palmaresRepository;
        this.documentoRepository = documentoRepository;
        this.traduccionService = traduccionService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.metricaRepository = metricaRepository;
    }

    @Transactional(readOnly = true)
    public List<DatosAntropometricosDTO> getHistorialAntropometrico(Judoka judoka) {
        // Buscar la prueba de antropometría
        Optional<PruebaEstandar> pruebaOpt = pruebaEstandarRepository.findByNombreKey("ejercicio.medicion_antropo.nombre");
        if (pruebaOpt.isEmpty()) return Collections.emptyList();

        PruebaEstandar prueba = pruebaOpt.get();
        // Obtener todas las métricas relevantes
        Metrica peso = metricaRepository.findByNombreKey("metrica.masa_corporal.nombre").orElse(null);
        Metrica estatura = metricaRepository.findByNombreKey("metrica.estatura.nombre").orElse(null);
        Metrica imc = metricaRepository.findByNombreKey("metrica.imc.nombre").orElse(null);

        if (peso == null || estatura == null) return Collections.emptyList();

        // Obtener todos los resultados de antropometría del judoka
        List<ResultadoPrueba> resultados = resultadoPruebaRepository.findByJudokaAndEjercicioPlanificado_PruebaEstandar(judoka, prueba);

        // Agrupar por fecha (asumiendo que en una misma fecha se registraron todas las métricas)
        Map<LocalDate, Map<Metrica, Double>> porFecha = new HashMap<>();
        for (ResultadoPrueba r : resultados) {
            LocalDate fecha = r.getFechaRegistro().toLocalDate();
            porFecha.computeIfAbsent(fecha, k -> new HashMap<>()).put(r.getMetrica(), r.getValor());
        }

        List<DatosAntropometricosDTO> historial = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<Metrica, Double>> entry : porFecha.entrySet()) {
            LocalDate fecha = entry.getKey();
            Map<Metrica, Double> valores = entry.getValue();
            Double pesoVal = valores.get(peso);
            Double estaturaVal = valores.get(estatura);
            Double imcVal = imc != null ? valores.get(imc) : null;
            if (pesoVal != null && estaturaVal != null) {
                if (imcVal == null) {
                    // Calcular IMC si no está registrado
                    imcVal = pesoVal / ((estaturaVal / 100) * (estaturaVal / 100));
                }
                historial.add(new DatosAntropometricosDTO(fecha, pesoVal, estaturaVal, imcVal));
            }
        }
        historial.sort(Comparator.comparing(DatosAntropometricosDTO::getFecha));
        return historial;
    }

    @Transactional(readOnly = true)
    public List<ResultadoPruebaDTO> getResultadosPruebas(Judoka judoka, Optional<Long> pruebaId,
                                                         Optional<LocalDate> desde, Optional<LocalDate> hasta) {
        List<ResultadoPrueba> resultados;
        if (pruebaId.isPresent()) {
            Optional<PruebaEstandar> pruebaOpt = pruebaEstandarRepository.findById(pruebaId.get());
            if (pruebaOpt.isPresent()) {
                resultados = resultadoPruebaRepository.findByJudokaAndEjercicioPlanificado_PruebaEstandarOrderByFechaRegistroAsc(judoka, pruebaOpt.get());
            } else {
                resultados = Collections.emptyList();
            }
        } else {
            // Todos los resultados
            resultados = resultadoPruebaRepository.findByJudokaOrderByFechaRegistroDesc(judoka);
        }

        // Filtrar por fechas
        if (desde.isPresent() || hasta.isPresent()) {
            resultados = resultados.stream()
                    .filter(r -> {
                        LocalDate f = r.getFechaRegistro().toLocalDate();
                        boolean ok = true;
                        if (desde.isPresent()) ok = ok && !f.isBefore(desde.get());
                        if (hasta.isPresent()) ok = ok && !f.isAfter(hasta.get());
                        return ok;
                    })
                    .collect(Collectors.toList());
        }

        return resultados.stream()
                .map(r -> {
                    Optional<ClasificacionRendimiento> clasif = resultadoPruebaService.getClasificacionParaResultado(r);
                    double puntos = clasif.map(c -> puntosDeClasificacion(c)).orElse(1.0);
                    String clasificacionTexto = clasif.map(c -> traduccionService.get(c)).orElse("");
                    return new ResultadoPruebaDTO(
                            r.getId(),
                            r.getFechaRegistro().toLocalDate(),
                            r.getEjercicioPlanificado().getPruebaEstandar().getNombreMostrar(traduccionService),
                            traduccionService.get(r.getMetrica().getNombreKey()),
                            r.getValor(),
                            clasificacionTexto,
                            puntos
                    );
                })
                .collect(Collectors.toList());
    }

    private double puntosDeClasificacion(ClasificacionRendimiento clas) {
        return switch (clas) {
            case EXCELENTE -> 5.0;
            case MUY_BIEN -> 4.0;
            case BUENO -> 3.0;
            case REGULAR, RAZONABLE -> 2.0;
            default -> 1.0;
        };
    }

    @Transactional(readOnly = true)
    public List<TareaEjecutadaDTO> getUltimasTareas(Judoka judoka, int limite) {
        List<EjecucionTarea> ejecuciones = ejecucionTareaRepository.findTop10ByJudokaOrderByFechaRegistroDesc(judoka);
        return ejecuciones.stream()
                .map(e -> new TareaEjecutadaDTO(
                        e.getFechaRegistro(),
                        e.getEjercicioPlanificado().getTareaDiaria().getNombre(),
                        e.isCompletado()
                ))
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JudokaInsignia> getInsignias(Judoka judoka) {
        return judokaInsigniaRepository.findByJudokaOrderByFechaObtencionDesc(judoka);
    }

    @Transactional(readOnly = true)
    public List<ParticipacionCompetencia> getPalmares(Judoka judoka) {
        return palmaresRepository.findByJudokaOrderByFechaDesc(judoka);
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> getDocumentos(Judoka judoka) {
        // Usar el repositorio para obtener los documentos del judoka
        List<DocumentoRequisito> docs = documentoRepository.findByJudoka(judoka);
        return docs.stream()
                .map(d -> new DocumentoDTO(
                        d.getId(),
                        traduccionService.get(d.getTipo()),
                        extraerNombreArchivo(d.getUrlArchivo()),
                        d.getUrlArchivo()
                ))
                .collect(Collectors.toList());
    }

    private String extraerNombreArchivo(String url) {
        if (url == null || url.isEmpty()) return "archivo";
        int idx = url.lastIndexOf('/');
        if (idx >= 0) return url.substring(idx + 1);
        return url;
    }

}