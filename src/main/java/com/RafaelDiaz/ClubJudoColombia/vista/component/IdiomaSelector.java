package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;

public class IdiomaSelector extends HorizontalLayout {

    public IdiomaSelector() {
        setSpacing(true);
        setPadding(false);
        setAlignItems(Alignment.CENTER);

        Button es = new Button("ES", new Icon(VaadinIcon.FLAG));
        es.addClickListener(e -> cambiarIdioma("es"));

        Button en = new Button("EN", new Icon(VaadinIcon.FLAG));
        en.addClickListener(e -> cambiarIdioma("en"));

        Button pt = new Button("PT", new Icon(VaadinIcon.FLAG));
        pt.addClickListener(e -> cambiarIdioma("pt"));

        add(es, en, pt);
    }

    private void cambiarIdioma(String codigoIdioma) { // Ej: "en", "es", "pt"
        Locale nuevoLocale = new Locale(codigoIdioma);

        // 1. Guardar en la sesión (Sobrevive si el usuario navega a otra ruta)
        VaadinSession.getCurrent().setLocale(nuevoLocale);

        // 2. Guardar en el UI (Aplica el cambio a la vista actual al instante)
        UI.getCurrent().setLocale(nuevoLocale);

        // NOTA: NO uses UI.getCurrent().getPage().reload() aquí.
        // Gracias a LocaleChangeObserver, la vista se actualizará sola sin parpadear.
    }
}