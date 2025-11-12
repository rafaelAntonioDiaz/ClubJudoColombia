package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.NormaEvaluacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NormaEvaluacionRepository extends JpaRepository<NormaEvaluacion, Long> {

    // (Más adelante, aquí añadiremos métodos de búsqueda complejos, como:
    // "Encuentra la clasificación para un Ejercicio, Sexo, Edad y Valor dados")

    // Por ahora, un método para limpiar los datos en el DataInitializer
    long countByFuente(String fuente);
    void deleteByFuente(String fuente);
    // --- AÑADIR ESTE MÉTODO ---
    /**
     * Busca todas las normas de clasificación (ej. EXCELENTE, BUENO, REGULAR...)
     * que coincidan con un ejercicio, métrica, sexo y edad específicos.
     *
     * @param ejercicio El ejercicio realizado.
     * @param metrica La métrica medida.
     * @param sexo El sexo del judoka.
     * @param edad La edad del judoka.
     * @return Una lista de Normas de Evaluación (ej. la fila de EXCELENTE, la de BUENO, etc.)
     */
    @Query("SELECT n FROM NormaEvaluacion n " +
            "WHERE n.pruebaEstandar = :ejercicio " +
            "AND n.metrica = :metrica " +
            "AND n.sexo = :sexo " +
            "AND :edad >= n.edadMin " +
            "AND :edad <= n.edadMax")
    List<NormaEvaluacion> findNormasPorCriterios(
            @Param("ejercicio") PruebaEstandar ejercicio,
            @Param("metrica") Metrica metrica,
            @Param("sexo") Sexo sexo,
            @Param("edad") int edad
    );
}