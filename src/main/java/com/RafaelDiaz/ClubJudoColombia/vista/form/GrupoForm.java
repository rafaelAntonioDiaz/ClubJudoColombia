package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Formulario para crear o editar un GrupoEntrenamiento.
 * @author RafaelDiaz
 * @version 2.1 (Simplificado)
 */
public class GrupoForm extends BaseForm<GrupoEntrenamiento> {

    private final TextField nombre = new TextField("Nombre del Grupo");
    private final TextArea descripcion = new TextArea("Descripción");

    public GrupoForm() {
        configureFields();
        addFields();
    }

    private void configureFields() {
        nombre.setRequired(true);
        nombre.setRequiredIndicatorVisible(true);
        nombre.setPlaceholder("Ej: Equipo Masculino Sub-13");
        nombre.setMaxLength(150);

        descripcion.setPlaceholder("Descripción del grupo...");
        descripcion.setMaxLength(500);
        descripcion.setHeight("150px");
    }

    private void addFields() {
        add(nombre, descripcion);
    }

    @Override
    protected Binder<GrupoEntrenamiento> createBinder() {
        Binder<GrupoEntrenamiento> binder = new Binder<>(GrupoEntrenamiento.class);

        binder.forField(nombre)
                .asRequired("El nombre del grupo es obligatorio")
                .bind(GrupoEntrenamiento::getNombre, GrupoEntrenamiento::setNombre);

        binder.forField(descripcion)
                .bind(GrupoEntrenamiento::getDescripcion, GrupoEntrenamiento::setDescripcion);

        return binder;
    }

    // ✅ ELIMINADO: SaveEvent, CancelEvent, addSaveListener, addCancelListener
}