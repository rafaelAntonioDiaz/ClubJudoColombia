package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MetricaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CatalogoInicializer {

    private final PruebaEstandarRepository pruebaRepo;
    private final MetricaRepository metricaRepo;

    public CatalogoInicializer(PruebaEstandarRepository pruebaRepo,
                               MetricaRepository metricaRepo) {
        this.pruebaRepo = pruebaRepo;
        this.metricaRepo = metricaRepo;
    }

    @Transactional
    public void inicializar() {
        if (pruebaRepo.count() > 0) {
            System.out.println(">>> Catálogo de pruebas ya existe. Omitiendo.");
            return;
        }

        System.out.println(">>> Cargando catálogo de pruebas y métricas...");

        // 1. Crear métricas base (si no existen)
        Map<String, Metrica> metricas = new HashMap<>();
        // --- 1. LISTA COMPLETA DE MÉTRICAS ---
        List<String> metKeys = List.of(
                // Antropometría
                "metrica.masa_corporal.nombre", "kg",
                "metrica.estatura.nombre", "m",
                "metrica.perimetro_cintura.nombre", "cm",
                "metrica.cintura.nombre", "cm", // (Mantenemos por compatibilidad)
                "metrica.envergadura.nombre", "cm",
                "metrica.imc.nombre", "kg/m2",
                "metrica.whtr.nombre", "indice",

                // Suspensión y Fuerza
                "metrica.tiempo_isometrico.nombre", "seg",
                "metrica.repeticiones_dinamicas.nombre", "reps",
                "metrica.tiempo_iso_kg.nombre", "s/kg",
                "metrica.rep_dinamicas_kg.nombre", "rep/kg",

                // SJFT
                "metrica.sjft_proyecciones_total.nombre", "reps",
                "metrica.sjft_fc_final.nombre", "bpm",
                "metrica.sjft_fc_1min.nombre", "bpm",
                "metrica.sjft_indice.nombre", "index",

                // Otras Pruebas Físicas
                "metrica.distancia.nombre", "cm",
                "metrica.distancia_6min.nombre", "m",
                "metrica.velocidad_20m.nombre", "seg",
                "metrica.agilidad_4x4.nombre", "seg",
                "metrica.abdominales_1min.nombre", "reps",
                "metrica.lanzamiento_balon.nombre", "cm",
                "metrica.flexibilidad_sit_reach.nombre", "cm",
                "metrica.repeticiones_uchikomi.nombre", "reps"
        );

        // --- 2. MÉTRICAS OCULTAS (Las que calcula el sistema) ---
        Set<String> metricasCalculadas = Set.of(
                "metrica.tiempo_iso_kg.nombre",
                "metrica.rep_dinamicas_kg.nombre",
                "metrica.sjft_indice.nombre",
                "metrica.imc.nombre",
                "metrica.whtr.nombre" // Usamos tu llave whtr.nombre
        );

        for (int i = 0; i < metKeys.size(); i += 2) {
            String key = metKeys.get(i);
            String unit = metKeys.get(i + 1);

            Metrica m = metricaRepo.findByNombreKey(key).orElseGet(() -> {
                Metrica nueva = new Metrica(key, unit);

                // ¡LA MAGIA! Si la llave está en nuestro Set, nace oculta para el Sensei
                nueva.setEsCalculada(metricasCalculadas.contains(key));

                return metricaRepo.save(nueva);
            });
            metricas.put(key, m);
        }

        // 2. Crear pruebas (basadas en el script SQL)
        // Antropometría
        // --- PRUEBA ANTROPOMÉTRICA (Ahora sí, coincidencia 100% exacta) ---
        crearPrueba("ejercicio.medicion_antropo.nombre", // <-- ¡Vuelve a su nombre original!
                "objetivo.antropometria", "desc.antropometria", CategoriaEjercicio.MEDICION_ANTROPOMETRICA,
                Set.of(
                        metricas.get("metrica.masa_corporal.nombre"),
                        metricas.get("metrica.estatura.nombre"),
                        metricas.get("metrica.perimetro_cintura.nombre"),
                        metricas.get("metrica.imc.nombre"),
                        metricas.get("metrica.whtr.nombre")
                ),
                com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo.ANTROPOMETRIA
        );

        // Potencia / Fuerza Explosiva
        crearPrueba("ejercicio.salto_horizontal_proesp.nombre",
                "objetivo.potencia_piernas", "desc.salto_proesp", CategoriaEjercicio.POTENCIA,
                Set.of(metricas.get("metrica.distancia.nombre")));

        crearPrueba("ejercicio.lanzamiento_balon.nombre",
                "objetivo.potencia_superior", "desc.lanzamiento", CategoriaEjercicio.POTENCIA,
                Set.of(metricas.get("metrica.lanzamiento_balon.nombre")));

        // Resistencia Isométrica / Dinámica
        crearPrueba("ejercicio.suspension_barra.nombre",
                "objetivo.fuerza_agarre", "desc.suspension", CategoriaEjercicio.RESISTENCIA_ISOMETRICA, // o la categoría que uses
                Set.of(
                        metricas.get("metrica.tiempo_isometrico.nombre"),
                        metricas.get("metrica.repeticiones_dinamicas.nombre"),
                        metricas.get("metrica.tiempo_iso_kg.nombre"),
                        metricas.get("metrica.rep_dinamicas_kg.nombre"),
                        metricas.get("metrica.masa_corporal.nombre")
                ),
                com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo.SBCG // <-- NUEVO PARÁMETRO
        );

        crearPrueba("ejercicio.abdominales_1min.nombre",
                "objetivo.resistencia_abdominal", "desc.abs", CategoriaEjercicio.RESISTENCIA_MUSCULAR_LOCALIZADA,
                Set.of(metricas.get("metrica.abdominales_1min.nombre")));

        // Velocidad / Agilidad
        crearPrueba("ejercicio.carrera_20m.nombre",
                "objetivo.velocidad", "desc.20m", CategoriaEjercicio.VELOCIDAD,
                Set.of(metricas.get("metrica.velocidad_20m.nombre")));
        crearPrueba("ejercicio.agilidad_4x4.nombre",
                "objetivo.agilidad", "desc.4x4", CategoriaEjercicio.AGILIDAD,
                Set.of(metricas.get("metrica.agilidad_4x4.nombre")));

        // Resistencia Aeróbica / Específica
        crearPrueba("ejercicio.carrera_6min.nombre",
                "objetivo.aerobico", "desc.6min", CategoriaEjercicio.APTITUD_AEROBICA,
                Set.of(metricas.get("metrica.distancia_6min.nombre")));
        crearPrueba("ejercicio.uchikomi_test.nombre",
                "objetivo.especifico", "desc.uchikomi", CategoriaEjercicio.RESISTENCIA_DINAMICA,
                Set.of(metricas.get("metrica.repeticiones_uchikomi.nombre")));
        crearPrueba("ejercicio.sjft.nombre",
                "objetivo.anaerobico_lactico", "desc.sjft", CategoriaEjercicio.APTITUD_ANAEROBICA,
                Set.of(
                        metricas.get("metrica.sjft_indice.nombre"),
                        metricas.get("metrica.sjft_proyecciones_total.nombre"),
                        metricas.get("metrica.sjft_fc_final.nombre"),
                        metricas.get("metrica.sjft_fc_1min.nombre")
                ),
                com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo.SJFT
        );

        // Flexibilidad
        crearPrueba("ejercicio.sit_reach.nombre",
                "objetivo.flexibilidad", "desc.sit_reach", CategoriaEjercicio.FLEXIBILIDAD,
                Set.of(metricas.get("metrica.flexibilidad_sit_reach.nombre")));

        System.out.println(">>> Catálogo cargado exitosamente.");
    }

    // Método sobrecargado para pruebas complejas
    private PruebaEstandar crearPrueba(String key, String objetivoKey, String descKey,
                                       CategoriaEjercicio cat, Set<Metrica> metricas,
                                       com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo formula) {
        PruebaEstandar p = new PruebaEstandar();
        p.setNombreKey(key);
        p.setObjetivoKey(objetivoKey);
        p.setDescripcionKey(descKey);
        p.setCategoria(cat);
        p.setMetricas(metricas);
        p.setEsGlobal(true);
        p.setFormulaCalculo(formula); // ¡AQUÍ NACE LA PRUEBA CON SU MATEMÁTICA!
        return pruebaRepo.save(p);
    }

    // Método original (Fallback para pruebas simples)
    private PruebaEstandar crearPrueba(String key, String objetivoKey, String descKey,
                                       CategoriaEjercicio cat, Set<Metrica> metricas) {
        return crearPrueba(key, objetivoKey, descKey, cat, metricas, com.RafaelDiaz.ClubJudoColombia.modelo.enums.FormulaCalculo.NINGUNA);
    }

}