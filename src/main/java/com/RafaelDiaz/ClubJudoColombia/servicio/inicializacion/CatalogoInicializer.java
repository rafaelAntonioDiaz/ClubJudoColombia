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
        List<String> metKeys = List.of(
                "metrica.masa_corporal.nombre", "kg",
                "metrica.estatura.nombre", "m",
                "metrica.imc.nombre", "kg/m2",
                "metrica.whtr.nombre", "ratio",
                "metrica.envergadura.nombre", "cm",
                "metrica.cintura.nombre", "cm",
                "metrica.distancia.nombre", "cm",
                "metrica.distancia_6min.nombre", "m",
                "metrica.tiempo_isometrico.nombre", "seg",
                "metrica.repeticiones_dinamicas.nombre", "reps",
                "metrica.tiempo_iso_kg.nombre", "s/kg",
                "metrica.rep_dinamicas_kg.nombre", "rep/kg",
                "metrica.velocidad_20m.nombre", "seg",
                "metrica.agilidad_4x4.nombre", "seg",
                "metrica.abdominales_1min.nombre", "reps",
                "metrica.lanzamiento_balon.nombre", "cm",
                "metrica.flexibilidad_sit_reach.nombre", "cm",
                "metrica.repeticiones_uchikomi.nombre", "reps",
                "metrica.sjft_proyecciones_total.nombre", "reps",
                "metrica.sjft_fc_final.nombre", "bpm",
                "metrica.sjft_fc_1min.nombre", "bpm",
                "metrica.sjft_indice.nombre", "index"
        );
        for (int i = 0; i < metKeys.size(); i += 2) {
            String key = metKeys.get(i);
            String unidad = metKeys.get(i + 1);
            metricas.put(key, metricaRepo.findByNombreKey(key).orElseGet(() -> {
                Metrica m = new Metrica();
                m.setNombreKey(key);
                m.setUnidad(unidad);
                return metricaRepo.save(m);
            }));
        }

        // 2. Crear pruebas (basadas en el script SQL)
        // Antropometría
        PruebaEstandar antropo = crearPrueba("ejercicio.medicion_antropo.nombre",
                "objetivo.composicion", "desc.antropo", CategoriaEjercicio.MEDICION_ANTROPOMETRICA,
                Set.of(metricas.get("metrica.masa_corporal.nombre"),
                        metricas.get("metrica.estatura.nombre"),
                        metricas.get("metrica.envergadura.nombre"),
                        metricas.get("metrica.whtr.nombre")));

        // Potencia / Fuerza Explosiva
        crearPrueba("ejercicio.salto_horizontal.nombre",
                "objetivo.potencia_piernas", "desc.salto_cbj", CategoriaEjercicio.POTENCIA,
                Set.of(metricas.get("metrica.distancia.nombre")));
        crearPrueba("ejercicio.salto_horizontal_proesp.nombre",
                "objetivo.potencia_piernas", "desc.salto_proesp", CategoriaEjercicio.POTENCIA,
                Set.of(metricas.get("metrica.distancia.nombre")));
        crearPrueba("ejercicio.lanzamiento_balon.nombre",
                "objetivo.potencia_superior", "desc.lanzamiento", CategoriaEjercicio.POTENCIA,
                Set.of(metricas.get("metrica.lanzamiento_balon.nombre")));

        // Resistencia Isométrica / Dinámica
        crearPrueba("ejercicio.suspension_barra.nombre",
                "objetivo.fuerza_agarre", "desc.suspension", CategoriaEjercicio.RESISTENCIA_ISOMETRICA,
                Set.of(metricas.get("metrica.tiempo_isometrico.nombre"),
                        metricas.get("metrica.repeticiones_dinamicas.nombre"),
                        metricas.get("metrica.tiempo_iso_kg.nombre"),
                        metricas.get("metrica.rep_dinamicas_kg.nombre")));

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
                Set.of(metricas.get("metrica.sjft_indice.nombre"),
                        metricas.get("metrica.sjft_proyecciones_total.nombre"),
                        metricas.get("metrica.sjft_fc_final.nombre"),
                        metricas.get("metrica.sjft_fc_1min.nombre")));

        // Flexibilidad
        crearPrueba("ejercicio.sit_reach.nombre",
                "objetivo.flexibilidad", "desc.sit_reach", CategoriaEjercicio.FLEXIBILIDAD,
                Set.of(metricas.get("metrica.flexibilidad_sit_reach.nombre")));

        System.out.println(">>> Catálogo cargado exitosamente.");
    }

    private PruebaEstandar crearPrueba(String key, String objetivoKey, String descKey,
                                       CategoriaEjercicio cat, Set<Metrica> metricas) {
        PruebaEstandar p = new PruebaEstandar();
        p.setNombreKey(key);
        p.setObjetivoKey(objetivoKey);
        p.setDescripcionKey(descKey);
        p.setCategoria(cat);
        p.setMetricas(metricas);
        p.setEsGlobal(true);
        return pruebaRepo.save(p);
    }
}