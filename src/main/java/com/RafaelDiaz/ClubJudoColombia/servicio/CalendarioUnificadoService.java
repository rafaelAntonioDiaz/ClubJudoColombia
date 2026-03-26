package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.dto.ItemCalendario;
import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoItem;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarioUnificadoService {

    private final SesionProgramadaRepository sesionRepo;
    private final MicrocicloService microcicloService;
    private final EjecucionTareaRepository ejecucionRepo;
    private final JudokaRepository judokaRepository;
    private final AsistenciaRepository asistenciaRepo;
    private final GrupoEntrenamientoRepository grupoRepo;
    private final SenseiRepository senseiRepository;

    public CalendarioUnificadoService(SesionProgramadaRepository sesionRepo,
                                      MicrocicloService microcicloService,
                                      EjecucionTareaRepository ejecucionRepo, JudokaRepository judokaRepository,
                                      AsistenciaRepository asistenciaRepo,
                                      GrupoEntrenamientoRepository grupoRepo, SenseiRepository senseiRepository) {
        this.sesionRepo = sesionRepo;
        this.microcicloService = microcicloService;
        this.ejecucionRepo = ejecucionRepo;
        this.judokaRepository = judokaRepository;
        this.asistenciaRepo = asistenciaRepo;
        this.grupoRepo = grupoRepo;
        this.senseiRepository = senseiRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemCalendario> obtenerItemsPorJudokaYMes(Long judokaId, YearMonth mes) {
        System.out.println(">>> [AGENDA] Buscando items para Judoka ID: " + judokaId + " en el mes: " + mes);
        Judoka judoka = judokaRepository.findById(judokaId)
                .orElseThrow(() -> new RuntimeException("Judoka no encontrado"));

        LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
        LocalDateTime finMes = mes.atEndOfMonth().atTime(23, 59, 59);

        List<ItemCalendario> items = new ArrayList<>();

        // 1. Sesiones grupales del grupo del judoka
        GrupoEntrenamiento grupoJudoka = judoka.getGrupo();
        if (grupoJudoka != null) {
            List<SesionProgramada> sesiones = sesionRepo.findByGrupoInAndFechaHoraInicioBetween(
                    Set.of(grupoJudoka), inicioMes, finMes);
            for (SesionProgramada s : sesiones) {
                items.add(mapearSesion(s, judoka));
            }
        }

        // 2. Tareas de microciclos (grupales e individuales)
        List<Microciclo> planes = microcicloService.buscarPlanesPorJudoka(judoka);
        System.out.println(">>> Planes encontrados para judoka " + judokaId + ": " + planes.size());
        for (Microciclo plan : planes) {
            if (!intersectaMes(plan, mes)) continue;
            for (EjercicioPlanificado ej : plan.getEjerciciosPlanificados()) {
                if (ej.getTareaDiaria() == null) continue;
                if (ej.isRequiereSupervision()) continue; // No mostrar tareas de clase en el calendario del judoka
                // Si tiene judoka asignado, debe coincidir con el actual
                if (ej.getJudokaAsignado() != null && !ej.getJudokaAsignado().equals(judoka)) {
                    continue;
                }
                // Si es grupal (sin judoka asignado) y el judoka no está en el grupo? pero ya estamos en planes del judoka, así que está bien.

                List<LocalDate> fechas = generarFechasEnMes(plan, ej, mes);
                System.out.println(">>> Ejercicio " + ej.getTareaDiaria().getNombre() + " genera " + fechas.size() + " fechas");
                for (LocalDate fecha : fechas) {
                    items.add(mapearTarea(ej, fecha, judoka));
                }
            }
        }

        System.out.println(">>> [AGENDA] Total de items encontrados: " + items.size());
        return items;
    }

    @Transactional(readOnly = true)
    public List<ItemCalendario> obtenerItemsPorSenseiYMes(Sensei sensei, YearMonth mes) {
        // Recargar el sensei con su usuario dentro de la transacción
        Sensei managedSensei = senseiRepository.findById(sensei.getId())
                .orElseThrow(() -> new RuntimeException("Sensei no encontrado"));

        // Inicializar el usuario (si es necesario, pero la consulta debería traerlo)
        Hibernate.initialize(managedSensei.getUsuario());

        LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
        LocalDateTime finMes = mes.atEndOfMonth().atTime(23, 59, 59);

        List<ItemCalendario> items = new ArrayList<>();

        // Obtener grupos del sensei (usando el managedSensei)
        List<GrupoEntrenamiento> grupos = grupoRepo.findBySensei(managedSensei);
        if (grupos != null && !grupos.isEmpty()) {
            grupos.forEach(g -> Hibernate.initialize(g));

            // Sesiones de esos grupos
            List<SesionProgramada> sesiones = sesionRepo.findByGrupoInAndFechaHoraInicioBetween(
                    grupos, inicioMes, finMes);
            for (SesionProgramada s : sesiones) {
                items.add(mapearSesion(s, null));
            }
        }

        System.out.println(">>> Items para sensei " + managedSensei.getUsuario().getNombre() + " en mes " + mes + ": " + items.size());
        return items;
    }


    @Transactional(readOnly = true)
    public List<ItemCalendario> obtenerItemsPorGrupoYMes(GrupoEntrenamiento grupo, YearMonth mes) {
        LocalDateTime inicioMes = mes.atDay(1).atStartOfDay();
        LocalDateTime finMes = mes.atEndOfMonth().atTime(23, 59, 59);

        // Inicializar el grupo para evitar LazyException
        Hibernate.initialize(grupo);

        List<SesionProgramada> sesiones = sesionRepo.findByGrupoInAndFechaHoraInicioBetween(
                Set.of(grupo), inicioMes, finMes);

        return sesiones.stream().map(s -> mapearSesion(s, null)).collect(Collectors.toList());
    }


    // ---------- Métodos privados de mapeo ----------

    private ItemCalendario mapearSesion(SesionProgramada s, Judoka judoka) {
        // Inicializar el grupo si es proxy
        if (s.getGrupo() != null) {
            Hibernate.initialize(s.getGrupo());
        }

        ItemCalendario item = new ItemCalendario();
        item.setId(s.getId());
        item.setTitulo(s.getNombre());
        item.setInicio(s.getFechaHoraInicio());
        item.setFin(s.getFechaHoraFin());
        item.setTipo(TipoItem.SESION_GRUPAL);
        item.setUbicacionEsperada(s.getGrupo() != null ? s.getGrupo().getLugarPractica() : "");
        item.setLatitudEsperada(s.getLatitud());
        item.setLongitudEsperada(s.getLongitud());
        item.setRequiereGps(s.getLatitud() != null);
        item.setGrupoNombre(s.getGrupo() != null ? s.getGrupo().getNombre() : "");
        item.setEntidadId(s.getId());

        // Estado según fecha
        LocalDateTime ahora = LocalDateTime.now();
        if (s.getFechaHoraFin().isBefore(ahora)) {
            item.setEstado("pasada");
            if (judoka != null) {
                // Aquí podrías buscar asistencia si tuvieras la relación
            }
        } else if (s.getFechaHoraInicio().isAfter(ahora)) {
            item.setEstado("programada");
        } else {
            item.setEstado("en_curso");
        }

        return item;
    }

    private ItemCalendario mapearTarea(EjercicioPlanificado ej, LocalDate fecha, Judoka judoka) {
        // Inicializar relaciones necesarias
        if (ej.getTareaDiaria() != null) {
            Hibernate.initialize(ej.getTareaDiaria());
        }
        if (ej.getMicrociclo() != null) {
            Hibernate.initialize(ej.getMicrociclo());
            if (ej.getMicrociclo().getGruposAsignados() != null) {
                ej.getMicrociclo().getGruposAsignados().forEach(g -> Hibernate.initialize(g));
            }
        }

        ItemCalendario item = new ItemCalendario();
        item.setId(ej.getId());
        item.setTitulo(ej.getTareaDiaria().getNombre());
        item.setInicio(fecha.atStartOfDay());
        item.setFin(fecha.atTime(23, 59, 59));
        item.setTipo(TipoItem.TAREA_INDIVIDUAL);
        item.setUbicacionEsperada("Libre");
        item.setRequiereGps(true);
        item.setEntidadId(ej.getId());
        item.setRequiereSupervision(ej.isRequiereSupervision());
        item.setGrupoNombre(ej.getMicrociclo().getGruposAsignados().stream()
                .findFirst().map(GrupoEntrenamiento::getNombre).orElse(""));
        item.setJudokaNombre(judoka.getNombre() + " " + judoka.getApellido());

        // Verificar si ya fue ejecutada hoy
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();
        Optional<EjecucionTarea> ejec = ejecucionRepo.findByJudokaAndEjercicioAndFechaBetween(
                judoka, ej, inicioDia, finDia);
        if (ejec.isPresent()) {
            item.setCompletado(true);
            item.setEstado("completada");
            item.setLatitudRegistrada(ejec.get().getLatitud());
            item.setLongitudRegistrada(ejec.get().getLongitud());
        } else {
            item.setCompletado(false);
            item.setEstado("pendiente");
        }
        return item;
    }

    private boolean intersectaMes(Microciclo plan, YearMonth mes) {
        LocalDate inicio = plan.getFechaInicio();
        LocalDate fin = plan.getFechaFin();
        if (inicio == null || fin == null) {
            // Loggear para depuración
            System.out.println(">>> WARN: Microciclo '" + plan.getNombre() + "' (ID: " + plan.getId() + ") tiene fechas nulas");
            return false; // No lo consideramos para el calendario
        }
        return !(fin.isBefore(mes.atDay(1)) || inicio.isAfter(mes.atEndOfMonth()));
    }

    private List<LocalDate> generarFechasEnMes(Microciclo plan, EjercicioPlanificado ej, YearMonth mes) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate inicio = plan.getFechaInicio().isBefore(mes.atDay(1)) ? mes.atDay(1) : plan.getFechaInicio();
        LocalDate fin = plan.getFechaFin().isAfter(mes.atEndOfMonth()) ? mes.atEndOfMonth() : plan.getFechaFin();
        for (LocalDate date = inicio; !date.isAfter(fin); date = date.plusDays(1)) {
            if (ej.getDiasAsignados().contains(date.getDayOfWeek())) {
                fechas.add(date);
            }
        }
        return fechas;
    }
}