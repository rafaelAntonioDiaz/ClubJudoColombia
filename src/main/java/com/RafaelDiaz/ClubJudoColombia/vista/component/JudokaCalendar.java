package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.dto.ItemCalendario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoItem;
import com.RafaelDiaz.ClubJudoColombia.servicio.CalendarioUnificadoService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@CssImport("./styles/calendar-judoka.css")
public class JudokaCalendar extends Div {

    private final TraduccionService traduccionService;
    private YearMonth currentMonth;
    private Div calendarGrid;
    private Span monthTitle;
    private List<ItemCalendario> currentItems;

    public JudokaCalendar(CalendarioUnificadoService calendarService, TraduccionService traduccionService) {
        this.traduccionService = traduccionService;
        this.currentMonth = YearMonth.now();
        this.currentItems = List.of();

        addClassName("judoka-calendar-container");
        buildHeader();
        buildCalendarGrid();
        render();
    }

    private void buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("calendar-header");
        header.setWidthFull();

        Button prevBtn = new Button(new Icon(VaadinIcon.CHEVRON_LEFT), e -> {
            currentMonth = currentMonth.minusMonths(1);
            render();
        });
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        monthTitle = new Span();
        monthTitle.addClassName("calendar-month-title");

        Button nextBtn = new Button(new Icon(VaadinIcon.CHEVRON_RIGHT), e -> {
            currentMonth = currentMonth.plusMonths(1);
            render();
        });
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        header.add(prevBtn, monthTitle, nextBtn);
        add(header);
    }

    private void buildCalendarGrid() {
        calendarGrid = new Div();
        calendarGrid.addClassName("calendar-grid");
        add(calendarGrid);
    }

    /**
     * Método público para actualizar el calendario con un nuevo mes y lista de items.
     */
    public void mostrarMes(YearMonth mes, List<ItemCalendario> items) {
        this.currentMonth = mes;
        this.currentItems = items != null ? items : List.of();
        render();
    }

    private void render() {
        calendarGrid.removeAll();

        Locale userLocale = UI.getCurrent().getLocale();
        if (userLocale == null) {
            userLocale = Locale.of("es", "CO");
        }

        // Título del mes
        String mesNombre = currentMonth.getMonth().getDisplayName(TextStyle.FULL, userLocale);
        monthTitle.setText(mesNombre + " " + currentMonth.getYear());

        // Cabeceras de días
        for (int i = 1; i <= 7; i++) {
            String nombreDia = DayOfWeek.of(i).getDisplayName(TextStyle.NARROW, userLocale);
            Span dayHeader = new Span(nombreDia);
            dayHeader.addClassName("day-name");
            calendarGrid.add(dayHeader);
        }

        // Agrupar items por día
        Map<LocalDate, List<ItemCalendario>> itemsPorDia = currentItems.stream()
                .collect(Collectors.groupingBy(item -> item.getInicio().toLocalDate()));

        LocalDate firstDay = currentMonth.atDay(1);
        int emptyCells = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentMonth.lengthOfMonth();

        // Celdas vacías antes del día 1
        for (int i = 0; i < emptyCells; i++) {
            calendarGrid.add(new Div());
        }

        // Celdas de los días del mes
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Div dayCell = new Div();
            dayCell.addClassName("day-cell");
            dayCell.setText(String.valueOf(day));

            if (date.equals(LocalDate.now())) {
                dayCell.addClassName("today");
            }

            if (itemsPorDia.containsKey(date)) {
                Div indicators = new Div();
                indicators.addClassName("day-indicators");
                for (ItemCalendario item : itemsPorDia.get(date)) {
                    Span dot = new Span();
                    dot.getStyle().set("background-color", "red");
                    String tooltip = item.getTitulo();
                    if (item.getTipo() == TipoItem.SESION_GRUPAL) {
                        tooltip += " - " + item.getInicio().toLocalTime() + " a " + item.getFin().toLocalTime();
                    }
                    if (item.getJudokaNombre() != null) {
                        tooltip += " - " + item.getJudokaNombre();
                    }
                    dot.setTitle(tooltip);
                    dot.addClassName("event-dot");
                    dot.addClassName(item.getTipo().name().toLowerCase());
                    dot.addClassName(item.getEstado().toLowerCase().replace(" ", "-"));
                    dot.setTitle(item.getTitulo() +
                            (item.getJudokaNombre() != null ? " - " + item.getJudokaNombre() : ""));
                    dot.addClickListener(e -> mostrarDetalleItem(item));
                    dot.getStyle().set("background-color", "red");
                    indicators.add(dot);
                    System.out.println(">>> Día " + date + " agregado dot para: " + item.getTitulo());
                }
                dayCell.add(indicators);
            }

            calendarGrid.add(dayCell);
        }
    }

    private void mostrarDetalleItem(ItemCalendario item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(item.getTitulo());

        Div content = new Div();
        content.add(new Span("Tipo: " + item.getTipo()));
        content.add(new Span("Estado: " + item.getEstado()));
        content.add(new Span("Grupo: " + (item.getGrupoNombre() != null ? item.getGrupoNombre() : "-")));
        if (item.getJudokaNombre() != null) {
            content.add(new Span("Judoka: " + item.getJudokaNombre()));
        }
        if (item.getLatitudEsperada() != null) {
            content.add(new Span("Ubicación esperada: " + item.getLatitudEsperada() + ", " + item.getLongitudEsperada()));
        }
        if (item.getLatitudRegistrada() != null) {
            content.add(new Span("Ubicación reportada: " + item.getLatitudRegistrada() + ", " + item.getLongitudRegistrada()));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        content.add(new Span("Inicio: " + item.getInicio().format(formatter)));
        content.add(new Span("Fin: " + item.getFin().format(formatter)));
        Button cerrar = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(cerrar);
        dialog.add(content);
        dialog.open();
    }
}