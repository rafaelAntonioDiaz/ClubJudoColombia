package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoMicrociclo;
import com.RafaelDiaz.ClubJudoColombia.repositorio.EjercicioPlanificadoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MicrocicloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class MicrocicloService {

    // Se eliminó la variable "repository" duplicada y nula. Todo usa "MicrocicloRepository".
    private final MicrocicloRepository microcicloRepository;
    private final EjercicioPlanificadoRepository ejercicioPlanificadoRepository;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;

    @Autowired
    public MicrocicloService(
            MicrocicloRepository microcicloRepository,
            EjercicioPlanificadoRepository ejercicioPlanificadoRepository,
            GrupoEntrenamientoRepository grupoEntrenamientoRepository) {
        this.microcicloRepository = microcicloRepository;
        this.ejercicioPlanificadoRepository = ejercicioPlanificadoRepository;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
    }

    /**
     * Crea un nuevo Plan de Entrenamiento para uno o más grupos.
     */
    @Transactional
    public Microciclo crearMicrociclo(String nombre, Sensei sensei, Set<GrupoEntrenamiento> grupos) {
        Microciclo plan = new Microciclo();
        plan.setNombre(nombre);
        plan.setSensei(sensei);
        plan.setGruposAsignados(grupos);
        plan.setEstado(EstadoMicrociclo.ACTIVO);
        return microcicloRepository.save(plan);
    }

    /**
     * Añade una Tarea (Prueba o Tarea Diaria) a un Plan.
     */
    @Transactional
    public Microciclo addEjercicioPlanificado(Microciclo plan, EjercicioPlanificado ejercicioPlanificado) {
        plan.addEjercicio(ejercicioPlanificado);
        return microcicloRepository.save(plan);
    }

    /**
     * Actualiza el estado de un Plan.
     */
    @Transactional
    public Optional<Microciclo> actualizarEstadoMicro(Long microId, EstadoMicrociclo nuevoEstado) {
        return microcicloRepository.findById(microId).map(micro -> {
            micro.setEstado(nuevoEstado);
            return microcicloRepository.save(micro);
        });
    }

    /**
     * Busca un Plan por ID, forzando la carga de las colecciones LAZY
     * para evitar errores en la Vista.
     */
    @Transactional(readOnly = true)
    public Optional<Microciclo> buscarPorId(Long planId) {
        Optional<Microciclo> planOpt = microcicloRepository.findById(planId);

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
     * Obtiene todos los planes registrados en el sistema.
     * Útil para administración o inicialización de datos.
     */
    @Transactional(readOnly = true)
    public List<Microciclo> listarPlanes() {
        return microcicloRepository.findAll();
    }

    /**
     * Busca todos los planes asignados a un Judoka (a través de sus grupos).
     */
    @Transactional(readOnly = true)
    public List<Microciclo> buscarPlanesPorJudoka(Judoka judoka) {
        List<GrupoEntrenamiento> gruposDelJudoka = grupoEntrenamientoRepository.findAllByJudokasContains(judoka);

        List<Microciclo> planes = gruposDelJudoka.stream()
                .flatMap(grupo -> microcicloRepository.findAllByGruposAsignadosContains(grupo).stream())
                .distinct()
                .collect(Collectors.toList());
        for (Microciclo plan : planes) {
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
    public List<Microciclo> buscarPlanesPorSensei(Sensei sensei) {
        List<Microciclo> planes = microcicloRepository.findBySenseiOrderByFechaInicioDesc(sensei);
        for (Microciclo plan : planes) {
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

    @Transactional(readOnly = true)
    public List<Microciclo> obtenerHistorialDelSensei(Sensei sensei) {
        List<Microciclo> microciclos = microcicloRepository.findBySenseiOrderByFechaInicioDesc(sensei);

        for (Microciclo micro : microciclos) {
            // 1. Despertamos los grupos
            micro.getGruposAsignados().size();

            // 2. Despertamos el Macrociclo (si tiene uno asignado)
            if (micro.getMacrociclo() != null) {
                micro.getMacrociclo().getNombre();
            }

            // 3. Despertamos la lista de ejercicios
            micro.getEjerciciosPlanificados().size();

            // 4. EL FIX: Despertamos las Tareas y Pruebas ADENTRO de cada ejercicio
            for (EjercicioPlanificado ej : micro.getEjerciciosPlanificados()) {
                if (ej.getTareaDiaria() != null) {
                    ej.getTareaDiaria().getNombre(); // Despierta la Tarea Diaria
                }
                if (ej.getPruebaEstandar() != null) {
                    ej.getPruebaEstandar().getNombreKey(); // Despierta la Prueba Estándar
                }
            }
        }

        return microciclos;
    }

    @Transactional
    public Microciclo guardarPlan(Microciclo plan) {
        return microcicloRepository.save(plan);
    }

    @Transactional
    public void eliminarPlan(Long planId) {
        microcicloRepository.deleteById(planId);
    }
}