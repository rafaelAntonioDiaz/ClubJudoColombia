package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SesionProgramadaRepository;
import com.RafaelDiaz.ClubJudoColombia.util.FestivosColombia; // Importamos tu clase de utilidad
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Servicio para la lógica de negocio de SesionProgramada.
 * Aquí es donde aplicamos las reglas (ej. no festivos, no domingos).
 */
@Service
public class SesionService {

    private final SesionProgramadaRepository sesionRepository;

    // Inyección de dependencias por constructor
    public SesionService(SesionProgramadaRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    /**
     * Guarda una nueva sesión, aplicando las reglas de negocio.
     *
     * @param sesion La sesión a crear.
     * @return La sesión guardada.
     * @throws IllegalArgumentException si la sesión viola una regla de negocio.
     */
    public SesionProgramada crearSesion(SesionProgramada sesion) {

        // 1. Validamos la fecha antes de guardar
        validarFechaSesion(sesion.getFecha(), sesion.isEsExcepcion());

        // 2. Si la validación pasa, guardamos en la BD
        return sesionRepository.save(sesion);
    }

    /**
     * Método helper privado para validar las reglas de negocio sobre la fecha.
     *
     * @param fecha La fecha de la sesión a validar.
     * @param esExcepcion Si la sesión está marcada como excepción (ignora reglas).
     */
    private void validarFechaSesion(LocalDate fecha, boolean esExcepcion) {
        // Si está marcada como excepción (ej. un seminario especial
        // en domingo), nos saltamos todas las validaciones.
        if (esExcepcion) {
            return; // Se permite
        }

        // --- Regla 1: No domingos ---
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Error: No se pueden programar sesiones en domingo, salvo excepción.");
        }

        // --- Regla 2: No festivos (usando tu clase) ---
        if (FestivosColombia.esFestivo(fecha)) {
            throw new IllegalArgumentException("Error: La fecha " + fecha + " es un día festivo en Colombia. No se puede programar, salvo excepción.");
        }
    }

    // --- (Más adelante añadiremos aquí métodos) ---
    // public List<SesionProgramada> findSesionesPorRango(...)
    // public void cancelarSesion(...)
}