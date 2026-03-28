package com.RafaelDiaz.ClubJudoColombia.vista.form;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.math.BigDecimal;

public class TarifaForm extends BaseForm<GrupoEntrenamiento> {

    private final TextField nombre = new TextField();
    private final TextArea descripcion = new TextArea();
    private final NumberField tarifaMensual = new NumberField();
    private final NumberField comisionSensei = new NumberField();
    private final Checkbox incluyeMatricula = new Checkbox();
    private final NumberField montoMatricula = new NumberField();
    private final NumberField diasGracia = new NumberField();

    public TarifaForm(TraduccionService traduccionService) {
        super(traduccionService);
        configurarCampos(traduccionService);
        construirLayout();
    }

    private void configurarCampos(TraduccionService traduccionService) {
        nombre.setLabel(traduccionService.get("grupos.form.nombre"));
        nombre.setRequired(true);
        descripcion.setLabel(traduccionService.get("grupos.form.descripcion"));
        tarifaMensual.setLabel(traduccionService.get("grupos.form.tarifa_mensual"));
        tarifaMensual.setRequired(true);
        tarifaMensual.setStep(1000.0);
        tarifaMensual.setMin(0.0);

        comisionSensei.setLabel(traduccionService.get("grupos.form.comision_sensei"));
        comisionSensei.setReadOnly(true);
        comisionSensei.setEnabled(false);
        comisionSensei.setValue(0.0);

        incluyeMatricula.setLabel(traduccionService.get("grupos.form.incluye_matricula"));
        montoMatricula.setLabel(traduccionService.get("grupos.form.monto_matricula"));
        montoMatricula.setStep(1000.0);
        montoMatricula.setMin(0.0);
        montoMatricula.setEnabled(false);

        diasGracia.setLabel(traduccionService.get("grupos.form.dias_gracia"));
        diasGracia.setRequired(true);
        diasGracia.setMin(0);
        diasGracia.setStep(1.0);
        diasGracia.setValue(5.0);

        incluyeMatricula.addValueChangeListener(e -> montoMatricula.setEnabled(e.getValue()));
    }

    private void construirLayout() {
        // Agregamos todos los campos
        add(nombre, descripcion, tarifaMensual, comisionSensei,
                incluyeMatricula, montoMatricula, diasGracia);
        // Aseguramos que los botones estén al final
        add(buttonLayout);
    }

    @Override
    protected Binder<GrupoEntrenamiento> createBinder() {
        Binder<GrupoEntrenamiento> binder = new Binder<>(GrupoEntrenamiento.class);

        binder.forField(nombre).asRequired().bind(GrupoEntrenamiento::getNombre, GrupoEntrenamiento::setNombre);
        binder.forField(descripcion).bind(GrupoEntrenamiento::getDescripcion, GrupoEntrenamiento::setDescripcion);

        // tarifaMensual: de Double a BigDecimal, y de BigDecimal a Double con null->0.0
        binder.forField(tarifaMensual)
                .withConverter(
                        d -> BigDecimal.valueOf(d),                      // Double -> BigDecimal
                        bd -> bd != null ? bd.doubleValue() : 0.0       // BigDecimal -> Double
                )
                .asRequired()
                .bind(GrupoEntrenamiento::getTarifaMensual, GrupoEntrenamiento::setTarifaMensual);

        // comisionSensei: mismo tratamiento
        binder.forField(comisionSensei)
                .withConverter(
                        d -> BigDecimal.valueOf(d),
                        bd -> bd != null ? bd.doubleValue() : 0.0
                )
                .bind(GrupoEntrenamiento::getComisionSensei, GrupoEntrenamiento::setComisionSensei);

        binder.forField(incluyeMatricula).bind(GrupoEntrenamiento::isIncluyeMatricula, GrupoEntrenamiento::setIncluyeMatricula);

        // montoMatricula: mismo tratamiento (puede ser null en BD, pero lo manejamos)
        binder.forField(montoMatricula)
                .withConverter(
                        d -> BigDecimal.valueOf(d),
                        bd -> bd != null ? bd.doubleValue() : 0.0
                )
                .bind(GrupoEntrenamiento::getMontoMatricula, GrupoEntrenamiento::setMontoMatricula);

        // diasGracia: int primitivo, no null
        binder.forField(diasGracia)
                .withConverter(Double::intValue, Integer::doubleValue)
                .asRequired()
                .bind(GrupoEntrenamiento::getDiasGracia, GrupoEntrenamiento::setDiasGracia);

        return binder;
    }

    @Override
    public void setBean(GrupoEntrenamiento bean) {
        super.setBean(bean);
        if (bean != null && bean.getComisionSensei() != null) {
            comisionSensei.setValue(bean.getComisionSensei().doubleValue());
        } else {
            comisionSensei.setValue(0.0);
        }
        montoMatricula.setEnabled(bean != null && bean.isIncluyeMatricula());
    }
}