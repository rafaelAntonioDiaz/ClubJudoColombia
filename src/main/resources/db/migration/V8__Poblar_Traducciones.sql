-- Versión 12: Mover la internacionalización (i18n) de .properties a la BD
-- Esto permite que los Senseis puedan añadir/editar ejercicios dinámicamente.

-- 1. Crear la tabla de traducciones
CREATE TABLE traducciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(255) NOT NULL,
    idioma VARCHAR(5) NOT NULL,
    texto TEXT NOT NULL,
    UNIQUE KEY uk_clave_idioma (clave, idioma)
);

-- 2. Poblar todas las traducciones en Español ('es')
INSERT INTO traducciones (clave, idioma, texto) VALUES
-- Métricas
('metrica.masa_corporal.nombre', 'es', 'Masa Corporal'),
('metrica.estatura.nombre', 'es', 'Estatura'),
('metrica.imc.nombre', 'es', 'Índice de Masa Corporal (IMC)'),
('metrica.whtr.nombre', 'es', 'Relación Cintura-Estatura (WHtR)'),
('metrica.distancia.nombre', 'es', 'Distancia'),
('metrica.tiempo_isometrico.nombre', 'es', 'Tiempo Isométrico'),
('metrica.repeticiones_dinamicas.nombre', 'es', 'Repeticiones Dinámicas'),
('metrica.rep_dinamicas_kg.nombre', 'es', 'Repeticiones Dinámicas (relativo al peso)'),
('metrica.tiempo_iso_kg.nombre', 'es', 'Tiempo Isométrico (relativo al peso)'),
('metrica.repeticiones_uchikomi.nombre', 'es', 'Repeticiones Uchi-komi'),
('metrica.sjft_proyecciones_s1.nombre', 'es', 'Proyecciones SJFT (Serie 1 - 15s)'),
('metrica.sjft_proyecciones_s2.nombre', 'es', 'Proyecciones SJFT (Serie 2 - 30s)'),
('metrica.sjft_proyecciones_s3.nombre', 'es', 'Proyecciones SJFT (Serie 3 - 30s)'),
('metrica.sjft_proyecciones_total.nombre', 'es', 'Proyecciones SJFT (Total)'),
('metrica.sjft_fc_final.nombre', 'es', 'FC Final (SJFT)'),
('metrica.sjft_fc_1min.nombre', 'es', 'FC 1min Post (SJFT)'),
('metrica.sjft_indice.nombre', 'es', 'Índice SJFT'),
('metrica.distancia_6min.nombre', 'es', 'Distancia Recorrida (6 min)'),
('metrica.flexibilidad_sit_reach.nombre', 'es', 'Flexibilidad (Sit and Reach)'),
('metrica.abdominales_1min.nombre', 'es', 'Abdominales (1 min)'),
('metrica.lanzamiento_balon.nombre', 'es', 'Lanzamiento Balón Medicinal'),
('metrica.agilidad_4x4.nombre', 'es', 'Agilidad (Cuadrado 4x4)'),
('metrica.velocidad_20m.nombre', 'es', 'Velocidad (20m)'),

-- Ejercicios (CBJ)
('ejercicio.medicion_antropo.nombre', 'es', 'Masa Corporal y Estatura (CBJ)'),
('ejercicio.medicion_antropo.objetivo', 'es', 'Medir la masa corporal y la estatura de los atletas como base para otras evaluaciones.'),
('ejercicio.medicion_antropo.descripcion', 'es', 'Material: Balanza, Estadiómetro o cinta fijada. Procedimiento: 1. Antes de iniciar los tests físicos, mensurar la masa corporal y estatura de los atletas.'),

('ejercicio.salto_horizontal.nombre', 'es', 'Salto Horizontal (CBJ)'),
('ejercicio.salto_horizontal.objetivo', 'es', 'Evaluar la potencia muscular de los miembros inferiores, factor determinante en la ejecución de técnicas durante el combate de judo.'),
('ejercicio.salto_horizontal.descripcion', 'es', 'Material: Cinta métrica, Cinta o esparadrapo. Procedimiento: 1. Fijar una cinta al suelo, perpendicularmente a una línea trazada como punto cero. 2. El atleta debe posicionarse atrás del marco cero, con los pies paralelos y ligeramente apartados. 3. El atleta saltará la mayor distancia posible, terminando el salto en pie. 4. La distancia será registrada en centímetros, a partir de la línea trazada hasta el talón más próximo. 5. Se realizarán 3-6 intentos con al menos 1 minuto de intervalo, registrando el mejor.'),

