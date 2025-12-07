package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Insignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.JudokaInsignia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaInsignia;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MiDoWidget extends VerticalLayout {

    private final TraduccionService traduccionService;
    private final List<Insignia> todasLasInsignias;
    private final List<JudokaInsignia> misLogros;
    private final Map<Long, JudokaInsignia> mapaLogros;

    public MiDoWidget(List<Insignia> todasLasInsignias,
                      List<JudokaInsignia> misLogros,
                      TraduccionService traduccionService) {
        this.todasLasInsignias = todasLasInsignias;
        this.misLogros = misLogros;
        this.traduccionService = traduccionService;

        // Mapeo rápido ID -> Logro
        this.mapaLogros = misLogros.stream()
                .collect(Collectors.toMap(
                        l -> l.getInsignia().getIdInsignia(),
                        l -> l,
                        (a, b) -> a
                ));

        addClassName("card-blanca");
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        refresh();
    }

    public void refresh() {
        removeAll();

        // --- CABECERA (Título + Botón Catálogo) ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout tituloLayout = new HorizontalLayout(
                new Icon(VaadinIcon.ADJUST), // Icono Do
                new H3(traduccionService.get("widget.mido.titulo"))
        );
        tituloLayout.setAlignItems(Alignment.CENTER);

        // Botón "Ver Todo" para generar deseo
        Button btnCatalogo = new Button(traduccionService.get("widget.mido.btn_catalogo"), new Icon(VaadinIcon.TROPHY));
        btnCatalogo.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        btnCatalogo.addClickListener(e -> abrirCatalogoCompleto());

        header.add(tituloLayout, btnCatalogo);
        add(header);

        // --- CUERPO DEL WIDGET (Lógica de Visualización) ---
        FlexLayout viasLayout = new FlexLayout();
        viasLayout.setWidthFull();
        viasLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        viasLayout.getStyle().set("gap", "20px");

        // 1. Filtrar qué insignias mostramos en el Dashboard
        List<Insignia> insigniasAVisualizar;
        boolean mostrarEnGris = false;

        if (misLogros.isEmpty()) {
            // CASO A: Novato (Sin logros) -> Mostramos las de Nivel 1 en GRIS
            // "Mira lo que puedes ganar pronto"
            insigniasAVisualizar = todasLasInsignias.stream()
                    .filter(i -> i.getNivelRequerido() != null && i.getNivelRequerido() == 1)
                    .collect(Collectors.toList());
            mostrarEnGris = true;

            // Mensaje motivacional
            Span mensaje = new Span(traduccionService.get("widget.mido.msg_inicio"));
            mensaje.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic").set("width", "100%");
            add(mensaje);

        } else {
            // CASO B: Veterano -> Mostramos SOLO lo que tiene (Orgullo)
            Set<Long> idsGanados = mapaLogros.keySet();
            insigniasAVisualizar = todasLasInsignias.stream()
                    .filter(i -> idsGanados.contains(i.getIdInsignia()))
                    .collect(Collectors.toList());
        }

        // 2. Construir las Columnas (Shin-Gi-Tai) con el subconjunto filtrado
        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.shin"),
                CategoriaInsignia.SHIN, "#E57373", insigniasAVisualizar, mostrarEnGris));

        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.gi"),
                CategoriaInsignia.GI, "#90A4AE", insigniasAVisualizar, mostrarEnGris));

        viasLayout.add(crearColumnaVia(traduccionService.get("widget.mido.tai"),
                CategoriaInsignia.TAI, "#4FC3F7", insigniasAVisualizar, mostrarEnGris));

        add(viasLayout);
    }

    /**
     * Abre un diálogo modal con TODAS las insignias (Bloqueadas y Desbloqueadas)
     * "El Salón de la Fama"
     */
    private void abrirCatalogoCompleto() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(traduccionService.get("widget.mido.catalogo_titulo"));
        dialog.setWidth("800px");
        dialog.setMaxWidth("90vw");

        FlexLayout layoutCompleto = new FlexLayout();
        layoutCompleto.setWidthFull();
        layoutCompleto.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layoutCompleto.getStyle().set("gap", "20px");

        // Mostramos TODAS, las que no tiene saldrán bloqueadas automáticamente
        layoutCompleto.add(crearColumnaVia(traduccionService.get("widget.mido.shin"), CategoriaInsignia.SHIN, "#E57373", todasLasInsignias, false));
        layoutCompleto.add(crearColumnaVia(traduccionService.get("widget.mido.gi"), CategoriaInsignia.GI, "#90A4AE", todasLasInsignias, false));
        layoutCompleto.add(crearColumnaVia(traduccionService.get("widget.mido.tai"), CategoriaInsignia.TAI, "#4FC3F7", todasLasInsignias, false));

        Button cerrar = new Button(traduccionService.get("btn.cerrar"), e -> dialog.close());
        dialog.add(layoutCompleto);
        dialog.getFooter().add(cerrar);
        dialog.open();
    }

    private VerticalLayout crearColumnaVia(String titulo, CategoriaInsignia cat, String color,
                                           List<Insignia> listaBase, boolean forzarGris) {
        VerticalLayout col = new VerticalLayout();
        col.setPadding(false);
        col.setSpacing(true);
        col.setMinWidth("200px");
        col.setFlexGrow(1);

        Span titleSpan = new Span(titulo);
        titleSpan.getStyle().set("font-weight", "bold").set("color", color)
                .set("border-bottom", "2px solid " + color).set("width", "100%");
        col.add(titleSpan);

        FlexLayout grid = new FlexLayout();
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        grid.getStyle().set("gap", "10px");

        List<Insignia> insigniasDeCategoria = listaBase.stream()
                .filter(i -> i.getCategoria() == cat)
                .collect(Collectors.toList());

        if (insigniasDeCategoria.isEmpty()) {
            Span empty = new Span("-");
            empty.addClassName("text-muted");
            grid.add(empty);
        }

        for (Insignia ins : insigniasDeCategoria) {
            // Si forzamos gris (novato), le pasamos null como logro
            // Si no, buscamos si lo tiene en el mapa
            JudokaInsignia logro = forzarGris ? null : mapaLogros.get(ins.getIdInsignia());
            grid.add(crearMedallaInteractiva(ins, logro, color));
        }
        col.add(grid);
        return col;
    }

    // ... (crearMedallaInteractiva y mostrarDetalleInsignia SON IGUALES AL ANTERIOR) ...
    // Solo copia esos dos métodos privados del código previo, no han cambiado.

    private Div crearMedallaInteractiva(Insignia ins, JudokaInsignia logro, String colorTema) {
        boolean desbloqueada = (logro != null);
        Div medalla = new Div();
        medalla.getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "center").set("width", "45px").set("height", "45px").set("border-radius", "50%").set("background-color", desbloqueada ? colorTema : "#f5f5f5").set("color", desbloqueada ? "white" : "#bdbdbd").set("cursor", "pointer").set("transition", "all 0.3s ease").set("box-shadow", desbloqueada ? "0 4px 6px rgba(0,0,0,0.1)" : "none");

        String claveNombre = "badge." + ins.getClave().toLowerCase() + ".nombre";
        String nombreTraducido = traduccionService.get(claveNombre);
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

        String claveNombre = "badge." + ins.getClave().toLowerCase() + ".nombre";
        String claveDesc = "badge." + ins.getClave().toLowerCase() + ".desc";

        String nombre = traduccionService.get(claveNombre);
        String descripcion = traduccionService.get(claveDesc);

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