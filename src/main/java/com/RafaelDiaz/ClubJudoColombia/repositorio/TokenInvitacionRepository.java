package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenInvitacionRepository extends JpaRepository<TokenInvitacion, Long> {
    Optional<TokenInvitacion> findByToken(String token);
}