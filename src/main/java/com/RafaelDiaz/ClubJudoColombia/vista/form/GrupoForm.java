package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.Locale;

public class GrupoForm extends BaseForm<GrupoEntrenamiento> {

    private final TraduccionService traduccionService;

    // Campos existentes
    private final TextField nombre;
    private final TextArea descripcion;
    private final TextField lugarPractica;
    private final NumberField latitud;
    private final NumberField longitud;
    private final IntegerField radioPermitidoMetros;
    private final MultiSelectComboBox<DayOfWeek> diasSemana;
    private final TimePicker horaInicio;
    private final TimePicker horaFin;

    // NUEVOS CAMPOS FINANCIEROS
    private final NumberField tarifaMensual;
    private final Checkbox incluyeMatricula;
    private final NumberField montoMatricula;
    private final IntegerField diasGracia;

    public GrupoForm(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;

        // Inicialización de componentes
        this.nombre = new TextField(traduccionService.get("grupos.form.nombre"));
        this.descripcion = new TextArea(traduccionService.get("grupos.form.descripcion"));
        this.lugarPractica = new TextField(traduccionService.get("grupos.form.lugar"));
        this.diasSemana = new MultiSelectComboBox<>(traduccionService.get("grupos.form.dias"));
        this.horaInicio = new TimePicker(traduccionService.get("grupos.form.hora_inicio"));
        this.horaFin = new TimePicker(traduccionService.get("grupos.form.hora_fin"));
        this.latitud = new NumberField(traduccionService.get("grupos.form.latitud"));
        this.longitud = new NumberField(traduccionService.get("grupos.form.longitud"));
        this.radioPermitidoMetros = new IntegerField(traduccionService.get("grupos.form.radio"));

        // Campos financieros
        this.tarifaMensual = new NumberField(traduccionService.get("grupos.form.tarifa_mensual"));
        this.incluyeMatricula = new Checkbox(traduccionService.get("grupos.form.incluye_matricula"));
        this.montoMatricula = new NumberField(traduccionService.get("grupos.form.monto_matricula"));
        this.diasGracia = new IntegerField(traduccionService.get("grupos.form.dias_gracia"));

        configureFields();
        configureFinancialFields();

        // Layout de horas y GPS (igual que antes)
        FormLayout layoutHoras = new FormLayout(horaInicio, horaFin);
        layoutHoras.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        FormLayout layoutGps = new FormLayout(latitud, longitud, radioPermitidoMetros);
        layoutGps.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        // Layout financiero (tarifa, matrícula, días gracia)
        FormLayout layoutFinanciero = new FormLayout(tarifaMensual, incluyeMatricula, montoMatricula, diasGracia);
        layoutFinanciero.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        add(nombre, descripcion, lugarPractica, layoutGps, diasSemana, layoutHoras, layoutFinanciero);
    }

    private void configureFields() {
        // Configuraciones existentes...
        nombre.setRequiredIndicatorVisible(true);
        descripcion.setMaxLength(255);
        lugarPractica.setPlaceholder(traduccionService.get("grupos.form.lugar.placeholder"));
        latitud.setStep(0.000001);
        latitud.setPlaceholder("Ej: 4.7110");
        latitud.setMin(-90);
        latitud.setMax(90);
        longitud.setStep(0.000001);
        longitud.setPlaceholder("Ej: -74.0721");
        longitud.setMin(-180);
        longitud.setMax(180);
        radioPermitidoMetros.setStepButtonsVisible(true);
        radioPermitidoMetros.setMin(10);
        radioPermitidoMetros.setMax(1000);
        radioPermitidoMetros.setValue(100);

        // Días de la semana
        diasSemana.setItems(DayOfWeek.values());
        Locale localeUsuario = UI.getCurrent() != null && UI.getCurrent().getLocale() != null
                ? UI.getCurrent().getLocale()
                : new Locale("es", "CO");
        diasSemana.setItemLabelGenerator(dia ->
                dia.getDisplayName(TextStyle.FULL, localeUsuario).toUpperCase());

        horaInicio.setStep(Duration.ofMinutes(15));
        horaFin.setStep(Duration.ofMinutes(15));
    }

