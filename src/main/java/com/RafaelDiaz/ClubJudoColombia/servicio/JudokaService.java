package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Reflexion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
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
    // --- CAMBIO 1: Inyectamos el servicio de la Nube ---
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;
    private final GamificationService gamificationService;
    private final ReflexionRepository reflexionRepository;

    public JudokaService(AlmacenamientoCloudService almacenamientoCloudService,
                         JudokaRepository judokaRepository,
                         GamificationService gamificationService,
                         ReflexionRepository reflexionRepository) {
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;
        this.gamificationService = gamificationService;
        this.reflexionRepository = reflexionRepository;
    }

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

    @Transactional
    public Judoka ascenderGrado(Judoka judoka, GradoCinturon nuevoGrado) {
        judoka.setGrado(nuevoGrado);
        Judoka guardado = judokaRepository.save(judoka);
        gamificationService.verificarLogrosGrado(guardado);
        return guardado;
    }

    @Transactional
    public Judoka save(Judoka judoka) {
        return judokaRepository.save(judoka);
    }

    public List<Judoka> findAllJudokas() {
        return judokaRepository.findAll();
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
            throw new RuntimeException("El tiempo de edición (24h) ha expirado. Esta entrada ya es permanente.");
        }
        reflexion.setContenido(nuevoContenido);
        reflexion.setFechaUltimaEdicion(LocalDateTime.now());
        reflexionRepository.save(reflexion);
    }

    // --- CAMBIO 2: Método refactorizado para enviar la foto a la Nube ---
    @Transactional
    public void actualizarFotoPerfil(Judoka judoka, InputStream inputStream, String filename) {
        try {
            // 1. Enviar a Cloudflare R2 (Usamos -1L para el Streaming puro)
            String nombreFinalGuardado = almacenamientoCloudService.subirArchivo(
                    judoka.getId(),
                    filename,
                    inputStream
            );

            // 2. Obtener la URL pública de la nube
            String urlEnLaNube = almacenamientoCloudService.obtenerUrl(judoka.getId(), nombreFinalGuardado);

            // 3. Actualizar referencia en BD
            judoka.setUrlFotoPerfil(urlEnLaNube);
            judokaRepository.save(judoka);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la foto de perfil en la nube: " + e.getMessage());
        }
    }
}