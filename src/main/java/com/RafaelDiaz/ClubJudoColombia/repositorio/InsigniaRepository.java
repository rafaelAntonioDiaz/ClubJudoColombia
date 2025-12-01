package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InsigniaRepository extends JpaRepository<Insignia, Long> {
    Optional<Insignia> findByClave(String clave);
}