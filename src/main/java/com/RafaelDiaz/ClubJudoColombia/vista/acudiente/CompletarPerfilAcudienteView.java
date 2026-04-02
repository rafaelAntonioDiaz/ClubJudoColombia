package com.RafaelDiaz.ClubJudoColombia.vista.acudiente;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Completar Perfil Acudiente | Club Judo Colombia")
@Route("completar-perfil-acudiente")
@RolesAllowed("ROLE_ACUDIENTE")
public class CompletarPerfilAcudienteView extends VerticalLayout implements HasUrlParameter<String> {

    private final SecurityService securityService;
    private final UsuarioRepository usuarioRepository;
    private final JudokaRepository judokaRepository;
    private final SenseiRepository senseiRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final AdmisionesService admisionesService;
    private final ConfiguracionService configuracionService;
    private final UserDetailsService userDetailsService;
    // --- Componentes UX ---
    private final PasswordField passwordField = new PasswordField("Crea tu Contraseña");
    private final PasswordField confirmarPasswordField = new PasswordField("Confirma tu Contraseña");
    private final Paragraph textoInstruccionPago = new Paragraph();
    // Lista dinámica de formularios para hijos
    private final VerticalLayout contenedorHijos = new VerticalLayout();
    private final List<FormularioHijo> listaFormulariosHijos = new ArrayList<>();

    // Pago global
    private boolean pagoSubido = false;
    private String urlComprobanteNube = null;
    // Admisiones
    private String tokenInvitacion;
    private Usuario usuarioToken; // Usuario asociado al token
    private boolean vieneDeToken = false;
    private boolean uiBuilt = false; // evita reconstrucción

    public CompletarPerfilAcudienteView(SecurityService securityService,
                                        UsuarioRepository usuarioRepository,
                                        JudokaRepository judokaRepository,
                                        SenseiRepository senseiRepository,
                                        PasswordEncoder passwordEncoder,
                                        AlmacenamientoCloudService almacenamientoCloudService,
                                        AdmisionesService admisionesService,
                                        ConfiguracionService configuracionService,
                                        UserDetailsService userDetailsService) {
        this.securityService = securityService;
        this.usuarioRepository = usuarioRepository;
        this.judokaRepository = judokaRepository;
        this.senseiRepository = senseiRepository;
        this.passwordEncoder = passwordEncoder;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.admisionesService = admisionesService;
        this.configuracionService = configuracionService;
        this.userDetailsService = userDetailsService;


    }

    private void agregarFormularioHijo() {
        FormularioHijo formHijo = new FormularioHijo();
        listaFormulariosHijos.add(formHijo);
        contenedorHijos.add(formHijo);
        actualizarTextoPago();
    }

    private void actualizarTextoPago() {
        com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema config = configuracionService.obtenerConfiguracion();
        int cantidadHijos = listaFormulariosHijos.size();

        // 1. Tomamos los valores crudos, protegiéndonos de posibles nulos
        BigDecimal canonBase = config.getFIN_SENSEI_MASTER_MENSUALIDAD() != null ? config.getFIN_SENSEI_MASTER_MENSUALIDAD(): BigDecimal.ZERO;
        BigDecimal matriculaAnual = config.getFIN_MATRICULA_ANUAL() != null ? config.getFIN_MATRICULA_ANUAL() : BigDecimal.ZERO;

        // 2. Combo de Ingreso: Matrícula + Primer Mes
        BigDecimal costoPorNino = canonBase.add(matriculaAnual);

        // 3. Multiplicamos por la cantidad de deportistas
        BigDecimal totalCalculado = costoPorNino.multiply(BigDecimal.valueOf(cantidadHijos));

        // 4. Formateamos a moneda
        String totalFormateado = configuracionService.obtenerFormatoMoneda().format(totalCalculado);

        // 5. Imprimimos el mensaje claro y transparente para el acudiente
        textoInstruccionPago.setText("Vas a registrar " + cantidadHijos + " deportista(s). " +
                "El pago de ingreso incluye la Matrícula Anual y la primera Mensualidad. " +
                "Realiza UNA SOLA transferencia por " + totalFormateado + " y adjunta el comprobante.");
    }

