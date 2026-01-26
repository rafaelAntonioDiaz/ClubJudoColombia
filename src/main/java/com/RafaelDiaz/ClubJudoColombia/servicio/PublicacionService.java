package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Comentario;
import com.RafaelDiaz.ClubJudoColombia.modelo.Publicacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ComentarioRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PublicacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final SenseiRepository senseiRepository; // <-- FALTABA

    @Autowired
    public PublicacionService(PublicacionRepository publicacionRepository,
                              ComentarioRepository comentarioRepository,
                              SenseiRepository senseiRepository) {
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.senseiRepository = senseiRepository;
    }
    public List<Publicacion> obtenerMuroDelDojo(Long dojoId) {
        return publicacionRepository.findBySenseiIdOrderByFechaDesc(dojoId);
    }

    // NUEVO MÉTODO DE GUARDADO
    public Publicacion crearPublicacion(Publicacion nueva, Long dojoId) {
        Sensei dojo = senseiRepository.findById(dojoId).orElseThrow();
        nueva.setSensei(dojo); // ¡El amarre de seguridad!
        nueva.setFecha(LocalDateTime.now());
        return publicacionRepository.save(nueva);
    }

    @Transactional
    public Publicacion guardar(Publicacion publicacion) {
        return publicacionRepository.save(publicacion);
    }

    @Transactional
    public void darLike(Publicacion publicacion) {
        publicacion.setLikes(publicacion.getLikes() + 1);
        publicacionRepository.save(publicacion);
    }
    @Transactional(readOnly = true)
    public List<Comentario> obtenerComentarios(Publicacion publicacion) {
        return comentarioRepository.findByPublicacionOrderByFechaAsc(publicacion);
    }

    @Transactional
    public Comentario comentar(Publicacion publicacion, Usuario autor, String texto) {
        Comentario comentario = new Comentario(publicacion, autor, texto);
        return comentarioRepository.save(comentario);
    }
}