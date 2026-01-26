package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

    public int calcularAsistenciaPromedio() {
        Sensei sensei = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        List<Object[]> resultados = asistenciaRepository.countAsistenciasUltimos30Dias(sensei.getId(), inicio);

        if (resultados.isEmpty()) return 0;
        Object[] datos = resultados.get(0);
        Long asistencias = (Long) datos[0];
        Long posibles = (Long) datos[1];

        if (posibles == 0) return 0;
        return (int) ((asistencias * 100) / posibles);
    }

    public Map<String, Double> getPromedioPoderDeCombatePorGrupo() {
        return Map.of("Sub-13", 4.2, "Mayores", 3.8, "Femenino", 4.5);
    }

    public List<Double> getAsistenciaUltimos30Dias() {
        return List.of(85.0, 90.0, 78.0, 92.0, 88.0);
    }
}