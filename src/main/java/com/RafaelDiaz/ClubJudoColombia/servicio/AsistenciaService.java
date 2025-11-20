package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsistenciaService {
    private final AsistenciaRepository repository;

    public AsistenciaService(AsistenciaRepository repository) {
        this.repository = repository;
    }

    public Asistencia registrarAsistencia(Asistencia asistencia) {
        if (repository.existsByJudokaIdAndSesionId(
                asistencia.getJudoka().getId(),
                asistencia.getSesion().getId())) {
            throw new RuntimeException("Asistencia ya registrada");
        }
        return repository.save(asistencia);
    }

    public boolean estaRegistrada(Long sesionId, Long judokaId) {
        return repository.existsByJudokaIdAndSesionId(judokaId, sesionId);
    }
}