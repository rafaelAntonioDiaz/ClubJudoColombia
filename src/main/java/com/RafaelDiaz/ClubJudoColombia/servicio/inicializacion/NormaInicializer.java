package com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class NormaInicializer {

    private final NormaEvaluacionRepository normaRepo;
    private final PruebaEstandarRepository pruebaRepo;
    private final MetricaRepository metricaRepo;

    public NormaInicializer(NormaEvaluacionRepository normaRepo,
                            PruebaEstandarRepository pruebaRepo,
                            MetricaRepository metricaRepo) {
        this.normaRepo = normaRepo;
        this.pruebaRepo = pruebaRepo;
        this.metricaRepo = metricaRepo;
    }

    @Transactional
    public void inicializar() {
        if (normaRepo.count() > 0) {
            System.out.println(">>> Normas de evaluación ya existen. Omitiendo.");
            return;
        }

        System.out.println(">>> Cargando normas de evaluación (CBJ y PROESP)...");
        cargarNormasPROESP_Salud();
        cargarNormasPROESP_Rendimiento();
        cargarNormasCBJ_Suspension();
        cargarNormasCBJ_Uchikomi();
        cargarNormasCBJ_SJFT();

    }

    private void cargarNormasCBJ_Suspension() {
        PruebaEstandar suspension = pruebaRepo.findByNombreKey("ejercicio.suspension_barra.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: suspension_barra"));

        Metrica tiempoIso = metricaRepo.findByNombreKey("metrica.tiempo_isometrico.nombre").orElseThrow();
        Metrica repsDin = metricaRepo.findByNombreKey("metrica.repeticiones_dinamicas.nombre").orElseThrow();
        Metrica tiempoIsoKg = metricaRepo.findByNombreKey("metrica.tiempo_iso_kg.nombre").orElseThrow();
        Metrica repsDinKg = metricaRepo.findByNombreKey("metrica.rep_dinamicas_kg.nombre").orElseThrow();

        // --- MASCULINO Sub-18 ---
        // Tiempo isométrico (segundos)
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 90.0, null);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 70.0, 89.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 41.0, 69.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 8.0, 40.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 7.0);

        // Repeticiones dinámicas
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 32.0, null);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 26.0, 31.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 14.0, 25.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 3.0, 13.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 2.0);

        // Tiempo isométrico relativo (s.kg)
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 5857.0, null);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 4507.0, 5856.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 2745.0, 4506.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 627.0, 2744.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 626.0);

        // Repeticiones dinámicas relativas (rep.kg)
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 2245.0, null);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 1738.0, 2244.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 785.0, 1737.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 227.0, 784.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 226.0);

        // --- MASCULINO Sub-21 (0-20) ---
        // Tiempo isométrico
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 76.0, null);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 66.0, 75.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 35.0, 65.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 7.0, 34.0);
        crearNorma(suspension, tiempoIso, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 6.0);

        // Repeticiones dinámicas
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 31.0, null);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 29.0, 30.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 16.0, 28.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 4.0, 15.0);
        crearNorma(suspension, repsDin, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 3.0);

        // Tiempo isométrico relativo
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 5714.0, null);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 4733.0, 5713.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 3159.0, 4732.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 823.0, 3158.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 822.0);

        // Repeticiones dinámicas relativas
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 2367.0, null);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 2027.0, 2366.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 1159.0, 2026.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 412.0, 1158.0);
        crearNorma(suspension, repsDinKg, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 411.0);

        // --- FEMENINO Sub-18 ---
        // Tiempo isométrico
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 75.0, null);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 56.0, 74.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 27.0, 55.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 13.0, 26.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 12.0);

        // Repeticiones dinámicas
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 23.0, null);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 17.0, 22.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 6.0, 16.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 2.0, 5.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 1.0);

        // Tiempo isométrico relativo
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 3406.0, null);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 2933.0, 3405.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 1515.0, 2932.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 555.0, 1514.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 554.0);

        // Repeticiones dinámicas relativas
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 1143.0, null);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 800.0, 1142.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 351.0, 799.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 145.0, 350.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 144.0);

        // --- FEMENINO Sub-21 (0-20) ---
        // Tiempo isométrico
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 72.0, null);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 58.0, 71.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 18.0, 57.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 3.0, 17.0);
        crearNorma(suspension, tiempoIso, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 2.0);

        // Repeticiones dinámicas
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 24.0, null);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 21.0, 23.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 4.0, 20.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 1.0, 3.0);
        crearNorma(suspension, repsDin, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 0.0);

        // Tiempo isométrico relativo
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 3934.0, null);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 3217.0, 3933.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 1233.0, 3216.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 244.0, 1232.0);
        crearNorma(suspension, tiempoIsoKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 243.0);

        // Repeticiones dinámicas relativas
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 1297.0, null);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 1057.0, 1296.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 307.0, 1056.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 59.0, 306.0);
        crearNorma(suspension, repsDinKg, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 58.0);
    }
    private void cargarNormasCBJ_Uchikomi() {
        PruebaEstandar uchikomi = pruebaRepo.findByNombreKey("ejercicio.uchikomi_test.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: uchikomi_test"));
        Metrica repsUchi = metricaRepo.findByNombreKey("metrica.repeticiones_uchikomi.nombre").orElseThrow();

        // Masculino Sub-18
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 71.0, null);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 64.0, 70.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 50.0, 63.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 47.0, 49.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 46.0);

        // Masculino Sub-21
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 77.0, null);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 67.0, 76.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 53.0, 66.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 41.0, 52.0);
        crearNorma(uchikomi, repsUchi, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 40.0);

        // Femenino Sub-18
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 66.0, null);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 59.0, 65.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 48.0, 58.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 44.0, 47.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 43.0);

        // Femenino Sub-21
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 68.0, null);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 64.0, 67.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 52.0, 63.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 44.0, 51.0);
        crearNorma(uchikomi, repsUchi, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 43.0);
    }
    private void cargarNormasCBJ_SJFT() {
        PruebaEstandar sjft = pruebaRepo.findByNombreKey("ejercicio.sjft.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: sjft"));
        Metrica proyecciones = metricaRepo.findByNombreKey("metrica.sjft_proyecciones_total.nombre").orElseThrow();
        Metrica fcFinal = metricaRepo.findByNombreKey("metrica.sjft_fc_final.nombre").orElseThrow();
        Metrica fc1min = metricaRepo.findByNombreKey("metrica.sjft_fc_1min.nombre").orElseThrow();
        Metrica indice = metricaRepo.findByNombreKey("metrica.sjft_indice.nombre").orElseThrow();

        // --- MASCULINO Sub-18 ---
        // Proyecciones totales
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 30.0, null);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 28.0, 29.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 25.0, 27.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 23.0, 24.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 22.0);

        // FC Final (bpm) - menor es mejor, así que el excelente es el valor más bajo
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 163.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 164.0, 174.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 175.0, 195.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 196.0, 200.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 201.0, null);

        // FC 1 min después
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 132.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 133.0, 148.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 149.0, 175.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 176.0, 184.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 185.0, null);

        // Índice SJFT (menor es mejor)
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 11.15);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.BUENO, 11.16, 12.38);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.REGULAR, 12.39, 14.32);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.DEBIL, 14.33, 15.92);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 15.93, null);

        // --- MASCULINO Sub-21 ---
        // Proyecciones totales
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 31.0, null);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 30.0, 30.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 26.0, 29.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 23.0, 25.0);
        crearNorma(sjft, proyecciones, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 22.0);

        // FC Final
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 162.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 163.0, 174.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 175.0, 188.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 189.0, 198.0);
        crearNorma(sjft, fcFinal, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 199.0, null);

        // FC 1 min
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 127.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 128.0, 144.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 145.0, 168.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 169.0, 184.0);
        crearNorma(sjft, fc1min, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 185.0, null);

        // Índice
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 10.40);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.BUENO, 10.41, 11.29);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.REGULAR, 11.30, 13.52);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.DEBIL, 13.53, 14.18);
        crearNorma(sjft, indice, Sexo.MASCULINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 14.19, null);

        // --- FEMENINO Sub-18 ---
        // Proyecciones
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, 28.0, null);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 27.0, 27.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 23.0, 26.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 21.0, 22.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, null, 20.0);

        // FC Final
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 168.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 169.0, 176.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 177.0, 193.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 194.0, 202.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 203.0, null);

        // FC 1 min
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 132.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 133.0, 148.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 149.0, 176.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 177.0, 189.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 190.0, null);

        // Índice
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.EXCELENTE, null, 11.53);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.BUENO, 11.54, 12.63);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.REGULAR, 12.64, 15.45);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.DEBIL, 15.46, 18.00);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 17, ClasificacionRendimiento.MUY_DEBIL, 18.01, null);

        // --- FEMENINO Sub-21 ---
        // Proyecciones
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, 30.0, null);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 28.0, 29.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 25.0, 27.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 22.0, 24.0);
        crearNorma(sjft, proyecciones, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, null, 21.0);

        // FC Final
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 168.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 169.0, 179.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 180.0, 190.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 191.0, 196.0);
        crearNorma(sjft, fcFinal, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 197.0, null);

        // FC 1 min
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 148.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 149.0, 157.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 158.0, 176.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 177.0, 180.0);
        crearNorma(sjft, fc1min, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 181.0, null);

        // Índice
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.EXCELENTE, null, 11.48);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.BUENO, 11.49, 12.00);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.REGULAR, 12.01, 14.70);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.DEBIL, 14.71, 17.45);
        crearNorma(sjft, indice, Sexo.FEMENINO, 0, 20, ClasificacionRendimiento.MUY_DEBIL, 17.46, null);
    }

    private void cargarNormasPROESP_Salud() {
        PruebaEstandar antropo = pruebaRepo.findByNombreKey("ejercicio.medicion_antropo.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: medicion_antropo"));
        PruebaEstandar carrera6min = pruebaRepo.findByNombreKey("ejercicio.carrera_6min.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: carrera_6min"));
        PruebaEstandar abdominales = pruebaRepo.findByNombreKey("ejercicio.abdominales_1min.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: abdominales_1min"));
        PruebaEstandar lanzamiento = pruebaRepo.findByNombreKey("ejercicio.lanzamiento_balon.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: lanzamiento_balon"));
        PruebaEstandar carrera20m = pruebaRepo.findByNombreKey("ejercicio.carrera_20m.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: carrera_20m"));

        Metrica imc = metricaRepo.findByNombreKey("metrica.imc.nombre").orElseThrow();
        Metrica whtr = metricaRepo.findByNombreKey("metrica.whtr.nombre").orElseThrow();
        Metrica distancia6min = metricaRepo.findByNombreKey("metrica.distancia_6min.nombre").orElseThrow();
        Metrica repsAbs = metricaRepo.findByNombreKey("metrica.abdominales_1min.nombre").orElseThrow();
        Metrica distanciaLanz = metricaRepo.findByNombreKey("metrica.lanzamiento_balon.nombre").orElseThrow();
        Metrica tiempo20m = metricaRepo.findByNombreKey("metrica.velocidad_20m.nombre").orElseThrow();

        // ========== IMC ==========
        Map<Integer, Double> riesgoIMC_M = Map.ofEntries(
                Map.entry(6, 17.7), Map.entry(7, 17.8), Map.entry(8, 19.2),
                Map.entry(9, 19.3), Map.entry(10, 20.7), Map.entry(11, 22.1),
                Map.entry(12, 22.2), Map.entry(13, 22.0), Map.entry(14, 22.2),
                Map.entry(15, 23.0), Map.entry(16, 24.0), Map.entry(17, 25.4)
        );
        for (Map.Entry<Integer, Double> e : riesgoIMC_M.entrySet()) {
            // 1. Si es MAYOR o igual al umbral -> ZONA_DE_RIESGO (valorMin = umbral)
            crearNorma(antropo, imc, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, e.getValue(), null);

            // 2. Si es MENOR al umbral -> BUENO / SALUDABLE (valorMax = umbral - 0.01)
            crearNorma(antropo, imc, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.BUENO, null, e.getValue() - 0.01);
        }

        Map<Integer, Double> riesgoIMC_F = Map.ofEntries(
                Map.entry(6, 17.0), Map.entry(7, 17.1), Map.entry(8, 18.2),
                Map.entry(9, 19.1), Map.entry(10, 20.9), Map.entry(11, 22.3),
                Map.entry(12, 22.6), Map.entry(13, 22.0), Map.entry(14, 22.0),
                Map.entry(15, 22.4), Map.entry(16, 24.0), Map.entry(17, 24.0)
        );
        for (Map.Entry<Integer, Double> e : riesgoIMC_F.entrySet()) {
            // Riesgo Femenino
            crearNorma(antropo, imc, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, e.getValue(), null);

            // Saludable Femenino
            crearNorma(antropo, imc, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.BUENO, null, e.getValue() - 0.01);
        }

        // ========== WHtR (Relación Cintura/Estatura) ==========
        // Riesgo (Mayor o igual a 0.5)
        crearNorma(antropo, whtr, Sexo.MASCULINO, 6, 17, ClasificacionRendimiento.ZONA_DE_RIESGO, 0.5, null);
        crearNorma(antropo, whtr, Sexo.FEMENINO, 6, 17, ClasificacionRendimiento.ZONA_DE_RIESGO, 0.5, null);

        // Saludable (Menor a 0.5)
        crearNorma(antropo, whtr, Sexo.MASCULINO, 6, 17, ClasificacionRendimiento.BUENO, null, 0.49);
        crearNorma(antropo, whtr, Sexo.FEMENINO, 6, 17, ClasificacionRendimiento.BUENO, null, 0.49);

        // ========== Carrera 6 min (riesgo por debajo del umbral) ==========
        Map<Integer, Integer> riesgoC6M_M = Map.ofEntries(
                Map.entry(6, 675), Map.entry(7, 730), Map.entry(8, 768),
                Map.entry(9, 820), Map.entry(10, 856), Map.entry(11, 930),
                Map.entry(12, 966), Map.entry(13, 995), Map.entry(14, 1060),
                Map.entry(15, 1130), Map.entry(16, 1190), Map.entry(17, 1190)
        );
        for (Map.Entry<Integer, Integer> e : riesgoC6M_M.entrySet()) {
            crearNorma(carrera6min, distancia6min, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        Map<Integer, Integer> riesgoC6M_F = Map.ofEntries(
                Map.entry(6, 630), Map.entry(7, 683), Map.entry(8, 715),
                Map.entry(9, 745), Map.entry(10, 790), Map.entry(11, 840),
                Map.entry(12, 900), Map.entry(13, 940), Map.entry(14, 985),
                Map.entry(15, 1005), Map.entry(16, 1070), Map.entry(17, 1110)
        );
        for (Map.Entry<Integer, Integer> e : riesgoC6M_F.entrySet()) {
            crearNorma(carrera6min, distancia6min, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        // ========== Abdominales 1 min (riesgo por debajo del umbral) ==========
        Map<Integer, Integer> riesgoAbs_M = Map.ofEntries(
                Map.entry(6, 18), Map.entry(7, 18), Map.entry(8, 24),
                Map.entry(9, 26), Map.entry(10, 31), Map.entry(11, 37),
                Map.entry(12, 41), Map.entry(13, 42), Map.entry(14, 43),
                Map.entry(15, 45), Map.entry(16, 46), Map.entry(17, 47)
        );
        for (Map.Entry<Integer, Integer> e : riesgoAbs_M.entrySet()) {
            crearNorma(abdominales, repsAbs, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        Map<Integer, Integer> riesgoAbs_F = Map.ofEntries(
                Map.entry(6, 18), Map.entry(7, 18), Map.entry(8, 18),
                Map.entry(9, 20), Map.entry(10, 26), Map.entry(11, 30),
                Map.entry(12, 30), Map.entry(13, 33), Map.entry(14, 34),
                Map.entry(15, 34), Map.entry(16, 34), Map.entry(17, 34)
        );
        for (Map.Entry<Integer, Integer> e : riesgoAbs_F.entrySet()) {
            crearNorma(abdominales, repsAbs, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        // ========== Lanzamiento de balón 2kg (riesgo por debajo del umbral) ==========
        Map<Integer, Integer> riesgoLanz_M = Map.ofEntries(
                Map.entry(6, 147), Map.entry(7, 168), Map.entry(8, 190),
                Map.entry(9, 210), Map.entry(10, 232), Map.entry(11, 260),
                Map.entry(12, 290), Map.entry(13, 335), Map.entry(14, 400),
                Map.entry(15, 440), Map.entry(16, 480), Map.entry(17, 500)
        );
        for (Map.Entry<Integer, Integer> e : riesgoLanz_M.entrySet()) {
            crearNorma(lanzamiento, distanciaLanz, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        Map<Integer, Integer> riesgoLanz_F = Map.ofEntries(
                Map.entry(6, 125), Map.entry(7, 140), Map.entry(8, 158),
                Map.entry(9, 175), Map.entry(10, 202), Map.entry(11, 228),
                Map.entry(12, 260), Map.entry(13, 280), Map.entry(14, 290),
                Map.entry(15, 306), Map.entry(16, 310), Map.entry(17, 315)
        );
        for (Map.Entry<Integer, Integer> e : riesgoLanz_F.entrySet()) {
            crearNorma(lanzamiento, distanciaLanz, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, null, e.getValue().doubleValue());
        }

        // ========== Carrera 20m (riesgo por encima del umbral) ==========
        // NOTA: En carrera 20m, un tiempo mayor indica peor rendimiento.
        // Los valores de corte son: para cada edad, si el tiempo es MAYOR que este valor, es riesgo.
        // Usamos valor_min para indicar el umbral inferior (el mínimo tiempo para ser riesgo).
        Map<Integer, Double> riesgo20M_M = Map.ofEntries(
                Map.entry(6, 4.81), Map.entry(7, 4.52), Map.entry(8, 4.31),
                Map.entry(9, 4.25), Map.entry(10, 4.09), Map.entry(11, 4.00),
                Map.entry(12, 3.88), Map.entry(13, 3.72), Map.entry(14, 3.54),
                Map.entry(15, 3.40), Map.entry(16, 3.28), Map.entry(17, 3.22)
        );
        for (Map.Entry<Integer, Double> e : riesgo20M_M.entrySet()) {
            crearNorma(carrera20m, tiempo20m, Sexo.MASCULINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, e.getValue(), null);
        }

        Map<Integer, Double> riesgo20M_F = Map.ofEntries(
                Map.entry(6, 5.22), Map.entry(7, 4.88), Map.entry(8, 4.66),
                Map.entry(9, 4.58), Map.entry(10, 4.44), Map.entry(11, 4.36),
                Map.entry(12, 4.28), Map.entry(13, 4.17), Map.entry(14, 4.16),
                Map.entry(15, 4.07), Map.entry(16, 4.01), Map.entry(17, 3.91)
        );
        for (Map.Entry<Integer, Double> e : riesgo20M_F.entrySet()) {
            crearNorma(carrera20m, tiempo20m, Sexo.FEMENINO, e.getKey(), e.getKey(),
                    ClasificacionRendimiento.ZONA_DE_RIESGO, e.getValue(), null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Carrera6min() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.carrera_6min.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: carrera_6min"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.distancia_6min.nombre").orElseThrow();

        // Definir los rangos para cada edad (6-17) y sexo
        // Estructura: edad, [DEBIL_max, RAZONABLE_min, RAZONABLE_max, BUENO_min, BUENO_max, MUY_BIEN_min, MUY_BIEN_max, EXCELENTE_min]
        // Usaremos un mapa de edad a una lista de 8 valores (los límites)
        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{730, 730, 826, 827, 956, 957, 1316, 1317});
        rangosM.put(7, new double[]{752, 752, 848, 849, 975, 976, 1302, 1303});
        rangosM.put(8, new double[]{774, 774, 870, 871, 995, 996, 1300, 1301});
        rangosM.put(9, new double[]{797, 797, 894, 895, 1018, 1019, 1309, 1310});
        rangosM.put(10, new double[]{817, 817, 916, 917, 1040, 1041, 1322, 1323});
        rangosM.put(11, new double[]{837, 837, 938, 939, 1062, 1063, 1338, 1339});
        rangosM.put(12, new double[]{860, 860, 964, 965, 1090, 1091, 1366, 1367});
        rangosM.put(13, new double[]{895, 895, 1004, 1005, 1136, 1137, 1421, 1422});
        rangosM.put(14, new double[]{939, 939, 1057, 1058, 1197, 1198, 1498, 1499});
        rangosM.put(15, new double[]{986, 986, 1112, 1113, 1262, 1263, 1584, 1585});
        rangosM.put(16, new double[]{1015, 1015, 1148, 1149, 1306, 1307, 1643, 1644});
        rangosM.put(17, new double[]{1038, 1038, 1176, 1177, 1341, 1342, 1691, 1692});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{672, 672, 767, 768, 900, 901, 1276, 1277});
        rangosF.put(7, new double[]{691, 691, 779, 780, 891, 892, 1158, 1159});
        rangosF.put(8, new double[]{707, 707, 791, 792, 895, 896, 1131, 1132});
        rangosF.put(9, new double[]{720, 720, 805, 806, 910, 911, 1148, 1149});
        rangosF.put(10, new double[]{729, 729, 818, 819, 931, 932, 1199, 1200});
        rangosF.put(11, new double[]{736, 736, 831, 832, 953, 954, 1250, 1251});
        rangosF.put(12, new double[]{743, 743, 835, 836, 947, 948, 1191, 1192});
        rangosF.put(13, new double[]{749, 749, 839, 840, 947, 948, 1178, 1179});
        rangosF.put(14, new double[]{751, 751, 847, 848, 969, 970, 1256, 1257});
        rangosF.put(15, new double[]{748, 748, 858, 859, 1005, 1006, 1390, 1391});
        rangosF.put(16, new double[]{746, 746, 865, 866, 1021, 1022, 1401, 1402});
        rangosF.put(17, new double[]{744, 744, 870, 871, 1027, 1028, 1389, 1390});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Abdominales() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.abdominales_1min.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: abdominales_1min"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.abdominales_1min.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{18, 18, 22, 23, 27, 28, 38, 39});
        rangosM.put(7, new double[]{20, 20, 25, 26, 30, 31, 42, 43});
        rangosM.put(8, new double[]{23, 23, 27, 28, 33, 34, 45, 46});
        rangosM.put(9, new double[]{25, 25, 29, 30, 35, 36, 47, 48});
        rangosM.put(10, new double[]{26, 26, 31, 32, 36, 37, 48, 49});
        rangosM.put(11, new double[]{27, 27, 32, 33, 38, 39, 49, 50});
        rangosM.put(12, new double[]{29, 29, 34, 35, 39, 40, 51, 52});
        rangosM.put(13, new double[]{30, 30, 35, 36, 41, 42, 53, 54});
        rangosM.put(14, new double[]{32, 32, 37, 38, 43, 44, 56, 57});
        rangosM.put(15, new double[]{34, 34, 39, 40, 46, 47, 59, 60});
        rangosM.put(16, new double[]{35, 35, 41, 42, 47, 48, 61, 62});
        rangosM.put(17, new double[]{36, 36, 42, 43, 48, 49, 62, 63});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{17, 17, 21, 22, 26, 27, 37, 37}); // EXCELENTE desde 37
        rangosF.put(7, new double[]{19, 19, 23, 24, 29, 30, 40, 40});
        rangosF.put(8, new double[]{20, 20, 25, 26, 31, 32, 43, 43});
        rangosF.put(9, new double[]{21, 21, 26, 27, 32, 33, 45, 45});
        rangosF.put(10, new double[]{22, 22, 27, 28, 33, 34, 45, 45});
        rangosF.put(11, new double[]{23, 23, 28, 29, 33, 34, 46, 46});
        rangosF.put(12, new double[]{23, 23, 28, 29, 34, 35, 46, 46});
        rangosF.put(13, new double[]{24, 24, 29, 30, 35, 36, 48, 48});
        rangosF.put(14, new double[]{24, 24, 29, 30, 35, 36, 49, 49});
        rangosF.put(15, new double[]{24, 24, 29, 30, 35, 36, 49, 49});
        rangosF.put(16, new double[]{23, 23, 29, 30, 35, 36, 49, 49});
        rangosF.put(17, new double[]{23, 23, 29, 30, 35, 36, 48, 48});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Lanzamiento() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.lanzamiento_balon.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: lanzamiento_balon"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.lanzamiento_balon.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{136.2, 136.2, 154.9, 155.0, 180.3, 180.4, 248.9, 249.0});
        rangosM.put(7, new double[]{154.9, 154.9, 175.5, 175.6, 201.3, 201.4, 261.3, 261.4});
        rangosM.put(8, new double[]{173.4, 173.4, 195.8, 195.9, 223.2, 223.3, 284.2, 284.3});
        rangosM.put(9, new double[]{192.2, 192.2, 216.7, 216.8, 246.9, 247.0, 315.2, 315.3});
        rangosM.put(10, new double[]{209.2, 209.2, 235.6, 235.7, 268.7, 268.8, 345.3, 345.4});
        rangosM.put(11, new double[]{230.1, 230.1, 259.1, 259.2, 295.0, 295.1, 376.7, 376.8});
        rangosM.put(12, new double[]{255.2, 255.2, 287.6, 287.7, 327.3, 327.4, 416.1, 416.2});
        rangosM.put(13, new double[]{295.6, 295.6, 333.9, 334.0, 379.9, 380.0, 479.6, 479.7});
        rangosM.put(14, new double[]{348.5, 348.5, 393.9, 394.0, 446.4, 446.5, 554.4, 554.5});
        rangosM.put(15, new double[]{405.1, 405.1, 456.1, 456.2, 512.9, 513.0, 623.4, 623.5});
        rangosM.put(16, new double[]{448.3, 448.3, 501.6, 501.7, 560.0, 560.1, 670.8, 670.9});
        rangosM.put(17, new double[]{486.8, 486.8, 541.2, 541.3, 600.1, 600.2, 710.3, 710.4});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{129.7, 129.7, 146.6, 146.7, 167.4, 167.5, 214.8, 214.9});
        rangosF.put(7, new double[]{141.7, 141.7, 159.9, 160.0, 182.0, 182.1, 230.4, 230.5});
        rangosF.put(8, new double[]{156.6, 156.6, 176.4, 176.5, 200.3, 200.4, 252.1, 252.2});
        rangosF.put(9, new double[]{174.1, 174.1, 195.8, 195.9, 222.1, 222.2, 279.5, 279.6});
        rangosF.put(10, new double[]{191.9, 191.9, 215.5, 215.6, 244.3, 244.4, 308.0, 308.1});
        rangosF.put(11, new double[]{214.3, 214.3, 240.2, 240.3, 271.8, 271.9, 341.8, 341.9});
        rangosF.put(12, new double[]{236.8, 236.8, 265.0, 265.1, 298.9, 299.0, 372.1, 372.2});
        rangosF.put(13, new double[]{261.3, 261.3, 292.1, 292.2, 328.2, 328.3, 403.4, 403.5});
        rangosF.put(14, new double[]{283.5, 283.5, 316.5, 316.6, 354.4, 354.5, 431.7, 431.8});
        rangosF.put(15, new double[]{299.9, 299.9, 334.1, 334.2, 373.4, 373.5, 452.8, 452.9});
        rangosF.put(16, new double[]{309.7, 309.7, 344.6, 344.7, 385.0, 385.1, 468.0, 468.1});
        rangosF.put(17, new double[]{318.4, 318.4, 353.7, 353.8, 395.5, 395.6, 484.0, 484.1});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Salto() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.salto_horizontal_proesp.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: salto_horizontal_proesp"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.distancia.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{100.1, 100.1, 111.5, 111.6, 125.6, 125.7, 157.9, 158.0});
        rangosM.put(7, new double[]{107.5, 107.5, 118.9, 119.0, 132.9, 133.0, 164.1, 164.2});
        rangosM.put(8, new double[]{114.7, 114.7, 126.2, 126.3, 140.1, 140.2, 170.6, 170.7});
        rangosM.put(9, new double[]{122.2, 122.2, 133.9, 134.0, 147.8, 147.9, 178.0, 178.1});
        rangosM.put(10, new double[]{129.6, 129.6, 141.5, 141.6, 155.7, 155.8, 185.8, 185.9});
        rangosM.put(11, new double[]{136.6, 136.6, 148.8, 148.9, 163.2, 163.3, 193.3, 193.4});
        rangosM.put(12, new double[]{143.1, 143.1, 155.8, 155.9, 170.5, 170.6, 201.1, 201.2});
        rangosM.put(13, new double[]{152.6, 152.6, 166.1, 166.2, 181.8, 181.9, 213.8, 213.9});
        rangosM.put(14, new double[]{164.0, 164.0, 178.8, 178.9, 195.7, 195.8, 229.9, 230.0});
        rangosM.put(15, new double[]{175.3, 175.3, 191.3, 191.4, 209.4, 209.5, 245.5, 245.6});
        rangosM.put(16, new double[]{182.6, 182.6, 199.3, 199.4, 218.1, 218.2, 255.2, 255.3});
        rangosM.put(17, new double[]{188.5, 188.5, 205.8, 205.9, 225.0, 225.1, 262.5, 262.6});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{88.3, 88.3, 99.2, 99.3, 112.8, 112.9, 143.1, 143.2});
        rangosF.put(7, new double[]{96.2, 96.2, 107.3, 107.4, 120.8, 120.9, 151.0, 151.1});
        rangosF.put(8, new double[]{103.5, 103.5, 114.6, 114.7, 128.3, 128.4, 158.4, 158.5});
        rangosF.put(9, new double[]{110.8, 110.8, 122.1, 122.2, 135.9, 136.0, 166.2, 166.3});
        rangosF.put(10, new double[]{117.7, 117.7, 129.2, 129.3, 143.3, 143.4, 174.0, 174.1});
        rangosF.put(11, new double[]{123.9, 123.9, 135.8, 135.9, 150.3, 150.4, 181.7, 181.8});
        rangosF.put(12, new double[]{128.0, 128.0, 140.3, 140.4, 155.3, 155.4, 187.6, 187.7});
        rangosF.put(13, new double[]{130.8, 130.8, 143.7, 143.8, 159.3, 159.4, 193.0, 193.1});
        rangosF.put(14, new double[]{132.0, 132.0, 145.6, 145.7, 161.9, 162.0, 197.3, 197.4});
        rangosF.put(15, new double[]{131.8, 131.8, 146.2, 146.3, 163.5, 163.6, 200.7, 200.8});
        rangosF.put(16, new double[]{131.2, 131.2, 146.2, 146.3, 164.3, 164.4, 203.2, 203.3});
        rangosF.put(17, new double[]{130.5, 130.5, 146.2, 146.3, 165.1, 165.2, 205.6, 205.7});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rF[7], null);
        }
        // Masculino Sub-18 (0-17 años)
        crearNorma(prueba, metrica, Sexo.MASCULINO, 15, 17, ClasificacionRendimiento.EXCELENTE, 261.0, null);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 15, 17, ClasificacionRendimiento.BUENO, 247.0, 260.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 15, 17, ClasificacionRendimiento.REGULAR, 212.0, 246.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 15, 17, ClasificacionRendimiento.DEBIL, 194.0, 211.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 15, 17, ClasificacionRendimiento.MUY_DEBIL, null, 193.0);

        // Masculino Sub-21 (0-20 años)
        crearNorma(prueba, metrica, Sexo.MASCULINO, 18, 20, ClasificacionRendimiento.EXCELENTE, 267.0, null);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 18, 20, ClasificacionRendimiento.BUENO, 259.0, 266.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 18, 20, ClasificacionRendimiento.REGULAR, 218.0, 258.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 18, 20, ClasificacionRendimiento.DEBIL, 199.0, 217.0);
        crearNorma(prueba, metrica, Sexo.MASCULINO, 18, 20, ClasificacionRendimiento.MUY_DEBIL, null, 198.0);

        // Femenino Sub-18 (0-17 años)
        crearNorma(prueba, metrica, Sexo.FEMENINO, 15, 17, ClasificacionRendimiento.EXCELENTE, 228.0, null);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 15, 17, ClasificacionRendimiento.BUENO, 214.0, 227.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 15, 17, ClasificacionRendimiento.REGULAR, 188.0, 213.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 15, 17, ClasificacionRendimiento.DEBIL, 169.0, 187.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 15, 17, ClasificacionRendimiento.MUY_DEBIL, null, 168.0);

        // Femenino Sub-21 (0-20 años)
        crearNorma(prueba, metrica, Sexo.FEMENINO, 18, 20, ClasificacionRendimiento.EXCELENTE, 226.0, null);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 18, 20, ClasificacionRendimiento.BUENO, 215.0, 225.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 18, 20, ClasificacionRendimiento.REGULAR, 181.0, 214.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 18, 20, ClasificacionRendimiento.DEBIL, 157.0, 180.0);
        crearNorma(prueba, metrica, Sexo.FEMENINO, 18, 20, ClasificacionRendimiento.MUY_DEBIL, null, 156.0);

    }
    private void cargarNormasPROESP_Rendimiento_Agilidad() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.agilidad_4x4.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: agilidad_4x4"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.agilidad_4x4.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{6.20, 6.21, 7.10, 7.11, 7.60, 7.61, 8.07, 8.08});
        rangosM.put(7, new double[]{6.01, 6.02, 6.90, 6.91, 7.39, 7.40, 7.85, 7.86});
        rangosM.put(8, new double[]{5.85, 5.86, 6.71, 6.72, 7.20, 7.21, 7.65, 7.66});
        rangosM.put(9, new double[]{5.69, 5.70, 6.53, 6.54, 7.00, 7.01, 7.45, 7.46});
        rangosM.put(10, new double[]{5.54, 5.55, 6.35, 6.36, 6.81, 6.82, 7.25, 7.26});
        rangosM.put(11, new double[]{5.37, 5.38, 6.15, 6.16, 6.60, 6.61, 7.02, 7.03});
        rangosM.put(12, new double[]{5.22, 5.23, 5.98, 5.99, 6.41, 6.42, 6.82, 6.83});
        rangosM.put(13, new double[]{5.08, 5.09, 5.80, 5.81, 6.22, 6.23, 6.62, 6.63});
        rangosM.put(14, new double[]{4.93, 4.94, 5.62, 5.63, 6.03, 6.04, 6.42, 6.43});
        rangosM.put(15, new double[]{4.76, 4.77, 5.42, 5.43, 5.81, 5.82, 6.19, 6.20});
        rangosM.put(16, new double[]{4.62, 4.63, 5.24, 5.25, 5.62, 5.63, 5.99, 6.00});
        rangosM.put(17, new double[]{4.47, 4.48, 5.07, 5.08, 5.43, 5.44, 5.79, 5.80});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{6.67, 6.68, 7.67, 7.68, 8.26, 8.27, 8.85, 8.86});
        rangosF.put(7, new double[]{6.32, 6.33, 7.35, 7.36, 7.93, 7.94, 8.47, 8.48});
        rangosF.put(8, new double[]{6.09, 6.10, 7.09, 7.10, 7.64, 7.65, 8.15, 8.16});
        rangosF.put(9, new double[]{5.97, 5.98, 6.87, 6.88, 7.37, 7.38, 7.85, 7.86});
        rangosF.put(10, new double[]{5.81, 5.82, 6.66, 6.67, 7.14, 7.15, 7.60, 7.61});
        rangosF.put(11, new double[]{5.67, 5.68, 6.49, 6.50, 6.95, 6.96, 7.39, 7.40});
        rangosF.put(12, new double[]{5.61, 5.62, 6.37, 6.38, 6.82, 6.83, 7.27, 7.28});
        rangosF.put(13, new double[]{5.47, 5.48, 6.25, 6.26, 6.70, 6.71, 7.15, 7.16});
        rangosF.put(14, new double[]{5.32, 5.33, 6.11, 6.12, 6.58, 6.59, 7.05, 7.06});
        rangosF.put(15, new double[]{5.21, 5.22, 6.00, 6.01, 6.48, 6.49, 6.97, 6.98});
        rangosF.put(16, new double[]{5.12, 5.13, 5.92, 5.93, 6.42, 6.43, 6.92, 6.93});
        rangosF.put(17, new double[]{5.02, 5.03, 5.84, 5.85, 6.36, 6.37, 6.88, 6.89});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Velocidad() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.carrera_20m.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: carrera_20m"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.velocidad_20m.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{3.61, 3.62, 4.21, 4.22, 4.57, 4.58, 4.94, 4.95});
        rangosM.put(7, new double[]{3.52, 3.53, 4.08, 4.09, 4.42, 4.43, 4.75, 4.76});
        rangosM.put(8, new double[]{3.44, 3.45, 3.97, 3.98, 4.28, 4.29, 4.59, 4.60});
        rangosM.put(9, new double[]{3.37, 3.38, 3.86, 3.87, 4.15, 4.16, 4.44, 4.45});
        rangosM.put(10, new double[]{3.30, 3.31, 3.76, 3.77, 4.03, 4.04, 4.30, 4.31});
        rangosM.put(11, new double[]{3.22, 3.23, 3.65, 3.66, 3.91, 3.92, 4.16, 4.17});
        rangosM.put(12, new double[]{3.14, 3.15, 3.56, 3.57, 3.80, 3.81, 4.04, 4.05});
        rangosM.put(13, new double[]{3.04, 3.05, 3.44, 3.45, 3.68, 3.69, 3.91, 3.92});
        rangosM.put(14, new double[]{2.92, 2.93, 3.30, 3.31, 3.54, 3.55, 3.78, 3.79});
        rangosM.put(15, new double[]{2.78, 2.79, 3.16, 3.17, 3.39, 3.40, 3.63, 3.64});
        rangosM.put(16, new double[]{2.68, 2.69, 3.05, 3.06, 3.28, 3.29, 3.53, 3.54});
        rangosM.put(17, new double[]{2.58, 2.59, 2.95, 2.96, 3.19, 3.20, 3.43, 3.44});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{3.98, 3.99, 4.56, 4.57, 4.91, 4.92, 5.27, 5.28});
        rangosF.put(7, new double[]{3.84, 3.85, 4.39, 4.40, 4.72, 4.73, 5.05, 5.06});
        rangosF.put(8, new double[]{3.72, 3.73, 4.23, 4.24, 4.55, 4.56, 4.86, 4.87});
        rangosF.put(9, new double[]{3.60, 3.61, 4.09, 4.10, 4.39, 4.40, 4.68, 4.69});
        rangosF.put(10, new double[]{3.50, 3.51, 3.97, 3.98, 4.25, 4.26, 4.53, 4.54});
        rangosF.put(11, new double[]{3.41, 3.42, 3.86, 3.87, 4.14, 4.15, 4.41, 4.42});
        rangosF.put(12, new double[]{3.34, 3.35, 3.79, 3.80, 4.06, 4.07, 4.33, 4.34});
        rangosF.put(13, new double[]{3.27, 3.28, 3.73, 3.74, 4.00, 4.01, 4.28, 4.29});
        rangosF.put(14, new double[]{3.20, 3.21, 3.67, 3.68, 3.96, 3.97, 4.25, 4.26});
        rangosF.put(15, new double[]{3.11, 3.12, 3.61, 3.62, 3.91, 3.92, 4.22, 4.23});
        rangosF.put(16, new double[]{3.03, 3.04, 3.55, 3.56, 3.87, 3.88, 4.21, 4.22});
        rangosF.put(17, new double[]{2.95, 2.96, 3.49, 3.50, 3.83, 3.84, 4.19, 4.20});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento_Flexibilidad() {
        PruebaEstandar prueba = pruebaRepo.findByNombreKey("ejercicio.sit_reach.nombre")
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada: sit_reach"));
        Metrica metrica = metricaRepo.findByNombreKey("metrica.flexibilidad_sit_reach.nombre").orElseThrow();

        Map<Integer, double[]> rangosM = new HashMap<>();
        rangosM.put(6, new double[]{34.3, 34.3, 41.2, 41.3, 50.3, 50.4, 73.9, 74.0});
        rangosM.put(7, new double[]{33.3, 33.3, 39.6, 39.7, 47.9, 48.0, 68.4, 68.5});
        rangosM.put(8, new double[]{32.3, 32.3, 38.3, 38.4, 45.9, 46.0, 63.9, 64.0});
        rangosM.put(9, new double[]{31.3, 31.3, 37.1, 37.2, 44.5, 44.6, 61.4, 61.5});
        rangosM.put(10, new double[]{30.4, 30.4, 36.4, 36.5, 43.8, 43.9, 60.7, 60.8});
        rangosM.put(11, new double[]{29.8, 29.8, 35.6, 35.7, 42.9, 43.0, 59.2, 59.3});
        rangosM.put(12, new double[]{29.4, 29.4, 35.1, 35.2, 42.1, 42.2, 57.8, 57.9});
        rangosM.put(13, new double[]{29.1, 29.1, 35.2, 35.3, 42.8, 42.9, 60.5, 60.6});
        rangosM.put(14, new double[]{28.7, 28.7, 35.6, 35.7, 44.7, 44.8, 67.1, 67.2});
        rangosM.put(15, new double[]{28.4, 28.4, 36.3, 36.4, 46.9, 47.0, 73.7, 73.8});
        rangosM.put(16, new double[]{28.4, 28.4, 36.7, 36.8, 48.0, 48.1, 76.5, 76.6});
        rangosM.put(17, new double[]{28.7, 28.7, 36.8, 36.9, 47.9, 48.0, 76.1, 76.2});

        Map<Integer, double[]> rangosF = new HashMap<>();
        rangosF.put(6, new double[]{37.0, 37.0, 43.8, 43.9, 52.5, 52.6, 73.4, 73.4}); // EXCELENTE desde 73.4
        rangosF.put(7, new double[]{35.3, 35.3, 41.8, 41.9, 49.9, 50.0, 69.1, 69.1});
        rangosF.put(8, new double[]{33.8, 33.8, 40.0, 40.1, 47.8, 47.9, 65.7, 65.7});
        rangosF.put(9, new double[]{32.4, 32.4, 38.6, 38.7, 46.2, 46.3, 63.6, 63.6});
        rangosF.put(10, new double[]{31.3, 31.3, 37.5, 37.6, 45.3, 45.4, 62.6, 62.6});
        rangosF.put(11, new double[]{30.6, 30.6, 36.7, 36.8, 44.2, 44.3, 61.0, 61.0});
        rangosF.put(12, new double[]{30.4, 30.4, 36.3, 36.4, 43.6, 43.7, 60.1, 60.1});
        rangosF.put(13, new double[]{30.3, 30.3, 36.6, 36.7, 44.5, 44.6, 62.9, 62.9});
        rangosF.put(14, new double[]{30.1, 30.1, 37.2, 37.3, 46.5, 46.6, 69.5, 69.5});
        rangosF.put(15, new double[]{29.6, 29.6, 37.8, 37.9, 48.8, 48.9, 77.1, 77.1});
        rangosF.put(16, new double[]{29.2, 29.2, 37.8, 37.9, 49.5, 49.6, 80.1, 80.1});
        rangosF.put(17, new double[]{28.9, 28.9, 37.4, 37.5, 48.9, 49.0, 79.0, 79.0});

        for (int edad = 6; edad <= 17; edad++) {
            double[] rM = rangosM.get(edad);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rM[0]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rM[1], rM[2]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.BUENO, rM[3], rM[4]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rM[5], rM[6]);
            crearNorma(prueba, metrica, Sexo.MASCULINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rM[7], null);

            double[] rF = rangosF.get(edad);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.DEBIL, null, rF[0]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.RAZONABLE, rF[1], rF[2]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.BUENO, rF[3], rF[4]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.MUY_BIEN, rF[5], rF[6]);
            crearNorma(prueba, metrica, Sexo.FEMENINO, edad, edad, ClasificacionRendimiento.EXCELENTE, rF[7], null);
        }
    }
    private void cargarNormasPROESP_Rendimiento() {
        cargarNormasPROESP_Rendimiento_Carrera6min();
        cargarNormasPROESP_Rendimiento_Abdominales();
        cargarNormasPROESP_Rendimiento_Lanzamiento();
        cargarNormasPROESP_Rendimiento_Salto();
        cargarNormasPROESP_Rendimiento_Agilidad();
        cargarNormasPROESP_Rendimiento_Velocidad();
        cargarNormasPROESP_Rendimiento_Flexibilidad();
    }
    private void crearNorma(PruebaEstandar prueba, Metrica metrica, Sexo sexo, int edadMin, int edadMax,
                            ClasificacionRendimiento clasif, Double min, Double max) {
        NormaEvaluacion n = new NormaEvaluacion();
        n.setFuente("cargada_inicializador");
        n.setPruebaEstandar(prueba);
        n.setMetrica(metrica);
        n.setSexo(sexo);
        n.setEdadMin(edadMin);
        n.setEdadMax(edadMax);
        n.setClasificacion(clasif);
        n.setValorMin(min);
        n.setValorMax(max);
        normaRepo.save(n);
    }
}