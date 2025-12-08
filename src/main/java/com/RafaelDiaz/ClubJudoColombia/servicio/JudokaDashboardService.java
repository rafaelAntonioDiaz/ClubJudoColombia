package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaInsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ParticipacionCompetenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
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

    public JudokaDashboardService(ResultadoPruebaService resultadoPruebaService,
                                  InsigniaRepository insigniaRepository,
                                  JudokaInsigniaRepository judokaInsigniaRepository,
                                  PruebaEstandarRepository pruebaEstandarRepository, ParticipacionCompetenciaRepository palmaresRepo) {
        this.resultadoPruebaService = resultadoPruebaService;
        this.insigniaRepository = insigniaRepository;
        this.judokaInsigniaRepository = judokaInsigniaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.palmaresRepo = palmaresRepo;
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
}