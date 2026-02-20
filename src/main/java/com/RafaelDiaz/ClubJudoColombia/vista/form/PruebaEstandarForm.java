package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.Metrica;
import com.RafaelDiaz.ClubJudoColombia.modelo.PruebaEstandar;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaEjercicio;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

/**
 * Formulario avanzado para que los Senseis creen sus propias Pruebas de Autor.
 */
public class PruebaEstandarForm extends BaseForm<PruebaEstandar> {

    private final TextField nombrePersonalizado = new TextField("Nombre de la Evaluación");
    private final ComboBox<CategoriaEjercicio> categoria = new ComboBox<>("Bloque Metodológico");
    private final MultiSelectComboBox<Metrica> metricas = new MultiSelectComboBox<>("Unidades de Medida a Evaluar");
    private final TextArea objetivoPersonalizado = new TextArea("Objetivo (¿Qué mide?)");
    private final TextArea descripcionPersonalizada = new TextArea("Descripción (¿Cómo se ejecuta?)");

    public PruebaEstandarForm(List<Metrica> metricasDisponibles, TraduccionService traduccionService) {
        configureFields(metricasDisponibles, traduccionService);
        // Los botones guardar/cancelar ya vienen del BaseForm
        add(nombrePersonalizado, categoria, metricas, objetivoPersonalizado, descripcionPersonalizada);
    }

    private void configureFields(List<Metrica> metricasDisponibles, TraduccionService traduccionService) {
        // Selector de Bloque (CAAV)
        categoria.setItems(CategoriaEjercicio.values());
        categoria.setItemLabelGenerator(CategoriaEjercicio::getDescripcion);
        categoria.setRequiredIndicatorVisible(true);

        // Selector Múltiple de Metrología
        metricas.setItems(metricasDisponibles);
        metricas.setItemLabelGenerator(m ->
                traduccionService.get(m.getNombreKey()) + " (" + m.getUnidad() + ")"
        );
        metricas.setRequiredIndicatorVisible(true);

        nombrePersonalizado.setRequiredIndicatorVisible(true);

        objetivoPersonalizado.setMaxLength(500);
        descripcionPersonalizada.setMaxLength(1000);
        descripcionPersonalizada.setHeight("120px");
    }

    @Override
    protected Binder<PruebaEstandar> createBinder() {
        Binder<PruebaEstandar> binder = new Binder<>(PruebaEstandar.class);

        binder.forField(nombrePersonalizado)
                .asRequired("El nombre es obligatorio")
                .bind(PruebaEstandar::getNombrePersonalizado, PruebaEstandar::setNombrePersonalizado);

        binder.forField(categoria)
                .asRequired("El bloque metodológico es obligatorio")
                .bind(PruebaEstandar::getCategoria, PruebaEstandar::setCategoria);

        binder.forField(metricas)
                .asRequired("Debe seleccionar al menos una unidad de medida")
                .bind(PruebaEstandar::getMetricas, PruebaEstandar::setMetricas);

        binder.forField(objetivoPersonalizado)
                .bind(PruebaEstandar::getObjetivoPersonalizado, PruebaEstandar::setObjetivoPersonalizado);

        binder.forField(descripcionPersonalizada)
                .bind(PruebaEstandar::getDescripcionPersonalizada, PruebaEstandar::setDescripcionPersonalizada);

        return binder;
    }
}