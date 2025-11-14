package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjecucionTarea;
import com.RafaelDiaz.ClubJudoColombia.repositorio.EjecucionTareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // --- NUEVO IMPORT ---

/**
 * --- SERVICIO ACTUALIZADO ---
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

    /**
     * --- ¡NUEVO MÉTODO! ---
     * Busca todas las ejecuciones de tareas, ordenadas por fecha descendente,
     * y fuerza la carga (fetch) de las entidades relacionadas para
     * mostrarlas en el Grid del Sensei.
     */
    @Transactional(readOnly = true)
    public List<EjecucionTarea> findAllWithDetails() {
        List<EjecucionTarea> ejecuciones = ejecucionTareaRepository.findAllByOrderByFechaRegistroDesc();

        // Forzar la carga (fetch) de datos LAZY
        for (EjecucionTarea ejecucion : ejecuciones) {
            // Despertar Judoka y Usuario
            ejecucion.getJudoka().getUsuario().getNombre();
            // Despertar Tarea
            ejecucion.getEjercicioPlanificado().getTareaDiaria().getNombre();
        }
        return ejecuciones;
    }
}