('ejercicio.suspension_barra.nombre', 'es', 'Suspensión en la Barra con Judogi (Isométrica y Dinámica)'),
('ejercicio.suspension_barra.objetivo', 'es', 'Evaluar la resistencia de fuerza isométrica y dinámica de los miembros superiores, predominantes en las disputas de agarre en judo.'),
('ejercicio.suspension_barra.descripcion', 'es', 'Material: Cronómetro, Barras de suspensión, 1 judogi por barra. Procedimiento: 1. El atleta ejecutará el agarre en un judogi fijado en una barra. 2. Isométrico: El atleta flexionará los brazos y permanecerá por el tiempo máximo de suspensión posible. 3. Dinámico: Tras un intervalo, el atleta realizará el número máximo de movimientos de flexión y extensión de codos hasta el agotamiento. 4. Se contabilizarán repeticiones con extensión >90° y flexión hasta que el mentón ultrapase la altura de las manos. 5. Para el cálculo del valor relativo, multiplicar el número de repeticiones y tiempo total por el peso corporal.'),

('ejercicio.uchikomi_test.nombre', 'es', 'Hikidashi Uchi-komi Test'),
('ejercicio.uchikomi_test.objetivo', 'es', 'Evaluar la aptitud anaeróbica en situación específica, responsable de acciones de alta intensidad en el combate de judo.'),
('ejercicio.uchikomi_test.descripcion', 'es', 'Material: Cronómetro. Procedimiento: 1. El ejecutante (tori) se posiciona con pies paralelos y realizando el agarre en un uke de peso y altura similar. 2. Al señal, el atleta deberá realizar la mayor cantidad de hikidashi uchi-komi durante 40 segundos. 3. Se contabilizarán las ejecuciones válidas donde el tori genera el desequilibrio del uke hacia adelante, realiza el giro y contacto del cuadril, y retorna rápidamente a la posición inicial. 4. El uke no debe ofrecer resistencia.'),

('ejercicio.sjft.nombre', 'es', 'Special Judo Fitness Test (SJFT)'),
('ejercicio.sjft.objetivo', 'es', 'Medir las aptitudes aeróbica y anaeróbica en situación específica para judo, relevantes para acciones de alta intensidad, trabajo intermitente y recuperación.'),
('ejercicio.sjft.descripcion', 'es', 'Material: Monitores de FC, Cronómetro, Cintas para marcación. Procedimiento: 1. Dos ukes a 6 metros de distancia; el tori a 3 metros. 2. Tres períodos (A=15s; B=30s; C=30s) con 10s de intervalo. 3. En cada período, proyectar a los ukes con ippon-seoi-nage el máximo posible. 4. Inmediatamente después del test y tras 1 minuto de reposo, verificar la FC. 5. Índice = (FC final + FC después de 1 min) / número total de proyecciones.'),

-- Ejercicios (PROESP-BR)
('ejercicio.carrera_6min.nombre', 'es', 'Prueba de Carrera/Marcha de 6 minutos'),
('ejercicio.carrera_6min.objetivo', 'es', 'Evaluar la aptitud cardiorrespiratoria.'),
('ejercicio.carrera_6min.descripcion', 'es', 'Material: Superficie plana marcada, Cinta métrica, Cronómetro, Hoja de registro. Procedimiento: 1. Dividir en grupos adecuados. 2. Explicar que deben correr el mayor tiempo posible, evitando badenes. 3. Informar tiempos 2, 4 y 5 min. 4. Al final, detenerse y registrar la distancia recorrida. Nota: Datos en metros sin decimales.'),

