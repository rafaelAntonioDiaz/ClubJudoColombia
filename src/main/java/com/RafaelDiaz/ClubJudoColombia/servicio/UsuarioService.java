package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // --- NUEVO ---
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // --- NUEVO: Campo para el codificador ---
    private final PasswordEncoder passwordEncoder;

    /**
     * Inyección de dependencias vía constructor.
     *
     * --- MODIFICADO ---
     * Ahora pedimos a Spring que nos inyecte AMBOS:
     * 1. El Repositorio (como antes).
     * 2. El PasswordEncoder (que definimos en SecurityConfig).
     */
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder; // --- NUEVO ---
    }

    /**
     * Obtiene todos los usuarios de la base de datos.
     */
    public List<Usuario> findAllUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * --- MÉTODO MODIFICADO Y MEJORADO ---
     *
     * Guarda un usuario.
     * Si se provee una contraseña en 'plainPassword',
     * la hashea y la establece en el usuario antes de guardarlo.
     *
     * @param usuario El usuario a guardar (con datos como username, nombre, etc.)
     * @param plainPassword La contraseña en texto plano (o null/vacía si no se cambia).
     * @return El usuario guardado.
     */
    public Usuario saveUsuario(Usuario usuario, String plainPassword) {
        if (usuario == null) {
            System.err.println("Error: Intentando guardar un usuario nulo.");
            return null;
        }

        // --- LÓGICA DE HASHING ---
        // Solo hasheamos y establecemos la contraseña si se proporcionó una nueva
        // (es decir, si no es nula Y no está vacía).
        if (plainPassword != null && !plainPassword.isEmpty()) {
            // Usamos el bean inyectado para codificar la contraseña
            usuario.setPasswordHash(passwordEncoder.encode(plainPassword));
        }

        // Guardamos el usuario.
        // Si plainPassword estaba vacía, el passwordHash del objeto 'usuario'
        // (que venía del binder) se mantiene intacto (conserva el hash antiguo).
        return usuarioRepository.save(usuario);
    }
    /**
     * Busca un usuario por su username.
     * Es una buena práctica exponer la funcionalidad del repositorio
     * a través de la capa de servicio.
     *
     * @param username El username a buscar.
     * @return Un Optional<Usuario> que contiene al usuario si se encuentra.
     */
    public Optional<Usuario> findByUsername(String username) {
        // Simplemente llamamos al método del repositorio
        return usuarioRepository.findByUsername(username);
    }
}