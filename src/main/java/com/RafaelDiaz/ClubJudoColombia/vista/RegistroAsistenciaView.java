package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.AsistenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // NUEVO: Importación
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Route(value = "asistencia", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Control de Clase | Club Judo Colombia")
public class RegistroAsistenciaView extends SenseiLayout {

    private final GrupoEntrenamientoService grupoService;
    private final TraduccionService traduccionService; // NUEVO: Servicio de traducción

    private ComboBox<GrupoEntrenamiento> grupoSelector;
    private final FlexLayout contenedorTarjetas;
    private final Set<Judoka> presentes = new HashSet<>();
    private final Button btnGuardarGlobal;

    @Autowired
    public RegistroAsistenciaView(GrupoEntrenamientoService grupoService,
                                  AsistenciaService asistenciaService,
                                  SecurityService securityService,
                                  AccessAnnotationChecker accessChecker,
                                  ConfiguracionService configuracionService,
                                  AuthenticationContext authenticationContext,
                                  TraduccionService traduccionService) { // NUEVO: Parámetro añadido
        super(securityService, accessChecker, configuracionService, authenticationContext);
        this.grupoService = grupoService;
        this.traduccionService = traduccionService; // NUEVO: Inicialización

        addClassName("asistencia-view");

        // Contenedor principal
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // 1. Cabecera (Selector de Grupo y Fecha)
        mainLayout.add(crearCabecera());

        // 2. Grilla de Tarjetas (El Tatami Virtual)
        contenedorTarjetas = new FlexLayout();
        contenedorTarjetas.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contenedorTarjetas.getStyle().set("gap", "20px");
        contenedorTarjetas.setSizeFull();
        contenedorTarjetas.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Scroll para las tarjetas
        Div scrollContainer = new Div(contenedorTarjetas);
        scrollContainer.setSizeFull();
        scrollContainer.getStyle().set("overflow-y", "auto");

        mainLayout.add(scrollContainer);

        // 3. Footer (Botón flotante o fijo)
        btnGuardarGlobal = new Button(traduccionService.get("asistencia.boton.cerrar_clase"),
                new Icon(VaadinIcon.CHECK_CIRCLE));
        btnGuardarGlobal.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnGuardarGlobal.setWidthFull();
        btnGuardarGlobal.setVisible(false);
        btnGuardarGlobal.addClickListener(e -> guardarAsistenciaMasiva());

        mainLayout.add(btnGuardarGlobal);

        setContent(mainLayout);
    }

    private Component crearCabecera() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.BASELINE);

        grupoSelector = new ComboBox<>(traduccionService.get("asistencia.selector.grupo"));
        grupoSelector.setItems(grupoService.findAll(0, 100, ""));
        grupoSelector.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        grupoSelector.setWidth("300px");
        grupoSelector.setPlaceholder(traduccionService.get("asistencia.placeholder.grupo"));

        grupoSelector.addValueChangeListener(e -> cargarAlumnos(e.getValue()));

        String fechaFormateada = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Span fechaHoy = new Span(traduccionService.get("asistencia.fecha") + ": " + fechaFormateada);
        fechaHoy.getStyle().set("font-weight", "bold").set("color", "gray");

        header.add(grupoSelector, fechaHoy);
        return header;
    }

    private void cargarAlumnos(GrupoEntrenamiento grupo) {
        contenedorTarjetas.removeAll();
        presentes.clear();

        if (grupo == null) {
            btnGuardarGlobal.setVisible(false);
            return;
        }

        List<Judoka> alumnos = grupoService.findJudokasEnGrupo(grupo.getId(), "", null, null);

        if (alumnos.isEmpty()) {
            contenedorTarjetas.add(new H3(traduccionService.get("asistencia.mensaje.sin_alumnos")));
            btnGuardarGlobal.setVisible(false);
            return;
        }

        for (Judoka alumno : alumnos) {
            contenedorTarjetas.add(crearTarjetaAlumno(alumno));
        }

        Notification.show(traduccionService.get("asistencia.notificacion.cargados") +
                        " " + alumnos.size() + " " +
                        traduccionService.get("asistencia.notificacion.alumnos"))
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        btnGuardarGlobal.setVisible(true);
    }

    private Component crearTarjetaAlumno(Judoka alumno) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("160px");
        card.setHeight("220px");
        card.addClassName("alumno-card");
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        card.setSpacing(false);
        card.setPadding(true);

        card.getStyle()
                .set("border", "2px solid #ddd")
                .set("border-radius", "12px")
                .set("background-color", "#f5f5f5")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s");

        // Avatar
        Avatar avatar = new Avatar(alumno.getUsuario().getNombre());
        avatar.setWidth("64px");
        avatar.setHeight("64px");

        // Nombre
        Span nombre = new Span(alumno.getUsuario().getNombre());
        nombre.getStyle().set("font-weight", "bold").set("font-size", "0.9rem").set("text-align", "center");

        Span apellido = new Span(alumno.getUsuario().getApellido());
        apellido.getStyle().set("font-size", "0.8rem").set("text-align", "center");

        // Estado Visual
        Span estado = new Span(traduccionService.get("asistencia.estado.ausente"));
        estado.getElement().getThemeList().add("badge");

        // --- BOTÓN DE PÁNICO (SOS) ---
        Button btnSOS = new Button(new Icon(VaadinIcon.AMBULANCE));
        btnSOS.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        btnSOS.setWidth("40px");
        btnSOS.setHeight("40px");
        btnSOS.getStyle().set("border-radius", "50%");

        // 1. Lógica Servidor: Abrir diálogo
        btnSOS.addClickListener(e -> mostrarDialogoSOS(alumno));

        // 2. Lógica Cliente: Detener propagación del clic para no activar la tarjeta
        btnSOS.getElement().executeJs("this.addEventListener('click', function(e) { e.stopPropagation(); });");

        // --- CLIC EN TARJETA (ASISTENCIA) ---
        card.addClickListener(e -> {
            boolean estaPresente = presentes.contains(alumno);
            if (estaPresente) {
                // Marcar Ausente
                presentes.remove(alumno);
                card.getStyle().set("background-color", "#f5f5f5").set("border-color", "#ddd");
                estado.setText(traduccionService.get("asistencia.estado.ausente"));
                estado.getElement().getThemeList().clear();
                estado.getElement().getThemeList().add("badge");
            } else {
                // Marcar Presente
                presentes.add(alumno);
                card.getStyle().set("background-color", "#e8f5e9").set("border-color", "#2e7d32");
                estado.setText(traduccionService.get("asistencia.estado.presente"));
                estado.getElement().getThemeList().clear();
                estado.getElement().getThemeList().add("badge success");
            }
        });

        card.add(btnSOS, avatar, nombre, apellido, estado);
        return card;
    }

    private void mostrarDialogoSOS(Judoka alumno) {
        Dialog d = new Dialog();
        d.setHeaderTitle(traduccionService.get("asistencia.dialog.sos.titulo"));

        VerticalLayout layout = new VerticalLayout();

        layout.add(new H4(alumno.getUsuario().getNombre() + " " + alumno.getUsuario().getApellido()));

        // Datos de emergencia con labels traducidos
        layout.add(crearFilaInfo(VaadinIcon.PHONE,
                traduccionService.get("asistencia.dialog.sos.acudiente_movil"),
                alumno.getTelefonoAcudiente()));
        layout.add(crearFilaInfo(VaadinIcon.ENVELOPE,
                traduccionService.get("asistencia.dialog.sos.email"),
                alumno.getUsuario().getEmail()));
        layout.add(crearFilaInfo(VaadinIcon.USER_HEART,
                traduccionService.get("asistencia.dialog.sos.eps"),
                alumno.getEps()));
        layout.add(crearFilaInfo(VaadinIcon.FAMILY,
                traduccionService.get("asistencia.dialog.sos.nombre_acudiente"),
                alumno.getNombreAcudiente()));

        Button btnLlamar = new Button(traduccionService.get("asistencia.dialog.sos.llamar_ahora"),
                new Icon(VaadinIcon.PHONE));
        btnLlamar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btnLlamar.setWidthFull();

        // Enlace tel: para móviles
        String telefonoLlamada = alumno.getTelefonoAcudiente() != null ? alumno.getTelefonoAcudiente() : alumno.getCelular();
        if (telefonoLlamada != null && !telefonoLlamada.isEmpty()) {
            Anchor link = new Anchor("tel:" + telefonoLlamada, btnLlamar);
            link.setWidthFull();
            layout.add(link);
        } else {
            btnLlamar.setEnabled(false);
            btnLlamar.setText(traduccionService.get("asistencia.dialog.sos.sin_telefono"));
            layout.add(btnLlamar);
        }

        Button cerrar = new Button(traduccionService.get("asistencia.boton.cerrar"), e -> d.close());

        d.add(layout);
        d.getFooter().add(cerrar);
        d.open();
    }

    private HorizontalLayout crearFilaInfo(VaadinIcon icon, String label, String valor) {
        HorizontalLayout h = new HorizontalLayout();
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon i = icon.create();
        i.setColor("gray");
        i.setSize("16px");

        Span lbl = new Span(label + ": ");
        lbl.getStyle().set("font-weight", "bold");

        Span val = new Span(valor != null ? valor : "---");

        h.add(i, lbl, val);
        return h;
    }

    private void guardarAsistenciaMasiva() {
        if (grupoSelector.getValue() == null) return;

        try {
            // TODO: Implementar guardado real en AsistenciaService
            Notification.show(traduccionService.get("asistencia.notificacion.registrada") +
                            " " + traduccionService.get("asistencia.notificacion.presentes") +
                            ": " + presentes.size())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Limpiar
            grupoSelector.clear();
            contenedorTarjetas.removeAll();

        } catch (Exception e) {
            Notification.show(traduccionService.get("asistencia.notificacion.error_guardar") +
                            e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}