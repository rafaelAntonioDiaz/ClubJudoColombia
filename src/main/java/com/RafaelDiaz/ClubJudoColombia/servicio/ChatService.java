package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.MensajeChat;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MensajeChatRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final SenseiRepository senseiRepository;

    @Autowired
    public ChatService(MensajeChatRepository mensajeChatRepository, SenseiRepository senseiRepository) {
        this.mensajeChatRepository = mensajeChatRepository;
        this.senseiRepository = senseiRepository;
    }

    public List<MensajeChat> obtenerHistorialDelDojo(Long dojoId) {
        return mensajeChatRepository.findBySenseiIdOrderByFechaAsc(dojoId);
    }

    public void enviarMensajeAlDojo(Usuario autor, String texto, Long dojoId) {
        Sensei dojo = senseiRepository.findById(dojoId).orElseThrow();
        MensajeChat msg = new MensajeChat(autor, texto);
        msg.setSensei(dojo);
        msg.setFecha(LocalDateTime.now());
        mensajeChatRepository.save(msg);
    }
}