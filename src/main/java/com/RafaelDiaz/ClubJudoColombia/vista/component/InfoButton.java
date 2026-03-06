package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class InfoButton extends Button {

    public InfoButton(String titulo, String contenido) {
        super(new Icon(VaadinIcon.INFO_CIRCLE));
        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        getElement().setAttribute("aria-label", "Más información");

        System.out.println("InfoButton - Título: " + titulo);
        System.out.println("InfoButton - Contenido: " + contenido);

        addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(titulo);
            // Usar Paragraph para mejor formato
            Paragraph texto = new Paragraph(contenido);
            texto.getStyle().set("max-width", "400px").set("white-space", "pre-line");
            dialog.add(texto);
            dialog.getFooter().add(new Button("Cerrar", ev -> dialog.close()));
            dialog.open();
        });
    }
}