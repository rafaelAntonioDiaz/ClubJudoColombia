-- ============================================================================
-- VERSIÓN 9: Añadir Días a Tareas - Estructura para Tareas Recurrentes
-- ============================================================================
-- Descripción: Este script añade la capacidad de programar tareas en días
-- específicos de la semana y crea tablas para las sesiones programadas.
-- Autor: Sistema Judo
-- Fecha: 2025
-- ============================================================================

-- ============================================================================
-- 1. TABLA: plan_tarea_dias
-- ============================================================================
-- Propósito: Asociar días de la semana a tareas planificadas
-- Permite que una tarea se ejecute en múltiples días específicos
-- Ejemplo: Una tarea de "Calentamiento" puede estar programada para
--          Lunes, Miércoles y Viernes
-- ============================================================================
CREATE TABLE IF NOT EXISTS plan_tarea_dias (
    id_ejercicio_plan BIGINT NOT NULL COMMENT 'FK a ejercicios_planificados.id_ejercicio_plan',
    dia_semana VARCHAR(20) NOT NULL COMMENT 'Día de la semana: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO',
    PRIMARY KEY (id_ejercicio_plan, dia_semana),
    FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan) ON DELETE CASCADE
) COMMENT 'Asocia días de ejecución a tareas planificadas';


