package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SabiduriaService {

    private final TraduccionRepository traduccionRepository;

    @Autowired
    public SabiduriaService(TraduccionRepository traduccionRepository) {
        this.traduccionRepository = traduccionRepository;
    }

    /**
     * Obtiene una frase de sabiduría que cambia cada semana.
     * Lógica:
     * - Random > 0.6 (40%): Estrategia de Combate (Sun Tzu / Musashi)
     * - Random <= 0.6 (60%): Principios y Conducta (Kano)
     */
    public Traduccion obtenerFraseSemanal(String idioma) {
        // 1. Semilla Determinista: Año + Número de Semana (Ej: 202345)
        // Esto asegura que la frase sea la misma para todos durante la semana.
        int year = LocalDate.now().getYear();
        int week = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        long seed = Long.parseLong(year + "" + week);

        Random random = new Random(seed);
        double probabilidad = random.nextDouble(); // 0.0 a 1.0

        List<Traduccion> poolFrases = new ArrayList<>();

        if (probabilidad > 0.6) {
            // ESTRATEGIA Y GUERRA (Sun Tzu, Musashi)
            poolFrases.addAll(traduccionRepository.findByClaveStartingWithAndIdioma("sabiduria.suntzu", idioma));
            poolFrases.addAll(traduccionRepository.findByClaveStartingWithAndIdioma("sabiduria.musashi", idioma));
        } else {
            // PRINCIPIOS Y FUNDAMENTOS (Kano)
            poolFrases.addAll(traduccionRepository.findByClaveStartingWithAndIdioma("sabiduria.kano", idioma));
        }

        // Fallback: Si no hay frases cargadas, evitar error
        if (poolFrases.isEmpty()) {
            return null;
        }

        // Selección final dentro del pool elegido
        int index = random.nextInt(poolFrases.size());
        return poolFrases.get(index);
    }
}