package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.vaadin.flow.spring.security.AuthenticationContext; // <--- IMPORTANTE
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository;
    private final AuthenticationContext authenticationContext; // <--- NUEVO CAMPO

    public SecurityService(UsuarioRepository usuarioRepository,
                           SenseiRepository senseiRepository,
                           JudokaRepository judokaRepository,
                           AuthenticationContext authenticationContext) { // <--- INYECCIÓN
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.judokaRepository = judokaRepository;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Obtiene el UserDetails del usuario actualmente autenticado.
     */
    public Optional<UserDetails> getAuthenticatedUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
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
                .flatMap(userDetails -> usuarioRepository.findByUsername(userDetails.getUsername()));
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
     * Busca el perfil de 'Judoka' del usuario actualmente logueado.
     */
    @Transactional(readOnly = true)
    public Optional<Judoka> getAuthenticatedJudoka() {
        Optional<Usuario> usuarioOpt = getAuthenticatedUsuario();
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        return judokaRepository.findByUsuario(usuarioOpt.get());
    }
    public boolean isSensei() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SENSEI")))
                .orElse(false);
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        authenticationContext.logout();
    }
}