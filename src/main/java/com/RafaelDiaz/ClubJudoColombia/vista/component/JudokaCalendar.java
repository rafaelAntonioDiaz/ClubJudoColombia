package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionProgramada;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionService.EventoCalendario;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.util.FestivosColombia;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI; // Importante para obtener Locale del usuario

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@CssImport("./styles/calendar-judoka.css")
public class JudokaCalendar extends Div {

    private final SesionService sesionService;
    private final TraduccionService traduccionService;
    private final Judoka judoka;

    private YearMonth currentMonth;
    private Div calendarGrid;
    private Span monthTitle;

    public JudokaCalendar(SesionService sesionService, TraduccionService traduccionService, Judoka judoka) {
        this.sesionService = sesionService;
        this.traduccionService = traduccionService;
        this.judoka = judoka;
        this.currentMonth = YearMonth.now();

        addClassName("judoka-calendar-container");
        buildHeader();
        buildCalendarGrid();
        refreshCalendar();
    }

    private void buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("calendar-header");
        header.setWidthFull();

        Button prevBtn = new Button(new Icon(VaadinIcon.CHEVRON_LEFT), e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        monthTitle = new Span();
        monthTitle.addClassName("calendar-month-title");

        Button nextBtn = new Button(new Icon(VaadinIcon.CHEVRON_RIGHT), e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
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

    private void refreshCalendar() {
        calendarGrid.removeAll();

        // 1. OBTENER IDIOMA DEL USUARIO (Corregido para Java 21)
        Locale userLocale = UI.getCurrent().getLocale();

        // --- CORRECCIÓN AQUÍ ---
        // Si no hay locale (ej. background thread), usamos el de Colombia por defecto.
        // En lugar de new Locale("es", "CO"), usamos Locale.of("es", "CO")
        if (userLocale == null) {
            userLocale = Locale.of("es", "CO");
        }

        // Título del Mes
        String mesNombre = currentMonth.getMonth().getDisplayName(TextStyle.FULL, userLocale);
        monthTitle.setText(mesNombre + " " + currentMonth.getYear());

        // Cabeceras (L M X...)
        for (int i = 1; i <= 7; i++) {
            String nombreDia = DayOfWeek.of(i).getDisplayName(TextStyle.NARROW, userLocale);
            Span dayHeader = new Span(nombreDia);
            dayHeader.addClassName("day-name");
            calendarGrid.add(dayHeader);
        }

        // Lógica de días (Igual que antes)
        LocalDate firstDay = currentMonth.atDay(1);
        int emptyCells = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int i = 0; i < emptyCells; i++) {
            calendarGrid.add(new Div());
        }

        List<EventoCalendario> eventos = sesionService.obtenerEventosMes(judoka, currentMonth);

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Div dayCell = new Div();
            dayCell.addClassName("day-cell");
            dayCell.setText(String.valueOf(day));

            // Festivos
            Optional<String> claveFestivo = FestivosColombia.nombreFestivo(date);
            if (claveFestivo.isPresent()) {
                dayCell.addClassName("holiday");
                String nombreFestivo = traduccionService.get(claveFestivo.get());
                dayCell.setTitle(nombreFestivo);

                Span holidayIcon = new Span("★");
                holidayIcon.addClassName("holiday-indicator");
                holidayIcon.setTitle(nombreFestivo);
                dayCell.add(holidayIcon);
            }

            if (date.equals(LocalDate.now())) {
                dayCell.addClassName("today");
            }

            // Eventos
            List<EventoCalendario> eventosDia = eventos.stream()
                    .filter(e -> e.sesion().getFechaHoraInicio().toLocalDate().equals(date))
                    .toList();

            if (!eventosDia.isEmpty()) {
                Div dotsWrapper = new Div();
                dotsWrapper.addClassName("events-wrapper");

                for (EventoCalendario ev : eventosDia) {
                    Span dot = new Span();
                    dot.addClassName("event-dot");
                    dot.addClassName(ev.estado().name().toLowerCase());
                    dot.addClickListener(e -> mostrarDetalleSesion(ev));
                    dot.setTitle(ev.sesion().getNombre());
                    dotsWrapper.add(dot);
                }
                dayCell.add(dotsWrapper);
            }

            calendarGrid.add(dayCell);
        }
    }

    private void mostrarDetalleSesion(EventoCalendario evento) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detalle de Sesión");

        SesionProgramada s = evento.sesion();
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        content.add(new H3(s.getNombre()));

        String nombreSensei = (s.getSensei() != null && s.getSensei().getUsuario() != null)
                ? s.getSensei().getUsuario().getNombre()
                : "Sensei";
        content.add(new Span("Sensei: " + nombreSensei));

        content.add(new Span("Hora: " + s.getFechaHoraInicio().toLocalTime() + " - " + s.getFechaHoraFin().toLocalTime()));

        String estadoTexto = switch (evento.estado()) {
            case FUTURA -> "📅 Programada";
            case ASISTIO -> "✅ Asististe";
            case FALTO -> "❌ No asististe";
        };
        Span badge = new Span(estadoTexto);
        badge.getElement().getThemeList().add("badge " + (evento.estado() == SesionService.EstadoSesion.ASISTIO ? "success" : "contrast"));
        content.add(badge);

        Button cerrar = new Button("Cerrar", e -> dialog.close());
        dialog.add(content);
        dialog.getFooter().add(cerrar);
        dialog.open();
    }
}