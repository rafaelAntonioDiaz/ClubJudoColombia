package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PruebaEstandarService {

    private final PruebaEstandarRepository repository;

    public PruebaEstandarService(PruebaEstandarRepository repository) {
        this.repository = repository;
    }

    public List<PruebaEstandar> findPruebasVisiblesParaSensei(Sensei sensei) {
        return repository.findGlobalesYDelSensei(sensei);
    }

    public List<PruebaEstandar> findPruebasVisiblesPorCategoria(List<CategoriaEjercicio> categorias, Sensei sensei) {
        return repository.findByCategoriaInAndGlobalOrSensei(categorias, sensei);
    }

    public PruebaEstandar guardarPruebaDeAutor(PruebaEstandar prueba, Sensei sensei) {
        prueba.setEsGlobal(false);
        prueba.setSenseiCreador(sensei);
        return repository.save(prueba);
    }
}