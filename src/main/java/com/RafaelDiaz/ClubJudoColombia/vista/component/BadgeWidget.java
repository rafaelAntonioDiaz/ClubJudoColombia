package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BadgeWidget extends VerticalLayout {

    public BadgeWidget() {
        addClassName("card-blanca"); // Usamos tu estilo de tarjeta existente
        setPadding(true);
        setSpacing(false);

        Span titulo = new Span("Mis Logros");
        titulo.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.1rem")
                .set("color", "var(--judo-navy)");
        add(titulo);

        FlexLayout badgesContainer = new FlexLayout();
        badgesContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        // --- CORRECCIÓN: Usar API de Estilos para el GAP ---
        badgesContainer.getStyle().set("gap", "15px");
        badgesContainer.getStyle().set("margin-top", "15px");

        // Simulamos lógica de desbloqueo (esto vendría de tu servicio)
        // Nota: Cambié SWORD por TROPHY porque SWORD no existe en VaadinIcon estándar
        badgesContainer.add(crearBadge("Guerrero", VaadinIcon.TROPHY, true, "Completaste 5 combates"));
        badgesContainer.add(crearBadge("Constante", VaadinIcon.CALENDAR_CLOCK, true, "Asistencia perfecta este mes"));
        badgesContainer.add(crearBadge("Velocista", VaadinIcon.STOPWATCH, false, "Corre 20m en menos de 3s (Bloqueado)"));
        badgesContainer.add(crearBadge("Samurái", VaadinIcon.DIAMOND, false, "Obtén Cinturón Negro (Bloqueado)"));

        add(badgesContainer);
    }

    private Div crearBadge(String nombre, VaadinIcon icono, boolean desbloqueado, String descripcion) {
        Div badge = new Div();
        badge.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("width", "80px")
                .set("text-align", "center")
                .set("cursor", "help"); // Cursor de ayuda para indicar que hay tooltip

        // Tooltip nativo
        badge.setTitle(descripcion);

        Icon icon = icono.create();
        icon.setSize("35px");

        if (desbloqueado) {
            icon.setColor("var(--judo-coral)"); // Dorado/Coral
            // Efecto de brillo CSS
            icon.getStyle().set("filter", "drop-shadow(0 0 5px rgba(255, 107, 107, 0.4))");
        } else {
            icon.setColor("#e0e0e0"); // Gris apagado
            icon.getStyle().set("filter", "grayscale(100%)");
        }

        Span text = new Span(nombre);
        text.getStyle()
                .set("font-size", "0.75rem")
                .set("margin-top", "5px")
                .set("color", desbloqueado ? "var(--judo-navy)" : "#999")
                .set("font-weight", desbloqueado ? "600" : "400");

        badge.add(icon, text);
        return badge;
    }
}