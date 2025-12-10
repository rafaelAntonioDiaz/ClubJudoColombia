package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.CampoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.CampoEntrenamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CampoService {

    private final CampoEntrenamientoRepository campoRepo;
    private final TraduccionService traduccionService; // <--- INYECCIÓN

    public CampoService(CampoEntrenamientoRepository campoRepo, TraduccionService traduccionService) {
        this.campoRepo = campoRepo;
        this.traduccionService = traduccionService;
    }

    @Transactional(readOnly = true)
    public List<CampoEntrenamiento> findAll() {
        return campoRepo.findAllWithDetails();
    }

    public void inscribirJudoka(Judoka judoka, String nombre, String ubicacion, LocalDate inicio, LocalDate fin, String objetivo) {
        CampoEntrenamiento campo = new CampoEntrenamiento(judoka, nombre, ubicacion, inicio, fin, objetivo);
        campoRepo.save(campo);
    }

    /**
     * Certifica que el judoka cumplió el campo y otorga puntos para ascenso.
     */
    public void certificarCumplimiento(CampoEntrenamiento campo, int puntosOtorgados) {
        campo.setCompletado(true);
        campo.setPuntosAscenso(puntosOtorgados);
        campoRepo.save(campo);
    }

    public void eliminar(CampoEntrenamiento campo) {
        campoRepo.delete(campo);
    }
}