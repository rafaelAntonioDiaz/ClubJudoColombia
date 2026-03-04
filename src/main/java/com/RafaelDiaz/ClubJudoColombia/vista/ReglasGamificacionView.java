package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.OperadorComparacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoEventoGamificacion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.*;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "reglas-gamificacion", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_SENSEI", "ROLE_MASTER"})
@PageTitle("Reglas de Gamificación | Club Judo Colombia")
public class ReglasGamificacionView extends VerticalLayout {

    private final ReglaGamificacionRepository reglaRepo;
    private final InsigniaRepository insigniaRepo;
    private final MetricaRepository metricaRepo;
    private final SecurityService securityService;
    private final TraduccionService traduccionService;

    private Grid<ReglaGamificacion> grid;
    private Sensei senseiActual;

    public ReglasGamificacionView(ReglaGamificacionRepository reglaRepo,
                                  InsigniaRepository insigniaRepo,
                                  MetricaRepository metricaRepo,
                                  SecurityService securityService,
                                  TraduccionService traduccionService) {
        this.reglaRepo = reglaRepo;
        this.insigniaRepo = insigniaRepo;
        this.metricaRepo = metricaRepo;
        this.securityService = securityService;
        this.traduccionService = traduccionService;

        senseiActual = securityService.getAuthenticatedSensei()
                .orElseThrow(() -> new RuntimeException("Sensei no autenticado"));

        add(new H2(traduccionService.get("gamificacion.titulo")));

        configurarGrid();
        cargarReglas();

        Button btnNueva = new Button(traduccionService.get("gamificacion.nueva_regla"), VaadinIcon.PLUS.create());
        btnNueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNueva.addClickListener(e -> abrirEditor(null));

        add(btnNueva, grid);
    }

    private void configurarGrid() {
        grid = new Grid<>(ReglaGamificacion.class, false);
        grid.addColumn(r -> r.getInsignia().getNombre()).setHeader("Insignia");
        grid.addColumn(r -> r.getTipoEvento().name()).setHeader("Evento");
        grid.addColumn(r -> r.getMetrica() != null ? r.getMetrica().getNombreKey() : "").setHeader("Métrica");
        grid.addColumn(r -> r.getOperador().name()).setHeader("Operador");
        grid.addColumn(ReglaGamificacion::getValorObjetivo).setHeader("Valor");
        grid.addComponentColumn(this::crearBotonesAccion).setHeader("Acciones");
    }

    private HorizontalLayout crearBotonesAccion(ReglaGamificacion regla) {
        Button editar = new Button(VaadinIcon.EDIT.create());
        editar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editar.addClickListener(e -> abrirEditor(regla));

        Button eliminar = new Button(VaadinIcon.TRASH.create());
        eliminar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        eliminar.addClickListener(e -> eliminarRegla(regla));

        return new HorizontalLayout(editar, eliminar);
    }

    private void cargarReglas() {
        grid.setItems(reglaRepo.findBySenseiWithInsignia(senseiActual));
    }

    private void abrirEditor(ReglaGamificacion regla) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(regla == null ? "Nueva Regla" : "Editar Regla");

        FormLayout form = new FormLayout();

        ComboBox<Insignia> cmbInsignia = new ComboBox<>("Insignia");
        cmbInsignia.setItems(insigniaRepo.findAll());
        cmbInsignia.setItemLabelGenerator(Insignia::getNombre);
        cmbInsignia.setRequired(true);

        ComboBox<TipoEventoGamificacion> cmbEvento = new ComboBox<>("Tipo de Evento");
        cmbEvento.setItems(TipoEventoGamificacion.values());
        cmbEvento.setRequired(true);

        ComboBox<Metrica> cmbMetrica = new ComboBox<>("Métrica (solo para pruebas)");
        cmbMetrica.setItems(metricaRepo.findAll());
        cmbMetrica.setItemLabelGenerator(Metrica::getNombreKey);
        cmbMetrica.setEnabled(false);

        ComboBox<OperadorComparacion> cmbOperador = new ComboBox<>("Operador");
        cmbOperador.setItems(OperadorComparacion.values());
        cmbOperador.setRequired(true);

        NumberField valorField = new NumberField("Valor Objetivo");
        valorField.setStep(0.01);
        valorField.setRequiredIndicatorVisible(true);

        TextField descripcion = new TextField("Descripción (opcional)");
        descripcion.setWidthFull();

        // Lógica: habilitar métrica solo si el evento es RESULTADO_PRUEBA
        cmbEvento.addValueChangeListener(e -> {
            boolean esPrueba = e.getValue() == TipoEventoGamificacion.RESULTADO_PRUEBA;
            cmbMetrica.setEnabled(esPrueba);
            if (!esPrueba) cmbMetrica.clear();
        });

        if (regla != null) {
            cmbInsignia.setValue(regla.getInsignia());
            cmbEvento.setValue(regla.getTipoEvento());
            cmbMetrica.setValue(regla.getMetrica());
            cmbOperador.setValue(regla.getOperador());
            valorField.setValue(regla.getValorObjetivo() != null ? regla.getValorObjetivo().doubleValue() : null);
            descripcion.setValue(regla.getDescripcion() != null ? regla.getDescripcion() : "");
        }

        form.add(cmbInsignia, cmbEvento, cmbMetrica, cmbOperador, valorField, descripcion);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button guardar = new Button("Guardar", e -> {
            try {
                if (cmbInsignia.isEmpty() || cmbEvento.isEmpty() || cmbOperador.isEmpty() || valorField.isEmpty()) {
                    Notification.show("Complete los campos obligatorios");
                    return;
                }

                ReglaGamificacion nueva = regla != null ? regla : new ReglaGamificacion();
                nueva.setSensei(senseiActual);
                nueva.setInsignia(cmbInsignia.getValue());
                nueva.setTipoEvento(cmbEvento.getValue());
                nueva.setMetrica(cmbMetrica.getValue());
                nueva.setOperador(cmbOperador.getValue());
                nueva.setValorObjetivo(BigDecimal.valueOf(valorField.getValue()));
                nueva.setDescripcion(descripcion.getValue());

                reglaRepo.save(nueva);
                Notification.show("Regla guardada");
                dialog.close();
                cargarReglas();
            } catch (DataIntegrityViolationException ex) {
                Notification.show("Error: Datos duplicados o inválidos");
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", ev -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    private void eliminarRegla(ReglaGamificacion regla) {
        reglaRepo.delete(regla);
        Notification.show("Regla eliminada");
        cargarReglas();
    }
}