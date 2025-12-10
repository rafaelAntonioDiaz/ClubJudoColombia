package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ArticuloInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticuloInventarioRepository extends JpaRepository<ArticuloInventario, Long> {
    // Buscar productos con stock bajo para alertas
    List<ArticuloInventario> findByCantidadStockLessThanEqual(int stockMinimo);
}