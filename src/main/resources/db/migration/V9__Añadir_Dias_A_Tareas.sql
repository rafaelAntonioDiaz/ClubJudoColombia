-- Versión 9: Añadir frecuencia (Días de la Semana) a las tareas del plan

-- Tabla para almacenar los días asignados a cada tarea
-- (Ej. Tarea 1 -> LUNES, Tarea 1 -> MIERCOLES)
CREATE TABLE plan_tarea_dias (
    id_ejercicio_plan BIGINT NOT NULL,
    dia_semana VARCHAR(20) NOT NULL, -- 'MONDAY', 'TUESDAY', etc.

    PRIMARY KEY (id_ejercicio_plan, dia_semana),
    FOREIGN KEY (id_ejercicio_plan) REFERENCES ejercicios_planificados(id_ejercicio_plan)
        ON DELETE CASCADE
);