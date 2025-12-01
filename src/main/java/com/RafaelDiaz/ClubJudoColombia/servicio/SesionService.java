package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SesionProgramadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class SesionService {

    private final SesionProgramadaRepository sesionRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final AsistenciaRepository asistenciaRepository;

    public SesionService(SesionProgramadaRepository sesionRepository,
                         GrupoEntrenamientoRepository grupoRepository,
                         AsistenciaRepository asistenciaRepository) {
        this.sesionRepository = sesionRepository;
        this.grupoRepository = grupoRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoCalendario> obtenerEventosMes(Judoka judoka, YearMonth mes) {
        // 1. Obtener grupos del judoka
        List<GrupoEntrenamiento> grupos = grupoRepository.findAllByJudokasContains(judoka);

        if (grupos.isEmpty()) return List.of();

        // 2. Rango de fechas
        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fin = mes.atEndOfMonth().atTime(23, 59, 59);

        // 3. Buscar sesiones
        List<SesionProgramada> sesiones = sesionRepository.findByGruposAndFechaBetween(grupos, inicio, fin);

        // 4. Transformar a DTO con estado
        List<EventoCalendario> eventos = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (SesionProgramada sesion : sesiones) {
            EstadoSesion estado;
            if (sesion.getFechaHoraInicio().isAfter(ahora)) {
                estado = EstadoSesion.FUTURA;
            } else {
                boolean asistio = asistenciaRepository.existsByJudokaIdAndSesionId(judoka.getId(), sesion.getId());
                estado = asistio ? EstadoSesion.ASISTIO : EstadoSesion.FALTO;
            }
            eventos.add(new EventoCalendario(sesion, estado));
        }
        return eventos;
    }

    // DTO interno para la vista
    public record EventoCalendario(SesionProgramada sesion, EstadoSesion estado) {}

    public enum EstadoSesion { FUTURA, ASISTIO, FALTO }
}