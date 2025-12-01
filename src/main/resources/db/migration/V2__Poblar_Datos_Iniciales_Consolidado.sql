-- ============================================================================
-- VERSIÓN 2: Poblado de Datos Iniciales - CONSOLIDADO
-- ============================================================================
-- Este archivo consolida V2, V3, V4, V5 y V7 para mejor rendimiento
-- Evita múltiples commits y reduce overhead de transacciones
-- ============================================================================

SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,NO_AUTO_VALUE_ON_ZERO';

START TRANSACTION;

-- ============================================================================
-- SECCIÓN 1: ROLES DEL SISTEMA
-- ============================================================================

INSERT INTO roles (nombre) VALUES
    ('ROLE_SENSEI'),      -- Para los Senseis
    ('ROLE_JUDOKA'),      -- Para los Judokas
    ('ROLE_COMPETIDOR'),  -- Rol adicional para Judokas que compiten
    ('ROLE_ADMIN'),       -- Para los administradores del sistema
    ('ROLE_MECENAS');     -- Para los patrocinadores

-- ============================================================================
-- SECCIÓN 2: MÉTRICAS DEL SISTEMA
-- ============================================================================

INSERT INTO metricas (nombre_key, unidad) VALUES
    ('metrica.masa_corporal.nombre', 'kg'),
    ('metrica.estatura.nombre', 'cm'),
    ('metrica.distancia.nombre', 'cm'),
    ('metrica.tiempo_isometrico.nombre', 's'),
    ('metrica.repeticiones_dinamicas.nombre', 'reps'),
    ('metrica.rep_dinamicas_kg.nombre', 'reps/kg'),
    ('metrica.tiempo_iso_kg.nombre', 's/kg'),
    ('metrica.repeticiones_uchikomi.nombre', 'reps'),
    ('metrica.sjft_proyecciones_s1.nombre', 'reps'),
    ('metrica.sjft_proyecciones_s2.nombre', 'reps'),
    ('metrica.sjft_proyecciones_s3.nombre', 'reps'),
    ('metrica.sjft_proyecciones_total.nombre', 'reps'),
    ('metrica.sjft_fc_final.nombre', 'bpm'),
    ('metrica.sjft_fc_1min.nombre', 'bpm'),
    ('metrica.sjft_indice.nombre', 'índice'),
    ('metrica.distancia_6min.nombre', 'm'),
    ('metrica.flexibilidad_sit_reach.nombre', 'cm'),
    ('metrica.abdominales_1min.nombre', 'reps'),
    ('metrica.lanzamiento_balon.nombre', 'cm'),
    ('metrica.agilidad_4x4.nombre', 's'),
    ('metrica.velocidad_20m.nombre', 's'),
    ('metrica.imc.nombre', 'kg/m²'),
    ('metrica.whtr.nombre', 'ratio');

-- ============================================================================
-- SECCIÓN 3: PRUEBAS ESTÁNDAR CON VIDEOS
-- ============================================================================

INSERT INTO pruebas_estandar (nombre_key, objetivo_key, descripcion_key, categoria, video_url) VALUES
    -- Ejercicios CBJ
    ('ejercicio.medicion_antropo.nombre',
     'ejercicio.medicion_antropo.objetivo',
     'ejercicio.medicion_antropo.descripcion',
     'MEDICION_ANTROPOMETRICA',
     'https://www.youtube.com/watch?v=3E5QU5L1VYA'),

    ('ejercicio.salto_horizontal.nombre',
     'ejercicio.salto_horizontal.objetivo',
     'ejercicio.salto_horizontal.descripcion',
     'POTENCIA',
     'https://www.youtube.com/watch?v=5hFrQCzoUMI'),

    ('ejercicio.suspension_barra.nombre',
     'ejercicio.suspension_barra.objetivo',
     'ejercicio.suspension_barra.descripcion',
     'RESISTENCIA_ISOMETRICA',
     'https://www.youtube.com/watch?v=JCxixp_VOmU'),

    ('ejercicio.uchikomi_test.nombre',
     'ejercicio.uchikomi_test.objetivo',
     'ejercicio.uchikomi_test.descripcion',
     'APTITUD_ANAEROBICA',
     'https://www.youtube.com/watch?v=ARQtcqFqGVg'),

    ('ejercicio.sjft.nombre',
     'ejercicio.sjft.objetivo',
     'ejercicio.sjft.descripcion',
     'APTITUD_AEROBICA',
     'https://www.youtube.com/watch?v=0yKhlncICFs'),

    -- Ejercicios PROESP-BR
    ('ejercicio.carrera_6min.nombre',
     'ejercicio.carrera_6min.objetivo',
     'ejercicio.carrera_6min.descripcion',
     'APTITUD_AEROBICA',
     'https://www.youtube.com/watch?v=1YxMuyf6cVs'),

    ('ejercicio.sit_reach.nombre',
     'ejercicio.sit_reach.objetivo',
     'ejercicio.sit_reach.descripcion',
     'FLEXIBILIDAD',
     'https://www.youtube.com/watch?v=cdugHSL6C_o'),

    ('ejercicio.abdominales_1min.nombre',
     'ejercicio.abdominales_1min.objetivo',
     'ejercicio.abdominales_1min.descripcion',
     'RESISTENCIA_MUSCULAR_LOCALIZADA',
     'https://www.youtube.com/watch?v=3E5QU5L1VYA'),

    ('ejercicio.lanzamiento_balon.nombre',
     'ejercicio.lanzamiento_balon.objetivo',
     'ejercicio.lanzamiento_balon.descripcion',
     'POTENCIA',
     'https://www.youtube.com/watch?v=1E2O4V2gM8U'),

    ('ejercicio.salto_horizontal_proesp.nombre',
     'ejercicio.salto_horizontal_proesp.objetivo',
     'ejercicio.salto_horizontal_proesp.descripcion',
     'POTENCIA',
     'https://www.youtube.com/watch?v=5hFrQCzoUMI'),

    ('ejercicio.agilidad_4x4.nombre',
     'ejercicio.agilidad_4x4.objetivo',
     'ejercicio.agilidad_4x4.descripcion',
     'AGILIDAD',
     'https://www.youtube.com/watch?v=51nryPJA_ZE'),

    ('ejercicio.carrera_20m.nombre',
     'ejercicio.carrera_20m.objetivo',
     'ejercicio.carrera_20m.descripcion',
     'VELOCIDAD',
     'https://www.youtube.com/watch?v=7h1G3Wj5t4k');

