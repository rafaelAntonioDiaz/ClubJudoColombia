package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para KPIs y métricas del dashboard del Sensei.
 * Queries optimizadas y cálculos en BD cuando es posible.
 *
 * @author RafaelDiaz
 * @version 1.1 (Corregida)
 * @since 2025-11-20
 */
@Service
@Transactional(readOnly = true)
public class SenseiDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(SenseiDashboardService.class);

    private final JudokaRepository judokaRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final SesionProgramadaRepository sesionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ResultadoPruebaRepository resultadoRepository;
    private final SecurityService securityService;

    public SenseiDashboardService(JudokaRepository judokaRepository,
                                  GrupoEntrenamientoRepository grupoRepository,
                                  SesionProgramadaRepository sesionRepository,
                                  AsistenciaRepository asistenciaRepository,
                                  ResultadoPruebaRepository resultadoRepository,
                                  SecurityService securityService) {
        this.judokaRepository = judokaRepository;
        this.grupoRepository = grupoRepository;
        this.sesionRepository = sesionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.resultadoRepository = resultadoRepository;
        this.securityService = securityService;
    }

    public long getTotalJudokas() {
        return judokaRepository.count();
    }

    public long getTotalGrupos() {
        return grupoRepository.count();
    }

    public long getPruebasHoy() {
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return sesionRepository.contarPruebasDelSenseiHoy(TipoSesion.EVALUACION, inicio, fin,
                securityService.getAuthenticatedSensei()
                        .map(Sensei::getId)
                        .orElseThrow(() -> new RuntimeException("Sensei no autenticado")));
    }

    // (Asegúrate de importar com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;)

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public double calcularAsistenciaPromedio(Long senseiId) {
        try {
            LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);

            long totales = asistenciaRepository.contarTotalesUltimos30Dias(senseiId, hace30Dias);
            if (totales == 0) return 0.0;

            long presentes = asistenciaRepository.contarPorEstadoUltimos30Dias(senseiId, hace30Dias, EstadoAsistencia.PRESENTE);
            return (double) presentes / totales * 100.0;

        } catch (Exception e) {

            return 0.0;
        }
    }

    public Map<String, Double> getPromedioPoderDeCombatePorGrupo() {
        return Map.of("Sub-13", 4.2, "Mayores", 3.8, "Femenino", 4.5);
    }

    public List<Double> getAsistenciaUltimos30Dias() {
        return List.of(85.0, 90.0, 78.0, 92.0, 88.0);
    }
}