    private void configureFinancialFields() {
        tarifaMensual.setRequiredIndicatorVisible(true);
        tarifaMensual.setStep(1000);
        tarifaMensual.setMin(0);
        tarifaMensual.setPlaceholder("Ej: 35000");

        incluyeMatricula.addValueChangeListener(e -> {
            montoMatricula.setVisible(e.getValue());
            if (!e.getValue()) {
                montoMatricula.clear();
            }
        });
        montoMatricula.setVisible(false);
        montoMatricula.setStep(1000);
        montoMatricula.setMin(0);

        diasGracia.setRequiredIndicatorVisible(true);
        diasGracia.setMin(0);
        diasGracia.setMax(30);
        diasGracia.setStepButtonsVisible(true);
        diasGracia.setValue(5); // valor por defecto
    }

    @Override
    protected Binder<GrupoEntrenamiento> createBinder() {
        Binder<GrupoEntrenamiento> binder = new Binder<>(GrupoEntrenamiento.class);

        // Campos existentes...
        binder.forField(nombre)
                .asRequired(traduccionService.get("grupos.form.error.nombre_requerido"))
                .bind(GrupoEntrenamiento::getNombre, GrupoEntrenamiento::setNombre);

        binder.forField(descripcion)
                .bind(GrupoEntrenamiento::getDescripcion, GrupoEntrenamiento::setDescripcion);

        binder.forField(lugarPractica)
                .bind(GrupoEntrenamiento::getLugarPractica, GrupoEntrenamiento::setLugarPractica);

        binder.forField(latitud)
                .withNullRepresentation(null)
                .bind(GrupoEntrenamiento::getLatitud, GrupoEntrenamiento::setLatitud);

        binder.forField(longitud)
                .withNullRepresentation(null)
                .bind(GrupoEntrenamiento::getLongitud, GrupoEntrenamiento::setLongitud);

        binder.forField(radioPermitidoMetros)
                .withNullRepresentation(100)
                .bind(GrupoEntrenamiento::getRadioPermitidoMetros, GrupoEntrenamiento::setRadioPermitidoMetros);

        binder.forField(horaInicio)
                .bind(GrupoEntrenamiento::getHoraInicio, GrupoEntrenamiento::setHoraInicio);

        binder.forField(horaFin)
                .bind(GrupoEntrenamiento::getHoraFin, GrupoEntrenamiento::setHoraFin);

        binder.forField(diasSemana)
                .bind(GrupoEntrenamiento::getDiasSemana, GrupoEntrenamiento::setDiasSemana);

        // Nuevos campos financieros
        binder.forField(tarifaMensual)
                .asRequired(traduccionService.get("grupos.form.error.tarifa_requerida"))
                .withConverter(
                        value -> value != null ? BigDecimal.valueOf(value) : null,
                        bd -> bd != null ? bd.doubleValue() : null
                )
                .bind(GrupoEntrenamiento::getTarifaMensual, GrupoEntrenamiento::setTarifaMensual);

        binder.forField(incluyeMatricula)
                .bind(GrupoEntrenamiento::isIncluyeMatricula, GrupoEntrenamiento::setIncluyeMatricula);

        binder.forField(montoMatricula)
                .withConverter(
                        value -> value != null ? BigDecimal.valueOf(value) : null,
                        bd -> bd != null ? bd.doubleValue() : null
                )
                .bind(GrupoEntrenamiento::getMontoMatricula, GrupoEntrenamiento::setMontoMatricula);

        binder.forField(diasGracia)
                .asRequired(traduccionService.get("grupos.form.error.dias_gracia_requeridos"))
                .bind(GrupoEntrenamiento::getDiasGracia, GrupoEntrenamiento::setDiasGracia);

        return binder;
    }
}