    private void procesarOnboarding() {
        if (passwordField.isEmpty() || !passwordField.getValue().equals(confirmarPasswordField.getValue())) {
            Notification.show("Las contraseñas no coinciden o están vacías.", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (listaFormulariosHijos.isEmpty() || listaFormulariosHijos.get(0).nombreField.isEmpty()) {
            Notification.show("Debes registrar al menos un deportista.", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Validación profunda: Cada niño debe tener sus 2 documentos
        for (FormularioHijo fh : listaFormulariosHijos) {
            if (fh.nombreField.isEmpty() || fh.fechaNacField.isEmpty()) {
                Notification.show("Completa los datos personales de todos los deportistas.", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!fh.waiverSubido || !fh.epsSubida) {
                Notification.show("Falta EPS o Exoneración para: " + fh.nombreField.getValue(), 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        if (!pagoSubido) {
            Notification.show("Debes subir el comprobante de pago global para continuar.", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Usuario acudiente;
            if (vieneDeToken) {
                // Usuario del token aún no activo, debemos establecer su contraseña y activarlo
                acudiente = usuarioToken;
                String pass = passwordField.getValue();
                if (pass.isEmpty() || !pass.equals(confirmarPasswordField.getValue())) {
                    Notification.show("Las contraseñas no coinciden o están vacías.", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                acudiente.setPasswordHash(passwordEncoder.encode(pass));
                acudiente.setActivo(true);
                usuarioRepository.save(acudiente);
            } else {
                acudiente = getAcudienteLogueado();
                // Si ya estaba autenticado, no se modifica la contraseña
            }

            Sensei senseiAsignado = senseiRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No hay un Sensei disponible para asignar."));

            for (FormularioHijo fh : listaFormulariosHijos) {
                Judoka nuevoHijo = new Judoka();
                nuevoHijo.setNombre(fh.nombreField.getValue());
                nuevoHijo.setApellido(fh.apellidoField.getValue());
                nuevoHijo.setFechaNacimiento(fh.fechaNacField.getValue());
                nuevoHijo.setAcudiente(acudiente);
                nuevoHijo.setSensei(senseiAsignado);
                nuevoHijo.setEstado(EstadoJudoka.EN_REVISION);
                nuevoHijo.setMayorEdad(false);

                Judoka guardado = judokaRepository.save(nuevoHijo);

                admisionesService.cargarRequisito(guardado, TipoDocumento.COMPROBANTE_PAGO, urlComprobanteNube);
                admisionesService.cargarRequisito(guardado, TipoDocumento.EPS, fh.urlEpsNube);
                admisionesService.cargarRequisito(guardado, TipoDocumento.WAIVER, fh.urlWaiverNube);
            }

            // Consumir el token si vino por invitación
            if (vieneDeToken && tokenInvitacion != null) {
                admisionesService.consumirToken(tokenInvitacion);
            }

            // Si vino de token, autenticar al usuario para que inicie sesión automáticamente
            if (vieneDeToken) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(acudiente.getUsername());
                autenticarUsuario(userDetails);
            }

            Notification.show("¡Registro completado exitosamente!", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(""));

        } catch (Exception e) {
            Notification.show("Error en el registro: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

    }

    private Usuario getAcudienteLogueado() {
        String username = securityService.getAuthenticatedUserDetails().get().getUsername();
        return usuarioRepository.findByUsername(username).orElseThrow();
    }

    private Upload crearComponenteSubida(String etiqueta, java.util.function.Consumer<String> onSuccess) {
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("application/pdf", ".pdf", "image/png", ".png", "image/jpeg", ".jpg");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setDropLabel(new Span(etiqueta));
        upload.setMaxFiles(1);

        upload.setUploadHandler(UploadHandler.inMemory((metadata, bytes) -> {
            try {
                Usuario acudiente = getAcudienteLogueado();
                InputStream in = new ByteArrayInputStream(bytes);
                String keyEnLaNube = almacenamientoCloudService.subirArchivo(acudiente.getId(), metadata.fileName(), in);
                String url = almacenamientoCloudService.obtenerUrl(acudiente.getId(), keyEnLaNube);

                getUI().ifPresent(ui -> ui.access(() -> {
                    onSuccess.accept(url);
                    Notification.show("¡" + metadata.fileName() + " subido con éxito!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));
            } catch (Exception ex) {
                getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error al subir archivo.", 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR)));
            }
        }));
        return upload;
    }

    // --- IMPORTANTE: No es 'static' para poder acceder al uploader de la clase padre ---
    private class FormularioHijo extends FormLayout {
        TextField nombreField = new TextField("Nombre");
        TextField apellidoField = new TextField("Apellido");
        DatePicker fechaNacField = new DatePicker("Fecha de Nacimiento");

        String urlEpsNube = null;
        String urlWaiverNube = null;
        boolean epsSubida = false;
        boolean waiverSubido = false;

        public FormularioHijo() {
            nombreField.setRequired(true);
            apellidoField.setRequired(true);
            fechaNacField.setRequired(true);

            getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            getStyle().set("padding", "15px");
            getStyle().set("border-radius", "8px");
            getStyle().set("margin-bottom", "15px");

            Upload uploadWaiver = crearComponenteSubida("Subir Waiver firmado", url -> {
                this.urlWaiverNube = url;
                this.waiverSubido = true;
            });

            Upload uploadEps = crearComponenteSubida("Subir EPS", url -> {
                this.urlEpsNube = url;
                this.epsSubida = true;
            });

            add(nombreField, apellidoField, fechaNacField, uploadWaiver, uploadEps);

            // UX: Los documentos ocupan toda la fila para verse mejor en móviles
            setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
            setColspan(uploadWaiver, 2);
            setColspan(uploadEps, 2);
        }
    }
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {
        System.out.println("DEBUG: token recibido = " + token);
        if (token != null && !token.isEmpty()) {
            try {
                TokenInvitacion tokenObj = admisionesService.validarTokenInvitacion(token);
                usuarioToken = tokenObj.getUsuarioInvitado();
                tokenInvitacion = token;
                vieneDeToken = true;

                // Verificar que si ya hay sesión, corresponda al mismo usuario
                Optional<Usuario> usuarioActual = securityService.getAuthenticatedUsuario();
                if (usuarioActual.isPresent() && !usuarioActual.get().getId().equals(usuarioToken.getId())) {
                    NotificationHelper.error("Esta invitación pertenece a otro usuario.");
                    getUI().ifPresent(ui -> ui.navigate(""));
                    return;
                }
            } catch (RuntimeException e) {
                System.out.println("DEBUG: token inválido - " + e.getMessage());
                NotificationHelper.error("Token inválido o expirado: " + e.getMessage());
                getUI().ifPresent(ui -> ui.navigate("login"));
                return;
            }
        } else {
            // Sin token, se espera que el usuario ya esté autenticado (edición de perfil)
            vieneDeToken = false;
            usuarioToken = securityService.getAuthenticatedUsuario()
                    .orElseThrow(() -> new RuntimeException("Usuario no autenticado."));
        }

        buildUI();
    }

    private void buildUI() {
        if (uiBuilt) return;
        uiBuilt = true;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("700px");
        card.setPadding(true);

        card.add(new H2("¡Bienvenido a la Academia!"));
        card.add(new Paragraph("Como acudiente, desde aquí podrás gestionar la cuenta de tus deportistas."));

        // 1. SECCIÓN CONTRASEÑA - solo si viene de token
        if (vieneDeToken) {
            System.out.println("DEBUG: vieneDeToken = true, mostrando campos de contraseña");
            passwordField.setRequired(true);
            confirmarPasswordField.setRequired(true);
            FormLayout formPassword = new FormLayout(passwordField, confirmarPasswordField);
            card.add(new H3("1. Tu Seguridad"), formPassword);
        } else {
            // Opcional: mostrar un mensaje si ya está autenticado
            card.add(new H3("1. Tu Perfil"));
            card.add(new Paragraph("Puedes editar la información de tus deportistas."));
        }
        // 2. DESCARGA DE FORMATO GLOBAL (sin cambios)
        card.add(new H3("2. Formato de Exoneración"));
        card.add(new Paragraph("Descarga este formato. Deberás firmar uno por cada deportista que registres y subirlo en su respectiva tarjeta."));
        Anchor enlaceDescarga = new Anchor("documentos/formato_waiver.pdf", "");
        enlaceDescarga.getElement().setAttribute("download", true);
        Button btnDescargar = new Button("Descargar Formato Vacío", new Icon(VaadinIcon.DOWNLOAD));
        btnDescargar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        enlaceDescarga.add(btnDescargar);
        card.add(enlaceDescarga);

        // 3. SECCIÓN HIJOS (Judokas + Documentos Granulares)
        card.add(new H3("3. Registra a tus Deportistas"));
        contenedorHijos.setPadding(false);
        // Solo añadimos un hijo por defecto si no hay ninguno
        if (listaFormulariosHijos.isEmpty()) {
            agregarFormularioHijo();
        }

        Button btnAñadirHijo = new Button("Añadir otro deportista", VaadinIcon.PLUS.create());
        btnAñadirHijo.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnAñadirHijo.addClickListener(e -> agregarFormularioHijo());
        card.add(contenedorHijos, btnAñadirHijo);

        // 4. SECCIÓN PAGO GLOBAL (sin cambios)
        card.add(new H3("4. Activación y Pago Unificado"));
        textoInstruccionPago.getStyle().set("font-weight", "bold");
        textoInstruccionPago.getStyle().set("color", "var(--lumo-primary-text-color)");
        card.add(textoInstruccionPago);

        Upload uploadPago = crearComponenteSubida("Sube aquí el comprobante Nequi GLOBAL (.png o .jpg)", url -> {
            this.urlComprobanteNube = url;
            this.pagoSubido = true;
        });
        card.add(uploadPago);

        // 5. BOTÓN FINALIZAR
        Button btnFinalizar = new Button("Guardar Perfil y Enviar a Revisión", VaadinIcon.CHECK_CIRCLE.create());
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnFinalizar.setWidthFull();
        btnFinalizar.addClickListener(e -> procesarOnboarding());

        card.add(new Hr(), btnFinalizar);
        add(card);
    }
    // Método auxiliar para autenticar después de establecer contraseña
    private void autenticarUsuario(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, request, null);
    }
}
