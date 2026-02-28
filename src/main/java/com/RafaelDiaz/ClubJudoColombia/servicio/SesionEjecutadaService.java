package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionEjecutada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SesionEjecutadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SesionEjecutadaService {

    private final SesionEjecutadaRepository repository;

    public SesionEjecutadaService(SesionEjecutadaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SesionEjecutada guardarSesion(SesionEjecutada sesion) {
        // Al guardar la sesión, JPA se encarga de guardar las asistencias
        // asociadas gracias al CascadeType.ALL que pusimos en la entidad.
        return repository.save(sesion);
    }
    public String generarResumenMetodologico(SesionEjecutada sesion) {
        long presentes = sesion.getListaAsistencia().stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.PRESENTE).count();
        int total = sesion.getGrupo().getJudokas().size();

        return String.format("Resumen de Clase:\n" +
                        "- Asistencia: %d/%d (%.1f%%)\n" +
                        "- Fase del Microciclo: %s\n" +
                        "- Observaciones del Sensei: %s",
                presentes, total, (presentes * 100.0 / total),
                sesion.getMicrociclo().getNombre(),
                sesion.getNotasRetroalimentacion());
    }
    // Añade este método en SesionEjecutadaService.java
    public List<SesionEjecutada> obtenerHistorialDelSensei(Sensei sensei) {
        // Usamos el método que creamos en el repositorio para traer todo en 1 solo viaje
        return repository.findBySenseiOrderByFechaHoraEjecucionDesc(sensei);
    }
}