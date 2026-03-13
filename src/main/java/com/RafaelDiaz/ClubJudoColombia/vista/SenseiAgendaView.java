package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.dto.ItemCalendario;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoItem;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.JudokaCalendar;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
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

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Route(value = "agenda", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Agenda & GPS | Club Judo Colombia")
public class SenseiAgendaView extends VerticalLayout {

    private final SesionService sesionService;
    private final GrupoEntrenamientoService grupoService;
    private final JudokaService judokaService;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    public final CalendarioUnificadoService calendarioService;

    private ComboBox<GrupoEntrenamiento> grupoSelector;
    private ComboBox<Judoka> judokaSelector;
    private Checkbox mostrarTareasIndividuales;
    private JudokaCalendar calendario;
    private YearMonth currentMonth = YearMonth.now();

    private Sensei senseiActual;

    @Autowired
    public SenseiAgendaView(SesionService sesionService,
                            GrupoEntrenamientoService grupoService,
                            JudokaService judokaService,
                            SecurityService securityService,
                            TraduccionService traduccionService,
                            CalendarioUnificadoService calendarioService) {
        this.sesionService = sesionService;
        this.grupoService = grupoService;
        this.judokaService = judokaService;
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.calendarioService = calendarioService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        buildHeader();
        buildFiltros();
        buildCalendario();
        actualizarVista();
    }

    private void buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 titulo = new H2(traduccionService.get("agenda.titulo"));
        Button btnNuevaSesion = new Button(traduccionService.get("agenda.btn.nueva"), new Icon(VaadinIcon.PLUS));
        btnNuevaSesion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevaSesion.addClickListener(e -> abrirDialogoSesion(new SesionProgramada()));

        header.add(titulo, btnNuevaSesion);
        add(header);
    }

    private void buildFiltros() {
        HorizontalLayout filtros = new HorizontalLayout();
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.END);
        filtros.setSpacing(true);

        grupoSelector = new ComboBox<>(traduccionService.get("generic.grupo"));
        grupoSelector.setItems(grupoService.findBySensei(senseiActual));
        grupoSelector.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        grupoSelector.setClearButtonVisible(true);
        grupoSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                // Cargar el grupo completo con judokas dentro de una transacción
                GrupoEntrenamiento grupoCompleto = grupoService.obtenerGrupoConJudokas(e.getValue().getId());
                judokaSelector.setItems(grupoCompleto.getJudokas());
            } else {
                judokaSelector.setItems(List.of());
            }
            judokaSelector.clear();
            actualizarVista();
        });

        judokaSelector = new ComboBox<>(traduccionService.get("generic.judoka"));
        judokaSelector.setItemLabelGenerator(j -> j.getNombre() + " " + j.getApellido());
        judokaSelector.setClearButtonVisible(true);
        judokaSelector.addValueChangeListener(e -> actualizarVista());

        mostrarTareasIndividuales = new Checkbox(traduccionService.get("agenda.mostrar.tareas"));
        mostrarTareasIndividuales.setValue(true);
        mostrarTareasIndividuales.addValueChangeListener(e -> actualizarVista());

        filtros.add(grupoSelector, judokaSelector, mostrarTareasIndividuales);
        add(filtros);
    }

    private void buildCalendario() {
        calendario = new JudokaCalendar(calendarioService, traduccionService);
        calendario.setWidthFull();

        HorizontalLayout nav = new HorizontalLayout();
        Button prev = new Button(new Icon(VaadinIcon.CHEVRON_LEFT), e -> {
            currentMonth = currentMonth.minusMonths(1);
            actualizarVista();
        });
        Button next = new Button(new Icon(VaadinIcon.CHEVRON_RIGHT), e -> {
            currentMonth = currentMonth.plusMonths(1);
            actualizarVista();
        });
        nav.add(prev, next);
        add(nav, calendario);
    }

    private void actualizarVista() {
        List<ItemCalendario> items = new ArrayList<>();

        if (judokaSelector.getValue() != null) {
            // Usar el ID del judoka
            items = calendarioService.obtenerItemsPorJudokaYMes(judokaSelector.getValue().getId(), currentMonth);
        } else if (grupoSelector.getValue() != null) {
            GrupoEntrenamiento grupo = grupoSelector.getValue();
            if (mostrarTareasIndividuales.getValue()) {
                // Cargar grupo completo con judokas para evitar LazyInitializationException
                GrupoEntrenamiento grupoCompleto = grupoService.obtenerGrupoConJudokas(grupo.getId());
                items.addAll(calendarioService.obtenerItemsPorGrupoYMes(grupoCompleto, currentMonth)); // sesiones
                for (Judoka j : grupoCompleto.getJudokas()) {
                    items.addAll(calendarioService.obtenerItemsPorJudokaYMes(j.getId(), currentMonth).stream()
                            .filter(item -> item.getTipo() == TipoItem.TAREA_INDIVIDUAL)
                            .toList());
                }
            } else {
                items = calendarioService.obtenerItemsPorGrupoYMes(grupo, currentMonth);
            }
        } else {
            items = calendarioService.obtenerItemsPorSenseiYMes(senseiActual, currentMonth);
        }

        calendario.mostrarMes(currentMonth, items);
    }
    private void abrirDialogoSesion(SesionProgramada sesion) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(sesion.getId() == null ?
                traduccionService.get("agenda.dialog.programar") :
                traduccionService.get("agenda.dialog.editar"));

        VerticalLayout form = new VerticalLayout();

        TextField nombre = new TextField(traduccionService.get("agenda.field.nombre"));
        nombre.setValue(sesion.getNombre() != null ? sesion.getNombre() : traduccionService.get("agenda.default.entrenamiento"));
        nombre.setWidthFull();

        ComboBox<GrupoEntrenamiento> grupoSelect = new ComboBox<>(traduccionService.get("generic.grupo"));
        grupoSelect.setItems(grupoService.findBySensei(senseiActual));
        grupoSelect.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        if (sesion.getGrupo() != null) grupoSelect.setValue(sesion.getGrupo());
        grupoSelect.setWidthFull();

        Select<TipoSesion> tipoSelect = new Select<>();
        tipoSelect.setLabel(traduccionService.get("generic.tipo"));
        tipoSelect.setItems(TipoSesion.values());
        tipoSelect.setValue(sesion.getTipoSesion() != null ? sesion.getTipoSesion() : TipoSesion.TECNICA);
        tipoSelect.setWidthFull();

        DateTimePicker inicio = new DateTimePicker(traduccionService.get("generic.inicio"));
        if (sesion.getFechaHoraInicio() != null) inicio.setValue(sesion.getFechaHoraInicio());
        inicio.setWidthFull();

        DateTimePicker fin = new DateTimePicker(traduccionService.get("generic.fin"));
        if (sesion.getFechaHoraFin() != null) fin.setValue(sesion.getFechaHoraFin());
        fin.setWidthFull();

        // --- NUEVO: Checkbox para usar ubicación del grupo ---
        Checkbox usarUbicacionGrupo = new Checkbox("Usar ubicación del grupo");
        usarUbicacionGrupo.setValue(true);
        usarUbicacionGrupo.setVisible(false); // Se muestra solo si el grupo seleccionado tiene ubicación

        NumberField lat = new NumberField(traduccionService.get("agenda.field.latitud"));
        lat.setStep(0.000001);
        lat.setPlaceholder("Ej: 4.7110");
        lat.setMin(-90);
        lat.setMax(90);

        NumberField lon = new NumberField(traduccionService.get("agenda.field.longitud"));
        lon.setStep(0.000001);
        lon.setPlaceholder("Ej: -74.0721");
        lon.setMin(-180);
        lon.setMax(180);

        IntegerField radio = new IntegerField(traduccionService.get("agenda.field.radio"));
        radio.setStepButtonsVisible(true);
        radio.setMin(10);
        radio.setMax(1000);

        // Si la sesión ya tiene valores, precargarlos y desactivar el checkbox
        if (sesion.getLatitud() != null) {
            lat.setValue(sesion.getLatitud());
            lon.setValue(sesion.getLongitud());
            radio.setValue(sesion.getRadioPermitidoMetros() != null ? sesion.getRadioPermitidoMetros() : 100);
            usarUbicacionGrupo.setVisible(false);
            usarUbicacionGrupo.setValue(false);
            lat.setEnabled(true);
            lon.setEnabled(true);
            radio.setEnabled(true);
        }

        // Cuando cambia el grupo, actualizar campos GPS si el grupo tiene ubicación
        grupoSelect.addValueChangeListener(e -> {
            GrupoEntrenamiento grupo = e.getValue();
            if (grupo != null && grupo.getLatitud() != null && grupo.getLongitud() != null) {
                usarUbicacionGrupo.setVisible(true);
                if (usarUbicacionGrupo.getValue()) {
                    lat.setValue(grupo.getLatitud());
                    lon.setValue(grupo.getLongitud());
                    radio.setValue(grupo.getRadioPermitidoMetros() != null ?
                            grupo.getRadioPermitidoMetros() : 100);
                    lat.setEnabled(false);
                    lon.setEnabled(false);
                    radio.setEnabled(false);
                }
            } else {
                usarUbicacionGrupo.setVisible(false);
            }
        });

        usarUbicacionGrupo.addValueChangeListener(e -> {
            GrupoEntrenamiento grupo = grupoSelect.getValue();
            if (grupo != null && grupo.getLatitud() != null && grupo.getLongitud() != null && e.getValue()) {
                lat.setValue(grupo.getLatitud());
                lon.setValue(grupo.getLongitud());
                radio.setValue(grupo.getRadioPermitidoMetros() != null ?
                        grupo.getRadioPermitidoMetros() : 100);
                lat.setEnabled(false);
                lon.setEnabled(false);
                radio.setEnabled(false);
            } else {
                // Habilitar edición manual
                lat.setEnabled(true);
                lon.setEnabled(true);
                radio.setEnabled(true);
            }
        });

        H2 tituloGps = new H2(traduccionService.get("agenda.section.gps"));
        tituloGps.getStyle().set("font-size", "1rem").set("margin-top", "1em");

        HorizontalLayout gpsRow = new HorizontalLayout(lat, lon);
        gpsRow.setWidthFull();

        Button btnGuardar = new Button(traduccionService.get("btn.guardar"), e -> {
            if (grupoSelect.getValue() == null || inicio.getValue() == null || fin.getValue() == null) {
                Notification.show(traduccionService.get("error.campos_obligatorios"))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
            sesion.setSensei(senseiActual);

            sesionService.guardar(sesion);
            actualizarVista();
            dialog.close();
            Notification.show(traduccionService.get("msg.success.saved"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();

        form.add(nombre, grupoSelect, tipoSelect, inicio, fin, usarUbicacionGrupo, tituloGps, gpsRow, radio, btnGuardar);
        dialog.add(form);
        dialog.open();
    }
}