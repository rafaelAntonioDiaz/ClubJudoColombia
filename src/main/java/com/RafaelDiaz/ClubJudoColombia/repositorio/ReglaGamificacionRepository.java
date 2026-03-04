package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ReglaGamificacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoEventoGamificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglaGamificacionRepository extends JpaRepository<ReglaGamificacion, Long> {
    @Query("SELECT r FROM ReglaGamificacion r JOIN FETCH r.insignia WHERE r.sensei = :sensei")
    List<ReglaGamificacion> findBySenseiWithInsignia(@Param("sensei") Sensei sensei);

    @Query("SELECT r FROM ReglaGamificacion r JOIN FETCH r.insignia WHERE r.sensei = :sensei AND r.tipoEvento = :tipoEvento")
    List<ReglaGamificacion> findBySenseiAndTipoEventoWithInsignia(@Param("sensei") Sensei sensei, @Param("tipoEvento") TipoEventoGamificacion tipoEvento);
}