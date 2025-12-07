package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon; // Importar
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JudokaService {

    private final JudokaRepository judokaRepository;
    private final GamificationService gamificationService; // <--- INYECCIÓN

    public JudokaService(JudokaRepository judokaRepository, GamificationService gamificationService) {
        this.judokaRepository = judokaRepository;
        this.gamificationService = gamificationService;
    }

    // ... (findByUsuario y findAllJudokasWithUsuario se quedan igual) ...
    @Transactional(readOnly = true)
    public Optional<Judoka> findByUsuario(Usuario usuario) {
        return judokaRepository.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<Judoka> findAllJudokasWithUsuario() {
        List<Judoka> judokas = judokaRepository.findAll();
        judokas.forEach(judoka -> judoka.getUsuario().getNombre());
        return judokas;
    }

    // --- NUEVO MÉTODO PARA ASCENDER (CON SENSOR GI) ---
    @Transactional
    public Judoka ascenderGrado(Judoka judoka, GradoCinturon nuevoGrado) {
        judoka.setGrado(nuevoGrado);
        Judoka guardado = judokaRepository.save(judoka);

        // --- SENSOR DE GAMIFICACIÓN (GI) ---
        gamificationService.verificarLogrosGrado(guardado);

        return guardado;
    }

    // Método genérico guardar (si lo usas en otros lados)
    @Transactional
    public Judoka save(Judoka judoka) {
        return judokaRepository.save(judoka);
    }
}