-- ============================================================================
-- ARQUITECTURA MAESTRA SINCRONIZADA (Java <-> DB)
-- ============================================================================
DROP ALL OBJECTS;

-- 1. USUARIOS
CREATE TABLE usuarios (
          id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
          username VARCHAR(150) NOT NULL UNIQUE,
          email VARCHAR(150) NOT NULL UNIQUE,
          password_hash VARCHAR(255) NOT NULL,
          nombre VARCHAR(150) NOT NULL,
          apellido VARCHAR(150) NOT NULL,
          activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE roles (
           id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
           nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios_roles (
            id_usuario BIGINT NOT NULL,
            id_rol BIGINT NOT NULL,
            PRIMARY KEY (id_usuario, id_rol),
            CONSTRAINT fk_ur_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
            CONSTRAINT fk_ur_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE
);

-- 2. PERFILES
CREATE TABLE senseis (
         id_sensei BIGINT AUTO_INCREMENT PRIMARY KEY,
         id_usuario BIGINT NOT NULL UNIQUE,
         grado_cinturon VARCHAR(50) NOT NULL,
         anos_practica INT NULL,
         biografia TEXT NULL,
         ruta_certificaciones_archivo VARCHAR(255) NULL,
         saldo_wallet NUMERIC,
         total_ganado_historico NUMERIC,
         datos_bancarios_nequi VARCHAR(20),
         CONSTRAINT fk_senseis_user FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE judokas (
         id_judoka BIGINT AUTO_INCREMENT PRIMARY KEY,
         id_usuario BIGINT NOT NULL UNIQUE,
         id_sensei BIGINT NULL,
         peso_kg DECIMAL(5,2),
         estatura_cm DECIMAL(5,2),
         fecha_nacimiento DATE NOT NULL,
         sexo ENUM('MASCULINO', 'FEMENINO', 'OTRO') NOT NULL,
         grado_cinturon VARCHAR(50) NOT NULL,
         palmares TEXT NULL,
         ocupacion_principal VARCHAR(200) NULL,
         es_competidor_activo TINYINT(1) DEFAULT 0,
         estado ENUM('PENDIENTE', 'EN_REVISION', 'ACTIVO', 'INACTIVO', 'RECHAZADO') NOT NULL DEFAULT 'ACTIVO',
         eps VARCHAR(100),
         ruta_certificado_eps VARCHAR(255),
         celular VARCHAR(20),
         nombre_acudiente VARCHAR(255),
         telefono_acudiente VARCHAR(20),
         ruta_autorizacion_waiver VARCHAR(255),
         url_foto_perfil VARCHAR(255),
         fecha_pre_registro DATETIME NULL,
         matricula_pagada TINYINT(1) DEFAULT 0,
         fecha_vencimiento_suscripcion DATETIME NULL,
         suscripcion_activa TINYINT(1) DEFAULT 0,
         CONSTRAINT fk_judokas_user FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
         CONSTRAINT fk_judokas_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

CREATE TABLE mecenas (
                         id_mecenas BIGINT AUTO_INCREMENT PRIMARY KEY,
                         id_usuario BIGINT NOT NULL UNIQUE,
                         tipo_mecenas ENUM('PERSONA_NATURAL', 'EMPRESA') NOT NULL,
                         nombre_empresa VARCHAR(255) NULL,
                         nit_empresa VARCHAR(50) NULL,
                         descripcion_patrocinio TEXT NULL,
                         CONSTRAINT fk_mecenas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- 3. COMPETENCIAS (PALMARÉS)
CREATE TABLE palmares (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          id_judoka BIGINT NOT NULL,
                          nombre_campeonato VARCHAR(255) NULL,
                          sede VARCHAR(255) NULL,
                          fecha DATE NULL,
                          nivel ENUM('INTERNACIONAL', 'NACIONAL', 'REGIONAL', 'DEPARTAMENTAL', 'LOCAL') NULL,
                          resultado ENUM('ORO', 'PLATA', 'BRONCE', 'QUINTO', 'PARTICIPACION') NULL,
                          url_foto VARCHAR(255) NULL,
                          url_video VARCHAR(255) NULL,
                          CONSTRAINT fk_palmares_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE
);

-- 4. GESTIÓN
CREATE TABLE grupos_entrenamiento (
                                      id_grupo BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      nombre VARCHAR(150) NOT NULL UNIQUE,
                                      descripcion TEXT,
                                      id_sensei BIGINT NULL,
                                      CONSTRAINT fk_grupo_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

CREATE TABLE judoka_grupos (
                               id_judoka BIGINT NOT NULL,
                               id_grupo BIGINT NOT NULL,
                               PRIMARY KEY (id_judoka, id_grupo),
                               CONSTRAINT fk_jg_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                               CONSTRAINT fk_jg_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_entrenamiento(id_grupo) ON DELETE CASCADE
);

-- 5. PLANES Y TAREAS
CREATE TABLE tareas_diarias (
                                id_tarea BIGINT AUTO_INCREMENT PRIMARY KEY,
                                nombre VARCHAR(255) NOT NULL,
                                descripcion TEXT NULL,
                                video_url VARCHAR(255) NULL,
                                meta_texto VARCHAR(100) NULL,
                                id_sensei BIGINT NULL,
                                CONSTRAINT fk_tarea_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

CREATE TABLE planes_entrenamiento (
                                      id_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      id_sensei BIGINT NOT NULL,
                                      tipo_sesion ENUM('TECNICA', 'COMBATE', 'ACONDICIONAMIENTO', 'ENTRENAMIENTO', 'EVALUACION') NOT NULL DEFAULT 'ENTRENAMIENTO',
                                      nombre_plan VARCHAR(200) NOT NULL,
                                      fecha_asignacion DATE NOT NULL,
                                      estado ENUM('ACTIVO', 'COMPLETADO', 'CANCELADO') NOT NULL,
                                      CONSTRAINT fk_plan_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE CASCADE
);

CREATE TABLE plan_grupos (
                             id_plan BIGINT NOT NULL,
                             id_grupo BIGINT NOT NULL,
                             PRIMARY KEY (id_plan, id_grupo),
                             CONSTRAINT fk_pg_plan FOREIGN KEY (id_plan) REFERENCES planes_entrenamiento(id_plan) ON DELETE CASCADE,
                             CONSTRAINT fk_pg_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_entrenamiento(id_grupo) ON DELETE CASCADE
);

-- 6. CORE MÉTRICAS
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
                                  categoria VARCHAR(50),
                                  video_url VARCHAR(255) NULL
);

CREATE TABLE prueba_estandar_metricas (
                                          id_ejercicio BIGINT NOT NULL,
                                          id_metrica BIGINT NOT NULL,
                                          PRIMARY KEY (id_ejercicio, id_metrica),
                                          CONSTRAINT fk_pem_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
                                          CONSTRAINT fk_pem_metrica FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica) ON DELETE CASCADE
);

CREATE TABLE normas_evaluacion (
                                   id_norma BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   fuente VARCHAR(100) NOT NULL,
                                   id_ejercicio BIGINT NOT NULL,
                                   id_metrica BIGINT NOT NULL,
                                   sexo ENUM('MASCULINO', 'FEMENINO') NOT NULL,
                                   edad_min INT NOT NULL,
                                   edad_max INT NOT NULL,
                                   clasificacion ENUM('EXCELENTE', 'MUY_BIEN', 'BUENO', 'RAZONABLE', 'REGULAR', 'DEBIL', 'MUY_DEBIL', 'ZONA_DE_RIESGO') NOT NULL,
                                   valor_min DOUBLE NULL,
                                   valor_max DOUBLE NULL,
                                   CONSTRAINT fk_norma_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
                                   CONSTRAINT fk_norma_metrica FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica) ON DELETE CASCADE
);

CREATE TABLE ejercicios_planificados (
                                         id_ejercicio_plan BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         id_plan BIGINT NOT NULL,
                                         id_prueba_estandar BIGINT NULL,
                                         id_tarea_diaria BIGINT NULL,
                                         notas_sensei VARCHAR(255) NULL,
                                         orden SMALLINT NOT NULL DEFAULT 0,
                                         CONSTRAINT fk_ep_plan FOREIGN KEY (id_plan) REFERENCES planes_entrenamiento(id_plan) ON DELETE CASCADE,
                                         CONSTRAINT fk_ep_prueba FOREIGN KEY (id_prueba_estandar) REFERENCES pruebas_estandar(id_ejercicio) ON DELETE CASCADE,
                                         CONSTRAINT fk_ep_tarea FOREIGN KEY (id_tarea_diaria) REFERENCES tareas_diarias(id_tarea) ON DELETE CASCADE
);

CREATE TABLE plan_tarea_dias (
                                 id_ejercicio_plan BIGINT NOT NULL,
                                 dia_semana VARCHAR(20) NOT NULL,
                                 PRIMARY KEY (id_ejercicio_plan, dia_semana),
                                 CONSTRAINT fk_ptd_ejercicio FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE
);

-- 7. RESULTADOS Y ASISTENCIA
CREATE TABLE resultados_pruebas (
                                    id_resultado BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    id_judoka BIGINT NOT NULL,
                                    id_ejercicio_plan BIGINT NOT NULL,
                                    id_metrica BIGINT NOT NULL,
                                    valor DOUBLE NOT NULL,
                                    numero_intento TINYINT NULL,
                                    notas_judoka VARCHAR(255) NULL,
                                    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT fk_res_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                                    CONSTRAINT fk_res_ejercicio FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE,
                                    CONSTRAINT fk_res_metrica FOREIGN KEY (id_metrica) REFERENCES metricas(id_metrica) ON DELETE CASCADE
);

CREATE TABLE ejecuciones_tareas (
                                    id_ejecucion BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    id_judoka BIGINT NOT NULL,
                                    id_ejercicio_plan BIGINT NOT NULL,
                                    completado TINYINT(1) NOT NULL DEFAULT 0,
                                    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    latitud DECIMAL(10,8) NULL,
                                    longitud DECIMAL(11,8) NULL,
                                    CONSTRAINT fk_ejec_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                                    CONSTRAINT fk_ejec_ejercicio FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE
);

CREATE TABLE sesiones_programadas (
                                      id_sesion BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      nombre VARCHAR(255) NOT NULL,
                                      tipo_sesion ENUM('TECNICA', 'COMBATE', 'ACONDICIONAMIENTO', 'EVALUACION', 'OTRO') NOT NULL,
                                      fecha_hora_inicio DATETIME NOT NULL,
                                      fecha_hora_fin DATETIME NOT NULL,
                                      id_grupo BIGINT NULL,
                                      id_sensei BIGINT NULL,
                                      latitud DECIMAL(10,8) NULL,
                                      longitud DECIMAL(11,8) NULL,
                                      radio_permitido_metros INT DEFAULT 100,
                                      version BIGINT DEFAULT 0,
                                      CONSTRAINT fk_sesiones_grupo FOREIGN KEY (id_grupo) REFERENCES grupos_entrenamiento(id_grupo) ON DELETE SET NULL,
                                      CONSTRAINT fk_sesiones_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

CREATE TABLE asistencias (
                             id_asistencia BIGINT AUTO_INCREMENT PRIMARY KEY,
                             id_judoka BIGINT NOT NULL,
                             id_sesion BIGINT NOT NULL,
                             presente TINYINT(1) NOT NULL,
                             tipo_sesion ENUM('TECNICA', 'COMBATE', 'ACONDICIONAMIENTO', 'EVALUACION', 'OTRO') NULL,
                             latitud DECIMAL(10,8) NULL,
                             longitud DECIMAL(11,8) NULL,
                             fecha_hora_marcacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             notas VARCHAR(255) NULL,
                             CONSTRAINT fk_asistencias_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                             CONSTRAINT fk_asistencias_sesion FOREIGN KEY (id_sesion) REFERENCES sesiones_programadas(id_sesion) ON DELETE CASCADE,
                             UNIQUE (id_judoka, id_sesion)
);

-- 8. EXTRAS (Chat, Gamificación, Finanzas)
CREATE TABLE mensajes_chat (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               id_autor BIGINT NOT NULL,
                               id_sensei BIGINT NOT NULL,
                               contenido TEXT NOT NULL,
                               fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_chat_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                               CONSTRAINT fk_chat_dojo FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE CASCADE
);

CREATE TABLE publicaciones (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               id_autor BIGINT NOT NULL,
                               id_sensei BIGINT NULL,
                               contenido TEXT,
                               imagen_url VARCHAR(255),
                               fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
                               likes INT DEFAULT 0,
                               CONSTRAINT fk_pub_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                               CONSTRAINT fk_pub_dojo FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE CASCADE
);

CREATE TABLE comentarios (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             id_publicacion BIGINT NOT NULL,
                             id_autor BIGINT NOT NULL,
                             contenido TEXT NOT NULL,
                             fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_com_pub FOREIGN KEY (id_publicacion) REFERENCES publicaciones(id) ON DELETE CASCADE,
                             CONSTRAINT fk_com_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE traducciones (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              clave VARCHAR(255) NOT NULL,
                              idioma VARCHAR(5) NOT NULL,
                              texto TEXT NOT NULL,
                              UNIQUE (clave, idioma)
);

CREATE TABLE insignias (
                           id_insignia BIGINT AUTO_INCREMENT PRIMARY KEY,
                           clave VARCHAR(50) NOT NULL UNIQUE,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion VARCHAR(255),
                           icono_vaadin VARCHAR(50),
                           categoria ENUM('SHIN', 'GI', 'TAI') NOT NULL,
                           nivel_requerido INT DEFAULT 1
);

CREATE TABLE judoka_insignias (
                                  id_logro BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  id_judoka BIGINT NOT NULL,
                                  id_insignia BIGINT NOT NULL,
                                  id_sensei BIGINT NULL,
                                  fecha_obtencion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_logro_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                                  CONSTRAINT fk_logro_insignia FOREIGN KEY (id_insignia) REFERENCES insignias(id_insignia) ON DELETE CASCADE,
                                  CONSTRAINT fk_logro_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL,
                                  UNIQUE (id_judoka, id_insignia)
);

CREATE TABLE documentos_requisitos (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       id_judoka BIGINT NOT NULL,
                                       tipo ENUM('WAIVER', 'CERTIFICADO_MEDICO', 'EPS', 'DOCUMENTO_IDENTIDAD') NOT NULL,
                                       url_archivo VARCHAR(255) NOT NULL,
                                       id_sensei BIGINT NULL,
                                       fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       fecha_validacion DATETIME NULL,
                                       validado_por_sensei TINYINT(1) DEFAULT 0,
                                       CONSTRAINT fk_docs_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                                       CONSTRAINT fk_docs_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

CREATE TABLE campos_entrenamiento (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      id_judoka BIGINT NOT NULL,
                                      id_sensei BIGINT NULL,
                                      nombre VARCHAR(255) NOT NULL,
                                      ubicacion VARCHAR(255),
                                      fecha_inicio DATE NOT NULL,
                                      fecha_fin DATE NOT NULL,
                                      objetivo VARCHAR(255),
                                      completado TINYINT(1) DEFAULT 0,
                                      puntos_ascenso INT DEFAULT 0,
                                      CONSTRAINT fk_campo_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
                                      CONSTRAINT fk_campo_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
);

-- ============================================================================
-- 9. MÓDULO FINANCIERO Y COMERCIAL (Sincronizado con Java)
-- ============================================================================

-- Configuración Global
CREATE TABLE configuracion_sistema (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nombre_organizacion VARCHAR(255) NOT NULL DEFAULT 'Mi Club de Judo',
                                       nivel VARCHAR(30),
                                       telefono_contacto VARCHAR(50) NULL,
                                       email_soporte VARCHAR(100) NULL,
                                       url_logo VARCHAR(255) NULL,
                                       moneda VARCHAR(10) DEFAULT 'COP'
);

INSERT IGNORE INTO configuracion_sistema (id, nombre_organizacion, nivel, moneda)
VALUES (1, 'Club de Judo Demo', 'CLUB', 'COP');

-- Conceptos Contables (Ej: "Mensualidad", "Compra Uniforme")
CREATE TABLE conceptos_financieros (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nombre VARCHAR(100) NOT NULL UNIQUE,
                                       tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
                                       valor_sugerido DECIMAL(12,2) NULL
);

-- Catálogo de Productos (Suscripciones y Servicios)
CREATE TABLE productos (
                           id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL,
                           descripcion VARCHAR(255),
                           precio_cop DECIMAL(12,2) NOT NULL,
                           tipo_subscripcion ENUM('PAGO_UNICO', 'MENSUAL', 'BIMENSUAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL') NOT NULL,
                           activo TINYINT(1) NOT NULL DEFAULT 1,
                           stripe_price_id VARCHAR(100) NULL UNIQUE
);

-- Pasarela de Pagos (Registro de Transacciones)
CREATE TABLE pagos (
                       id_pago BIGINT AUTO_INCREMENT PRIMARY KEY,
                       id_usuario BIGINT NOT NULL,
                       id_producto BIGINT NOT NULL,
                       monto_cop DECIMAL(12,2) NOT NULL,
                       fecha_creacion DATETIME NOT NULL,
                       fecha_pago_exitoso DATETIME NULL,
                       estado ENUM('PENDIENTE', 'PAGADO', 'FALLIDO') NOT NULL,
                       metodo_pago ENUM('EFECTIVO', 'NEQUI') NOT NULL,
                       url_comprobante VARCHAR(500) NULL,
                       CONSTRAINT fk_pagos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                       CONSTRAINT fk_pagos_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
);

-- Movimientos de Caja (Libro Diario)
CREATE TABLE movimientos_caja (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  monto DECIMAL(12,2) NOT NULL,
                                  tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
                                  metodo_pago ENUM('EFECTIVO', 'NEQUI') NOT NULL,
                                  id_concepto BIGINT NOT NULL,
                                  id_judoka BIGINT NULL,
                                  observacion TEXT NULL,
                                  url_soporte VARCHAR(255) NULL,
                                  registrado_por VARCHAR(150) NULL,
                                  CONSTRAINT fk_mov_concepto FOREIGN KEY (id_concepto) REFERENCES conceptos_financieros(id) ON DELETE SET NULL,
                                  CONSTRAINT fk_mov_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE SET NULL
);

-- Inventario (Tienda Física)
CREATE TABLE inventario_articulos (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      nombre VARCHAR(150) NOT NULL,
                                      descripcion VARCHAR(255) NULL,
                                      cantidad_stock INT NOT NULL DEFAULT 0,
                                      stock_minimo_alerta INT DEFAULT 2,
                                      precio_venta DECIMAL(12,2) NOT NULL,
                                      precio_costo DECIMAL(12,2) NOT NULL
);
-- Tabla para la entidad Reflexion.java
CREATE TABLE reflexiones (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             id_judoka BIGINT NOT NULL,
                             contenido TEXT NOT NULL,
                             fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             fecha_ultima_edicion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Integridad referencial
                             CONSTRAINT fk_reflexion_judoka
                                 FOREIGN KEY (id_judoka)
                                     REFERENCES judokas(id_judoka)
                                     ON DELETE CASCADE
);

-- Opcional: Índice para buscar rápidamente las reflexiones de un alumno
CREATE INDEX idx_reflexiones_judoka ON reflexiones(id_judoka);