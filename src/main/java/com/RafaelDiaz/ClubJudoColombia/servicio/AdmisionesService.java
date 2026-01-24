package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.DocumentoRequisitoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
@Service
public class AdmisionesService {

    private final JudokaRepository judokaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoRequisitoRepository documentoRepository;
    private final RolRepository rolRepository;
    private final TraduccionService traduccionService;
    private final TokenInvitacionRepository tokenRepository;
    private final EmailService emailService;
    public AdmisionesService(JudokaRepository judokaRepository,
                             UsuarioRepository usuarioRepository,
                             DocumentoRequisitoRepository documentoRepository,
                             RolRepository rolRepository,
                             TraduccionService traduccionService,
                             TokenInvitacionRepository tokenRepository,
                             EmailService emailService) {
        this.judokaRepository = judokaRepository; // 3. Faltaba esta asignación
        this.usuarioRepository = usuarioRepository;
        this.documentoRepository = documentoRepository;
        this.rolRepository = rolRepository;
        this.traduccionService = traduccionService;
        this.tokenRepository = tokenRepository;   // 4. Asignación agregada
        this.emailService = emailService;         // 5. Asignación agregada
    }
    /**
     * FASE 1: El Sensei inicia el proceso (Invita al aspirante)
     */
    @Transactional
    public void generarInvitacion(String nombre, String apellido, String email, String baseUrl) {
        // 1. Crear el Usuario (Inactivo y sin clave aún)
        // Generamos un username temporal basado en el email
        String username = email.split("@")[0] + "_" + System.currentTimeMillis() % 1000;
        Usuario nuevoUsuario = new Usuario(username, "PENDIENTE", nombre, apellido);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setActivo(false); // Inactivo hasta que cree su clave
        usuarioRepository.save(nuevoUsuario);

        // 2. Crear el Judoka (Estado PENDIENTE)
        Judoka nuevoJudoka = new Judoka();
        nuevoJudoka.setUsuario(nuevoUsuario);
        nuevoJudoka.setEstado(EstadoJudoka.PENDIENTE);
        // Nota: Los datos físicos se llenarán en el Asistente.
        judokaRepository.save(nuevoJudoka);

        // 3. Generar el Token (Válido por 48 horas)
        TokenInvitacion token = new TokenInvitacion(nuevoJudoka, 48);
        tokenRepository.save(token);

        // 4. Enviar el correo con el Magic Link
        emailService.enviarInvitacionMagicLink(email, nombre, token.getToken(), baseUrl);
    }
    /**
     * Sube un documento (Waiver, Médico, etc)
     */
    @Transactional
    public void cargarRequisito(Judoka judoka, TipoDocumento tipo, String urlArchivo) {
        DocumentoRequisito doc = new DocumentoRequisito(judoka, tipo, urlArchivo);
        documentoRepository.save(doc);

        // Si estaba RECHAZADO, vuelve a PENDIENTE para revisión
        if (judoka.getEstado() == EstadoJudoka.RECHAZADO) {
            judoka.setEstado(EstadoJudoka.PENDIENTE);
            judokaRepository.save(judoka);
        }
    }

    /**
     * El Sensei marca manualmente que pagó la matrícula
     */
    @Transactional
    public void registrarPagoMatricula(Judoka judoka) {
        judoka.setMatriculaPagada(true);
        judokaRepository.save(judoka);
    }

    /**
     * 🚀 EL GRAN TRIGGER: ACTIVACIÓN DEFINITIVA
     */
    @Transactional
    public void activarJudoka(Judoka judoka) {
        List<String> faltantes = new ArrayList<>();

        // 1. Validar Waiver
        boolean tieneWaiver = judoka.getDocumentos().stream()
                .anyMatch(d -> d.getTipo() == TipoDocumento.WAIVER);

        if (!tieneWaiver) {
            faltantes.add(traduccionService.get("error.admisiones.falta_waiver")); // I18N
        }

        // 2. Validar Pago
        if (!judoka.isMatriculaPagada()) {
            faltantes.add(traduccionService.get("error.admisiones.falta_pago")); // I18N
        }

        // 3. Decisión
        if (!faltantes.isEmpty()) {
            throw new RuntimeException(traduccionService.get("error.admisiones.requisitos_incompletos") + ": " + String.join(" ", faltantes));
        }

        // 4. ÉXITO: Activar
        judoka.setEstado(EstadoJudoka.ACTIVO);

        // Actualizar Roles del Usuario
        Rol rolJudoka = rolRepository.findByNombre("ROLE_JUDOKA")
                .orElseThrow(() -> new RuntimeException("Error Crítico: No existe el rol ROLE_JUDOKA en la base de datos."));

        Usuario usuario = judoka.getUsuario();
        usuario.setRoles(Collections.singleton(rolJudoka));
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
        judokaRepository.save(judoka);
    }

    @Transactional
    public void rechazarAspirante(Judoka judoka, String motivo) {
        judoka.setEstado(EstadoJudoka.RECHAZADO);
        judokaRepository.save(judoka);
    }
}