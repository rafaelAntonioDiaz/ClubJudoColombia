package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Macrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MacrocicloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MacrocicloService {

    private final MacrocicloRepository repository;

    public MacrocicloService(MacrocicloRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Macrociclo> obtenerHistorialDelSensei(Sensei sensei) {
        List<Macrociclo> macrociclos = repository.findBySenseiOrderByFechaInicioDesc(sensei);

        // Despertamos los microciclos internos para poder dibujar la línea de tiempo en Vaadin
        for (Macrociclo macro : macrociclos) {
            macro.getMicrociclos().size();
        }

        return macrociclos;
    }

    @Transactional
    public Macrociclo guardarMacrociclo(Macrociclo macrociclo) {
        return repository.save(macrociclo);
    }

    @Transactional
    public void eliminarMacrociclo(Long id) {
        repository.deleteById(id);
    }
}