-- ============================================================================
-- VERSIÓN 10: Corregir inconsistencias de nombre de columnas
-- ============================================================================
-- Problema:
-- - V1 creó la columna con nombre "tipoSesion" (camelCase - INCORRECTO en SQL)
-- - Hibernate espera "tipo_sesion" (snake_case - CORRECTO en SQL)
-- - Las columnas en SQL deben estar en snake_case por convención
--
-- Solución: Renombrar columnas a estándar snake_case
-- ============================================================================

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS sp_corregir_nombres_columnas()
BEGIN
    DECLARE col_exists INT;
    DECLARE col_new_exists INT;

    -- ========================================================================
    -- 1. TABLA: planes_entrenamiento - Renombrar tipoSesion → tipo_sesion
    -- ========================================================================

    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'planes_entrenamiento'
      AND COLUMN_NAME = 'tipoSesion';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'planes_entrenamiento'
      AND COLUMN_NAME = 'tipo_sesion';

    -- Si existe la columna vieja (tipoSesion) y NO existe la nueva (tipo_sesion)
    IF col_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE planes_entrenamiento
        CHANGE COLUMN tipoSesion tipo_sesion VARCHAR(50) NOT NULL DEFAULT 'ENTRENAMIENTO'
        COMMENT 'Tipo de sesión: TECNICA, COMBATE, ACONDICIONAMIENTO, ENTRENAMIENTO, etc.';
    END IF;

    -- Si NO existe la columna nueva (tipo_sesion), crearla
    IF col_new_exists = 0 AND col_exists = 0 THEN
        ALTER TABLE planes_entrenamiento
        ADD COLUMN tipo_sesion VARCHAR(50) NOT NULL DEFAULT 'ENTRENAMIENTO'
        COMMENT 'Tipo de sesión: TECNICA, COMBATE, ACONDICIONAMIENTO, ENTRENAMIENTO, etc.';
    END IF;

    -- ========================================================================
    -- 2. TABLA: asistencias - Renombrar tipoSesion → tipo_sesion
    -- ========================================================================

    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'asistencias'
      AND COLUMN_NAME = 'tipoSesion';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'asistencias'
      AND COLUMN_NAME = 'tipo_sesion';

    -- Si existe la columna vieja (tipoSesion) y NO existe la nueva (tipo_sesion)
    IF col_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE asistencias
        CHANGE COLUMN tipoSesion tipo_sesion VARCHAR(50) NULL
        COMMENT 'Tipo de sesión registrado en el momento de la asistencia';
    END IF;

    -- Si NO existe la columna nueva (tipo_sesion), crearla
    IF col_new_exists = 0 AND col_exists = 0 THEN
        ALTER TABLE asistencias
        ADD COLUMN tipo_sesion VARCHAR(50) NULL
        COMMENT 'Tipo de sesión registrado en el momento de la asistencia';
    END IF;

    -- ========================================================================
    -- 3. TABLA: sesiones_programadas - Renombrar tipo → tipo_sesion (para consistencia)
    -- ========================================================================

    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'tipo';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'tipo_sesion';

    -- Si existe 'tipo' pero no 'tipo_sesion', renombrar para consistencia
    IF col_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE sesiones_programadas
        CHANGE COLUMN tipo tipo_sesion VARCHAR(50) NOT NULL
        COMMENT 'Tipo de sesión: TECNICA, COMBATE, ACONDICIONAMIENTO, etc.';
    END IF;

END$$

DELIMITER ;

-- Ejecutar el procedimiento
CALL sp_corregir_nombres_columnas();

-- Limpiar el procedimiento
DROP PROCEDURE IF EXISTS sp_corregir_nombres_columnas;