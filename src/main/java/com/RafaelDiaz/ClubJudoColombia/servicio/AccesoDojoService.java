package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccesoDojoService {
    private final JudokaRepository judokaRepository;

    public AccesoDojoService(JudokaRepository judokaRepository) {
        this.judokaRepository = judokaRepository;
    }

    @Transactional
    public String generarNuevoPase(Judoka judoka) {
        String token = UUID.randomUUID().toString();
        judoka.setTokenAccesoDirecto(token);
        judoka.setFechaGeneracionToken(LocalDateTime.now());
        judokaRepository.save(judoka);
        // Retornamos el path que espera el MagicLinkView (@Route("acceso-dojo"))
        return "/acceso-dojo/" + token;
    }

    public Optional<Judoka> validarPase(String token) {
        return judokaRepository.findByTokenAccesoDirecto(token)
                // Validamos que el token no tenga más de 24 horas de generado
                .filter(j -> j.getFechaGeneracionToken().isAfter(LocalDateTime.now().minusHours(24)));
    }
}