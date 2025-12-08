package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.ParticipacionCompetencia;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaDashboardService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PalmaresDialog extends Dialog {

    public PalmaresDialog(JudokaDashboardService service,
                          TraduccionService traduccionService,
                          Judoka judoka) {

        setHeaderTitle("Palmarés Deportivo"); // i18n pendiente
        setWidth("700px");
        setMaxWidth("95vw");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // 1. BARRA DE ASCENSO A CINTURÓN NEGRO
        int puntosActuales = service.calcularPuntosAscenso(judoka);
        int metaPuntos = 500; // Ejemplo: 500 puntos para Shodan
        double progreso = (double) puntosActuales / metaPuntos;

        Div progressContainer = new Div();
        progressContainer.setWidthFull();
        progressContainer.addClassName("card-blanca");
        progressContainer.getStyle().set("padding", "15px").set("background", "#2C3E50").set("color", "white");

        H4 tituloBarra = new H4("Camino al Cinturón Negro (Shodan)");
        tituloBarra.getStyle().set("margin", "0 0 10px 0").set("color", "#ECF0F1");

        ProgressBar bar = new ProgressBar();
        bar.setValue(progreso > 1.0 ? 1.0 : progreso);
        bar.getStyle().set("--lumo-primary-color", "#F1C40F"); // Barra dorada

        Span lblPuntos = new Span(puntosActuales + " / " + metaPuntos + " pts");
        lblPuntos.getStyle().set("font-size", "0.9rem").set("float", "right");

        progressContainer.add(tituloBarra, bar, lblPuntos);
        content.add(progressContainer);

        // 2. TIMELINE DE COMPETENCIAS
        List<ParticipacionCompetencia> historial = service.getPalmares(judoka);

        if (historial.isEmpty()) {
            content.add(new Paragraph("Aún no hay registros de competencias."));
        } else {
            VerticalLayout timeline = new VerticalLayout();
            timeline.setPadding(false);
            timeline.setSpacing(true);

            for (ParticipacionCompetencia item : historial) {
                timeline.add(crearTarjetaCompetencia(item));
            }
            content.add(timeline);
        }

        add(content);

        Button cerrar = new Button("Cerrar", e -> this.close());
        getFooter().add(cerrar);
    }

    private Div crearTarjetaCompetencia(ParticipacionCompetencia item) {
        Div card = new Div();
        card.addClassName("card-blanca");
        card.setWidthFull();
        card.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("padding", "15px")
                .set("gap", "15px")
                .set("border-left", "5px solid " + item.getResultado().getColorHex());

        // A. FECHA (Izquierda)
        VerticalLayout fechaLayout = new VerticalLayout();
        fechaLayout.setSpacing(false);
        fechaLayout.setPadding(false);
        fechaLayout.setWidth("60px");
        fechaLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span dia = new Span(String.valueOf(item.getFecha().getDayOfMonth()));
        dia.getStyle().set("font-size", "1.5rem").set("font-weight", "bold");
        Span mes = new Span(item.getFecha().format(DateTimeFormatter.ofPattern("MMM")).toUpperCase());
        mes.getStyle().set("font-size", "0.8rem").set("color", "gray");
        fechaLayout.add(dia, mes);

        // B. INFO CENTRAL
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoLayout.setFlexGrow(1, infoLayout); // Ocupa el espacio disponible

        H4 titulo = new H4(item.getNombreCampeonato());
        titulo.getStyle().set("margin", "0");
        Span subtitulo = new Span(item.getNivel().getNombre() + " • " + item.getSede());
        subtitulo.getStyle().set("font-size", "0.9rem").set("color", "gray");
        infoLayout.add(titulo, subtitulo);

        // C. MEDALLA Y PUNTOS
        VerticalLayout resultadoLayout = new VerticalLayout();
        resultadoLayout.setSpacing(false);
        resultadoLayout.setPadding(false);
        resultadoLayout.setAlignItems(FlexComponent.Alignment.END);

        Icon medalIcon = VaadinIcon.MEDAL.create();
        medalIcon.setColor(item.getResultado().getColorHex());
        medalIcon.setSize("24px");

        Span puntos = new Span("+" + item.getPuntosCalculados() + " pts");
        puntos.getStyle().set("font-weight", "bold").set("color", "#27AE60"); // Verde éxito

        resultadoLayout.add(medalIcon, puntos);

        // D. BOTÓN DE VIDEO (Si existe)
        Button btnVideo = null;
        if (item.getUrlVideo() != null && !item.getUrlVideo().isEmpty()) {
            btnVideo = new Button(new Icon(VaadinIcon.YOUTUBE));
            btnVideo.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL); // Rojo YouTube
            btnVideo.setTooltipText("Ver Combate");

            String url = item.getUrlVideo();
            btnVideo.addClickListener(e -> UI.getCurrent().getPage().open(url, "_blank"));
        }

        // Armar tarjeta
        card.add(fechaLayout, infoLayout, resultadoLayout);
        if (btnVideo != null) card.add(btnVideo);

        return card;
    }
}