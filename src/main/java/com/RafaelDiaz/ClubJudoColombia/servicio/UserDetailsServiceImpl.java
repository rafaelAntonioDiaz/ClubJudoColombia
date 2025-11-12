package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Servicio para cargar los detalles de un usuario para Spring Security.
 *
 * @Service: Lo marcamos como un bean de Spring.
 * implements UserDetailsService: Es la interfaz que Spring Security
 * busca para manejar la autenticación.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Este es el ÚNICO método que Spring Security necesita.
     * Recibe un 'username' (del formulario de login) y debe devolver
     * un objeto 'UserDetails' (que Spring entiende).
     *
     * @param username El username escrito en el formulario.
     * @return Un objeto UserDetails.
     * @throws UsernameNotFoundException Si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos nuestro 'Usuario' en la BD usando nuestro repositorio
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No se encontró el usuario: " + username));

        // 2. "Traducimos" nuestro Usuario (JPA) al User (Spring Security)
        return new User(
                usuario.getUsername(),
                usuario.getPasswordHash(),
                usuario.isActivo(), // (cuenta habilitada)
                true, // (cuenta no expirada)
                true, // (credenciales no expiradas)
                true, // (cuenta no bloqueada)
                mapRolesToAuthorities(usuario.getRoles()) // Los roles
        );
    }

    /**
     * Método helper para "traducir" nuestra colección de entidades 'Rol'
     * a la colección de 'GrantedAuthority' que Spring Security necesita.
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Rol> roles) {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }
}