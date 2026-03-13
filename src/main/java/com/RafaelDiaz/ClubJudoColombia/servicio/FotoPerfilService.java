package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FotoPerfilService {

    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository;

    public FotoPerfilService(SenseiRepository senseiRepository, JudokaRepository judokaRepository) {
        this.senseiRepository = senseiRepository;
        this.judokaRepository = judokaRepository;
    }

    @Transactional(readOnly = true)
    public String getFotoPerfilUrl(Usuario usuario) {
        // Si es sensei, devolver su foto
        return senseiRepository.findByUsuario(usuario)
                .map(Sensei::getUrlFotoPerfil)
                .orElseGet(() -> {
                    // Si no, buscar el primer judoka asociado como acudiente
                    return judokaRepository.findByAcudiente(usuario).stream()
                            .findFirst()
                            .map(Judoka::getUrlFotoPerfil)
                            .orElse(null);
                });
    }
}