('ejercicio.sit_reach.nombre', 'es', 'Prueba de Sentarse y Estirarse'),
('ejercicio.sit_reach.objetivo', 'es', 'Evaluar la flexibilidad.'),
('ejercicio.sit_reach.descripcion', 'es', 'Material: Cinta métrica, cinta adhesiva. Procedimiento: 1. Cinta métrica en el suelo. 2. Cinta adhesiva de 30cm perpendicular a la marca de 38cm. 3. Alumno descalzo, talones en la marca de 38cm, separados 30cm. 4. Rodillas extendidas, manos superpuestas, inclinarse lentamente lo máximo posible. 5. Mantener posición para registrar. 6. Dos intentos, registrar el mejor. Nota: Registrar en cm con un decimal.'),

('ejercicio.abdominales_1min.nombre', 'es', 'Prueba de Abdominales de 1 minuto'),
('ejercicio.abdominales_1min.objetivo', 'es', 'Evaluar la resistencia muscular localizada.'),
('ejercicio.abdominales_1min.descripcion', 'es', 'Material: Alfombrillas, cronómetro. Procedimiento: 1. Alumno en decúbito supino, rodillas flexionadas a 45º, brazos cruzados sobre el pecho. 2. Evaluador sujeta los tobillos. 3. A la señal, flexionar el torso hasta que los codos toquen los muslos y volver. 4. Máximo de repeticiones completas en 1 minuto. Nota: Registrar número de movimientos completos.'),

('ejercicio.lanzamiento_balon.nombre', 'es', 'Prueba de Lanzamiento de Balón Medicinal (2 kg)'),
('ejercicio.lanzamiento_balon.objetivo', 'es', 'Evaluar la potencia de las extremidades superiores.'),
('ejercicio.lanzamiento_balon.descripcion', 'es', 'Material: Cinta métrica, balón medicinal de 2 kg. Procedimiento: 1. Cinta métrica fijada perpendicular a la pared, punto cero junto a la pared. 2. Alumno sentado, rodillas extendidas, espalda pegada a la pared. 3. Sujeta el balón cerca del pecho. 4. A la señal, lanzar el balón lo más lejos posible, manteniendo la espalda contra la pared. 5. Registrar distancia donde el balón tocó el suelo por primera vez. 6. Dos intentos, registrar el mejor. Nota: Registrar en cm con un decimal.'),

('ejercicio.salto_horizontal_proesp.nombre', 'es', 'Prueba de Salto Horizontal (PROESP)'),
('ejercicio.salto_horizontal_proesp.objetivo', 'es', 'Evaluar la potencia de las extremidades inferiores.'),
('ejercicio.salto_horizontal_proesp.descripcion', 'es', 'Material: Cinta métrica. Procedimiento: 1. Cinta métrica fijada al suelo, perpendicular a la línea de salida. 2. Alumno detrás de la línea, pies paralelos, rodillas semiflexionadas. 3. A la señal, saltar la mayor distancia posible, aterrizando con ambos pies. 4. Dos intentos, registrar el mejor. Nota: Registrar en cm con un decimal.'),

('ejercicio.agilidad_4x4.nombre', 'es', 'Prueba de 4x4 metros cuadrados (Agilidad)'),
('ejercicio.agilidad_4x4.objetivo', 'es', 'Evaluar la agilidad.'),
('ejercicio.agilidad_4x4.descripcion', 'es', 'Material: Cronómetro, cuatro conos. Suelo antideslizante. Procedimiento: 1. Trazar un cuadrado de 4m. Cono en cada ángulo. 2. Alumno detrás de la línea de salida. 3. A la señal, correr y tocar el cono diagonal. 4. Correr a tocar el cono a su izquierda (o derecha). 5. Correr a tocar el siguiente cono diagonal. 6. Correr al último cono (punto de partida). 7. Dos intentos, registrar el mejor tiempo. Nota: Registrar en segundos con dos decimales.'),

('ejercicio.carrera_20m.nombre', 'es', 'Prueba de Carrera de 20 metros (Velocidad)'),
('ejercicio.carrera_20m.objetivo', 'es', 'Evaluar la velocidad.'),
('ejercicio.carrera_20m.descripcion', 'es', 'Material: Cronómetro, conos. Procedimiento: 1. Pista de 20m marcada con línea de salida, línea de cronometraje (20m) y línea de meta (2m después). 2. Alumno parte de pie detrás de la primera línea. 3. A la señal, avanzar lo más rápido posible hacia la línea de meta. 4. Dos intentos, registrar el mejor tiempo. Nota: Registrar en segundos con dos decimales.');