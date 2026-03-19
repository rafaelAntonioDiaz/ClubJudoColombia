package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import java.util.Optional;

@PageTitle("Completar Perfil de Profesor")
@Route("completar-perfil-sensei")
@RolesAllowed({"ROLE_SENSEI", "ROLE_MASTER"})
public class CompletarPerfilSenseiView extends VerticalLayout implements HasUrlParameter<String> {

    private final SecurityService securityService;
    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    private final TokenInvitacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private String token;

    public CompletarPerfilSenseiView(SecurityService securityService,
                                     UsuarioRepository usuarioRepository, SenseiRepository senseiRepository, TokenInvitacionRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.securityService = securityService;
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        construirUI();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.token = parameter;
        construirUI();
    }

    private void construirUI() {
        // ... (el mismo código de UI que ya tenías, pero ahora usando el token)
        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("500px");
        card.setPadding(true);

        H2 titulo = new H2("¡Bienvenido a tu nuevo SaaS!");
        Paragraph subtitulo = new Paragraph("Configura tu perfil de Profesor y dale un nombre a tu espacio de trabajo.");

        com.vaadin.flow.component.textfield.TextField nombreDojoField = new com.vaadin.flow.component.textfield.TextField("Nombre de tu Club / Dojo");
        nombreDojoField.setRequired(true);
        nombreDojoField.setPlaceholder("Ej. Club de Judo Sakura");
        nombreDojoField.setWidthFull();

        ComboBox<GradoCinturon> comboGrado = new ComboBox<>("Tu Grado Actual");
        comboGrado.setItems(GradoCinturon.values());
        comboGrado.setItemLabelGenerator(Enum::name);
        comboGrado.setRequired(true);
        comboGrado.setWidthFull();

        IntegerField anosPractica = new IntegerField("Años de Práctica");
        anosPractica.setMin(0);
        anosPractica.setWidthFull();

        com.vaadin.flow.component.textfield.PasswordField passwordField = new com.vaadin.flow.component.textfield.PasswordField("Crea tu Contraseña");
        passwordField.setRequired(true);
        passwordField.setHelperText("Con tu email y esta clave ingresarás a partir de ahora.");
        passwordField.setWidthFull();

        Button btnGuardar = new Button("Crear mi Dojo");
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();

        btnGuardar.addClickListener(e -> {
            if (comboGrado.isEmpty() || nombreDojoField.isEmpty() || passwordField.isEmpty()) {
                NotificationHelper.error("Todos los campos son obligatorios.");
                return;
            }
            guardarPerfil(comboGrado.getValue(), anosPractica.getValue() != null ? anosPractica.getValue() : 0,
                    nombreDojoField.getValue().trim(), passwordField.getValue());
        });

        card.add(titulo, subtitulo, nombreDojoField, comboGrado, anosPractica, passwordField, btnGuardar);
        add(card);
    }

    private void guardarPerfil(GradoCinturon grado, int anos, String nombreDojo, String rawPassword) {
        try {
            // 1. Obtener usuario autenticado
            String username = securityService.getAuthenticatedUserDetails().get().getUsername();
            Usuario usuarioBase = usuarioRepository.findByUsername(username).orElseThrow();

            // 2. Cambiar contraseña
            usuarioBase.setPasswordHash(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuarioBase);

            // 3. Buscar el token para obtener esClubPropio
            TokenInvitacion tokenInvitacion = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token no encontrado"));
            Boolean esClubPropio = tokenInvitacion.getEsClubPropio(); // puede ser null, pero para sensei debería estar

            // 4. Crear sensei con el valor
            Sensei nuevoSensei = new Sensei();
            nuevoSensei.setUsuario(usuarioBase);
            nuevoSensei.setGrado(grado);
            nuevoSensei.setAnosPractica(anos);
            nuevoSensei.setNombreDojo(nombreDojo);
            nuevoSensei.setEsClubPropio(esClubPropio != null ? esClubPropio : false); // <-- ASIGNAR

            Sensei guardado = senseiRepository.save(nuevoSensei);

            // 5. Refrescar sesión
            VaadinSession.getCurrent().setAttribute("CURRENT_SENSEI_ID", guardado.getId());

            NotificationHelper.success("¡" + nombreDojo + " configurado con éxito!");

            getUI().ifPresent(ui -> ui.navigate(SenseiDashboardView.class));

        } catch (Exception ex) {
            NotificationHelper.error("Hubo un error al guardar tu perfil.");
            ex.printStackTrace();
        }
    }

}