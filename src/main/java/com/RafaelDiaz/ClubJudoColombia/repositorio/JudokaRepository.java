package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JudokaRepository extends JpaRepository<Judoka, Long> {

    Optional<Judoka> findByUsuario(Usuario usuario);

    // --- MÉTODOS PARA ADMISIONES Y LIMPIEZA ---

    // 1. Para CleanupService (No necesita detalles pesados, solo fechas)
    List<Judoka> findByEstadoAndFechaPreRegistroBefore(EstadoJudoka estado, LocalDateTime fechaLimite);

    // 2. Para ValidacionIngresoView (NECESITA Usuario y Documentos cargados)
    // Usamos JOIN FETCH para evitar LazyInitializationException
    @Query("SELECT DISTINCT j FROM Judoka j " +
            "JOIN FETCH j.usuario " +           // Carga obligatoria del Usuario
            "LEFT JOIN FETCH j.documentos " +   // Carga opcional de Documentos (si tiene)
            "WHERE j.estado = :estado")
    List<Judoka> findByEstadoWithDetails(@Param("estado") EstadoJudoka estado);

    // Mantenemos el método simple por compatibilidad, pero la vista usará el de arriba
    List<Judoka> findByEstado(EstadoJudoka estado);
    // 1. Traer TODOS los judokas con sus datos de usuario cargados (Para el ComboBox 'Agregar')
    @Query("SELECT DISTINCT j FROM Judoka j JOIN FETCH j.usuario")
    List<Judoka> findAllWithUsuario();

    // 2. Traer solo los judokas de un GRUPO específico con sus datos (Para la Grilla 'Miembros')
    // Asume que la relación en Judoka se llama 'grupos' (ManyToMany o OneToMany inverso)
    // Si la relación está mapeada desde Grupo, usamos esta variante segura:
    @Query("SELECT DISTINCT j FROM GrupoEntrenamiento g JOIN g.judokas j JOIN FETCH j.usuario WHERE g.id = :grupoId")
    List<Judoka> findByGrupoIdWithUsuario(@Param("grupoId") Long grupoId);
}