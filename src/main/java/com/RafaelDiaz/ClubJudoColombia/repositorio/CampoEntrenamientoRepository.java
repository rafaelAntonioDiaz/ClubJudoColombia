package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.CampoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoEntrenamientoRepository extends JpaRepository<CampoEntrenamiento, Long> {

    // Para el perfil del Judoka
    List<CampoEntrenamiento> findByJudokaOrderByFechaInicioDesc(Judoka judoka);

    // Para el Sensei (Gestión con datos cargados)
    @Query("SELECT c FROM CampoEntrenamiento c JOIN FETCH c.judoka j JOIN FETCH j.usuario ORDER BY c.fechaInicio DESC")
    List<CampoEntrenamiento> findAllWithDetails();
}