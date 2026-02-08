package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext; // <--- IMPORTANTE
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    private final JudokaRepository judokaRepository;
    private final AuthenticationContext authenticationContext;
    private static final String SENSEI_ID_SESSION_KEY = "CURRENT_SENSEI_ID";
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


    @Transactional(readOnly = true)
    public Optional<Judoka> getAuthenticatedJudoka() {
        return getAuthenticatedUserDetails().flatMap(userDetails -> {
            return usuarioRepository.findByUsername(userDetails.getUsername())
                    .flatMap(usuario -> {
                        // Usamos el método armonizado
                        List<Judoka> judokas = judokaRepository.findByAcudiente(usuario);
                        // Retornamos el primero para no romper las vistas actuales
                        return judokas.stream().findFirst();
                    });
        });
    }

    public boolean isSensei() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SENSEI")))
                .orElse(false);
    }
    /**
     * MAGIA MULTI-TENANT: Obtiene el ID del Sensei logueado.
     * Si ya está en la caché de la sesión, lo devuelve al instante.
     * Si no, lo busca en la BD y lo guarda en la caché.
     */
    public Long getSenseiIdActual() {
        VaadinSession session = VaadinSession.getCurrent();

        // 1. ¿Ya lo tenemos en la caché?
        Long cachedSenseiId = (Long) session.getAttribute(SENSEI_ID_SESSION_KEY);
        if (cachedSenseiId != null) {
            return cachedSenseiId;
        }

        // 2. Si no está en caché, buscamos al usuario logueado
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            String username = userDetails.get().getUsername();

            // Buscamos su Usuario y su Perfil de Sensei
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario != null) {
                Optional<Sensei> sensei = senseiRepository.findByUsuario(usuario);
                if (sensei.isPresent()) {
                    Long senseiId = sensei.get().getId();

                    // 3. ¡Lo guardamos en la sesión para no volver a buscarlo!
                    session.setAttribute(SENSEI_ID_SESSION_KEY, senseiId);
                    return senseiId;
                }
            }
        }

        // Si no es un Sensei (ej. es solo ROLE_JUDOKA) o no está logueado
        return null;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        authenticationContext.logout();
    }
}