-- ============================================================================
-- VERSIÓN 8: Sincronización Final con Código Java (Admisiones, Finanzas, GPS)
-- ============================================================================

-- 1. ADMISIONES: Actualizar tabla JUDOKAS
-- Agregamos estados y control de ingreso
ALTER TABLE judokas
    ADD COLUMN estado ENUM('PENDIENTE', 'EN_REVISION', 'ACTIVO', 'INACTIVO', 'RECHAZADO') NOT NULL DEFAULT 'ACTIVO',
    ADD COLUMN fecha_pre_registro DATETIME NULL,
    ADD COLUMN matricula_pagada TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN eps VARCHAR(100) NULL,
    ADD COLUMN ruta_certificado_eps VARCHAR(255) NULL;

-- Actualizar registros viejos para que no sean borrados por el CleanupService
UPDATE judokas SET estado = 'ACTIVO', fecha_pre_registro = NOW() WHERE estado = 'PENDIENTE';

-- 2. ADMISIONES: Nueva tabla DOCUMENTOS_REQUISITOS
CREATE TABLE documentos_requisitos (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   id_judoka BIGINT NOT NULL,
   tipo ENUM('WAIVER', 'CERTIFICADO_MEDICO', 'EPS', 'DOCUMENTO_IDENTIDAD') NOT NULL,
   url_archivo VARCHAR(255) NOT NULL,
   id_sensei BIGINT NULL COMMENT 'Null = Pendiente, ID = Validado por...',
   fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   fecha_validacion DATETIME NULL,

   CONSTRAINT fk_docs_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
   CONSTRAINT fk_docs_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. GPS: Actualizar SESIONES_PROGRAMADAS
-- Agregamos coordenadas para el check-in geo-referenciado
ALTER TABLE sesiones_programadas
    ADD COLUMN latitud DECIMAL(10,8) NULL,
    ADD COLUMN longitud DECIMAL(11,8) NULL,
    ADD COLUMN radio_permitido_metros INT NOT NULL DEFAULT 100;




-- 4. COMPETENCIA: Palmarés Estructurado
-- Reemplazamos el campo de texto simple por una tabla relacional
CREATE TABLE participacion_competencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_judoka BIGINT NOT NULL,
    nombre_evento VARCHAR(200) NOT NULL,
    lugar VARCHAR(100) NULL,
    fecha DATE NOT NULL,
    nivel ENUM('LOCAL', 'DEPARTAMENTAL', 'NACIONAL', 'INTERNACIONAL') NOT NULL,
    resultado ENUM('ORO', 'PLATA', 'BRONCE', 'QUINTO', 'SEPTIMO', 'PARTICIPACION') NOT NULL,
    video_url VARCHAR(255) NULL,
    CONSTRAINT fk_palmares_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 5. FINANZAS Y CONFIGURACIÓN (Módulo Club)
-- CORREGIDO: Tabla estructurada para coincidir con la Entidad Java
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_organizacion VARCHAR(255) NOT NULL DEFAULT 'Mi Club de Judo',
    nivel ENUM('CLUB', 'LIGA', 'FEDERACION') NOT NULL DEFAULT 'CLUB',
    telefono_contacto VARCHAR(50) NULL,
    email_soporte VARCHAR(100) NULL,
    moneda VARCHAR(10) DEFAULT 'COP'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inicializar configuración por defecto (Si está vacía)
INSERT IGNORE INTO configuracion_sistema (id, nombre_organizacion, nivel, moneda)
VALUES (1, 'Club de Judo Demo', 'CLUB', 'COP');

CREATE TABLE conceptos_financieros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
    valor_sugerido DECIMAL(12,2) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimientos_caja (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    descripcion VARCHAR(255) NULL,
    metodo_pago ENUM('EFECTIVO', 'TRANSFERENCIA', 'TARJETA') NOT NULL,
    id_concepto BIGINT NULL,
    id_usuario_responsable BIGINT NULL, -- Quién registró el movimiento
    CONSTRAINT fk_mov_concepto FOREIGN KEY (id_concepto) REFERENCES conceptos_financieros(id) ON DELETE SET NULL,
    CONSTRAINT fk_mov_usuario FOREIGN KEY (id_usuario_responsable) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. INVENTARIO (Tienda)
CREATE TABLE articulos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cantidad_stock INT NOT NULL DEFAULT 0,
    precio_venta DECIMAL(12,2) NOT NULL,
    precio_costo DECIMAL(12,2) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

