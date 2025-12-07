package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // IMPORTANTE: Mantiene la transacción abierta para la gamificación
public class AsistenciaService {

    private final AsistenciaRepository repository;
    private final GamificationService gamificationService; // <--- INYECCIÓN

    public AsistenciaService(AsistenciaRepository repository, GamificationService gamificationService) {
        this.repository = repository;
        this.gamificationService = gamificationService;
    }

    public Asistencia registrarAsistencia(Asistencia asistencia) {
        if (repository.existsByJudokaIdAndSesionId(
                asistencia.getJudoka().getId(),
                asistencia.getSesion().getId())) {
            throw new RuntimeException("Asistencia ya registrada");
        }

        Asistencia guardada = repository.save(asistencia);

        // --- SENSOR DE GAMIFICACIÓN (SHIN) ---
        gamificationService.verificarLogrosAsistencia(guardada.getJudoka());

        return guardada;
    }

    public boolean estaRegistrada(Long sesionId, Long judokaId) {
        return repository.existsByJudokaIdAndSesionId(judokaId, sesionId);
    }
}