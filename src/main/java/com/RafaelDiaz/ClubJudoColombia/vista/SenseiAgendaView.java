package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.servicio.GrupoEntrenamientoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route(value = "agenda", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Agenda & GPS | Club Judo Colombia")
public class SenseiAgendaView extends VerticalLayout {

    private final SesionService sesionService;
    private final GrupoEntrenamientoService grupoService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService; // <--- I18n

    private Grid<SesionProgramada> gridSesiones;

    @Autowired
    public SenseiAgendaView(SesionService sesionService,
                            GrupoEntrenamientoService grupoService,
                            SecurityService securityService,
                            TraduccionService traduccionService) {
        this.sesionService = sesionService;
        this.grupoService = grupoService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 titulo = new H2(traduccionService.get("agenda.titulo"));
        Button btnNueva = new Button(traduccionService.get("agenda.btn.nueva"), new Icon(VaadinIcon.PLUS));
        btnNueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNueva.addClickListener(e -> abrirDialogoSesion(new SesionProgramada()));

        header.add(titulo, btnNueva);

        gridSesiones = new Grid<>(SesionProgramada.class, false);
        gridSesiones.setSizeFull();
        configureGrid();
        actualizarGrid();

        add(header, gridSesiones);
    }

    private void configureGrid() {
        gridSesiones.addColumn(SesionProgramada::getNombre)
                .setHeader(traduccionService.get("agenda.grid.sesion")).setAutoWidth(true);

        gridSesiones.addColumn(s -> s.getGrupo() != null ? s.getGrupo().getNombre() : "-")
                .setHeader(traduccionService.get("generic.grupo")).setAutoWidth(true);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        gridSesiones.addColumn(s -> s.getFechaHoraInicio().format(fmt) + " - " + s.getFechaHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")))
                .setHeader(traduccionService.get("generic.horario")).setAutoWidth(true);

        gridSesiones.addComponentColumn(s -> {
            if (s.getLatitud() != null && s.getLongitud() != null) {
                Icon gpsIcon = VaadinIcon.MAP_MARKER.create();
                gpsIcon.setColor("green");
                gpsIcon.setTooltipText(traduccionService.get("agenda.tooltip.gps_activo") + ": " + s.getLatitud() + ", " + s.getLongitud());
                return gpsIcon;
            } else {
                Icon gpsIcon = VaadinIcon.MAP_MARKER.create();
                gpsIcon.setColor("gray");
                gpsIcon.setTooltipText(traduccionService.get("agenda.tooltip.sin_gps"));
                return gpsIcon;
            }
        }).setHeader("GPS");

        gridSesiones.addComponentColumn(s -> {
            Button edit = new Button(new Icon(VaadinIcon.EDIT));
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            edit.addClickListener(e -> abrirDialogoSesion(s));
            return edit;
        });
    }

    private void actualizarGrid() {
        gridSesiones.setItems(sesionService.findAll());
    }

    private void abrirDialogoSesion(SesionProgramada sesion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(sesion.getId() == null ? traduccionService.get("agenda.dialog.programar") : traduccionService.get("agenda.dialog.editar"));

        VerticalLayout form = new VerticalLayout();

        TextField nombre = new TextField(traduccionService.get("agenda.field.nombre"));
        nombre.setValue(sesion.getNombre() != null ? sesion.getNombre() : traduccionService.get("agenda.default.entrenamiento"));
        nombre.setWidthFull();

        ComboBox<GrupoEntrenamiento> grupoSelect = new ComboBox<>(traduccionService.get("generic.grupo"));
        grupoSelect.setItems(grupoService.findAll(0, 100, ""));
        grupoSelect.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        if (sesion.getGrupo() != null) grupoSelect.setValue(sesion.getGrupo());
        grupoSelect.setWidthFull();

        Select<TipoSesion> tipoSelect = new Select<>();
        tipoSelect.setLabel(traduccionService.get("generic.tipo"));
        tipoSelect.setItems(TipoSesion.values());
        if (sesion.getTipoSesion() != null) tipoSelect.setValue(sesion.getTipoSesion());
        else tipoSelect.setValue(TipoSesion.TECNICA);
        tipoSelect.setWidthFull();

        DateTimePicker inicio = new DateTimePicker(traduccionService.get("generic.inicio"));
        if (sesion.getFechaHoraInicio() != null) inicio.setValue(sesion.getFechaHoraInicio());
        inicio.setWidthFull();

        DateTimePicker fin = new DateTimePicker(traduccionService.get("generic.fin"));
        if (sesion.getFechaHoraFin() != null) fin.setValue(sesion.getFechaHoraFin());
        fin.setWidthFull();

        H2 tituloGps = new H2(traduccionService.get("agenda.section.gps"));
        tituloGps.getStyle().set("font-size", "1rem").set("margin-top", "1em");

        NumberField lat = new NumberField(traduccionService.get("agenda.field.latitud"));
        if (sesion.getLatitud() != null) lat.setValue(sesion.getLatitud());
        lat.setPlaceholder("Ej: 4.7110");

        NumberField lon = new NumberField(traduccionService.get("agenda.field.longitud"));
        if (sesion.getLongitud() != null) lon.setValue(sesion.getLongitud());
        lon.setPlaceholder("Ej: -74.0721");

        HorizontalLayout gpsRow = new HorizontalLayout(lat, lon);
        gpsRow.setWidthFull();

        IntegerField radio = new IntegerField(traduccionService.get("agenda.field.radio"));
        radio.setValue(sesion.getRadioPermitidoMetros() != null ? sesion.getRadioPermitidoMetros() : 100);
        radio.setStepButtonsVisible(true);
        radio.setMin(10);
        radio.setMax(1000);

        Button btnGuardar = new Button(traduccionService.get("btn.guardar"), e -> {
            if (grupoSelect.getValue() == null || inicio.getValue() == null || fin.getValue() == null) {
                Notification.show(traduccionService.get("error.campos_obligatorios")).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            sesion.setNombre(nombre.getValue());
            sesion.setGrupo(grupoSelect.getValue());
            sesion.setTipoSesion(tipoSelect.getValue());
            sesion.setFechaHoraInicio(inicio.getValue());
            sesion.setFechaHoraFin(fin.getValue());
            sesion.setLatitud(lat.getValue());
            sesion.setLongitud(lon.getValue());
            sesion.setRadioPermitidoMetros(radio.getValue());

            securityService.getAuthenticatedSensei().ifPresent(sesion::setSensei);

            sesionService.guardar(sesion);

            actualizarGrid();
            dialog.close();
            Notification.show(traduccionService.get("msg.success.saved")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();

        form.add(nombre, grupoSelect, tipoSelect, inicio, fin, tituloGps, gpsRow, radio, btnGuardar);
        dialog.add(form);
        dialog.open();
    }
}