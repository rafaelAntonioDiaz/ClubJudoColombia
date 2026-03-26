package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TokenInvitacionRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@PageTitle("Completar Perfil de Profesor")
@Route("completar-perfil-sensei")
@RolesAllowed({"ROLE_SENSEI", "ROLE_MASTER"})
public class CompletarPerfilSenseiView extends VerticalLayout implements HasUrlParameter<String> {

    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    private final TokenInvitacionRepository tokenRepository;
    private final TraduccionService traduccionService;

    private TokenInvitacion token;

    private TextField nombreDojoField;
    private ComboBox<GradoCinturon> comboGrado;
    private IntegerField anosPractica;

    public CompletarPerfilSenseiView(UsuarioRepository usuarioRepository,
                                     SenseiRepository senseiRepository,
                                     TokenInvitacionRepository tokenRepository,
                                     TraduccionService traduccionService) {
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.tokenRepository = tokenRepository;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, String tokenUuid) {
        token = tokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        construirUI();
    }

    private void construirUI() {
        removeAll();

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("500px");
        card.setPadding(true);

        H2 titulo = new H2("¡Bienvenido a tu nuevo SaaS!");
        Paragraph subtitulo = new Paragraph("Configura tu perfil de Profesor y dale un nombre a tu espacio de trabajo.");

        nombreDojoField = new TextField("Nombre de tu Club / Dojo");
        nombreDojoField.setRequired(true);
        nombreDojoField.setPlaceholder("Ej. Club de Judo Sakura");
        nombreDojoField.setWidthFull();

        comboGrado = new ComboBox<>("Tu Grado Actual");
        comboGrado.setItems(GradoCinturon.values());
        comboGrado.setItemLabelGenerator(Enum::name);
        comboGrado.setRequired(true);
        comboGrado.setWidthFull();

        anosPractica = new IntegerField("Años de Práctica");
        anosPractica.setMin(0);
        anosPractica.setWidthFull();

        Button btnGuardar = new Button("Crear mi Dojo");
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();
        btnGuardar.addClickListener(e -> guardarPerfil());

        card.add(titulo, subtitulo, nombreDojoField, comboGrado, anosPractica, btnGuardar);
        add(card);
    }

    @Transactional
    private void guardarPerfil() {
        if (nombreDojoField.isEmpty() || comboGrado.isEmpty()) {
            NotificationHelper.error("Todos los campos son obligatorios.");
            return;
        }

        try {
            // Obtener el usuario del token (ya activo)
            Usuario usuario = token.getUsuarioInvitado();
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado en el token");
            }

            // Verificar que el sensei no exista ya (evitar duplicados)
            if (senseiRepository.findByUsuario(usuario).isPresent()) {
                NotificationHelper.error("Ya existe un perfil de sensei para este usuario.");
                return;
            }

            // Crear el sensei
            Sensei nuevoSensei = new Sensei();
            nuevoSensei.setUsuario(usuario);
            nuevoSensei.setGrado(comboGrado.getValue());
            nuevoSensei.setAnosPractica(anosPractica.getValue() != null ? anosPractica.getValue() : 0);
            nuevoSensei.setNombreClub(nombreDojoField.getValue().trim());
            nuevoSensei.setEsClubPropio(token.getEsClubPropio() != null ? token.getEsClubPropio() : false);
            nuevoSensei.setComisionPorcentaje(token.getPorcentajeComision() != null ? token.getPorcentajeComision() : BigDecimal.ZERO);

            Sensei guardado = senseiRepository.saveAndFlush(nuevoSensei);
            System.out.println("DEBUG: Sensei guardado con ID: " + guardado.getId());

            // Guardar ID en sesión de Vaadin
            VaadinSession.getCurrent().setAttribute("CURRENT_SENSEI_ID", guardado.getId());
            System.out.println("DEBUG: ID guardado en sesión: " + VaadinSession.getCurrent().getAttribute("CURRENT_SENSEI_ID"));

            // Marcar token como usado (opcional)
            token.setUsado(true);
            tokenRepository.save(token);

            NotificationHelper.success("¡" + nombreDojoField.getValue() + " configurado con éxito!");
            UI.getCurrent().navigate(SenseiDashboardView.class);

        } catch (Exception ex) {
            NotificationHelper.error("Hubo un error al guardar tu perfil: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}