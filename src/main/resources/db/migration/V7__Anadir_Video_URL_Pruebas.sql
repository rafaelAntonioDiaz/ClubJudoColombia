-- Versión 7: Añadir la columna video_url a las pruebas_estandar y poblarla

-- 1. Alterar la tabla para añadir la nueva columna
ALTER TABLE pruebas_estandar
ADD COLUMN video_url VARCHAR(255) NULL; -- 'NULL' permite que pruebas_estandar futuros no tengan video

-- 2. Actualizar los pruebas_estandar existentes con las URLs
-- (Usamos las claves i18n que definimos en V4 para encontrar el ejercicio correcto)

-- --- Videos PROESP-Br ---
UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=1YxMuyf6cVs'
WHERE nombre_key = 'ejercicio.carrera_6min.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=cdugHSL6C_o'
WHERE nombre_key = 'ejercicio.sit_reach.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=3E5QU5L1VYA'
WHERE nombre_key = 'ejercicio.abdominales_1min.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=1E2O4V2gM8U'
WHERE nombre_key = 'ejercicio.lanzamiento_balon.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=5hFrQCzoUMI'
WHERE nombre_key = 'ejercicio.salto_horizontal_proesp.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=51nryPJA_ZE'
WHERE nombre_key = 'ejercicio.agilidad_4x4.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=7h1G3Wj5t4k'
WHERE nombre_key = 'ejercicio.carrera_20m.nombre';

-- --- Videos CBJ ---
UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=3E5QU5L1VYA' -- (Usando el de Abdominales como demo de "Masa", según tu lista)
WHERE nombre_key = 'ejercicio.medicion_antropo.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=5hFrQCzoUMI'
WHERE nombre_key = 'ejercicio.salto_horizontal.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=JCxixp_VOmU'
WHERE nombre_key = 'ejercicio.suspension_barra.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=ARQtcqFqGVg'
WHERE nombre_key = 'ejercicio.uchikomi_test.nombre';

UPDATE pruebas_estandar SET video_url = 'https://www.youtube.com/watch?v=0yKhlncICFs'
WHERE nombre_key = 'ejercicio.sjft.nombre';