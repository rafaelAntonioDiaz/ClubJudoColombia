-- Versión 5: Poblar la tabla de unión (prueba_estandar_metricas) (CORREGIDO)

-- (Asignar IDs de métricas...)
SET @METRICA_MASA = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.masa_corporal.nombre');
SET @METRICA_ESTATURA = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.estatura.nombre');
SET @METRICA_IMC = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.imc.nombre');
SET @METRICA_WHTR = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.whtr.nombre');
SET @METRICA_DISTANCIA = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.distancia.nombre');
SET @METRICA_SUSP_ISO_S = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.tiempo_isometrico.nombre');
SET @METRICA_SUSP_DIN_REP = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.repeticiones_dinamicas.nombre');
SET @METRICA_SUSP_ISO_KG = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.tiempo_iso_kg.nombre');
SET @METRICA_SUSP_DIN_KG = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.rep_dinamicas_kg.nombre');
SET @METRICA_UCHIKOMI_REP = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.repeticiones_uchikomi.nombre');
SET @METRICA_SJFT_S1 = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_proyecciones_s1.nombre');
SET @METRICA_SJFT_S2 = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_proyecciones_s2.nombre');
SET @METRICA_SJFT_S3 = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_proyecciones_s3.nombre');
SET @METRICA_SJFT_TOTAL = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_proyecciones_total.nombre');
SET @METRICA_SJFT_FC_FIN = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_fc_final.nombre');
SET @METRICA_SJFT_FC_1MIN = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_fc_1min.nombre');
SET @METRICA_SJFT_INDICE = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.sjft_indice.nombre');
SET @METRICA_CARRERA_6MIN = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.distancia_6min.nombre');
SET @METRICA_SIT_REACH = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.flexibilidad_sit_reach.nombre');
SET @METRICA_ABDOMINALES_1MIN = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.abdominales_1min.nombre');
SET @METRICA_LANZ_BALON = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.lanzamiento_balon.nombre');
SET @METRICA_AGILIDAD_4X4 = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.agilidad_4x4.nombre');
SET @METRICA_CARRERA_20M = (SELECT id_metrica FROM metricas WHERE nombre_key = 'metrica.velocidad_20m.nombre');

-- (Asignar IDs de pruebas_estandar...)
SET @EJERCICIO_ANTROPO = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.medicion_antropo.nombre');
SET @EJERCICIO_SALTO_H_CBJ = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.salto_horizontal.nombre');
SET @EJERCICIO_SUSPENSION = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.suspension_barra.nombre');
SET @EJERCICIO_UCHIKOMI = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.uchikomi_test.nombre');
SET @EJERCICIO_SJFT = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.sjft.nombre');
SET @EJERCICIO_CARRERA_6MIN = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.carrera_6min.nombre');
SET @EJERCICIO_SIT_REACH = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.sit_reach.nombre');
SET @EJERCICIO_ABDOMINALES_1MIN = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.abdominales_1min.nombre');
SET @EJERCICIO_LANZ_BALON = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.lanzamiento_balon.nombre');
SET @EJERCICIO_SALTO_H_PROESP = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.salto_horizontal_proesp.nombre');
SET @EJERCICIO_AGILIDAD_4X4 = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.agilidad_4x4.nombre');
-- --- ¡TYPO CORREGIDO! (eliminado 'eje' de 'ejepruebas_estandar') ---
SET @EJERCICIO_CARRERA_20M = (SELECT id_ejercicio FROM pruebas_estandar WHERE nombre_key = 'ejercicio.carrera_20m.nombre');

-- (Poblar la tabla de unión...)
INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_ANTROPO, @METRICA_MASA),
(@EJERCICIO_ANTROPO, @METRICA_ESTATURA),
(@EJERCICIO_ANTROPO, @METRICA_IMC),
(@EJERCICIO_ANTROPO, @METRICA_WHTR);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_SALTO_H_CBJ, @METRICA_DISTANCIA);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_SUSPENSION, @METRICA_SUSP_ISO_S),
(@EJERCICIO_SUSPENSION, @METRICA_SUSP_DIN_REP),
(@EJERCICIO_SUSPENSION, @METRICA_SUSP_ISO_KG),
(@EJERCICIO_SUSPENSION, @METRICA_SUSP_DIN_KG);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_UCHIKOMI, @METRICA_UCHIKOMI_REP);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_SJFT, @METRICA_SJFT_S1),
(@EJERCICIO_SJFT, @METRICA_SJFT_S2),
(@EJERCICIO_SJFT, @METRICA_SJFT_S3),
(@EJERCICIO_SJFT, @METRICA_SJFT_TOTAL),
(@EJERCICIO_SJFT, @METRICA_SJFT_FC_FIN),
(@EJERCICIO_SJFT, @METRICA_SJFT_FC_1MIN),
(@EJERCICIO_SJFT, @METRICA_SJFT_INDICE);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_CARRERA_6MIN, @METRICA_CARRERA_6MIN);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_SIT_REACH, @METRICA_SIT_REACH);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_ABDOMINALES_1MIN, @METRICA_ABDOMINALES_1MIN);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_LANZ_BALON, @METRICA_LANZ_BALON);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_SALTO_H_PROESP, @METRICA_DISTANCIA);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_AGILIDAD_4X4, @METRICA_AGILIDAD_4X4);

INSERT INTO prueba_estandar_metricas (id_ejercicio, id_metrica) VALUES
(@EJERCICIO_CARRERA_20M, @METRICA_CARRERA_20M);