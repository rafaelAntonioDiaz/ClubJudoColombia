package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final JudokaInsigniaRepository logroRepo;
    private final InsigniaRepository insigniaRepo;

    public GamificationService(JudokaInsigniaRepository logroRepo, InsigniaRepository insigniaRepo) {
        this.logroRepo = logroRepo;
        this.insigniaRepo = insigniaRepo;
    }

    @Transactional
    public void verificarLogrosFisicos(Judoka judoka, ResultadoPrueba resultado) {
        String claveMetrica = resultado.getMetrica().getNombreKey();
        Double valor = resultado.getValor();

        // 1. TAI_HERCULES (Fuerza: Flexiones o Abdominales > 40)
        if (claveMetrica.contains("abdominales") || claveMetrica.contains("flexiones")) {
            if (valor >= 40.0) desbloquear(judoka, "TAI_HERCULES");
        }

        // 2. TAI_VELOCIDAD (Velocidad: 20m < 3.5s - Menos es mejor)
        if (claveMetrica.contains("velocidad_20m") && valor <= 3.5) {
            desbloquear(judoka, "TAI_VELOCIDAD");
        }

        // 3. TAI_RESISTENCIA (SJFT Indice < 12 - Menos es mejor)
        if (claveMetrica.contains("sjft") && valor <= 12.0) {
            desbloquear(judoka, "TAI_RESISTENCIA");
        }
    }

    private void desbloquear(Judoka judoka, String claveInsignia) {
        if (!logroRepo.existsByJudokaAndInsignia_Clave(judoka, claveInsignia)) {
            insigniaRepo.findByClave(claveInsignia).ifPresent(insignia -> {
                JudokaInsignia logro = new JudokaInsignia();
                logro.setJudoka(judoka);
                logro.setInsignia(insignia);
                logroRepo.save(logro);
            });
        }
    }
}