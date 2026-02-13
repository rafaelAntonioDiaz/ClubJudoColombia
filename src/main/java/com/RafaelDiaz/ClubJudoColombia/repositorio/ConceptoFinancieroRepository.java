package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptoFinancieroRepository extends JpaRepository<ConceptoFinanciero, Long> {
    List<ConceptoFinanciero> findByTipo(TipoTransaccion tipo); // Para llenar los Dropdowns
    Optional<ConceptoFinanciero> findByNombre(String nombre);
}