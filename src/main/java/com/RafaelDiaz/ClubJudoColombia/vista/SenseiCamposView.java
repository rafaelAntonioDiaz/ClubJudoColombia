package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.CampoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.CampoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Set;

@Route(value = "gestion-campos", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Campos de Entrenamiento | Club Judo Colombia")
public class SenseiCamposView extends VerticalLayout {

    private final CampoService campoService;
    private final JudokaRepository judokaRepository;
    private final TraduccionService traduccionService; // <--- I18n
    private Grid<CampoEntrenamiento> grid;

    @Autowired
    public SenseiCamposView(CampoService campoService,
                            JudokaRepository judokaRepository,
                            TraduccionService traduccionService) {
        this.campoService = campoService;
        this.judokaRepository = judokaRepository;
        this.traduccionService = traduccionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 titulo = new H2(traduccionService.get("campos.titulo"));
        Button btnNuevo = new Button(traduccionService.get("campos.btn.programar"), new Icon(VaadinIcon.PLUS));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.addClickListener(e -> abrirDialogoProgramacion());

        header.add(titulo, btnNuevo);

        configureGrid();
        actualizarGrid();

        add(header, grid);
    }

    private void configureGrid() {
        grid = new Grid<>(CampoEntrenamiento.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(new LocalDateRenderer<>(CampoEntrenamiento::getFechaInicio, "dd/MM/yyyy"))
                .setHeader(traduccionService.get("generic.fecha_inicio")).setWidth("110px").setFlexGrow(0);

        grid.addColumn(CampoEntrenamiento::getNombre)
                .setHeader(traduccionService.get("campos.grid.nombre")).setSortable(true).setFlexGrow(1);

        grid.addColumn(c -> c.getJudoka().getUsuario().getNombre() + " " + c.getJudoka().getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.judoka")).setSortable(true).setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(c -> {
            if (c.isCompletado()) {
                Span badge = new Span("+" + c.getPuntosAscenso() + " " + traduccionService.get("generic.pts"));
                badge.getElement().getThemeList().add("badge success");
                return badge;
            } else {
                Span badge = new Span(traduccionService.get("campos.estado.en_curso"));
                badge.getElement().getThemeList().add("badge contrast");
                return badge;
            }
        })).setHeader(traduccionService.get("generic.estado")).setAutoWidth(true);

        grid.addComponentColumn(this::crearBotonesAccion).setHeader(traduccionService.get("generic.acciones"));
    }

    private Component crearBotonesAccion(CampoEntrenamiento campo) {
        Button btnCertificar = new Button(new Icon(VaadinIcon.CHECK_CIRCLE));
        btnCertificar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        btnCertificar.setTooltipText(traduccionService.get("campos.btn.certificar"));
        btnCertificar.setEnabled(!campo.isCompletado());
        btnCertificar.addClickListener(e -> abrirDialogoCertificar(campo));

        Button btnEliminar = new Button(new Icon(VaadinIcon.TRASH));
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnEliminar.setTooltipText(traduccionService.get("btn.eliminar"));
        btnEliminar.addClickListener(e -> {
            campoService.eliminar(campo);
            NotificationHelper.success(traduccionService.get("msg.success.deleted"));
            actualizarGrid();
        });

        return new HorizontalLayout(btnCertificar, btnEliminar);
    }

    private void actualizarGrid() {
        grid.setItems(campoService.findAll());
    }

    private void abrirDialogoProgramacion() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(traduccionService.get("campos.dialog.programar.titulo"));
        dialog.setWidth("600px");

        VerticalLayout form = new VerticalLayout();
        TextField nombre = new TextField(traduccionService.get("campos.field.nombre"));
        nombre.setPlaceholder(traduccionService.get("campos.placeholder.ej_campamento"));
        nombre.setWidthFull();

        TextField lugar = new TextField(traduccionService.get("campos.field.lugar"));
        TextField objetivo = new TextField(traduccionService.get("campos.field.objetivo"));
        objetivo.setPlaceholder(traduccionService.get("campos.placeholder.ej_tactico"));

        HorizontalLayout fechas = new HorizontalLayout();
        DatePicker inicio = new DatePicker(traduccionService.get("generic.fecha_inicio"));
        DatePicker fin = new DatePicker(traduccionService.get("generic.fecha_fin"));
        inicio.setValue(LocalDate.now());
        fin.setValue(LocalDate.now().plusDays(3));
        fechas.add(inicio, fin);

        MultiSelectComboBox<Judoka> judokas = new MultiSelectComboBox<>(traduccionService.get("campos.field.convocados"));
        judokas.setItems(judokaRepository.findAllWithUsuario());
        judokas.setItemLabelGenerator(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido());
        judokas.setWidthFull();

        Button guardar = new Button(traduccionService.get("btn.crear"), e -> {
            Set<Judoka> seleccionados = judokas.getValue();
            if (seleccionados.isEmpty() || nombre.isEmpty()) return;

            seleccionados.forEach(j ->
                    campoService.inscribirJudoka(j, nombre.getValue(), lugar.getValue(),
                            inicio.getValue(), fin.getValue(), objetivo.getValue())
            );
            NotificationHelper.success(traduccionService.get("campos.msg.programado") + " " + seleccionados.size());
            actualizarGrid();
            dialog.close();
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.setWidthFull();

        form.add(nombre, lugar, objetivo, fechas, judokas, guardar);
        dialog.add(form);
        dialog.open();
    }

    private void abrirDialogoCertificar(CampoEntrenamiento campo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(traduccionService.get("campos.dialog.certificar.titulo") + " " + campo.getJudoka().getUsuario().getNombre());

        VerticalLayout form = new VerticalLayout();
        form.add(new Span(traduccionService.get("campos.label.pregunta_cumplimiento")));

        IntegerField puntos = new IntegerField(traduccionService.get("campos.field.puntos_ascenso"));
        puntos.setValue(10);
        puntos.setStepButtonsVisible(true);
        puntos.setMin(0);
        puntos.setMax(100);

        Button confirmar = new Button(traduccionService.get("campos.btn.confirmar_puntos"), e -> {
            campoService.certificarCumplimiento(campo, puntos.getValue());
            NotificationHelper.success(traduccionService.get("msg.success.updated"));
            actualizarGrid();
            dialog.close();
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        form.add(puntos, confirmar);
        dialog.add(form);
        dialog.open();
    }
}