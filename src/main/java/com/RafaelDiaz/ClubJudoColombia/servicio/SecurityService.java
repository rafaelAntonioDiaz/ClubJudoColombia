package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka; // --- NUEVO IMPORT ---
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository; // --- NUEVO IMPORT ---
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // --- 1. IMPORTAR ---
import java.util.Optional;

@Service
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository; // --- NUEVO CAMPO ---

    // --- CONSTRUCTOR ACTUALIZADO ---
    public SecurityService(UsuarioRepository usuarioRepository,
                           SenseiRepository senseiRepository,
                           JudokaRepository judokaRepository) { // --- NUEVO PARÁMETRO ---
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.judokaRepository = judokaRepository; // --- NUEVA ASIGNACIÓN ---
    }

    /**
     * Obtiene el UserDetails del usuario actualmente logueado.
     */
    public Optional<UserDetails> getAuthenticatedUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails)) {
            return Optional.empty();
        }
        return Optional.of((UserDetails) authentication.getPrincipal());
    }

    /**
     * Busca el 'Usuario' (nuestra entidad) del usuario logueado.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> getAuthenticatedUsuario() {
        return getAuthenticatedUserDetails()
                .flatMap(userDetails -> usuarioRepository.
                        findByUsername(userDetails.getUsername()));
    }

    /**
     * Busca el perfil de 'Sensei' del usuario actualmente logueado.
     */
    @Transactional(readOnly = true)
    public Optional<Sensei> getAuthenticatedSensei() {
        Optional<Usuario> usuarioOpt = getAuthenticatedUsuario();
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        return senseiRepository.findByUsuario(usuarioOpt.get());
    }

    /**
     * --- NUEVO MÉTODO ---
     * Busca el perfil de 'Judoka' del usuario actualmente logueado.
     *
     * @return Optional<Judoka>
     */
    @Transactional(readOnly = true)
    public Optional<Judoka> getAuthenticatedJudoka() {
        Optional<Usuario> usuarioOpt = getAuthenticatedUsuario();
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        // Usamos el Usuario para buscar su perfil de Judoka
        return judokaRepository.findByUsuario(usuarioOpt.get());
    }
}