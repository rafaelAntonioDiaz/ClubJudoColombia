package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // IMPORTANTE: Mantiene la transacción abierta para la gamificación
public class AsistenciaService {

    private final AsistenciaRepository repository;
    private final GamificationService gamificationService;
    private final GrupoEntrenamientoRepository grupoRepository;

    public AsistenciaService(AsistenciaRepository repository,
                             GamificationService gamificationService,
                             GrupoEntrenamientoRepository grupoRepository) {
        this.repository = repository;
        this.gamificationService = gamificationService;
        this.grupoRepository = grupoRepository;
    }

    public Asistencia registrarAsistencia(Asistencia asistencia) {
        // Validamos usando los IDs para evitar duplicados en la misma clase ejecutada
        if (repository.existsByJudokaIdAndSesionId(
                asistencia.getJudoka().getId(),
                asistencia.getSesion().getId())) {
            throw new RuntimeException("La asistencia de este Judoka ya fue registrada para esta sesión.");
        }

        Asistencia guardada = repository.save(asistencia);

        // --- SENSOR DE GAMIFICACIÓN (SHIN) ---
        // ¡Solo premiamos al judoka si realmente vino al dojo!
        if (guardada.getEstado() == EstadoAsistencia.PRESENTE ||
                guardada.getEstado() == EstadoAsistencia.LLEGADA_TARDE) {
            gamificationService.verificarLogrosAsistencia(guardada.getJudoka());
        }

        return guardada;
    }

    public boolean estaRegistrada(Long sesionEjecutadaId, Long judokaId) {
        return repository.existsByJudokaIdAndSesionId(judokaId, sesionEjecutadaId);
    }

    // =========================================================================
    // NOTA ARQUITECTÓNICA SOBRE EL CHECK-IN GPS:
    // El método realizarCheckInGps ha sido temporalmente comentado porque
    // la entidad Asistencia ahora apunta a SesionEjecutada (la clase real)
    // en lugar de SesionProgramada (la teoría).
    // Reactivaremos esta función en la Fase 3, para que el GPS del alumno
    // valide contra la ubicación en vivo del "Modo Tatami" del Sensei.
    // =========================================================================

    /*
    @Transactional
    public void realizarCheckInGps(Judoka judoka, double latUser, double lonUser) {
        // Lógica de Haversine y validación que restauraremos pronto...
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // ...
        return 0.0;
    }
    */
}