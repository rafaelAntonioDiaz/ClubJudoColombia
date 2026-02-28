package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Macrociclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.Microciclo;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MesocicloATC;
import com.RafaelDiaz.ClubJudoColombia.servicio.MacrocicloService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "gestion-macrociclos", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiMacrocicloView extends VerticalLayout {

    private final MacrocicloService macrocicloService;
    private final SecurityService securityService;
    private Sensei senseiActual;

    private Grid<Macrociclo> grid;

    public SenseiMacrocicloView(MacrocicloService macrocicloService, SecurityService securityService) {
        this.macrocicloService = macrocicloService;
        this.securityService = securityService;
        this.senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Error: Sensei no encontrado"));

        setSizeFull();
        setPadding(true);

        configurarCabecera();
        configurarGrid();
        actualizarGrid();
    }

    private void configurarCabecera() {
        HorizontalLayout cabecera = new HorizontalLayout();
        cabecera.setWidthFull();
        cabecera.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecera.setAlignItems(Alignment.BASELINE);

        Button btnNuevo = new Button("Nuevo Macrociclo", new Icon(VaadinIcon.PLUS));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirFormulario(new Macrociclo()));

        cabecera.add(new H1("Mis Macrociclos (Temporadas)"), btnNuevo);
        add(cabecera);
    }

    private void configurarGrid() {
        grid = new Grid<>(Macrociclo.class, false);
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Macrociclo::getNombre).setHeader("Nombre / Torneo").setFlexGrow(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        grid.addColumn(m -> (m.getFechaInicio() != null && m.getFechaFin() != null)
                        ? m.getFechaInicio().format(formatter) + " - " + m.getFechaFin().format(formatter) : "Sin fechas")
                .setHeader("Periodo").setAutoWidth(true);

        // --- LA MAGIA VISUAL: LÍNEA DE TIEMPO DE MICROCICLOS ---
        grid.addComponentColumn(this::crearLineaDeTiempo)
                .setHeader("Línea de Tiempo (Microciclos)").setFlexGrow(2);

        grid.addComponentColumn(m -> {
            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
            btnEditar.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            btnEditar.addClickListener(e -> abrirFormulario(m));
            return btnEditar;
        }).setHeader("Acciones").setAutoWidth(true);

        add(grid);
    }

    /**
     * Dibuja los bloques de Lego (A-T-C-R) que están dentro de este Macrociclo.
     */
    private HorizontalLayout crearLineaDeTiempo(Macrociclo macrociclo) {
        HorizontalLayout linea = new HorizontalLayout();
        linea.setSpacing(true);
        linea.setPadding(false);
        linea.setAlignItems(FlexComponent.Alignment.CENTER);

        if (macrociclo.getMicrociclos().isEmpty()) {
            Span empty = new Span("Vacío (Agrega microciclos desde el Taller)");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            empty.getStyle().set("font-size", "var(--lumo-font-size-s)");
            linea.add(empty);
            return linea;
        }

        for (Microciclo micro : macrociclo.getMicrociclos()) {
            Span bloque = crearBadgeFase(micro.getMesocicloATC());
            // Tooltip nativo de HTML para ver el nombre de la semana al pasar el mouse
            bloque.getElement().setProperty("title", micro.getNombre() + " (" + micro.getFechaInicio() + ")");
            linea.add(bloque);
        }

        return linea;
    }

    private Span crearBadgeFase(MesocicloATC fase) {
        Span badge = new Span();
        badge.getElement().getThemeList().add("badge");
        badge.getStyle().set("width", "25px"); // Bloques cuadrados tipo Gantt
        badge.getStyle().set("height", "25px");
        badge.getStyle().set("display", "flex");
        badge.getStyle().set("justify-content", "center");
        badge.getStyle().set("align-items", "center");
        badge.getStyle().set("font-weight", "bold");

        if (fase == null) {
            badge.setText("?");
            return badge;
        }

        // Mostramos solo la inicial (A, T, C, R)
        badge.setText(fase.name().substring(0, 1));

        switch (fase) {
            case NIVELACION: case ADQUISICION: badge.getElement().getThemeList().add("primary"); break;
            case TRANSFERENCIA: badge.getElement().getThemeList().add("warning"); break;
            case COMPETENCIA: badge.getElement().getThemeList().add("error"); break;
            case REFUERZO: badge.getElement().getThemeList().add("contrast"); break;
            case RECUPERACION: badge.getElement().getThemeList().add("success"); break;
        }
        return badge;
    }

    private void abrirFormulario(Macrociclo macrociclo) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle(macrociclo.getId() == null ? "Nuevo Macrociclo" : "Editar Macrociclo");

        FormLayout form = new FormLayout();
        TextField nombreField = new TextField("Nombre (Ej. Juegos Nacionales)");
        TextArea objetivoField = new TextArea("Objetivo Principal");
        DatePicker inicioPicker = new DatePicker("Fecha de Inicio");
        DatePicker finPicker = new DatePicker("Fecha de Fin");

        nombreField.setValue(macrociclo.getNombre() != null ? macrociclo.getNombre() : "");
        objetivoField.setValue(macrociclo.getObjetivoPrincipal() != null ? macrociclo.getObjetivoPrincipal() : "");
        inicioPicker.setValue(macrociclo.getFechaInicio());
        finPicker.setValue(macrociclo.getFechaFin());

        form.add(nombreField, objetivoField, inicioPicker, finPicker);
        dialog.add(form);

        Button btnGuardar = new Button("Guardar", e -> {
            macrociclo.setSensei(senseiActual);
            macrociclo.setNombre(nombreField.getValue());
            macrociclo.setObjetivoPrincipal(objetivoField.getValue());
            macrociclo.setFechaInicio(inicioPicker.getValue());
            macrociclo.setFechaFin(finPicker.getValue());

            macrocicloService.guardarMacrociclo(macrociclo);
            NotificationHelper.success("Macrociclo guardado");
            actualizarGrid();
            dialog.close();
        });
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(btnCancelar, btnGuardar);
        dialog.open();
    }

    private void actualizarGrid() {
        grid.setItems(macrocicloService.obtenerHistorialDelSensei(senseiActual));
    }
}