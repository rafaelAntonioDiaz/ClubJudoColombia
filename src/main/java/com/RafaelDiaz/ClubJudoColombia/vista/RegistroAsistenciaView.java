package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Vista para registrar asistencia en tiempo real con geolocalización.
 *
 * @author RafaelDiaz
 * @version 1.4 (Corregida para Vaadin 24.8.4)
 * @since 2025-11-20
 */
@Route("registrar-asistencia")
@RolesAllowed("ROLE_SENSEI")
public class RegistroAsistenciaView extends SenseiLayout implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RegistroAsistenciaView.class);

    private final SesionProgramadaService sesionService;
    private final AsistenciaService asistenciaService;
    private final GrupoEntrenamientoService grupoService;

    private final ComboBox<GrupoEntrenamiento> grupoCombo = new ComboBox<>("Seleccionar Grupo");
    private final ComboBox<SesionProgramada> sesionCombo = new ComboBox<>("Sesión Activa");
    private final Grid<Judoka> gridJudokas = new Grid<>(Judoka.class, false);
    private final Button btnCheckIn = new Button("Check-in con GPS", new Icon(VaadinIcon.MAP_MARKER));

    private SesionProgramada sesionActual;

    public RegistroAsistenciaView(SesionProgramadaService sesionService,
                                  AsistenciaService asistenciaService,
                                  GrupoEntrenamientoService grupoService,
                                  SecurityService securityService,
                                  AccessAnnotationChecker accessChecker) {
        super(securityService, accessChecker);
        this.sesionService = sesionService;
        this.asistenciaService = asistenciaService;
        this.grupoService = grupoService;

        configureCombos();
        configureGrid();
        configureCheckInButton();
        buildLayout();

        logger.info("RegistroAsistenciaView inicializada correctamente");
    }

    private void configureCombos() {
        grupoCombo.setItems(grupoService.findAll(0, 100, ""));
        grupoCombo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        grupoCombo.setWidth("400px");
        grupoCombo.setPlaceholder("Seleccione un grupo...");
        grupoCombo.setClearButtonVisible(true);
        grupoCombo.addValueChangeListener(event -> {
            GrupoEntrenamiento grupo = event.getValue();
            if (grupo != null) {
                sesionCombo.setItems(sesionService.findActivas(grupo.getId(), 0, 50));
                gridJudokas.getDataProvider().refreshAll();
            } else {
                sesionCombo.setItems();
            }
        });

        sesionCombo.setItemLabelGenerator(s -> String.format("%s - %s",
                s.getTipoSesion().name(), s.getFechaHoraInicio().toString()));
        sesionCombo.setWidth("400px");
        sesionCombo.setPlaceholder("Seleccione sesión activa...");
        sesionCombo.setClearButtonVisible(true);
        sesionCombo.setEnabled(false);
        sesionCombo.addValueChangeListener(event -> {
            this.sesionActual = event.getValue();
            btnCheckIn.setEnabled(sesionActual != null);
            gridJudokas.getDataProvider().refreshAll();
        });
    }

    private void configureGrid() {
        gridJudokas.addColumn(j -> String.format("%s %s",
                        j.getUsuario().getNombre(), j.getUsuario().getApellido()))
                .setHeader("Nombre Completo")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        gridJudokas.addColumn(Judoka::getGrado)
                .setHeader("Grado")
                .setSortable(true)
                .setAutoWidth(true);

        gridJudokas.addColumn(Judoka::getSexo)
                .setHeader("Sexo")
                .setSortable(true)
                .setAutoWidth(true);

        gridJudokas.addColumn(j -> String.format("%d años", j.getEdad()))
                .setHeader("Edad")
                .setSortable(true)
                .setAutoWidth(true);

        gridJudokas.addComponentColumn(this::crearIndicadorAsistencia)
                .setHeader("Estado Asistencia")
                .setAutoWidth(true)
                .setFlexGrow(0);

        gridJudokas.setPageSize(20);
        gridJudokas.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridJudokas.asSingleSelect().addValueChangeListener(event ->
                btnCheckIn.setEnabled(event.getValue() != null && sesionActual != null)
        );

        gridJudokas.setDataProvider(DataProvider.fromFilteringCallbacks(
                this::fetchJudokasDelGrupo,
                this::countJudokasDelGrupo
        ));
    }

    private com.vaadin.flow.component.Component crearIndicadorAsistencia(Judoka judoka) {
        if (sesionActual == null) return new Icon(VaadinIcon.QUESTION_CIRCLE_O);

        boolean asistio = asistenciaService.estaRegistrada(sesionActual.getId(), judoka.getId());
        Icon icon = asistio ? new Icon(VaadinIcon.CHECK_CIRCLE) : new Icon(VaadinIcon.CIRCLE_THIN);
        icon.setColor(asistio ? "var(--lumo-success-color)" : "var(--lumo-contrast-60pct)");
        icon.setSize("20px");
        return icon;
    }

    private void configureCheckInButton() {
        btnCheckIn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnCheckIn.setIcon(new Icon(VaadinIcon.MAP_MARKER));
        btnCheckIn.setEnabled(false);
        btnCheckIn.setTooltipText("Registrar asistencia con ubicación GPS");
        btnCheckIn.addClickListener((ComponentEventListener<ClickEvent<Button>> & Serializable) event -> {
            Judoka judokaSeleccionado = gridJudokas.asSingleSelect().getValue();
            if (judokaSeleccionado == null || sesionActual == null) {
                NotificationHelper.warning("Seleccione judoka y sesión");
                return;
            }
            realizarCheckIn(judokaSeleccionado);
        });
    }

    private void realizarCheckIn(Judoka judoka) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "return navigator.geolocation.getCurrentPosition().then(" +
                        "  pos => ({lat: pos.coords.latitude, lon: pos.coords.longitude})," +
                        "  err => null);"
        ).then(jsonValue -> {
            if (jsonValue == null) {
                NotificationHelper.warning("GPS no disponible");
                return;
            }

            try {
                // ✅ CORREGIDO PARA VAADIN 24.8.4: Conversión explícita a JsonObject
                elemental.json.JsonObject jsonObject = (elemental.json.JsonObject) jsonValue;

                // ✅ CORREGIDO: Usar getNumber() que retorna JsonNumber, luego asDouble()
                double lat = jsonObject.getNumber("lat");
                double lon = jsonObject.getNumber("lon");

                Asistencia asistencia = new Asistencia();
                asistencia.setJudoka(judoka);
                asistencia.setSesion(sesionActual);
                asistencia.setPresente(true);
                asistencia.setFechaHoraMarcacion(LocalDateTime.now());
                asistencia.setLatitud(lat);
                asistencia.setLongitud(lon);

                asistenciaService.registrarAsistencia(asistencia);

                NotificationHelper.success(String.format(
                        "Asistencia registrada: %s %s",
                        judoka.getUsuario().getNombre(),
                        judoka.getUsuario().getApellido()
                ));

                gridJudokas.getDataProvider().refreshItem(judoka);
                logger.info("Check-in exitoso: Judoka {} en Sesión {}", judoka.getId(), sesionActual.getId());

            } catch (Exception e) {
                NotificationHelper.error("Error: " + e.getMessage());
                logger.error("Error en check-in", e);
            }
        }));
    }

    private void buildLayout() {
        H2 titulo = new H2("Registro de Asistencia en Tiempo Real");

        HorizontalLayout combosLayout = new HorizontalLayout(grupoCombo, sesionCombo);
        combosLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        combosLayout.setWidthFull();

        VerticalLayout mainContent = new VerticalLayout(titulo, combosLayout, gridJudokas, btnCheckIn);
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.setAlignItems(FlexComponent.Alignment.STRETCH);

        // ✅ CORREGIDO: Si getContent() causa error, usar add() directo o castear
        // Opción 1: add(mainContent); (si SenseiLayout extiende VerticalLayout)
        // Opción 2: ((VerticalLayout) getContent()).add(mainContent);

        com.vaadin.flow.component.html.Div content =
                (com.vaadin.flow.component.html.Div) getContent();
        content.add(mainContent);
    }

    private Stream<Judoka> fetchJudokasDelGrupo(Query<Judoka, Void> query) {
        GrupoEntrenamiento grupo = grupoCombo.getValue();
        if (grupo == null) return Stream.empty();

        return grupoService.findJudokasEnGrupo(grupo.getId(), null, null, null)
                .stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    private int countJudokasDelGrupo(Query<Judoka, Void> query) {
        GrupoEntrenamiento grupo = grupoCombo.getValue();
        if (grupo == null) return 0;

        return grupoService.findJudokasEnGrupo(grupo.getId(), null, null, null).size();
    }
}