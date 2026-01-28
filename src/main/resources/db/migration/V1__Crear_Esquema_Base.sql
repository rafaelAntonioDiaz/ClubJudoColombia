-- ============================================================================
-- VERSIÓN 1: Creación del Esquema Base - OPTIMIZADO
-- ============================================================================
-- Cambios aplicados:
-- 1. Uso de ENUM para campos con valores limitados
-- 2. Índices compuestos para búsquedas frecuentes
-- 3. Constraint names explícitos para mejor debugging
-- 4. Comentarios en columnas críticas
-- 5. Mejora en tipos de datos (TINYINT para booleanos)
-- ============================================================================

-- ====================
-- 1. TABLAS DE USUARIOS Y PERFILES
-- ====================

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    apellido VARCHAR(150) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1=Activo, 0=Inactivo',
    INDEX idx_activo (activo),
    INDEX idx_nombre_apellido (apellido, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios_roles (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_rol FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol) ON DELETE CASCADE,
    INDEX idx_rol (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE judokas (
    id_judoka BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    peso_kg DECIMAL(5,2) NULL COMMENT 'Peso en kilogramos con 2 decimales',
    estatura_cm DECIMAL(5,2) NULL COMMENT 'Estatura en centímetros con 2 decimales',
    fecha_nacimiento DATE NOT NULL,
    sexo ENUM('MASCULINO', 'FEMENINO', 'OTRO') NOT NULL,
    grado_cinturon VARCHAR(50) NOT NULL,
    palmares TEXT NULL,
    ocupacion_principal VARCHAR(200) NULL,
    es_competidor_activo TINYINT(1) NOT NULL DEFAULT 0,
    celular VARCHAR(13) NULL,
    nombre_acudiente VARCHAR(255) NULL,
    telefono_acudiente VARCHAR(20) NULL,
    ruta_autorizacion_waiver VARCHAR(255) NULL,
    CONSTRAINT fk_judokas_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_sexo (sexo),
    INDEX idx_fecha_nacimiento (fecha_nacimiento),
    INDEX idx_competidor_activo (es_competidor_activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE senseis (
    id_sensei BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    grado_cinturon VARCHAR(50) NOT NULL,
    anos_practica INT NULL,
    ruta_certificaciones_archivo VARCHAR(255) NULL,
    biografia TEXT NULL,
    CONSTRAINT fk_senseis_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mecenas (
    id_mecenas BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    tipo_mecenas ENUM('INDIVIDUAL', 'EMPRESA', 'INSTITUCION') NOT NULL,
    nombre_empresa VARCHAR(255) NULL,
    nit_empresa VARCHAR(50) NULL,
    descripcion_patrocinio TEXT NULL,
    CONSTRAINT fk_mecenas_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 2. TABLAS DE PRUEBAS ESTÁNDAR
-- ====================

CREATE TABLE metricas (
    id_metrica BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_key VARCHAR(255) NOT NULL UNIQUE,
    unidad VARCHAR(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pruebas_estandar (
    id_ejercicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_key VARCHAR(200) NOT NULL UNIQUE,
    objetivo_key TEXT NOT NULL,
    descripcion_key TEXT NOT NULL,
    categoria ENUM(
        'MEDICION_ANTROPOMETRICA',
        'POTENCIA',
        'RESISTENCIA_ISOMETRICA',
        'APTITUD_ANAEROBICA',
        'APTITUD_AEROBICA',
        'FLEXIBILIDAD',
        'RESISTENCIA_MUSCULAR_LOCALIZADA',
        'AGILIDAD',
        'VELOCIDAD'
    ) NOT NULL,
    video_url VARCHAR(255) NULL,
    INDEX idx_categoria (categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prueba_estandar_metricas (
    id_ejercicio BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    PRIMARY KEY (id_ejercicio, id_metrica),
    CONSTRAINT fk_prueba_metricas_ejercicio FOREIGN KEY (id_ejercicio)
        REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
    CONSTRAINT fk_prueba_metricas_metrica FOREIGN KEY (id_metrica)
        REFERENCES metricas(id_metrica) ON DELETE CASCADE,
    INDEX idx_metrica (id_metrica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE normas_evaluacion (
    id_norma BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente VARCHAR(100) NOT NULL,
    id_ejercicio BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    sexo ENUM('MASCULINO', 'FEMENINO') NOT NULL,
    edad_min INT NOT NULL,
    edad_max INT NOT NULL,
    clasificacion ENUM(
        'EXCELENTE',
        'MUY_BIEN',
        'BUENO',
        'RAZONABLE',
        'REGULAR',
        'DEBIL',
        'MUY_DEBIL',
        'ZONA_DE_RIESGO'
    ) NOT NULL,
    valor_min DOUBLE NULL,
    valor_max DOUBLE NULL,
    CONSTRAINT fk_normas_ejercicio FOREIGN KEY (id_ejercicio)
        REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
    CONSTRAINT fk_normas_metrica FOREIGN KEY (id_metrica)
        REFERENCES metricas(id_metrica) ON DELETE CASCADE,
    -- Índice compuesto para búsquedas de normas por características del judoka
    INDEX idx_busqueda_normas (fuente, id_ejercicio, id_metrica, sexo, edad_min, edad_max),
    INDEX idx_clasificacion (clasificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 3. TABLAS DE TAREAS DIARIAS
-- ====================

CREATE TABLE tareas_diarias (
    id_tarea BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT NULL,
    video_url VARCHAR(255) NULL,
    meta_texto VARCHAR(100) NULL,
    id_sensei BIGINT NULL,
    CONSTRAINT fk_tareas_sensei FOREIGN KEY (id_sensei)
        REFERENCES senseis(id_sensei) ON DELETE SET NULL,
    INDEX idx_sensei_creador (id_sensei)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 4. TABLAS DE GRUPOS Y PLANES
-- ====================

CREATE TABLE grupos_entrenamiento (
  id_grupo BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL UNIQUE,
  descripcion TEXT NULL,
  id_sensei BIGINT NULL,
  CONSTRAINT fk_grupos_sensei FOREIGN KEY (id_sensei)
      REFERENCES senseis(id_sensei) ON DELETE SET NULL,
  INDEX idx_sensei_grupo (id_sensei)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE judoka_grupos (
    id_judoka BIGINT NOT NULL,
    id_grupo BIGINT NOT NULL,
    PRIMARY KEY (id_judoka, id_grupo),
    CONSTRAINT fk_judoka_grupos_judoka FOREIGN KEY (id_judoka)
        REFERENCES judokas(id_judoka) ON DELETE CASCADE,
    CONSTRAINT fk_judoka_grupos_grupo FOREIGN KEY (id_grupo)
        REFERENCES grupos_entrenamiento(id_grupo) ON DELETE CASCADE,
    INDEX idx_grupo (id_grupo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE planes_entrenamiento (
    id_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sensei BIGINT NOT NULL,
    tipo_sesion ENUM(
        'TECNICA',
        'COMBATE',
        'ACONDICIONAMIENTO',
        'ENTRENAMIENTO',
        'EVALUACION'
    ) NOT NULL DEFAULT 'ENTRENAMIENTO',
    nombre_plan VARCHAR(200) NOT NULL,
    fecha_asignacion DATE NOT NULL,
    estado ENUM('ACTIVO', 'COMPLETADO', 'CANCELADO') NOT NULL,
    CONSTRAINT fk_planes_sensei FOREIGN KEY (id_sensei)
        REFERENCES senseis(id_sensei) ON DELETE CASCADE,
    INDEX idx_sensei_fecha (id_sensei, fecha_asignacion),
    INDEX idx_estado (estado),
    INDEX idx_tipo_sesion (tipo_sesion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_grupos (
    id_plan BIGINT NOT NULL,
    id_grupo BIGINT NOT NULL,
    PRIMARY KEY (id_plan, id_grupo),
    CONSTRAINT fk_plan_grupos_plan FOREIGN KEY (id_plan)
        REFERENCES planes_entrenamiento(id_plan) ON DELETE CASCADE,
    CONSTRAINT fk_plan_grupos_grupo FOREIGN KEY (id_grupo)
        REFERENCES grupos_entrenamiento(id_grupo) ON DELETE CASCADE,
    INDEX idx_grupo (id_grupo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ejercicios_planificados (
    id_ejercicio_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_plan BIGINT NOT NULL,
    id_prueba_estandar BIGINT NULL,
    id_tarea_diaria BIGINT NULL,
    notas_sensei VARCHAR(255) NULL,
    orden SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ejercicios_plan_plan FOREIGN KEY (id_plan)
        REFERENCES planes_entrenamiento(id_plan) ON DELETE CASCADE,
    CONSTRAINT fk_ejercicios_plan_prueba FOREIGN KEY (id_prueba_estandar)
        REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
    CONSTRAINT fk_ejercicios_plan_tarea FOREIGN KEY (id_tarea_diaria)
        REFERENCES tareas_diarias(id_tarea) ON DELETE CASCADE,
    -- Al menos uno de los dos debe estar presente
    CONSTRAINT chk_ejercicio_o_tarea CHECK (
        id_prueba_estandar IS NOT NULL OR id_tarea_diaria IS NOT NULL
    ),
    INDEX idx_plan_orden (id_plan, orden),
    INDEX idx_prueba (id_prueba_estandar),
    INDEX idx_tarea (id_tarea_diaria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_tarea_dias (
    id_ejercicio_plan BIGINT NOT NULL,
    -- Usamos VARCHAR para aceptar los nombres en Inglés que envía Java (MONDAY, SUNDAY...)
    dia_semana VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_ejercicio_plan, dia_semana),
    CONSTRAINT fk_plan_dias_ejercicio FOREIGN KEY (id_ejercicio_plan)
        REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE,
    INDEX idx_dia_semana (dia_semana)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 5. TABLAS DE RESULTADOS
-- ====================

CREATE TABLE resultados_pruebas (
    id_resultado BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_ejercicio_plan BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    valor DOUBLE NOT NULL,
    numero_intento TINYINT NULL,
    notas_judoka VARCHAR(255) NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resultados_judoka FOREIGN KEY (id_judoka)
        REFERENCES judokas(id_judoka) ON DELETE CASCADE,
    CONSTRAINT fk_resultados_ejercicio FOREIGN KEY (id_ejercicio_plan)
        REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE,
    CONSTRAINT fk_resultados_metrica FOREIGN KEY (id_metrica)
        REFERENCES metricas(id_metrica) ON DELETE CASCADE,
    INDEX idx_judoka_fecha (id_judoka, fecha_registro),
    INDEX idx_ejercicio_metrica (id_ejercicio_plan, id_metrica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ejecuciones_tareas (
    id_ejecucion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_ejercicio_plan BIGINT NOT NULL,
    completado TINYINT(1) NOT NULL DEFAULT 0,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitud DECIMAL(10,8) NULL,
    longitud DECIMAL(11,8) NULL,
    CONSTRAINT fk_ejecuciones_judoka FOREIGN KEY (id_judoka)
        REFERENCES judokas(id_judoka) ON DELETE CASCADE,
    CONSTRAINT fk_ejecuciones_ejercicio FOREIGN KEY (id_ejercicio_plan)
        REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE,
    INDEX idx_judoka_fecha (id_judoka, fecha_registro),
    INDEX idx_completado (completado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 6. TABLAS DE SESIONES Y ASISTENCIA
-- ====================

CREATE TABLE sesiones_programadas (
    id_sesion BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    tipo_sesion ENUM(
        'TECNICA',
        'COMBATE',
        'ACONDICIONAMIENTO',
        'EVALUACION',
        'OTRO'
    ) NOT NULL,
    fecha_hora_inicio DATETIME NOT NULL,
    fecha_hora_fin DATETIME NOT NULL,
    id_grupo BIGINT NULL,
    id_sensei BIGINT NULL,
    CONSTRAINT fk_sesiones_grupo FOREIGN KEY (id_grupo)
        REFERENCES grupos_entrenamiento(id_grupo) ON DELETE SET NULL,
    CONSTRAINT fk_sesiones_sensei FOREIGN KEY (id_sensei)
        REFERENCES senseis(id_sensei) ON DELETE SET NULL,
    INDEX idx_fecha_inicio (fecha_hora_inicio),
    INDEX idx_grupo_fecha (id_grupo, fecha_hora_inicio),
    INDEX idx_sensei (id_sensei)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE asistencias (
    id_asistencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    tipo_sesion ENUM(
        'TECNICA',
        'COMBATE',
        'ACONDICIONAMIENTO',
        'EVALUACION',
        'OTRO'
    ) NULL,
    id_sesion BIGINT NOT NULL,
    presente TINYINT(1) NOT NULL,
    latitud DECIMAL(10,8) NULL,
    longitud DECIMAL(11,8) NULL,
    fecha_hora_marcacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notas VARCHAR(255) NULL,
    CONSTRAINT fk_asistencias_judoka FOREIGN KEY (id_judoka)
        REFERENCES judokas(id_judoka) ON DELETE CASCADE,
    CONSTRAINT fk_asistencias_sesion FOREIGN KEY (id_sesion)
        REFERENCES sesiones_programadas(id_sesion) ON DELETE CASCADE,
    UNIQUE KEY uk_judoka_sesion (id_judoka, id_sesion),
    INDEX idx_sesion_presente (id_sesion, presente),
    INDEX idx_fecha_marcacion (fecha_hora_marcacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================
-- 7. TABLA DE TRADUCCIONES (i18n)
-- ====================

CREATE TABLE traducciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(255) NOT NULL,
    idioma VARCHAR(5) NOT NULL,
    texto TEXT NOT NULL,
    UNIQUE KEY uk_clave_idioma (clave, idioma),
    INDEX idx_idioma (idioma)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;