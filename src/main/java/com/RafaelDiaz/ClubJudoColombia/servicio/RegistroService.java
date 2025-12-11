package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final JudokaRepository judokaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TraduccionService traduccionService;

    // Cache temporal (Email -> Código). Se borra al reiniciar la app (aceptable para MVP)
    private final Map<String, String> codigosVerificacion = new ConcurrentHashMap<>();

    public RegistroService(UsuarioRepository usuarioRepository,
                           JudokaRepository judokaRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           TraduccionService traduccionService) {
        this.usuarioRepository = usuarioRepository;
        this.judokaRepository = judokaRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.traduccionService = traduccionService;
    }

    public void iniciarRegistro(String email, String nombre) {
        if (usuarioRepository.findByUsername(email).isPresent()) {
            throw new RuntimeException(traduccionService.get("error.usuario.existe"));
        }

        String codigo = String.format("%06d", new Random().nextInt(999999));
        codigosVerificacion.put(email, codigo);

        emailService.enviarCodigoActivacion(email, codigo, nombre);
    }

    public boolean validarCodigo(String email, String codigo) {
        String codigoReal = codigosVerificacion.get(email);
        return codigoReal != null && codigoReal.equals(codigo);
    }

    @Transactional
    public void finalizarRegistro(Usuario usuario, Judoka judoka) {
        // 1. Encriptar contraseña y configurar Usuario
        // CORRECCIÓN: Usamos getPasswordHash() que temporalmente tiene el raw password
        String rawPassword = usuario.getPasswordHash();
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword)); // Usamos el setter correcto

        usuario.setActivo(true); // Usuario activo para poder loguearse

        Rol rolJudoka = rolRepository.findByNombre("ROLE_JUDOKA")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_JUDOKA no encontrado"));
        usuario.setRoles(Collections.singleton(rolJudoka));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Configurar y guardar Judoka
        judoka.setUsuario(usuarioGuardado);
        judoka.setEstado(EstadoJudoka.PENDIENTE); // Estado inicial
        judoka.setGradoCinturon(GradoCinturon.BLANCO);
        judoka.setFechaPreRegistro(LocalDateTime.now());

        judokaRepository.save(judoka);

        // 3. Limpiar código
        codigosVerificacion.remove(usuario.getEmail());
    }
}