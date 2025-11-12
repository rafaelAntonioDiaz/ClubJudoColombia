package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * --- NUEVO SERVICIO ---
 * Maneja la lógica de negocio para la entidad Judoka.
 */
@Service
public class JudokaService {

    private final JudokaRepository judokaRepository;

    public JudokaService(JudokaRepository judokaRepository) {
        this.judokaRepository = judokaRepository;
    }

    /**
     * Busca todos los Judokas y fuerza la inicialización (fetch)
     * de sus Usuarios asociados para evitar LazyInitializationException.
     * @return Lista de Judokas con sus Usuarios cargados.
     */
    @Transactional(readOnly = true)
    public List<Judoka> findAllJudokasWithUsuario() {
        List<Judoka> judokas = judokaRepository.findAll();
        // --- ¡LA SOLUCIÓN! ---
        // Forzamos a Hibernate a "despertar" el Usuario de cada Judoka
        // mientras la sesión (@Transactional) sigue abierta.
        judokas.forEach(judoka -> judoka.getUsuario().getNombre());
        return judokas;
    }
}