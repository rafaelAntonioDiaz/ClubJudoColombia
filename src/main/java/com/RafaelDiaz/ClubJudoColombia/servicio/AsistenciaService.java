package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional // IMPORTANTE: Mantiene la transacción abierta para la gamificación
public class AsistenciaService {

    private final AsistenciaRepository repository;
    private final GamificationService gamificationService; // <--- INYECCIÓN
    private final SesionService sesionService; // <--- NUEVO
    private final GrupoEntrenamientoRepository grupoRepository;
    public AsistenciaService(AsistenciaRepository repository, GamificationService gamificationService,
                             SesionService sesionService,
                             GrupoEntrenamientoRepository grupoRepository) {
        this.repository = repository;
        this.gamificationService = gamificationService;
        this.sesionService = sesionService;
        this.grupoRepository = grupoRepository;
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
    @Transactional
    public void realizarCheckInGps(Judoka judoka, double latUser, double lonUser) {
        // 1. Buscar grupos del alumno
        List<GrupoEntrenamiento> grupos = grupoRepository.findAllByJudokasContains(judoka);
        if (grupos.isEmpty()) {
            throw new RuntimeException("No perteneces a ningún grupo de entrenamiento.");
        }

        // 2. Buscar si ALGUNO de sus grupos tiene clase AHORA
        Optional<SesionProgramada> sesionOpt = Optional.empty();
        for (GrupoEntrenamiento grupo : grupos) {
            sesionOpt = sesionService.buscarSesionActivaParaCheckIn(grupo);
            if (sesionOpt.isPresent()) break; // Encontramos una clase activa
        }

        if (sesionOpt.isEmpty()) {
            throw new RuntimeException("No hay sesiones activas o próximas para tus grupos en este momento.");
        }

        SesionProgramada sesion = sesionOpt.get();

        // 3. Validar si ya marcó asistencia
        if (repository.existsByJudokaAndSesion(judoka, sesion)) {
            throw new RuntimeException("¡Ya marcaste asistencia para esta clase!");
        }

        // 4. VALIDACIÓN GPS (La Matemática)
        if (sesion.getLatitud() != null && sesion.getLongitud() != null) {
            double distanciaMetros = calcularDistancia(latUser, lonUser, sesion.getLatitud(), sesion.getLongitud());
            int radioPermitido = sesion.getRadioPermitidoMetros() != null ? sesion.getRadioPermitidoMetros() : 100; // Default 100m

            if (distanciaMetros > radioPermitido) {
                throw new RuntimeException(String.format("Estás demasiado lejos del Dojo (%d m). Distancia máxima permitida: %d m.", (int) distanciaMetros, radioPermitido));
            }
        }

        // 5. ÉXITO! Guardar Asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setJudoka(judoka);
        asistencia.setSesion(sesion);
        asistencia.setPresente(true);
        asistencia.setFechaHoraMarcacion(LocalDateTime.now());
        asistencia.setLatitud(latUser);
        asistencia.setLongitud(lonUser);
        asistencia.setNotas("Check-in GPS Exitoso");

        // Reutilizamos el flujo de guardado, que ya contiene la llamada a Gamification
        registrarAsistencia(asistencia);
    }

    /**
     * Fórmula de Haversine para calcular distancia entre dos puntos en metros.
     */
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanciaKm = R * c;
        return distanciaKm * 1000; // Convertir a metros
    }
    public boolean estaRegistrada(Long sesionId, Long judokaId) {
        return repository.existsByJudokaIdAndSesionId(judokaId, sesionId);
    }
}