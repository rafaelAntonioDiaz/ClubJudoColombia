package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SenseiDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(SenseiDashboardService.class);

    private final JudokaRepository judokaRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final SesionProgramadaRepository sesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ResultadoPruebaService resultadoPruebaService;
    private final SecurityService securityService;

    public SenseiDashboardService(JudokaRepository judokaRepository,
                                  GrupoEntrenamientoRepository grupoRepository,
                                  SesionProgramadaRepository sesionRepository,
                                  AsistenciaRepository asistenciaRepository,
                                  ResultadoPruebaService resultadoPruebaService,
                                  SecurityService securityService) {
        this.judokaRepository = judokaRepository;
        this.grupoRepository = grupoRepository;
        this.sesionRepository = sesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.resultadoPruebaService = resultadoPruebaService;
        this.securityService = securityService;
    }

    private Sensei getCurrentSensei() {
        return securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));
    }

    public long getTotalJudokas() {
        Sensei sensei = getCurrentSensei();
        return judokaRepository.countBySenseiId(sensei.getId());
    }

    public long getTotalGrupos() {
        Sensei sensei = getCurrentSensei();
        return grupoRepository.countBySenseiId(sensei.getId());
    }

    public long getPruebasHoy() {
        Sensei sensei = getCurrentSensei();
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return sesionRepository.contarPruebasDelSenseiHoy(TipoSesion.EVALUACION, inicio, fin, sensei.getId());
    }

    public double calcularAsistenciaPromedio(Long senseiId) {
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        long totales = asistenciaRepository.contarTotalesUltimos30Dias(senseiId, hace30Dias);
        if (totales == 0) return 0.0;
        long presentes = asistenciaRepository.contarPorEstadoUltimos30Dias(senseiId, hace30Dias, EstadoAsistencia.PRESENTE);
        return (double) presentes / totales * 100.0;
    }

    public Map<String, Double> getPromedioPoderDeCombatePorGrupo() {
        Sensei sensei = getCurrentSensei();
        List<GrupoEntrenamiento> grupos = grupoRepository.findBySensei(sensei);
        Map<String, Double> resultado = new LinkedHashMap<>();

        for (GrupoEntrenamiento grupo : grupos) {
            List<Judoka> judokas = judokaRepository.findByGrupo(grupo);
            if (judokas.isEmpty()) {
                resultado.put(grupo.getNombre(), 0.0);
                continue;
            }
            double promedio = judokas.stream()
                    .filter(j -> j.getEstado() == EstadoJudoka.ACTIVO)
                    .mapToDouble(j -> resultadoPruebaService.calcularPoderDeCombate(j))
                    .average()
                    .orElse(0.0);
            resultado.put(grupo.getNombre(), Math.round(promedio * 10) / 10.0);
        }
        return resultado;
    }

    public List<Double> getAsistenciaUltimos30Dias() {
        Sensei sensei = getCurrentSensei();
        LocalDate today = LocalDate.now();
        List<Double> resultado = new ArrayList<>(30);

        for (int i = 29; i >= 0; i--) {
            LocalDate fecha = today.minusDays(i);
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.atTime(LocalTime.MAX);
            long total = asistenciaRepository.countBySenseiAndDateBetween(sensei.getId(), inicio, fin);
            long presentes = asistenciaRepository.countBySenseiAndDateBetweenAndEstado(sensei.getId(), inicio, fin, EstadoAsistencia.PRESENTE);
            double porcentaje = total == 0 ? 0.0 : (presentes * 100.0 / total);
            resultado.add(porcentaje);
        }
        return resultado;
    }
}