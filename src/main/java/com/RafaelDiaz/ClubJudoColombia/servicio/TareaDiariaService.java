package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TareaDiariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * --- NUEVO SERVICIO ---
 * Maneja la lógica de negocio para la biblioteca de Tareas Diarias
 * (los ejercicios dinámicos creados por el Sensei).
 */
@Service
public class TareaDiariaService {

    private final TareaDiariaRepository tareaDiariaRepository;

    public TareaDiariaService(TareaDiariaRepository tareaDiariaRepository) {
        this.tareaDiariaRepository = tareaDiariaRepository;
    }

    /**
     * Guarda una nueva tarea o actualiza una existente.
     * @param tarea La TareaDiaria a guardar.
     * @param sensei El Sensei que la está creando/editando.
     * @return La tarea guardada.
     */
    @Transactional
    public TareaDiaria guardarTarea(TareaDiaria tarea, Sensei sensei) {
        // Asignamos el sensei que la creó
        tarea.setSenseiCreador(sensei);
        return tareaDiariaRepository.save(tarea);
    }

    /**
     * Elimina una tarea de la biblioteca.
     * (Aquí deberíamos añadir lógica para verificar si la tarea
     * está siendo usada en un plan, pero lo dejamos simple por ahora).
     * @param tareaId El ID de la tarea a eliminar.
     */
    @Transactional
    public void eliminarTarea(Long tareaId) {
        tareaDiariaRepository.deleteById(tareaId);
    }

    /**
     * Obtiene todas las tareas de la biblioteca.
     * @return Una lista de todas las Tareas Diarias.
     */
    public List<TareaDiaria> findAll() {
        return tareaDiariaRepository.findAll();
    }
}