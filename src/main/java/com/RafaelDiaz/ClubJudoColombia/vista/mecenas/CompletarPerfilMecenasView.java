package com.RafaelDiaz.ClubJudoColombia.vista.mecenas;

import com.RafaelDiaz.ClubJudoColombia.modelo.Mecenas;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoMecenas;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MecenasRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route("completar-perfil-mecenas")
@RolesAllowed("ROLE_MECENAS")
@PageTitle("Completar Perfil Mecenas | Club Judo Colombia")
public class CompletarPerfilMecenasView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final MecenasRepository mecenasRepository;
    private final UsuarioRepository usuarioRepository;
    private final TraduccionService traduccionService;

    private Mecenas mecenas;

    // Componentes
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout form = new FormLayout();

    private final ComboBox<TipoMecenas> tipoMecenas = new ComboBox<>();
    private final TextField nombreEmpresa = new TextField();
    private final TextField nitEmpresa = new TextField();
    private final TextArea descripcionPatrocinio = new TextArea();

    private final Button btnGuardar = new Button();

    public CompletarPerfilMecenasView(SecurityService securityService,
                                      MecenasRepository mecenasRepository,
                                      UsuarioRepository usuarioRepository,
                                      TraduccionService traduccionService) {
        this.securityService = securityService;
        this.mecenasRepository = mecenasRepository;
        this.usuarioRepository = usuarioRepository;
        this.traduccionService = traduccionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configurar componentes
        tipoMecenas.setLabel(traduccionService.get("mecenas.tipo"));
        tipoMecenas.setItems(TipoMecenas.values());
        tipoMecenas.setItemLabelGenerator(t -> traduccionService.get("mecenas.tipo." + t.name().toLowerCase()));
        tipoMecenas.addValueChangeListener(e -> ajustarVisibilidad());

        nombreEmpresa.setLabel(traduccionService.get("mecenas.nombre_empresa"));
        nitEmpresa.setLabel(traduccionService.get("mecenas.nit"));
        descripcionPatrocinio.setLabel(traduccionService.get("mecenas.descripcion"));
        descripcionPatrocinio.setHeight("100px");

        btnGuardar.setText(traduccionService.get("boton.guardar"));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.addClickListener(e -> guardar());

        form.add(tipoMecenas, nombreEmpresa, nitEmpresa, descripcionPatrocinio);
        form.setMaxWidth("600px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("700px");
        card.setPadding(true);
        card.add(titulo, descripcion, form, btnGuardar);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        // No se usa id, se obtiene el mecenas del usuario autenticado
        Usuario usuarioActual = securityService.getAuthenticatedUsuario()
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));
        Optional<Mecenas> opt = mecenasRepository.findByUsuario(usuarioActual);
        if (opt.isEmpty()) {
            // Si no existe, creamos uno nuevo (pero debería haberse creado en la invitación)
            mecenas = new Mecenas();
            mecenas.setUsuario(usuarioActual);
            mecenas.setTipo(TipoMecenas.PERSONA_NATURAL);
            mecenas = mecenasRepository.save(mecenas);
        } else {
            mecenas = opt.get();
        }

        titulo.setText(traduccionService.get("perfil.completar.titulo", usuarioActual.getNombre()));
        descripcion.setText(traduccionService.get("perfil.mecenas.descripcion"));

        // Cargar datos existentes
        tipoMecenas.setValue(mecenas.getTipo());
        nombreEmpresa.setValue(mecenas.getNombreEmpresa());
        nitEmpresa.setValue(mecenas.getNitEmpresa());
        descripcionPatrocinio.setValue(mecenas.getDescripcionPatrocinio());

        ajustarVisibilidad();
    }

    private void ajustarVisibilidad() {
        boolean esEmpresa = tipoMecenas.getValue() == TipoMecenas.EMPRESA;
        nombreEmpresa.setVisible(esEmpresa);
        nitEmpresa.setVisible(esEmpresa);
    }

    private void guardar() {
        if (tipoMecenas.getValue() == null) {
            Notification.show(traduccionService.get("error.seleccione_tipo")).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (tipoMecenas.getValue() == TipoMecenas.EMPRESA && nombreEmpresa.isEmpty()) {
            Notification.show(traduccionService.get("error.nombre_empresa_requerido")).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            mecenas.setTipo(tipoMecenas.getValue());
            mecenas.setNombreEmpresa(nombreEmpresa.getValue());
            mecenas.setNitEmpresa(nitEmpresa.getValue());
            mecenas.setDescripcionPatrocinio(descripcionPatrocinio.getValue());
            mecenasRepository.save(mecenas);

            Notification.show(traduccionService.get("perfil.guardado_exito"), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("dashboard-mecenas"));

        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}