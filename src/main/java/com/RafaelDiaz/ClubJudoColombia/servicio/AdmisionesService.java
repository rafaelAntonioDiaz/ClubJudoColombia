package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdmisionesService {
    private final SecurityService securityService;
    private final EmailService emailService;
    private final ConfiguracionService configuracionService;
    private final GrupoEntrenamientoService grupoService;
    private final JudokaRepository judokaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoRequisitoRepository documentoRepository;
    private final RolRepository rolRepository;
    private final GrupoEntrenamientoRepository grupoRepository;
    private final TraduccionService traduccionService;
    private final TokenInvitacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final JudokaService judokaService;
    private static final Logger logger = LoggerFactory.getLogger(AdmisionesService.class);

    public AdmisionesService(SecurityService securityService, ConfiguracionService configuracionService, GrupoEntrenamientoService grupoService, JudokaRepository judokaRepository,
                             UsuarioRepository usuarioRepository,
                             DocumentoRequisitoRepository documentoRepository,
                             RolRepository rolRepository, GrupoEntrenamientoRepository grupoRepository,
                             TraduccionService traduccionService,
                             TokenInvitacionRepository tokenRepository,
                             EmailService emailService, PasswordEncoder passwordEncoder,
                             JudokaService judokaService) {
        this.securityService = securityService;
        this.configuracionService = configuracionService;
        this.grupoService = grupoService;
        this.judokaRepository = judokaRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentoRepository = documentoRepository;
        this.rolRepository = rolRepository;
        this.grupoRepository = grupoRepository;
        this.traduccionService = traduccionService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.judokaService = judokaService; // 3. Asignación
    }

    /**
     * FASE 1: Motor Unificado de Invitaciones.
     * Genera el usuario de forma temprana, le asigna su rol y devuelve el Token
     * para que la UI ensamble el mensaje de WhatsApp.
     */
    @Transactional
    public String generarInvitacion(String nombre, String apellido,
                                    String email, String celular,
                                    String rolEsperado, String baseUrl,
                                    Boolean esClubPropio, Long grupoId) {

        // 1. Validar que el grupo sea obligatorio para roles de judoka
        if (("ROLE_JUDOKA".equals(rolEsperado) || "ROLE_JUDOKA_ADULTO".equals(rolEsperado)) && grupoId == null) {
            throw new RuntimeException("Debes seleccionar un grupo para el nuevo judoka.");
        }

        // 2. Obtener el sensei que invita (puede ser null si invita el Master)
        Sensei senseiActual = securityService.getAuthenticatedSensei().orElse(null);

        // 3. Si se proporcionó grupoId, validar que exista y pertenezca al sensei (o al sensei del grupo si el que invita es Master)
        GrupoEntrenamiento grupo = null;
        if (grupoId != null) {
            grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new RuntimeException("El grupo seleccionado no existe."));

            if (senseiActual != null && !grupo.getSensei().getId().equals(senseiActual.getId())) {
                throw new RuntimeException("No puedes invitar a un grupo que no te pertenece.");
            }
        }

        // 4. Crear o recuperar el Usuario base
        Usuario usuarioInvitado = usuarioRepository.findByUsername(email).orElse(new Usuario());
        usuarioInvitado.setUsername(email);
        usuarioInvitado.setEmail(email);
        usuarioInvitado.setNombre(nombre);
        usuarioInvitado.setApellido(apellido);
        usuarioInvitado.setCelular(celular);
        usuarioInvitado.setActivo(false);
        usuarioInvitado.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        // Asignar rol
        Rol rol = rolRepository.findByNombre(rolEsperado)
                .orElseThrow(() -> new RuntimeException("El rol " + rolEsperado + " no existe."));
        usuarioInvitado.setRoles(Set.of(rol));

        usuarioRepository.saveAndFlush(usuarioInvitado);

        // 5. Crear Judoka si el rol lo requiere
        Judoka judokaVinculado = null;
        if ("ROLE_JUDOKA".equals(rolEsperado) || "ROLE_JUDOKA_ADULTO".equals(rolEsperado)) {

            judokaVinculado = new Judoka();

            // --- DIFERENCIACIÓN CLAVE ---
            if ("ROLE_JUDOKA_ADULTO".equals(rolEsperado)) {
                // Adulto: el acudiente es él mismo
                judokaVinculado.setAcudiente(usuarioInvitado);
            } else {
                // Menor: el acudiente es el usuario que se está creando (el padre/madre)
                // Nota: En este flujo, el usuario invitado es el acudiente, no el judoka.
                // El judoka menor no tiene usuario propio, solo el acudiente.
                // Por lo tanto, NO debemos crear un usuario para el menor. El usuario ya es el acudiente.
                // Pero aquí estamos creando un judoka asociado al acudiente, eso está bien.
                judokaVinculado.setAcudiente(usuarioInvitado);
            }

            judokaVinculado.setCelular(celular);
            judokaVinculado.setSensei(senseiActual);
            judokaVinculado.setEstado(EstadoJudoka.PENDIENTE);

            // Asignar grupo de facturación (nuevo campo)
            judokaVinculado.setGrupoFacturacion(grupo);

            // Si el senseiActual es null (Master invita), asignamos el sensei del grupo
            if (senseiActual == null && grupo != null) {
                judokaVinculado.setSensei(grupo.getSensei());
            }

            judokaRepository.saveAndFlush(judokaVinculado);

            // Inicializar datos adicionales del judoka (peso, talla, etc.) - si aplica
            judokaService.inicializarJudokaNuevo(judokaVinculado);

            // Asignar el judoka al grupo (relación many-to-many)
            if (grupo != null) {
                grupoService.addJudokaToGrupo(grupo.getId(), judokaVinculado.getId());
            }
        }

        // 6. Generar el Token de invitación
        TokenInvitacion token = new TokenInvitacion();
        token.setUsuarioInvitado(usuarioInvitado);
        token.setRolEsperado(rolEsperado);
        token.setSensei(senseiActual);
        token.setJudoka(judokaVinculado);
        token.setGrupo(grupo); // Guardamos el grupo para referencia histórica
        token.setEsClubPropio(rolEsperado.equals("ROLE_SENSEI") ? esClubPropio : null);
        token.generarToken(48); // Válido 48 horas
        tokenRepository.save(token);

        // 7. (Opcional) Enviar email
        if (email != null && !email.isEmpty()) {
            emailService.enviarInvitacionMagicLink(email, nombre, token.getToken(), baseUrl);
        }

        // 8. Retornar el token
        return token.getToken();
    }

    /**
     * Sube un documento (Waiver, Médico, etc)
     */
    @Transactional
    public void cargarRequisito(Judoka judoka, TipoDocumento tipo, String urlArchivo) {
        DocumentoRequisito doc = new DocumentoRequisito(judoka, tipo, urlArchivo);
        documentoRepository.save(doc);

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
     * ACTIVACIÓN DEFINITIVA
     */
    @Transactional
    public void activarJudoka(Judoka judoka) {
        List<String> faltantes = new ArrayList<>();

        // Identificar si es SaaS o alumno directo del Master
        boolean esSaaS = judoka.getSensei() != null && !judoka.getSensei().getUsuario().getUsername().equals("master_admin");

        // --- LA TRINIDAD DEL DOJO PRINCIPAL ---
        // Solo exigimos Waiver y EPS si NO es SaaS
        if (!esSaaS) {
            boolean tieneWaiver = judoka.getDocumentos().stream()
                    .anyMatch(d -> d.getTipo() == TipoDocumento.WAIVER);
            if (!tieneWaiver) {
                faltantes.add(traduccionService.get("error.admisiones.falta_waiver"));
            }

            boolean tieneEps = judoka.getDocumentos().stream()
                    .anyMatch(d -> d.getTipo() == TipoDocumento.EPS);
            if (!tieneEps) {
                faltantes.add(traduccionService.get("error.admisiones.falta_eps") != null ?
                        traduccionService.get("error.admisiones.falta_eps") : "Falta Certificado EPS");
            }
        }

        // --- REQUISITO UNIVERSAL ---
        // El pago sí es obligatorio para absolutamente todos
        if (!judoka.isMatriculaPagada()) {
            faltantes.add(traduccionService.get("error.admisiones.falta_pago"));
        }

        // Si falta algo, bloqueamos la activación y mostramos la lista exacta de lo que falta
        if (!faltantes.isEmpty()) {
            throw new RuntimeException(traduccionService.get("error.admisiones.requisitos_incompletos") + ": " + String.join(", ", faltantes));
        }

        // --- SI PASA TODAS LAS PRUEBAS, LO ACTIVAMOS ---
        judoka.setEstado(EstadoJudoka.ACTIVO);

        Usuario usuario = judoka.getUsuario();
        // NO reasignamos roles, ya vienen correctos desde la invitación
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
        judokaRepository.save(judoka);
    }

    @Transactional
    public void rechazarAspirante(Judoka judoka, String motivo) {
        judoka.setEstado(EstadoJudoka.RECHAZADO);
        judokaRepository.save(judoka);
    }

    @Transactional
    public Judoka obtenerJudokaPorToken(String uuid) {
        TokenInvitacion token = tokenRepository.findByToken(uuid)
                .orElseThrow(() -> new RuntimeException("Token inválido o no existe."));

        if (!token.isValido()) {
            throw new RuntimeException("El enlace ha expirado o ya fue utilizado.");
        }

        Judoka judoka = token.getJudoka();
        judoka.getUsuario().getNombre();
        return judoka;
    }

    /**
     * Helper para pruebas o creación manual
     */
    @Transactional
    public String crearAspiranteYGenerarToken(String nombre, String apellido, String email) {
        Usuario nuevoUsuario = new Usuario(email, "PENDIENTE", nombre, apellido);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setUsername(email);

        nuevoUsuario.setActivo(false);
        usuarioRepository.save(nuevoUsuario);

        Judoka nuevoJudoka = new Judoka();
        nuevoJudoka.setAcudiente(nuevoUsuario);
        nuevoJudoka.setSensei(securityService.getAuthenticatedSensei().get());
        nuevoJudoka.setEstado(EstadoJudoka.PENDIENTE);
        judokaRepository.save(nuevoJudoka);

        judokaService.inicializarJudokaNuevo(nuevoJudoka);

        TokenInvitacion token = new TokenInvitacion(nuevoJudoka, 48);
        tokenRepository.save(token);
        return token.getToken();
    }

    @Transactional
    public void consumirToken(String uuid) {
        TokenInvitacion token = tokenRepository.findByToken(uuid)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));
        token.setUsado(true);
        tokenRepository.save(token);
    }
    // En AdmisionesService.java

    @Transactional
    public void registrarJudokaAdulto(String nombre, String apellido, String email, String telefono, String nombreEmergencia, String telEmergencia) {

        // 1. BUSCAR EL ROL EN LA BD (No puedes asignar el String directo)
        // Asumimos que "ROLE_JUDOKA_ADULTO" ya existe en tu tabla de roles.
        Rol rolAdulto = rolRepository.findByNombre("ROLE_JUDOKA_ADULTO")
                .orElseThrow(() -> new RuntimeException("Error: El rol ROLE_JUDOKA_ADULTO no existe en la BD."));

        // 2. CREAR EL USUARIO
        Usuario usuarioAdulto = new Usuario();
        usuarioAdulto.setNombre(nombre);
        usuarioAdulto.setApellido(apellido);
        usuarioAdulto.setEmail(email);
        usuarioAdulto.setUsername(email);

        // Asegúrate de inyectar passwordEncoder en el constructor de AdmisionesService
        usuarioAdulto.setPasswordHash(passwordEncoder.encode("contraseña"));

        // CORRECCIÓN: Asignamos un Set<Rol> que contiene el objeto encontrado
        usuarioAdulto.setRoles(java.util.Set.of(rolAdulto));

        usuarioAdulto.setActivo(true); // Importante para que pueda loguearse
        usuarioRepository.save(usuarioAdulto);

        // 3. CREAR EL JUDOKA (Perfil deportivo)
        Judoka perfilDeportivo = new Judoka();
        perfilDeportivo.setNombre(nombre);
        perfilDeportivo.setApellido(apellido);
        perfilDeportivo.setMayorEdad(true); // Flag de UI


        // Datos de emergencia (Crítico para adultos)
        perfilDeportivo.setNombreContactoEmergencia(nombreEmergencia);
        perfilDeportivo.setTelefonoEmergencia(telEmergencia);

        // 4. EL PATRÓN AUTO-ACUDIENTE
        // El usuario se asigna a sí mismo como responsable financiero
        perfilDeportivo.setAcudiente(usuarioAdulto);

        // Inicializamos fechas para que no entre en NullPointer en las vistas
        perfilDeportivo.setFechaVencimientoSuscripcion(java.time.LocalDate.now().plusDays(30)); // Regalo de bienvenida
    }
    /**
     * Validación de Onboarding.
     * Lee el token, activa el usuario y quema la invitación.
     */
    @Transactional
    public Usuario validarYActivarInvitacion(String tokenUuid) {
        TokenInvitacion token = tokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token inválido o no existe."));

        if (!token.isValido()) {
            throw new RuntimeException("El enlace ha expirado o ya fue utilizado.");
        }

        Usuario usuario = token.getUsuarioInvitado();
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        token.setUsado(true);
        tokenRepository.save(token);
        usuario.getRoles().size();
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<Judoka> obtenerJudokasParaValidacion() {
        // 1. Buscamos solo los que nos interesan
        List<Judoka> lista = judokaRepository.findAll().stream()
                .filter(j -> j.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.EN_REVISION ||
                        j.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.PENDIENTE)
                .collect(java.util.stream.Collectors.toList());

        for (Judoka j : lista) {
            // Despertamos Sensei y su Usuario
            if (j.getSensei() != null && j.getSensei().getUsuario() != null) {
                j.getSensei().getUsuario().getUsername();
                j.getSensei().getUsuario().getNombre();
            }

            try {
                if (j.getUsuario() != null) {
                    j.getUsuario().getNombre();
                }
            } catch (Exception ignored) { }

            if (j.getDocumentos() != null) {
                j.getDocumentos().size(); // Llamar a .size() obliga a Hibernate a traer los documentos
            }
        }
        return lista;
    }
    // Solo valida que el token sea válido, sin activar ni consumir
    @Transactional(readOnly = true)
    public TokenInvitacion validarTokenInvitacion(String tokenUuid) {
        TokenInvitacion token = tokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException(
                        traduccionService.get("error.token_invalido") // o mensaje directo
                ));
        if (!token.isValido()) {
            throw new RuntimeException(traduccionService.get("error.token_expirado"));
        }

        // Inicializar el usuario y sus roles dentro de la transacción
        Usuario usuario = token.getUsuarioInvitado();
        Hibernate.initialize(usuario);
        Hibernate.initialize(usuario.getRoles()); // si roles son lazy

        return token;
    }

    // Activa la cuenta después de que el usuario eligió su contraseña
    @Transactional
    public Usuario activarInvitacionConPassword(String tokenUuid, String password) {
        TokenInvitacion token = tokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token inválido."));
        if (!token.isValido()) {
            throw new RuntimeException("El enlace ha expirado o ya fue utilizado.");
        }
        Usuario usuario = token.getUsuarioInvitado();
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        token.setUsado(true);
        tokenRepository.save(token);
        return usuario;
    }
}