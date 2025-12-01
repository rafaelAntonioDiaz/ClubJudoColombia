package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Comentario;
import com.RafaelDiaz.ClubJudoColombia.modelo.Publicacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ComentarioRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository repository;
    private ComentarioRepository comentarioRepository;

    public PublicacionService(PublicacionRepository repository,
                              ComentarioRepository comentarioRepository) {
        this.repository = repository;
        this.comentarioRepository = comentarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Publicacion> obtenerTodas() {
        return repository.findAllByOrderByFechaDesc();
    }

    @Transactional
    public Publicacion guardar(Publicacion publicacion) {
        return repository.save(publicacion);
    }

    @Transactional
    public void darLike(Publicacion publicacion) {
        publicacion.setLikes(publicacion.getLikes() + 1);
        repository.save(publicacion);
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