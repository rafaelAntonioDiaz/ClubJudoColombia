package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaInsignia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsigniaInicializer {

    private final InsigniaRepository insigniaRepo;

    public InsigniaInicializer(InsigniaRepository insigniaRepo) {
        this.insigniaRepo = insigniaRepo;
    }

    @Transactional
    public void inicializar() {
        if (insigniaRepo.count() > 0) {
            System.out.println(">>> Catálogo de insignias ya existe. Omitiendo.");
            return;
        }

        System.out.println(">>> Cargando insignias de gamificación...");
        List<Insignia> catalogo = List.of(
                crearInsignia("SHIN_INICIO", "Primer Paso", "Tu primer entrenamiento.",
                        "HANDSHAKE", CategoriaInsignia.SHIN, 1),
                crearInsignia("SHIN_CONSTANCIA", "Guerrero Constante", "10 entrenamientos completados.",
                        "FIRE", CategoriaInsignia.SHIN, 5),
                crearInsignia("SHIN_COMPROMISO", "Guardián del Dojo", "50 clases.",
                        "SHIELD", CategoriaInsignia.SHIN, 10),
                crearInsignia("GI_CINTURON", "Nuevo Horizonte", "Ascenso de grado.",
                        "DIPLOMA", CategoriaInsignia.GI, 1),
                crearInsignia("GI_IPON", "Maestro del Ippon", "Ejecución perfecta en combate.",
                        "DIAMOND", CategoriaInsignia.GI, 5),
                crearInsignia("TAI_HERCULES", "Fuerza de Hércules", "Superaste las métricas de fuerza.",
                        "BARBELL", CategoriaInsignia.TAI, 3),
                crearInsignia("TAI_VELOCIDAD", "Rayo Veloz", "Velocidad élite.",
                        "FLASH", CategoriaInsignia.TAI, 3),
                crearInsignia("COMP_ORO", "Espíritu Dorado", "Ganador de medalla de ORO.",
                        "TROPHY", CategoriaInsignia.SHIN, 10)
        );
        insigniaRepo.saveAll(catalogo);
    }

    private Insignia crearInsignia(String clave, String nombre, String desc, String icono,
                                   CategoriaInsignia cat, int nivel) {
        Insignia i = new Insignia();
        i.setClave(clave);
        i.setNombre(nombre);
        i.setDescripcion(desc);
        i.setIconoVaadin(icono);
        i.setCategoria(cat);
        i.setNivelRequerido(nivel);
        return i;
    }
}