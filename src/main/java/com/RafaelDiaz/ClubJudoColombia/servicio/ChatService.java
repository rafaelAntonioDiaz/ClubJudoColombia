package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.MensajeChat;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MensajeChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final MensajeChatRepository repository;

    public ChatService(MensajeChatRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MensajeChat> obtenerHistorialChat() {
        return repository.findAllByOrderByFechaAsc();
    }

    @Transactional
    public MensajeChat enviarMensaje(Usuario autor, String contenido) {
        MensajeChat mensaje = new MensajeChat(autor, contenido);
        return repository.save(mensaje);
    }
}