package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext; // <--- IMPORTANTE
import org.hibernate.Hibernate;
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
                .map(UserDetails::getUsername)
                .flatMap(usuarioRepository::findByUsernameWithRoles)
                .map(usuario -> {
                    // Forzar inicialización de cualquier proxy restante
                    Hibernate.initialize(usuario);
                    Hibernate.initialize(usuario.getRoles());
                    return usuario;
                });
    }

    /**
     * Busca el perfil de 'Sensei' del usuario actualmente logueado.
     */
    @Transactional(readOnly = true)
    public Optional<Sensei> getAuthenticatedSensei() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Long senseiId = (Long) session.getAttribute(SENSEI_ID_SESSION_KEY);
            if (senseiId != null) {
                return senseiRepository.findById(senseiId)
                        .map(sensei -> {
                            Hibernate.initialize(sensei.getUsuario()); // Forzar carga
                            return sensei;
                        });
            }
        }
        return getAuthenticatedUsuario()
                .flatMap(senseiRepository::findByUsuario)
                .map(sensei -> {
                    Hibernate.initialize(sensei.getUsuario()); // Forzar carga
                    return sensei;
                });
    }

    @Transactional(readOnly = true)
    public Optional<Judoka> getAuthenticatedJudoka() {
        VaadinSession session = VaadinSession.getCurrent();
        // 1. Prioridad: ¿Viene por Magic Link? (ID guardado en sesión de Vaadin)
        if (session != null) {
            Long judokaId = (Long) session.getAttribute("JUDOKA_ACTUAL_ID");
            if (judokaId != null) {
                return judokaRepository.findByIdWithDetails(judokaId);
            }
        }

        // 2. Intentar obtener el UserDetails y ver si el username tiene formato "judoka_"
        Optional<UserDetails> userDetailsOpt = getAuthenticatedUserDetails();
        if (userDetailsOpt.isPresent()) {
            String username = userDetailsOpt.get().getUsername();
            if (username != null && username.startsWith("judoka_")) {
                try {
                    Long id = Long.parseLong(username.substring(7));
                    return judokaRepository.findByIdWithDetails(id);
                } catch (NumberFormatException e) {
                    // Ignorar, no es un ID válido
                }
            }
        }

        // 3. Fallback: usuario autenticado normal (Acudiente/Sensei/Master)
        return getAuthenticatedUserDetails().flatMap(userDetails ->
                usuarioRepository.findByUsername(userDetails.getUsername())
                        .flatMap(usuario -> {
                            List<Judoka> judokas = judokaRepository.findByAcudienteWithDetails(usuario);
                            return judokas.stream().findFirst();
                        })
        );
    }


    public boolean isSensei() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SENSEI")))
                .orElse(false);
    }
    public boolean isMecenas() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_MECENAS")))
                .orElse(false);
    }
    public boolean isAcudiente() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ACUDIENTE")))
                .orElse(false);
    }
    public boolean isMaster() {
        return getAuthenticatedUserDetails()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER")))
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
    @Transactional(readOnly = true)
    public String getAuthenticatedNombreCompleto() {
        return getAuthenticatedUserDetails()
                .map(UserDetails::getUsername)
                .flatMap(usuarioRepository::findByUsernameWithRoles)
                .map(u -> u.getNombre() + " " + u.getApellido())
                .orElse("Usuario");
    }
    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        authenticationContext.logout();
    }

    /**
     * Record simple para los datos del sensei que se muestran en la UI.
     */
    public record SenseiProfile(String fullName, String avatarUrl, String clubName) {}

    @Transactional(readOnly = true)
    public SenseiProfile getAuthenticatedSenseiProfile() {
        return getAuthenticatedSensei()
                .map(sensei -> {
                    Usuario usuario = sensei.getUsuario();
                    String fullName = usuario.getNombre() + " " + usuario.getApellido();
                    String avatarUrl = sensei.getUrlFotoPerfil();
                    String clubName = sensei.getNombreClub() != null ? sensei.getNombreClub() : "";
                    return new SenseiProfile(fullName, avatarUrl, clubName);
                })
                .orElse(new SenseiProfile("Sensei", null, ""));
    }

    public record JudokaProfile(String fullName, String avatarUrl) {}

    @Transactional(readOnly = true)
    public JudokaProfile getAuthenticatedJudokaProfile() {
        return getAuthenticatedJudoka()
                .map(judoka -> {
                    String fullName = judoka.getNombre() + " " + judoka.getApellido();
                    String avatarUrl = judoka.getUrlFotoPerfil();
                    return new JudokaProfile(fullName, avatarUrl);
                })
                .orElse(new JudokaProfile("Judoka", null));
    }
}