package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.Locale;

public class GrupoForm extends BaseForm<GrupoEntrenamiento> {

    private final TraduccionService traduccionService;

    private final TextField nombre;
    private final TextArea descripcion;

    // --- NUEVOS CAMPOS DE LOGÍSTICA ---
    private final TextField lugarPractica;
    private final NumberField latitud;
    private final NumberField longitud;
    private final IntegerField radioPermitidoMetros;
    private final MultiSelectComboBox<DayOfWeek> diasSemana;
    private final TimePicker horaInicio;
    private final TimePicker horaFin;

    public GrupoForm(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;

        // Inicializamos los componentes usando las claves de traducción
        this.nombre = new TextField(traduccionService.get("grupos.form.nombre"));
        this.descripcion = new TextArea(traduccionService.get("grupos.form.descripcion"));
        this.lugarPractica = new TextField(traduccionService.get("grupos.form.lugar"));
        this.diasSemana = new MultiSelectComboBox<>(traduccionService.get("grupos.form.dias"));
        this.horaInicio = new TimePicker(traduccionService.get("grupos.form.hora_inicio"));
        this.horaFin = new TimePicker(traduccionService.get("grupos.form.hora_fin"));
        this.latitud = new NumberField(traduccionService.get("grupos.form.latitud"));
        this.longitud = new NumberField(traduccionService.get("grupos.form.longitud"));
        this.radioPermitidoMetros = new IntegerField(traduccionService.get("grupos.form.radio"));

        configureFields();

        // Agrupamos las horas en una sola fila para ahorrar espacio visual
        FormLayout layoutHoras = new FormLayout(horaInicio, horaFin);
        layoutHoras.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        // Agregamos todos los campos al lienzo del BaseForm
        FormLayout layoutGps = new FormLayout(latitud, longitud, radioPermitidoMetros);
        layoutGps.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        add(nombre, descripcion, lugarPractica, layoutGps, diasSemana, layoutHoras);
    }

    private void configureFields() {
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
        // Configuración de los Días de la Semana
        diasSemana.setItems(DayOfWeek.values());

        // i18n Dinámico: Detecta el idioma del navegador o usa ES_CO por defecto
        Locale localeUsuario = UI.getCurrent() != null && UI.getCurrent().getLocale() != null
                ? UI.getCurrent().getLocale()
                : new Locale("es", "CO");

        diasSemana.setItemLabelGenerator(dia ->
                dia.getDisplayName(TextStyle.FULL, localeUsuario).toUpperCase()
        );

        // Configuración de los Relojes (Saltos de 15 en 15 minutos)
        horaInicio.setStep(Duration.ofMinutes(15));
        horaFin.setStep(Duration.ofMinutes(15));
    }

    @Override
    protected Binder<GrupoEntrenamiento> createBinder() {
        Binder<GrupoEntrenamiento> binder = new Binder<>(GrupoEntrenamiento.class);

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

        return binder;
    }
}