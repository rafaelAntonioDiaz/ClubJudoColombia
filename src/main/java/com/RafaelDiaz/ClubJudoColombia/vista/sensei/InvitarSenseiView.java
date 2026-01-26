package com.RafaelDiaz.ClubJudoColombia.vista.sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Rol;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.RolRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.UsuarioService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

@Route(value = "invitar-sensei", layout = SenseiLayout.class)
// ¡MAGIA PURA! Solo el dueño de la plataforma (Tú) puede entrar aquí
@RolesAllowed("ROLE_MASTER")
@PageTitle("Invitar Sensei (Cliente) | Club Judo Colombia")
public class InvitarSenseiView extends VerticalLayout {

    @Autowired
    public InvitarSenseiView(UsuarioService usuarioService,
                             SenseiRepository senseiRepository,
                             RolRepository rolRepository,
                             TraduccionService traduccionService) {

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("500px");
        card.setPadding(true);

        H2 titulo = new H2("Invitar Nuevo Sensei (Cliente)");
        Span subtitulo = new Span("Crea un nuevo Dojo en la plataforma. Se enviará una credencial temporal.");
        subtitulo.getStyle().set("color", "var(--lumo-secondary-text-color)");

        TextField nombre = new TextField("Nombre");
        TextField apellido = new TextField("Apellido");
        EmailField email = new EmailField("Correo Electrónico");
        TextField username = new TextField("Usuario (Ej: miyamoto)");

        ComboBox<GradoCinturon> grado = new ComboBox<>("Grado Actual");
        grado.setItems(GradoCinturon.values());
        grado.setValue(GradoCinturon.NEGRO_1_DAN);

        nombre.setWidthFull(); apellido.setWidthFull(); email.setWidthFull(); username.setWidthFull(); grado.setWidthFull();

        Button btnInvitar = new Button("Crear Cuenta Sensei", new Icon(VaadinIcon.PAPERPLANE));
        btnInvitar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnInvitar.setWidthFull();

        btnInvitar.addClickListener(e -> {
            if (nombre.isEmpty() || email.isEmpty() || username.isEmpty()) {
                NotificationHelper.error("Llene todos los campos");
                return;
            }

            try {
                // 1. Crear el Usuario base
                Usuario u = new Usuario(username.getValue(), "HASH_PENDIENTE", nombre.getValue(), apellido.getValue());
                u.setEmail(email.getValue());
                u.setActivo(true);

                // 2. Darle los permisos de Sensei (Cliente)
                Rol rolSensei = rolRepository.findByNombre("ROLE_SENSEI").orElseThrow();
                Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseThrow();
                u.setRoles(new HashSet<>());
                u.getRoles().add(rolSensei);
                u.getRoles().add(rolAdmin);

                // Guardar con contraseña temporal
                String tempPass = "Judo2026*";
                u = usuarioService.saveUsuario(u, tempPass);

                // 3. Crear su Perfil de Tatami (Aquí nace el ID del nuevo Dojo SaaS)
                Sensei s = new Sensei();
                s.setUsuario(u);
                s.setGrado(grado.getValue());
                s.setAnosPractica(10);
                senseiRepository.save(s);

                NotificationHelper.success("Sensei creado. Usuario: " + username.getValue() + " / Clave: " + tempPass);

                // Limpiar formulario
                nombre.clear(); apellido.clear(); email.clear(); username.clear();
            } catch (Exception ex) {
                NotificationHelper.error("Error: " + ex.getMessage());
            }
        });

        card.add(titulo, subtitulo, nombre, apellido, email, username, grado, btnInvitar);
        add(card);
    }
}