-- ============================================================================
-- SECCIÓN 4: RELACIÓN PRUEBAS-MÉTRICAS
-- ============================================================================

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica)
SELECT pe.id_ejercicio, m.id_metrica
FROM (
    -- Medición Antropométrica
    SELECT 'ejercicio.medicion_antropo.nombre' AS ejercicio, 'metrica.masa_corporal.nombre' AS metrica
    UNION ALL SELECT 'ejercicio.medicion_antropo.nombre', 'metrica.estatura.nombre'
    UNION ALL SELECT 'ejercicio.medicion_antropo.nombre', 'metrica.imc.nombre'
    UNION ALL SELECT 'ejercicio.medicion_antropo.nombre', 'metrica.whtr.nombre'

    -- Salto Horizontal CBJ
    UNION ALL SELECT 'ejercicio.salto_horizontal.nombre', 'metrica.distancia.nombre'

    -- Suspensión en Barra
    UNION ALL SELECT 'ejercicio.suspension_barra.nombre', 'metrica.tiempo_isometrico.nombre'
    UNION ALL SELECT 'ejercicio.suspension_barra.nombre', 'metrica.repeticiones_dinamicas.nombre'
    UNION ALL SELECT 'ejercicio.suspension_barra.nombre', 'metrica.tiempo_iso_kg.nombre'
    UNION ALL SELECT 'ejercicio.suspension_barra.nombre', 'metrica.rep_dinamicas_kg.nombre'

    -- Uchikomi Test
    UNION ALL SELECT 'ejercicio.uchikomi_test.nombre', 'metrica.repeticiones_uchikomi.nombre'

    -- SJFT
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_proyecciones_s1.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_proyecciones_s2.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_proyecciones_s3.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_proyecciones_total.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_fc_final.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_fc_1min.nombre'
    UNION ALL SELECT 'ejercicio.sjft.nombre', 'metrica.sjft_indice.nombre'

    -- Carrera 6 min
    UNION ALL SELECT 'ejercicio.carrera_6min.nombre', 'metrica.distancia_6min.nombre'

    -- Sit and Reach
    UNION ALL SELECT 'ejercicio.sit_reach.nombre', 'metrica.flexibilidad_sit_reach.nombre'

    -- Abdominales
    UNION ALL SELECT 'ejercicio.abdominales_1min.nombre', 'metrica.abdominales_1min.nombre'

    -- Lanzamiento Balón
    UNION ALL SELECT 'ejercicio.lanzamiento_balon.nombre', 'metrica.lanzamiento_balon.nombre'

    -- Salto Horizontal PROESP
    UNION ALL SELECT 'ejercicio.salto_horizontal_proesp.nombre', 'metrica.distancia.nombre'

    -- Agilidad 4x4
    UNION ALL SELECT 'ejercicio.agilidad_4x4.nombre', 'metrica.agilidad_4x4.nombre'

    -- Carrera 20m
    UNION ALL SELECT 'ejercicio.carrera_20m.nombre', 'metrica.velocidad_20m.nombre'
) AS relaciones
JOIN pruebas_estandar pe ON pe.nombre_key = relaciones.ejercicio
JOIN metricas m ON m.nombre_key = relaciones.metrica;

COMMIT;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;