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

    private final TextField nombrePersonalizado;
    private final ComboBox<CategoriaEjercicio> categoria;
    private final MultiSelectComboBox<Metrica> metricas;
    private final TextArea objetivoPersonalizado;
    private final TextArea descripcionPersonalizada;

    private final TraduccionService traduccionService;

    public PruebaEstandarForm(List<Metrica> metricasDisponibles, TraduccionService traduccionService) {
        super(traduccionService); // BaseForm se encarga de los botones traducidos
        this.traduccionService = traduccionService;

        // Inicializar componentes con etiquetas traducidas
        nombrePersonalizado = new TextField(traduccionService.get("prueba.estandar.nombre"));
        categoria = new ComboBox<>(traduccionService.get("prueba.estandar.categoria"));
        metricas = new MultiSelectComboBox<>(traduccionService.get("prueba.estandar.metricas"));
        objetivoPersonalizado = new TextArea(traduccionService.get("prueba.estandar.objetivo"));
        descripcionPersonalizada = new TextArea(traduccionService.get("prueba.estandar.descripcion"));

        configureFields(metricasDisponibles);
        add(nombrePersonalizado, categoria, metricas, objetivoPersonalizado, descripcionPersonalizada);
    }

    private void configureFields(List<Metrica> metricasDisponibles) {
        // Bloque metodológico (CAAV) con opciones traducidas
        categoria.setItems(CategoriaEjercicio.values());
        categoria.setItemLabelGenerator(c -> traduccionService.get("categoria." + c.name()));
        categoria.setRequiredIndicatorVisible(true);

        // Métricas con traducción de nombre y unidad
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
                .asRequired(traduccionService.get("prueba.estandar.error.nombre_requerido"))
                .bind(PruebaEstandar::getNombrePersonalizado, PruebaEstandar::setNombrePersonalizado);

        binder.forField(categoria)
                .asRequired(traduccionService.get("prueba.estandar.error.categoria_requerida"))
                .bind(PruebaEstandar::getCategoria, PruebaEstandar::setCategoria);

        binder.forField(metricas)
                .asRequired(traduccionService.get("prueba.estandar.error.metricas_requeridas"))
                .bind(PruebaEstandar::getMetricas, PruebaEstandar::setMetricas);

        binder.forField(objetivoPersonalizado)
                .bind(PruebaEstandar::getObjetivoPersonalizado, PruebaEstandar::setObjetivoPersonalizado);

        binder.forField(descripcionPersonalizada)
                .bind(PruebaEstandar::getDescripcionPersonalizada, PruebaEstandar::setDescripcionPersonalizada);

        return binder;
    }
}