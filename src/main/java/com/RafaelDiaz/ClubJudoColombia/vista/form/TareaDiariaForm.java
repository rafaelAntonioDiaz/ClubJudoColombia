package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.TareaDiaria;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * --- FORMULARIO REFACTORIZADO CON BaseForm ---
 * Formulario para crear o editar una TareaDiaria (Acondicionamiento).
 * Hereda botones, validación y eventos estandarizados.
 *
 * <p><b>Cambios clave:</b>
 * <ul>
 *   <li>Extiende BaseForm&lt;TareaDiaria&gt; (no FormLayout)</li>
 *   <li>Elimina botones save/cancel (heredados)</li>
 *   <li>Usa setBean() en lugar de setTarea()</li>
 *   <li>Implementa createBinder() para lógica de binding específica</li>
 *   <li>Eventos: SaveEvent/CancelEvent heredados de BaseForm</li>
 * </ul>
 *
 * @author RafaelDiaz
 * @version 3.0 (Refactorizado)
 * @since 2025-11-19
 */
public class TareaDiariaForm extends BaseForm<TareaDiaria> {

    // --- Campos específicos de TareaDiaria ---
    private final TextField nombre = new TextField("Nombre de la Tarea");
    private final TextField metaTexto = new TextField("Meta (ej. 4x15 reps, 30 min)");
    private final TextArea descripcion = new TextArea("Descripción/Procedimiento");
    private final TextField videoUrl = new TextField("URL del Video (YouTube)");

    /**
     * Constructor. Configura campos y layout.
     */
    public TareaDiariaForm() {
        super(); // Llama al constructor de BaseForm (configura botones)
        configureFields();
        addFieldsToForm();
    }


    /**
     * Configura validación y propiedades de los campos.
     */
    private void configureFields() {
        nombre.setRequired(true);
        nombre.setRequiredIndicatorVisible(true);
        nombre.setMaxLength(150);

        metaTexto.setRequired(true);
        metaTexto.setRequiredIndicatorVisible(true);
        metaTexto.setMaxLength(100);
        metaTexto.setPlaceholder("Ej: 4 series de 15 repeticiones");

        descripcion.setMaxLength(500);
        descripcion.setHeight("150px");
        descripcion.setPlaceholder("Descripción detallada del ejercicio...");

        videoUrl.setMaxLength(300);
        videoUrl.setPlaceholder("https://youtube.com/...");
    }

    /**
     * Añade los campos al layout del formulario.
     * Los botones se agregan automáticamente desde BaseForm.
     */
    private void addFieldsToForm() {
        // Los campos se añaden ANTES del layout de botones (que ya está en BaseForm)
        add(nombre, metaTexto, descripcion, videoUrl);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Crea y configura el Binder específico para TareaDiaria.
     * Define las reglas de validación y mapeo de campos.
     */
    @Override
    protected Binder<TareaDiaria> createBinder() {
        Binder<TareaDiaria> binder = new Binder<>(TareaDiaria.class);

        // Binding con validación
        binder.forField(nombre)
                .asRequired("El nombre de la tarea es obligatorio")
                .bind(TareaDiaria::getNombre, TareaDiaria::setNombre);

        binder.forField(metaTexto)
                .asRequired("La meta es obligatoria (ej. 4x15 reps)")
                .bind(TareaDiaria::getMetaTexto, TareaDiaria::setMetaTexto);

        binder.forField(descripcion)
                .bind(TareaDiaria::getDescripcion, TareaDiaria::setDescripcion);

        binder.forField(videoUrl)
                .bind(TareaDiaria::getVideoUrl, TareaDiaria::setVideoUrl);

        return binder;
    }
}