-- Categorías: SHIN (Mente), GI (Técnica), TAI (Cuerpo)
CREATE TABLE insignias (
    id_insignia BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'BADGE_CONSTANCIA'
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    icono_vaadin VARCHAR(50) NOT NULL, -- Ej: 'FIRE'
    categoria ENUM('SHIN', 'GI', 'TAI') NOT NULL,
    nivel_requerido INT DEFAULT 1 -- 1=Básico, 2=Medio, 3=Difícil
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Relación Judoka <-> Insignias
CREATE TABLE judoka_insignias (
    id_logro BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_insignia BIGINT NOT NULL,
    fecha_obtencion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_logro_judoka FOREIGN KEY (id_judoka)
        REFERENCES judokas(id_judoka) ON DELETE CASCADE,
    CONSTRAINT fk_logro_insignia FOREIGN KEY (id_insignia)
        REFERENCES insignias(id_insignia) ON DELETE CASCADE,

    UNIQUE KEY uk_judoka_insignia (id_judoka, id_insignia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --- CARGA INICIAL DE INSIGNIAS (SHIN-GI-TAI) ---

-- SHIN (Mente/Espíritu - Rojo/Fuego)
INSERT INTO insignias (clave, nombre, descripcion, icono_vaadin, categoria, nivel_requerido) VALUES
('SHIN_INICIO', 'Primer Paso', 'Completaste tu primer entrenamiento. El viaje comienza.', 'FOOT', 'SHIN', 1),
('SHIN_CONSTANCIA', 'Espíritu Indomable', '10 Asistencias consecutivas sin faltar.', 'FIRE', 'SHIN', 2),
('SHIN_COMPROMISO', 'Guardián del Dojo', '50 Asistencias totales acumuladas.', 'INSTITUTION', 'SHIN', 3);

-- GI (Técnica - Blanco/Plata)
INSERT INTO insignias (clave, nombre, descripcion, icono_vaadin, categoria, nivel_requerido) VALUES
('GI_CINTURON', 'Nuevo Horizonte', 'Has ascendido de grado (Cinturón).', 'ACADEMY_CAP', 'GI', 3),
('GI_TECNICO', 'Técnica Pura', 'Evaluación técnica sobresaliente.', 'MAGIC', 'GI', 2);

-- TAI (Cuerpo - Azul/Agua)
INSERT INTO insignias (clave, nombre, descripcion, icono_vaadin, categoria, nivel_requerido) VALUES
('TAI_HERCULES', 'Hércules', 'Superaste 40 flexiones en un minuto.', 'DUMBBELL', 'TAI', 2),
('TAI_VELOCIDAD', 'Relámpago', 'Corriste 20m en menos de 3.5 segundos.', 'STOPWATCH', 'TAI', 2),
('TAI_RESISTENCIA', 'Pulmones de Acero', 'Índice SJFT Excelente.', 'HEART', 'TAI', 3);