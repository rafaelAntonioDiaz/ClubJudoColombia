package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaInsignia;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- IMPORTANTE
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MiDoWidget extends VerticalLayout {

    private final TraduccionService traduccionService; // <--- CAMPO NUEVO

    // CONSTRUCTOR ACTUALIZADO: Recibe el servicio de traducción
    public MiDoWidget(List<Insignia> todasLasInsignias,
                      List<JudokaInsignia> misLogros,
                      TraduccionService traduccionService) {
        this.traduccionService = traduccionService;

        addClassName("card-blanca");
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        // Cabecera
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        Icon doIcon = VaadinIcon.ADJUST.create();
        doIcon.setColor("var(--judo-navy)");

        // Traducción del título del widget
        header.add(doIcon, new H3(traduccionService.get("widget.mido.titulo")));
        add(header);

        FlexLayout viasLayout = new FlexLayout();
        viasLayout.setWidthFull();
        viasLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        viasLayout.getStyle().set("gap", "20px");

        Map<Long, JudokaInsignia> mapaLogros = misLogros.stream()
                .collect(Collectors.toMap(
                        l -> l.getInsignia().getIdInsignia(),
                        l -> l,
                        (a, b) -> a
                ));

        // Pasamos las claves de traducción para las columnas
        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.shin"),
                CategoriaInsignia.SHIN, "#E57373", todasLasInsignias, mapaLogros));

        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.gi"),
                CategoriaInsignia.GI, "#90A4AE", todasLasInsignias, mapaLogros));

        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.tai"),
                CategoriaInsignia.TAI, "#4FC3F7", todasLasInsignias, mapaLogros));

        add(viasLayout);
    }

    // ... (crearColumnaVia sigue igual, solo recibe el título ya traducido) ...
    private VerticalLayout crearColumnaVia(String titulo, CategoriaInsignia cat, String color,
                                           List<Insignia> todas, Map<Long, JudokaInsignia> mapaLogros) {
        // ... código de layout igual ...
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(true);
        col.setMinWidth("250px");
        col.setFlexGrow(1);

        Span titleSpan = new Span(titulo);
        titleSpan.getStyle().set("font-weight", "bold").set("color", color).set("border-bottom", "2px solid " + color).set("padding-bottom", "5px").set("display", "inline-block").set("width", "100%");
        col.add(titleSpan);

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getStyle().set("gap", "10px");

        List<Insignia> insigniasDeCategoria = todas.stream()
                .filter(i -> i.getCategoria() == cat)
                .collect(Collectors.toList());

        for (Insignia ins : insigniasDeCategoria) {
            JudokaInsignia logroObtenido = mapaLogros.get(ins.getIdInsignia()); // Asumiendo getIdInsignia()
            grid.add(crearMedallaInteractiva(ins, logroObtenido, color));
        }
        col.add(grid);
        return col;
    }

    private Div crearMedallaInteractiva(Insignia ins, JudokaInsignia logro, String colorTema) {
        boolean desbloqueada = (logro != null);
        Div medalla = new Div();
        // ... (Estilos visuales iguales) ...
        medalla.getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "center").set("width", "45px").set("height", "45px").set("border-radius", "50%").set("background-color", desbloqueada ? colorTema : "#f5f5f5").set("color", desbloqueada ? "white" : "#bdbdbd").set("cursor", "pointer").set("transition", "all 0.3s ease").set("box-shadow", desbloqueada ? "0 4px 6px rgba(0,0,0,0.1)" : "none");

        // --- TRADUCCIÓN DEL NOMBRE ---
        // Clave: badge.[clave_insignia].nombre -> ej: badge.shin_inicio.nombre
        String claveNombre = "badge." + ins.getClave().toLowerCase() + ".nombre";
        String nombreTraducido = traduccionService.get(claveNombre);

        // Si la traducción falla y devuelve la clave formateada, usamos el nombre de la BD como fallback
        if (nombreTraducido.contains(claveNombre)) nombreTraducido = ins.getNombre();

        medalla.setTitle(nombreTraducido);

        Icon icon;
        try { icon = VaadinIcon.valueOf(ins.getIconoVaadin()).create(); } catch (Exception e) { icon = VaadinIcon.STAR.create(); }
        icon.setSize("24px");
        medalla.add(icon);

        medalla.addClickListener(e -> mostrarDetalleInsignia(ins, logro, colorTema));
        return medalla;
    }

    private void mostrarDetalleInsignia(Insignia ins, JudokaInsignia logro, String colorTema) {
        boolean desbloqueada = (logro != null);
        Dialog dialog = new Dialog();

        // Claves i18n dinámicas
        String claveNombre = "badge." + ins.getClave().toLowerCase() + ".nombre";
        String claveDesc = "badge." + ins.getClave().toLowerCase() + ".desc";

        String nombre = traduccionService.get(claveNombre);
        String descripcion = traduccionService.get(claveDesc);

        // Título del diálogo
        dialog.setHeaderTitle(desbloqueada ?
                traduccionService.get("badge.estado.desbloqueada") :
                traduccionService.get("badge.estado.bloqueada"));

        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(true);

        Icon bigIcon;
        try { bigIcon = VaadinIcon.valueOf(ins.getIconoVaadin()).create(); } catch (Exception e) { bigIcon = VaadinIcon.STAR.create(); }
        bigIcon.setSize("60px");
        bigIcon.setColor(desbloqueada ? colorTema : "#e0e0e0");
        content.add(bigIcon);

        H3 tituloH3 = new H3(nombre);
        tituloH3.getStyle().set("margin", "0").set("color", "var(--judo-navy)");
        content.add(tituloH3);

        Paragraph descP = new Paragraph(descripcion);
        descP.getStyle().set("text-align", "center").set("font-style", "italic");
        content.add(descP);

        if (desbloqueada) {
            Span estado = new Span(traduccionService.get("badge.label.obtenida") + ": " +
                    logro.getFechaObtencion().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            estado.getElement().getThemeList().add("badge success");
            content.add(estado);
        } else {
            Span estado = new Span(traduccionService.get("badge.label.pendiente"));
            estado.getElement().getThemeList().add("badge contrast");
            content.add(estado);
        }

        Button cerrar = new Button(traduccionService.get("btn.cerrar"), e -> dialog.close());
        cerrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (!desbloqueada) cerrar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        dialog.add(content);
        dialog.getFooter().add(cerrar);
        dialog.open();
    }
}