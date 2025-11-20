-- ============================================================================
-- VERSIÓN 11: Corregir nombres de columnas en sesiones_programadas
-- ============================================================================
-- Problema:
-- - V1 creó columnas con nombres en camelCase (fechaHoraInicio, fechaHoraFin)
-- - Hibernate espera nombres en snake_case (fecha_hora_inicio, fecha_hora_fin)
-- - Esto causa error: "Unknown column 'sp1_0.fecha_hora_inicio'"
--
-- Solución: Renombrar ALL las columnas a estándar snake_case
-- ============================================================================

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS sp_corregir_sesiones_programadas()
BEGIN
    DECLARE col_old_exists INT;
    DECLARE col_new_exists INT;

    -- ========================================================================
    -- 1. Renombrar: fechaHoraInicio → fecha_hora_inicio
    -- ========================================================================
    SELECT COUNT(*) INTO col_old_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'fechaHoraInicio';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'fecha_hora_inicio';

    -- Si existe la columna vieja y NO existe la nueva
    IF col_old_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE sesiones_programadas
        CHANGE COLUMN fechaHoraInicio fecha_hora_inicio DATETIME NOT NULL
        COMMENT 'Fecha y hora de inicio de la sesión';
    END IF;

    -- ========================================================================
    -- 2. Renombrar: fechaHoraFin → fecha_hora_fin
    -- ========================================================================
    SELECT COUNT(*) INTO col_old_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'fechaHoraFin';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'fecha_hora_fin';

    -- Si existe la columna vieja y NO existe la nueva
    IF col_old_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE sesiones_programadas
        CHANGE COLUMN fechaHoraFin fecha_hora_fin DATETIME NOT NULL
        COMMENT 'Fecha y hora de finalización de la sesión';
    END IF;

    -- ========================================================================
    -- 3. Renombrar: tipo → tipo_sesion (para consistencia con otras tablas)
    -- ========================================================================
    SELECT COUNT(*) INTO col_old_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'tipo';

    SELECT COUNT(*) INTO col_new_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sesiones_programadas'
      AND COLUMN_NAME = 'tipo_sesion';

    -- Si existe la columna vieja y NO existe la nueva
    IF col_old_exists > 0 AND col_new_exists = 0 THEN
        ALTER TABLE sesiones_programadas
        CHANGE COLUMN tipo tipo_sesion VARCHAR(50) NOT NULL
        COMMENT 'Tipo de sesión: TECNICA, COMBATE, ACONDICIONAMIENTO, etc.';
    END IF;

    -- ========================================================================
    -- 4. Renombrar: id_sesion → id (para consistencia con V9)
    -- ========================================================================
    -- NOTA: Solo si V9 creó tabla con 'id' como primary key
    -- Si V1 ya tiene id_sesion, NO hacemos nada

END$$

DELIMITER ;

-- Ejecutar el procedimiento
CALL sp_corregir_sesiones_programadas();

-- Limpiar el procedimiento
DROP PROCEDURE IF EXISTS sp_corregir_sesiones_programadas;

-- ============================================================================
-- VERIFICACIÓN: Mostrar estructura final de sesiones_programadas
-- ============================================================================
-- Descomenta la siguiente línea para verificar:
-- SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sesiones_programadas'
-- ORDER BY ORDINAL_POSITION;