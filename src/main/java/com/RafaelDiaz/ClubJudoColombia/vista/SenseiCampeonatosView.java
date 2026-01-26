package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ResultadoCompetencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.CompetenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Set;

@Route(value = "gestion-campeonatos", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
@PageTitle("Campeonatos | Club Judo Colombia")
public class SenseiCampeonatosView extends VerticalLayout {

    private final CompetenciaService competenciaService;
    private final JudokaRepository judokaRepository;
    private final TraduccionService traduccionService;
    private final SecurityService securityService;

    private Grid<ParticipacionCompetencia> grid;

    @Autowired
    public SenseiCampeonatosView(CompetenciaService competenciaService,
                                 JudokaRepository judokaRepository,
                                 TraduccionService traduccionService,
                                 SecurityService securityService) {
        this.competenciaService = competenciaService;
        this.judokaRepository = judokaRepository;
        this.traduccionService = traduccionService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 titulo = new H2(traduccionService.get("campeonatos.titulo"));
        Button btnNueva = new Button(traduccionService.get("campeonatos.btn.nueva_convocatoria"), new Icon(VaadinIcon.PLUS));
        btnNueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNueva.addClickListener(e -> abrirDialogoConvocatoria());

        header.add(titulo, btnNueva);

        configureGrid();
        actualizarGrid();

        add(header, grid);
    }

    private void configureGrid() {
        grid = new Grid<>(ParticipacionCompetencia.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(new LocalDateRenderer<>(ParticipacionCompetencia::getFecha, "dd/MM/yyyy"))
                .setHeader(traduccionService.get("generic.fecha")).setWidth("120px").setFlexGrow(0);

        grid.addColumn(ParticipacionCompetencia::getNombreCampeonato)
                .setHeader(traduccionService.get("campeonatos.grid.evento")).setSortable(true).setFlexGrow(1);

        grid.addColumn(p -> p.getJudoka().getUsuario().getNombre() + " " + p.getJudoka().getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.judoka")).setSortable(true).setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(p -> {
            // Usamos traducción automática del Enum ResultadoCompetencia
            Span badge = new Span(traduccionService.get(p.getResultado()));
            String theme = switch (p.getResultado()) {
                case ORO -> "success";
                case PLATA -> "contrast";
                case BRONCE -> "error";
                default -> "primary";
            };
            badge.getElement().getThemeList().add("badge " + theme);
            return badge;
        })).setHeader(traduccionService.get("campeonatos.grid.resultado")).setAutoWidth(true);

        grid.addComponentColumn(p -> {
            Button edit = new Button(new Icon(VaadinIcon.EDIT));
            edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            edit.setTooltipText(traduccionService.get("btn.editar"));
            edit.addClickListener(e -> abrirDialogoResultado(p));

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            delete.setTooltipText(traduccionService.get("btn.eliminar"));
            delete.addClickListener(e -> {
                competenciaService.eliminarParticipacion(p);
                NotificationHelper.success(traduccionService.get("msg.success.deleted"));
                actualizarGrid();
            });

            HorizontalLayout actions = new HorizontalLayout(edit, delete);
            if (p.getUrlVideo() != null && !p.getUrlVideo().isEmpty()) {
                Anchor link = new Anchor(p.getUrlVideo(), new Icon(VaadinIcon.YOUTUBE));
                link.setTarget("_blank");
                link.getStyle().set("color", "red");
                actions.addComponentAsFirst(link); // Usamos el método correcto de Vaadin
            }
            return actions;
        }).setHeader(traduccionService.get("generic.acciones"));
    }

    private void actualizarGrid() {
        grid.setItems(competenciaService.findAll());
    }

    private void abrirDialogoConvocatoria() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(traduccionService.get("campeonatos.dialog.convocatoria.titulo"));

        VerticalLayout form = new VerticalLayout();
        TextField nombre = new TextField(traduccionService.get("campeonatos.field.nombre_evento"));
        TextField lugar = new TextField(traduccionService.get("campeonatos.field.lugar"));
        DatePicker fecha = new DatePicker(traduccionService.get("generic.fecha"));
        fecha.setValue(LocalDate.now());

        ComboBox<NivelCompetencia> nivel = new ComboBox<>(traduccionService.get("campeonatos.field.nivel"));
        nivel.setItems(NivelCompetencia.values());
        nivel.setItemLabelGenerator(traduccionService::get);
        nivel.setValue(NivelCompetencia.DEPARTAMENTAL);

        MultiSelectComboBox<Judoka> judokas =
                new MultiSelectComboBox<>(traduccionService.get("campeonatos.field.seleccionar_atletas"));
        Long miSenseiId = securityService.getSenseiIdActual();
        judokas.setItems(judokaRepository.findBySenseiIdWithUsuario(miSenseiId));
        judokas.setItemLabelGenerator(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido());
        judokas.setWidthFull();

        Button guardar = new Button(traduccionService.get("btn.crear"), e -> {
            Set<Judoka> seleccionados = judokas.getValue();
            if (seleccionados.isEmpty() || nombre.isEmpty()) {
                Notification.show(traduccionService.get("error.campos_obligatorios")).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            seleccionados.forEach(j -> competenciaService.inscribirJudoka(j, nombre.getValue(), lugar.getValue(), fecha.getValue(), nivel.getValue()));
            Notification.show(seleccionados.size() + " " + traduccionService.get("campeonatos.msg.inscritos"));
            actualizarGrid();
            dialog.close();
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(nombre, new HorizontalLayout(lugar, fecha), nivel, judokas, guardar);
        dialog.add(form);
        dialog.open();
    }

    private void abrirDialogoResultado(ParticipacionCompetencia p) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(traduccionService.get("campeonatos.dialog.resultado.titulo") + " " + p.getJudoka().getUsuario().getNombre());

        VerticalLayout form = new VerticalLayout();
        ComboBox<ResultadoCompetencia> resultado = new ComboBox<>(traduccionService.get("campeonatos.field.medalla"));
        resultado.setItems(ResultadoCompetencia.values());
        resultado.setItemLabelGenerator(traduccionService::get);
        resultado.setValue(p.getResultado());

        TextField video = new TextField(traduccionService.get("campeonatos.field.link_video"));
        video.setValue(p.getUrlVideo() != null ? p.getUrlVideo() : "");

        Button guardar = new Button(traduccionService.get("btn.guardar"), e -> {
            competenciaService.registrarResultado(p, resultado.getValue(), video.getValue());
            actualizarGrid();
            dialog.close();
            NotificationHelper.success(traduccionService.get("msg.success.updated"));
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(resultado, video, guardar);
        dialog.add(form);
        dialog.open();
    }
}