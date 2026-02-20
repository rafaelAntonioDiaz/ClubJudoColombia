package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PruebaEstandarRepository extends JpaRepository<PruebaEstandar, Long> {

    Optional<PruebaEstandar> findByNombreKey(String nombreKey);
    @Query("SELECT p FROM PruebaEstandar p WHERE p.esGlobal = true OR p.senseiCreador = :sensei")
    List<PruebaEstandar> findGlobalesYDelSensei(@Param("sensei") Sensei sensei);

    @Query("SELECT p FROM PruebaEstandar p WHERE p.categoria IN :categorias AND (p.esGlobal = true OR p.senseiCreador = :sensei)")
    List<PruebaEstandar> findByCategoriaInAndGlobalOrSensei(@Param("categorias") List<CategoriaEjercicio> categorias, @Param("sensei") Sensei sensei);
}