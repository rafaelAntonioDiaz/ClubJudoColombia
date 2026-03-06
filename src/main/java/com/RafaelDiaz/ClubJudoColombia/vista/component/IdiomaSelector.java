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

    private void cambiarIdioma(String lang) {
        UI.getCurrent().setLocale(new Locale(lang));
        // Opcional: guardar preferencia en sesión
        VaadinSession.getCurrent().setAttribute("locale", new Locale(lang));
        UI.getCurrent().getPage().reload(); // Recarga para aplicar cambios
    }
}