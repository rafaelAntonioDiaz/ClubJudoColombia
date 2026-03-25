package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiGruposView;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
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

import java.math.BigDecimal;
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
    private final TraduccionService traduccionService;

    private TokenInvitacion token;
    private String tokenUuid;

    public CompletarPerfilSenseiView(SecurityService securityService,
                                     UsuarioRepository usuarioRepository, SenseiRepository senseiRepository, TokenInvitacionRepository tokenRepository, PasswordEncoder passwordEncoder, TraduccionService traduccionService) {
        this.securityService = securityService;
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        construirUI();
    }

    @Override
    public void setParameter(BeforeEvent event, String tokenUuid) {
        this.tokenUuid = tokenUuid;
        token = tokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        construirUI();
    }

    private void construirUI() {
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

            Sensei sensei = securityService.getAuthenticatedSensei()
                    .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));
            sensei.setComisionPorcentaje(token.getPorcentajeComision());
            senseiRepository.save(sensei);

            Notification.show("Perfil completado. Ahora puedes crear tus grupos.");
            UI.getCurrent().navigate(SenseiGruposView.class);

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
            // 1. Obtener usuario autenticado (el usuario existe, pero sensei no)
            String username = securityService.getAuthenticatedUserDetails().get().getUsername();
            Usuario usuarioBase = usuarioRepository.findByUsername(username).orElseThrow();

            // 2. Cambiar contraseña
            usuarioBase.setPasswordHash(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuarioBase);

            // 3. Buscar el token para obtener esClubPropio y porcentaje de comisión
            TokenInvitacion tokenInvitacion = tokenRepository.findByToken(tokenUuid)
                    .orElseThrow(() -> new RuntimeException("Token no encontrado"));
            Boolean esClubPropio = tokenInvitacion.getEsClubPropio();
            BigDecimal comisionPorcentaje = tokenInvitacion.getPorcentajeComision();

            // 4. Crear nuevo sensei (no existe aún)
            Sensei nuevoSensei = new Sensei();
            nuevoSensei.setUsuario(usuarioBase);
            nuevoSensei.setGrado(grado);
            nuevoSensei.setAnosPractica(anos);
            nuevoSensei.setNombreClub(nombreDojo);   // <-- nombreClub
            nuevoSensei.setEsClubPropio(esClubPropio != null ? esClubPropio : false);
            nuevoSensei.setComisionPorcentaje(comisionPorcentaje != null ? comisionPorcentaje : BigDecimal.ZERO);

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