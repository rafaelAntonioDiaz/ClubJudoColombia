package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Reflexion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon; // Importar
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ReflexionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JudokaService {
    private final FileStorageService fileStorageService;
    private final JudokaRepository judokaRepository;
    private final GamificationService gamificationService; // <--- INYECCIÓN
    private final ReflexionRepository reflexionRepository;
    public JudokaService(FileStorageService fileStorageService, JudokaRepository judokaRepository,
                         GamificationService gamificationService,
                         ReflexionRepository reflexionRepository) {
        this.fileStorageService = fileStorageService;
        this.judokaRepository = judokaRepository;
        this.gamificationService = gamificationService;
        this.reflexionRepository = reflexionRepository;
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
    public List<Reflexion> obtenerHistorialReflexiones(Judoka judoka) {
        return reflexionRepository.findByJudokaOrderByFechaCreacionDesc(judoka);
    }

    @Transactional
    public void crearReflexion(Judoka judoka, String contenido) {
        Reflexion nueva = new Reflexion(judoka, contenido);
        reflexionRepository.save(nueva);
    }

    @Transactional
    public void editarReflexion(Reflexion reflexion, String nuevoContenido) {
        if (!reflexion.esEditable()) {
            throw new
                    RuntimeException("El tiempo de edición (24h) ha expirado. " +
                    "Esta entrada ya es permanente.");
        }
        reflexion.setContenido(nuevoContenido);
        reflexion.setFechaUltimaEdicion(LocalDateTime.now());
        reflexionRepository.save(reflexion);
    }
    @Transactional
    public void actualizarFotoPerfil(Judoka judoka,
             InputStream inputStream, String filename) {
        try {
            // 1. Guardar archivo físico (reutilizando tu lógica existente)
            String rutaGuardada = fileStorageService.save(inputStream, filename);

            // 2. Actualizar referencia en BD
            judoka.setUrlFotoPerfil(rutaGuardada);
            judokaRepository.save(judoka);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la foto de perfil: " + e.getMessage());
        }
    }
}
