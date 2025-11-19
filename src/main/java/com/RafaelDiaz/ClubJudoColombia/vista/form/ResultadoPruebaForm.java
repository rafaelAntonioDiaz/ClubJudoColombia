package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.EjercicioPlanificado;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar; // --- REFACTORIZADO ---
import com.RafaelDiaz.ClubJudoColombia.modelo.ResultadoPrueba; // --- REFACTORIZADO ---
import com.RafaelDiaz.ClubJudoColombia.repositorio.MetricaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout; // --- AÑADIDO ---
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * --- FORMULARIO REFACTORIZADO ---
 * (Antes 'ResultadoForm')
 * Este formulario es para el SENSEI, para que registre
 * los datos brutos de una Prueba Estandar.
 * NO contiene GPS.
 */
public class ResultadoPruebaForm extends VerticalLayout { // Cambiado a VerticalLayout

    private final TraduccionService traduccionService;
    private final MetricaRepository metricaRepository;

    // --- Componentes ---
    private H3 tituloPrueba = new H3();
    private Span notasSenseiOriginales = new Span(); // Notas del plan
    private FormLayout formLayout = new FormLayout();
    private TextArea notasJudoka = new TextArea("Notas (opcional)"); // Notas del Sensei sobre el Judoka
    private Button btnGuardar = new Button("Guardar Resultados");
    private Button btnCancelar = new Button("Cancelar");
    // (infoGps eliminado)

    // --- Estado ---
    private EjercicioPlanificado ejercicioPlanificadoActual;
    private Judoka judokaActual;
    private List<NumberField> camposDeMetricas = new ArrayList<>();
    // (latitud y longitud eliminados)

    public ResultadoPruebaForm(TraduccionService traduccionService, MetricaRepository metricaRepository) {
        this.traduccionService = traduccionService;
        this.metricaRepository = metricaRepository;

        setWidth("100%");
        // --- Layout Actualizado (sin infoGps) ---
        add(tituloPrueba, notasSenseiOriginales, formLayout, notasJudoka, new HorizontalLayout(btnGuardar, btnCancelar));

        // (obtenerUbicacion() eliminado)

        btnGuardar.addClickListener(event -> validarYGuardar());
        btnCancelar.addClickListener(event -> fireEvent(new CancelEvent(this)));
    }

    /**
     * Prepara el formulario para registrar los datos de una Prueba Estandar
     * para un Judoka específico.
     */
    public void setPrueba(EjercicioPlanificado ep, Judoka judoka) {
        this.ejercicioPlanificadoActual = ep;
        this.judokaActual = judoka;

        formLayout.removeAll();
        camposDeMetricas.clear();
        notasJudoka.clear();

        // --- Lógica Actualizada para 'PruebaEstandar' ---
        PruebaEstandar prueba = ep.getPruebaEstandar();
        if (prueba == null) {
            // Esto no debería pasar si la UI se filtra correctamente
            tituloPrueba.setText("Error: Esto es una Tarea Diaria, no una Prueba.");
            return;
        }

        String nombrePrueba = traduccionService.get(prueba.getNombreKey());
        tituloPrueba.setText("Registrar Prueba: " + nombrePrueba + " para " + judoka.getUsuario().getNombre());
        notasSenseiOriginales.setText("Notas del Plan: " + (ep.getNotasSensei() != null ? ep.getNotasSensei() : "Ninguna"));

        // Creamos campos para TODAS las métricas de la prueba (ej. SJFT)
        Set<Metrica> metricas = prueba.getMetricas();
        if (metricas.isEmpty()) {
            formLayout.add(new Span("Esta prueba no tiene métricas definidas."));
            return;
        }

        for (Metrica metrica : metricas) {
            String etiqueta = traduccionService.get(metrica.getNombreKey()) + " (" + metrica.getUnidad() + ")";
            NumberField campo = new NumberField(etiqueta);
            campo.setPlaceholder("0.0");
            campo.getElement().setProperty("metricaId", metrica.getId());
            formLayout.add(campo);
            camposDeMetricas.add(campo);
        }
    }

    /**
     * Valida y crea los objetos ResultadoPrueba (los datos brutos del Sensei).
     */
    private void validarYGuardar() {
        if (judokaActual == null || ejercicioPlanificadoActual == null) {
            Notification.show("Error: No hay una prueba o judoka seleccionado.", 3000, Notification.Position.MIDDLE);
            return;
        }

        List<ResultadoPrueba> nuevosResultados = new ArrayList<>();

        for (NumberField campo : camposDeMetricas) {
            Double valor = campo.getValue();
            if (valor != null) {

                String metricaIdString = campo.getElement().getProperty("metricaId");
                Long metricaId = Long.parseLong(metricaIdString);
                Metrica metrica = metricaRepository.findById(metricaId)
                        .orElseThrow(() -> new RuntimeException("Error: Métrica no encontrada con ID: " + metricaId));

                // --- Usamos la entidad 'ResultadoPrueba' ---
                ResultadoPrueba res = new ResultadoPrueba();
                res.setJudoka(judokaActual);
                res.setEjercicioPlanificado(ejercicioPlanificadoActual);
                res.setMetrica(metrica);
                res.setValor(valor);
                res.setNotasJudoka(notasJudoka.getValue()); // Notas del Sensei sobre la prueba
                // (GPS ya no está aquí)

                nuevosResultados.add(res);
            }
        }

        if (nuevosResultados.isEmpty()) {
            Notification.show("Por favor, ingrese al menos un valor.", 3000, Notification.Position.MIDDLE);
            return;
        }

        fireEvent(new SaveEvent(this, nuevosResultados));
    }

    // --- (Todos los métodos de GPS eliminados) ---

    // --- Sistema de Eventos Personalizados (Actualizado a ResultadoPrueba) ---

    public static abstract class ResultadoPruebaFormEvent extends ComponentEvent<ResultadoPruebaForm> {
        private List<ResultadoPrueba> resultados;

        protected ResultadoPruebaFormEvent(ResultadoPruebaForm source, List<ResultadoPrueba> resultados) {
            super(source, false);
            this.resultados = resultados;
        }

        public List<ResultadoPrueba> getResultados() {
            return resultados;
        }
    }

    public static class SaveEvent extends ResultadoPruebaFormEvent {
        SaveEvent(ResultadoPruebaForm source, List<ResultadoPrueba> resultados) {
            super(source, resultados);
        }
    }

    public static class CancelEvent extends ResultadoPruebaFormEvent {
        CancelEvent(ResultadoPruebaForm source) {
            super(source, null);
        }
    }

    // Métodos para que la Vista (el 'padre') pueda escuchar
    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}