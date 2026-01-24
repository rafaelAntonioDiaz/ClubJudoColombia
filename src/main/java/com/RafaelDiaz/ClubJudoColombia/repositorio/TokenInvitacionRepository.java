package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenInvitacionRepository extends JpaRepository<TokenInvitacion, Long> {
    Optional<TokenInvitacion> findByToken(String token);
}