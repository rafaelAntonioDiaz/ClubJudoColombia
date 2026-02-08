package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConceptoFinancieroRepository extends JpaRepository<ConceptoFinanciero, Long> {
    List<ConceptoFinanciero> findByTipo(TipoTransaccion tipo); // Para llenar los Dropdowns
    Optional<ConceptoFinanciero> findByNombre(String nombre);
}