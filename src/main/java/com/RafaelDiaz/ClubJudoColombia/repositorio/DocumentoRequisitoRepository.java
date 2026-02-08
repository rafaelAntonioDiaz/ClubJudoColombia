package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentoRequisitoRepository extends JpaRepository<DocumentoRequisito, Long> {
    // Aquí podemos agregar métodos personalizados si los necesitamos en el futuro.
    // Por ahora, con los métodos básicos de JpaRepository (save, findById, delete) es suficiente.
    Optional<DocumentoRequisito> findByEventoAsociadoAndJudoka(String eventoAsociado, Judoka judoka);
}