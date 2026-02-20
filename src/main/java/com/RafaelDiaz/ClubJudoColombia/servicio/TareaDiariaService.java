package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class TareaDiariaService {

    private final TareaDiariaRepository repository;

    public TareaDiariaService(TareaDiariaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TareaDiaria guardarTarea(TareaDiaria tarea, Sensei sensei) {
        tarea.setSenseiCreador(sensei);
        return repository.save(tarea);
    }

    public List<TareaDiaria> findAll() {
        return repository.findAll();
    }

    public long count() {
        return repository.count();
    }

    @Transactional
    public void eliminarTarea(Long id) {
        repository.deleteById(id);
    }

    // En TareaDiariaService.java
    public List<TareaDiaria> findByCategoriaIn(List<CategoriaEjercicio> categorias) {
        return repository.findByCategoriaIn(categorias);
    }

    public long countByCategoriaIn(List<CategoriaEjercicio> categorias) {
        return repository.countByCategoriaIn(categorias);
    }
}