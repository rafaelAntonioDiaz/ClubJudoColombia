package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final JudokaInsigniaRepository logroRepo;
    private final InsigniaRepository insigniaRepo;
    private final AsistenciaRepository asistenciaRepo; // <--- NUEVO REPO INYECTADO

    public GamificationService(JudokaInsigniaRepository logroRepo,
                               InsigniaRepository insigniaRepo,
                               AsistenciaRepository asistenciaRepo) {
        this.logroRepo = logroRepo;
        this.insigniaRepo = insigniaRepo;
        this.asistenciaRepo = asistenciaRepo;
    }

    // --- 1. LÓGICA TAI (Cuerpo) - Ya la tenías ---
    @Transactional
    public void verificarLogrosFisicos(Judoka judoka, ResultadoPrueba resultado) {
        String claveMetrica = resultado.getMetrica().getNombreKey();
        Double valor = resultado.getValor();

        if (claveMetrica.contains("abdominales") || claveMetrica.contains("flexiones")) {
            if (valor >= 40.0) desbloquear(judoka, "TAI_HERCULES");
        }
        if (claveMetrica.contains("velocidad_20m") && valor <= 3.5) {
            desbloquear(judoka, "TAI_VELOCIDAD");
        }
        if (claveMetrica.contains("sjft") && valor <= 12.0) {
            desbloquear(judoka, "TAI_RESISTENCIA");
        }
    }

    // --- 2. LÓGICA SHIN (Mente/Constancia) - ¡NUEVO! ---
    @Transactional
    public void verificarLogrosAsistencia(Judoka judoka) {
        long totalClases = asistenciaRepo.countByJudoka(judoka);

        // Primer Paso (1 clase)
        if (totalClases >= 1) desbloquear(judoka, "SHIN_INICIO");

        // Espíritu Indomable (10 clases)
        // Nota: Idealmente verificaríamos "consecutivas", pero por ahora usamos total
        if (totalClases >= 10) desbloquear(judoka, "SHIN_CONSTANCIA");

        // Guardián del Dojo (50 clases)
        if (totalClases >= 50) desbloquear(judoka, "SHIN_COMPROMISO");
    }

    // --- 3. LÓGICA GI (Técnica/Grados) - ¡NUEVO! ---
    @Transactional
    public void verificarLogrosGrado(Judoka judoka) {
        // Si no es cinturón blanco (Rokkyu), asumimos que ascendió
        if (judoka.getGrado() != GradoCinturon.BLANCO) {
            desbloquear(judoka, "GI_CINTURON");
        }
        // Aquí podrías agregar más lógica (ej: Cinturón Negro desbloquea "Maestro")
    }

    // --- HELPER PRIVADO ---
    private void desbloquear(Judoka judoka, String claveInsignia) {
        if (!logroRepo.existsByJudokaAndInsignia_Clave(judoka, claveInsignia)) {
            insigniaRepo.findByClave(claveInsignia).ifPresent(insignia -> {
                JudokaInsignia logro = new JudokaInsignia();
                logro.setJudoka(judoka);
                logro.setInsignia(insignia);
                logroRepo.save(logro);
                System.out.println(">>> ¡GAMIFICATION! " + judoka.getUsuario().getNombre() + " ganó " + claveInsignia);
            });
        }
    }
}