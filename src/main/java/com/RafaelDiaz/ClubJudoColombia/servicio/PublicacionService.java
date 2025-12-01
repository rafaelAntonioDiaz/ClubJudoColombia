package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Publicacion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository repository;

    public PublicacionService(PublicacionRepository repository) {
        this.repository = repository;
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
}