package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Tarjeta de KPI para el dashboard con icono y valor.
 * Componente reutilizable y responsive.
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-20
 */
public class KpiCard extends VerticalLayout {

    public KpiCard(String titulo, Object valor, Icon icono) {
        icono.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.PRIMARY);
        H3 valorLabel = new H3(valor.toString());
        valorLabel.addClassName(LumoUtility.FontSize.XXXLARGE);
        Span tituloLabel = new Span(titulo);
        tituloLabel.addClassName(LumoUtility.FontSize.SMALL);

        add(icono, valorLabel, tituloLabel);
        setAlignItems(Alignment.CENTER);
        addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Background.CONTRAST_5
        );
        setWidth("250px");
    }
}