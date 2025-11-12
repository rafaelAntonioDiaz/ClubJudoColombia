package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoPlan;
import com.RafaelDiaz.ClubJudoColombia.repositorio.EjercicioPlanificadoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PlanEntrenamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * --- SERVICIO REFACTORIZADO ---
 * Maneja la lógica de negocio para Planes de Entrenamiento,
 * que ahora se asignan a Grupos.
 */
@Service
public class PlanEntrenamientoService {

    private final PlanEntrenamientoRepository planEntrenamientoRepository;
    private final EjercicioPlanificadoRepository ejercicioPlanificadoRepository;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;

    @Autowired
    public PlanEntrenamientoService(
            PlanEntrenamientoRepository planEntrenamientoRepository,
            EjercicioPlanificadoRepository ejercicioPlanificadoRepository,
            GrupoEntrenamientoRepository grupoEntrenamientoRepository) {
        this.planEntrenamientoRepository = planEntrenamientoRepository;
        this.ejercicioPlanificadoRepository = ejercicioPlanificadoRepository;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
    }

    /**
     * Crea un nuevo Plan de Entrenamiento para uno o más grupos.
     */
    @Transactional
    public PlanEntrenamiento crearPlanEntrenamiento(String nombre, Sensei sensei, Set<GrupoEntrenamiento> grupos) {
        PlanEntrenamiento plan = new PlanEntrenamiento();
        plan.setNombre(nombre);
        plan.setSensei(sensei);
        plan.setGruposAsignados(grupos);
        plan.setFechaAsignacion(LocalDate.now());
        plan.setEstado(EstadoPlan.PENDIENTE);
        return planEntrenamientoRepository.save(plan);
    }

    /**
     * Añade una Tarea (Prueba o Tarea Diaria) a un Plan.
     */
    @Transactional
    public PlanEntrenamiento addEjercicioPlanificado(PlanEntrenamiento plan, EjercicioPlanificado ejercicioPlanificado) {
        plan.addEjercicio(ejercicioPlanificado);
        return planEntrenamientoRepository.save(plan);
    }

    /**
     * Actualiza el estado de un Plan.
     */
    @Transactional
    public Optional<PlanEntrenamiento> actualizarEstadoPlan(Long planId, EstadoPlan nuevoEstado) {
        return planEntrenamientoRepository.findById(planId).map(plan -> {
            plan.setEstado(nuevoEstado);
            return planEntrenamientoRepository.save(plan);
        });
    }

    /**
     * Busca un Plan por ID, forzando la carga de las colecciones LAZY
     * para evitar errores en la Vista.
     */
    @Transactional(readOnly = true)
    public Optional<PlanEntrenamiento> buscarPorId(Long planId) {
        Optional<PlanEntrenamiento> planOpt = planEntrenamientoRepository.findById(planId);

        planOpt.ifPresent(plan -> {
            // Nivel 1: Despertar la lista de EjerciciosPlanificados
            List<EjercicioPlanificado> tareas = plan.getEjerciciosPlanificados();
            // Nivel 2: Despertar los objetos LAZY dentro de CADA tarea
            for (EjercicioPlanificado tarea : tareas) {
                if (tarea.getPruebaEstandar() != null) {
                    tarea.getPruebaEstandar().getNombreKey(); // Despertar Prueba
                }
                if (tarea.getTareaDiaria() != null) {
                    tarea.getTareaDiaria().getNombre(); // Despertar Tarea
                }
            }
            plan.getGruposAsignados().size();
        });

        return planOpt;
    }

    /**
     * Busca todos los planes asignados a un Judoka (a través de sus grupos).
     */
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> buscarPlanesPorJudoka(Judoka judoka) {
        List<GrupoEntrenamiento> gruposDelJudoka = grupoEntrenamientoRepository.findAllByJudokasContains(judoka);

        List<PlanEntrenamiento> planes = gruposDelJudoka.stream()
                .flatMap(grupo -> planEntrenamientoRepository.findAllByGruposAsignadosContains(grupo).stream())
                .distinct()
                .collect(Collectors.toList());

        // Forzar la carga de las tareas (Pruebas/Tareas) para cada plan
        for (PlanEntrenamiento plan : planes) {
            List<EjercicioPlanificado> tareas = plan.getEjerciciosPlanificados();
            for (EjercicioPlanificado tarea : tareas) {
                if (tarea.getPruebaEstandar() != null) {
                    tarea.getPruebaEstandar().getNombreKey();
                }
                if (tarea.getTareaDiaria() != null) {
                    tarea.getTareaDiaria().getNombre();
                }
            }
        }

        return planes;
    }

    /**
     * Busca todos los planes creados por un Sensei.
     */
    @Transactional(readOnly = true)
    public List<PlanEntrenamiento> buscarPlanesPorSensei(Sensei sensei) {
        List<PlanEntrenamiento> planes = planEntrenamientoRepository.findBySenseiOrderByFechaAsignacionDesc(sensei);

        // Forzar la inicialización
        for (PlanEntrenamiento plan : planes) {
            List<EjercicioPlanificado> tareas = plan.getEjerciciosPlanificados();
            for (EjercicioPlanificado tarea : tareas) {
                if (tarea.getPruebaEstandar() != null) {
                    tarea.getPruebaEstandar().getNombreKey();
                }
                if (tarea.getTareaDiaria() != null) {
                    tarea.getTareaDiaria().getNombre();
                }
            }
        }

        return planes;
    }

    @Transactional
    public PlanEntrenamiento guardarPlan(PlanEntrenamiento plan) {
        return planEntrenamientoRepository.save(plan);
    }

    @Transactional
    public void eliminarPlan(Long planId) {
        planEntrenamientoRepository.deleteById(planId);
    }
}