package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.repositorio.EjecucionTareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * --- NUEVO SERVICIO ---
 * Maneja la lógica de las Tareas Diarias
 * (el "check" + GPS del Judoka).
 */
@Service
public class EjecucionTareaService {

    private final EjecucionTareaRepository ejecucionTareaRepository;

    public EjecucionTareaService(EjecucionTareaRepository ejecucionTareaRepository) {
        this.ejecucionTareaRepository = ejecucionTareaRepository;
    }

    @Transactional
    public EjecucionTarea registrarEjecucion(EjecucionTarea ejecucion) {
        // (Aquí se podría añadir validación, ej. "solo 1 check por día")
        return ejecucionTareaRepository.save(ejecucion);
    }

    // (Añadiremos métodos de búsqueda si es necesario)
}