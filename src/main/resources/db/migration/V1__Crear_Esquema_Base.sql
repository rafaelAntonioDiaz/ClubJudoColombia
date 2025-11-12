-- Versión 1: Creación de todas las tablas del esquema inicial (CORREGIDO Y CONSOLIDADO)

-- 1. Tablas de Usuarios y Perfiles
CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    apellido VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
CREATE TABLE usuarios_roles (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);
CREATE TABLE judokas (
    id_judoka BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    peso_kg DOUBLE,
    estatura_cm DOUBLE,
    fecha_nacimiento DATE NOT NULL,
    sexo VARCHAR(50) NOT NULL,
    grado_cinturon VARCHAR(50) NOT NULL,
    palmares TEXT,
    ocupacion_principal VARCHAR(200),
    es_competidor_activo BOOLEAN NOT NULL DEFAULT FALSE,
    nombre_acudiente VARCHAR(255),
    telefono_acudiente VARCHAR(20),
    ruta_autorizacion_waiver VARCHAR(255),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);
CREATE TABLE senseis (
    id_sensei BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    grado_cinturon VARCHAR(50) NOT NULL,
    anos_practica INT,
    ruta_certificaciones_archivo VARCHAR(255),
    biografia TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);
CREATE TABLE mecenas (
    id_mecenas BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    tipo_mecenas VARCHAR(50) NOT NULL,
    nombre_empresa VARCHAR(255),
    nit_empresa VARCHAR(50),
    descripcion_patrocinio TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- 2. Tablas de Pruebas Estándar (Flujo 1: Sensei)
CREATE TABLE metricas (
    id_metrica BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_key VARCHAR(255) NOT NULL UNIQUE,
    unidad VARCHAR(10) NOT NULL
);
CREATE TABLE pruebas_estandar (
    id_ejercicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_key VARCHAR(200) NOT NULL UNIQUE,
    objetivo_key TEXT NOT NULL,
    descripcion_key TEXT NOT NULL,
    categoria VARCHAR(50) NOT NULL
);
CREATE TABLE prueba_estandar_metricas (
    id_ejercicio BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    PRIMARY KEY (id_ejercicio, id_metrica),
    FOREIGN KEY (id_ejercicio) REFERENCES pruebas_estandar(id_ejercicio),
    FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica)
);
CREATE TABLE normas_evaluacion (
    id_norma BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente VARCHAR(100) NOT NULL,
    id_ejercicio BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    sexo VARCHAR(50) NOT NULL,
    edad_min INT NOT NULL,
    edad_max INT NOT NULL,
    clasificacion VARCHAR(50) NOT NULL,
    valor_min DOUBLE,
    valor_max DOUBLE,
    FOREIGN KEY (id_ejercicio) REFERENCES pruebas_estandar(id_ejercicio), -- CORREGIDO
    FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica)
);

-- 3. Tablas de Tareas Diarias (Flujo 2: Judoka)
CREATE TABLE tareas_diarias (
    id_tarea BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    video_url VARCHAR(255),
    meta_texto VARCHAR(100),
    id_sensei_creador BIGINT,
    FOREIGN KEY (id_sensei_creador) REFERENCES senseis(id_sensei)
);

-- 4. Tablas de Grupos y Planes
CREATE TABLE grupos_entrenamiento (
    id_grupo BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT
);
CREATE TABLE judoka_grupos (
    id_judoka BIGINT NOT NULL,
    id_grupo BIGINT NOT NULL,
    PRIMARY KEY (id_judoka, id_grupo),
    FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka),
    FOREIGN KEY (id_grupo) REFERENCES grupos_entrenamiento(id_grupo)
);
CREATE TABLE planes_entrenamiento (
    id_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sensei_creador BIGINT NOT NULL,
    nombre_plan VARCHAR(200) NOT NULL,
    fecha_asignacion DATE NOT NULL,
    estado VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_sensei_creador) REFERENCES senseis(id_sensei)
);
CREATE TABLE plan_grupos (
    id_plan BIGINT NOT NULL,
    id_grupo BIGINT NOT NULL,
    PRIMARY KEY (id_plan, id_grupo),
    FOREIGN KEY (id_plan) REFERENCES planes_entrenamiento(id_plan),
    FOREIGN KEY (id_grupo) REFERENCES grupos_entrenamiento(id_grupo)
);
CREATE TABLE ejercicios_planificados (
    id_ejercicio_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_plan BIGINT NOT NULL,
    id_prueba_estandar BIGINT NULL,
    id_tarea_diaria BIGINT NULL,
    notas_sensei VARCHAR(255),
    orden INT,
    FOREIGN KEY (id_plan) REFERENCES planes_entrenamiento(id_plan),
    FOREIGN KEY (id_prueba_estandar) REFERENCES pruebas_estandar(id_ejercicio),
    FOREIGN KEY (id_tarea_diaria) REFERENCES tareas_diarias(id_tarea)
);

-- 5. Tablas de Resultados (Pruebas y Tareas)
CREATE TABLE resultados_pruebas (
    id_resultado BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_ejercicio_plan BIGINT NOT NULL,
    id_metrica BIGINT NOT NULL,
    valor DOUBLE NOT NULL,
    numero_intento INT,
    notas_judoka VARCHAR(255),
    fecha_registro DATETIME NOT NULL,
    FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka),
    FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan),
    FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica)
);
CREATE TABLE ejecuciones_tareas (
    id_ejecucion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_ejercicio_plan BIGINT NOT NULL,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro DATETIME NOT NULL,
    latitud DOUBLE,
    longitud DOUBLE,
    FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka),
    FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan)
);

-- 6. Tablas de Asistencia
CREATE TABLE sesiones_programadas (
    id_sesion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_sensei BIGINT NOT NULL,
    grupo VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    es_excepcion BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei)
);
CREATE TABLE asistencias (
    id_asistencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    id_sesion BIGINT NOT NULL,
    presente BOOLEAN NOT NULL,
    fecha_hora_marcacion DATETIME NOT NULL,
    notas VARCHAR(255),
    UNIQUE KEY uk_judoka_sesion (id_judoka, id_sesion),
    FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka),
    FOREIGN KEY (id_sesion) REFERENCES sesiones_programadas(id_sesion)
);