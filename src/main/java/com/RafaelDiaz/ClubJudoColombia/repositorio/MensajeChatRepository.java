package com.RafaelDiaz.ClubJudoColombia.repositorio;

import com.RafaelDiaz.ClubJudoColombia.modelo.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {
    // Importante: Orden ascendente (antiguos arriba, nuevos abajo) para el chat
    List<MensajeChat> findBySenseiIdOrderByFechaAsc(Long senseiId);
}