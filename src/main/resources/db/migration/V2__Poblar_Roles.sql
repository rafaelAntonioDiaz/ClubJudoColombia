-- Versión 2: Poblar la tabla de Roles (Corregido)
-- Nombres de roles actualizados a Judoka y Sensei.

INSERT INTO roles (nombre) VALUES
('ROLE_SENSEI'),      -- Para los Senseis (antes Entrenador)
('ROLE_JUDOKA'),      -- Para los Judokas (antes Practicante)
('ROLE_COMPETIDOR'),  -- Rol adicional para Judokas que compiten
('ROLE_ADMIN'),      -- Para los administradores del sistema
('ROLE_MECENAS');     -- Para los patrocinadoresAS');     -- Para los patrocinadores