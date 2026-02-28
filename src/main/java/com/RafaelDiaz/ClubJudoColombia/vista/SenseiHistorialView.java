package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.SesionEjecutada;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionEjecutadaService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "historial", layout = SenseiLayout.class)
@PageTitle("Bitácora de Clases | Club Judo Colombia")
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiHistorialView extends VerticalLayout {

    private final SesionEjecutadaService sesionService;
    private final Sensei senseiActual;

    public SenseiHistorialView(SesionEjecutadaService sesionService, SecurityService securityService) {
        this.sesionService = sesionService;
        this.senseiActual = securityService.getAuthenticatedSensei().orElseThrow();

        setSizeFull();
        setSpacing(true);

        HorizontalLayout cabecera = new HorizontalLayout(new Icon(VaadinIcon.ARCHIVE), new H2("Bitácora de Clases"));
        cabecera.setAlignItems(Alignment.CENTER);
        add(cabecera);

        cargarHistorial();
    }

    private void cargarHistorial() {
        try {
            List<SesionEjecutada> sesiones = sesionService.obtenerHistorialDelSensei(senseiActual);

            if (sesiones == null || sesiones.isEmpty()) {
                add(new Span("Aún no has registrado ninguna clase en el Tatami."));
                return;
            }

            Accordion acordeon = new Accordion();
            acordeon.setWidthFull();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");
        for (SesionEjecutada sesion : sesiones) {
            VerticalLayout contenido = new VerticalLayout();
            contenido.setPadding(false);
            contenido.setSpacing(true);

            // 1. Mostrar la Fase R (Notas del Sensei)
            Span tituloNotas = new Span("Fase R (Observaciones):");
            tituloNotas.getStyle().set("font-weight", "bold");

            Span notas = new Span(sesion.getNotasRetroalimentacion() != null && !sesion.getNotasRetroalimentacion().isEmpty()
                    ? sesion.getNotasRetroalimentacion()
                    : "Sin observaciones registradas.");
            notas.getStyle().set("font-style", "italic");
            notas.getStyle().set("color", "var(--lumo-secondary-text-color)");

            // 2. Resumen de Asistencia
            long presentes = sesion.getListaAsistencia().stream()
                    .filter(a -> a.getEstado() == EstadoAsistencia.PRESENTE)
                    .count();
            int total = sesion.getListaAsistencia().size();
            Span txtAsistencia = new Span(String.format("Asistencia: %d de %d judokas", presentes, total));
            txtAsistencia.getStyle().set("font-weight", "bold");
            txtAsistencia.getStyle().set("margin-top", "10px");

            // 3. Lista de ausentes (gamificación inversa para alertas)
            VerticalLayout listaAusentes = new VerticalLayout();
            listaAusentes.setPadding(false);
            listaAusentes.setSpacing(false);

            for (Asistencia a : sesion.getListaAsistencia()) {
                if (a.getEstado() == EstadoAsistencia.AUSENTE) {
                    Span falta = new Span("❌ Faltó: " + a.getJudoka().getNombre());
                    falta.getStyle().set("color", "var(--lumo-error-text-color)");
                    falta.getStyle().set("font-size", "var(--lumo-font-size-s)");
                    listaAusentes.add(falta);
                }
            }

            contenido.add(tituloNotas, notas, txtAsistencia, listaAusentes);

            // Título del panel colapsable
            String tituloPanel = sesion.getFechaHoraEjecucion().format(formatter) +
                    " | Grupo: " + sesion.getGrupo().getNombre();

            acordeon.add(tituloPanel, contenido);
        }
            add(acordeon);

        } catch (Exception e) {
            // EL ESCUDO: Si la tabla no existe o la BD está vacía, no explota la UI.
            Span error = new Span("Aún no hay datos de historial disponibles. Ve al Modo Tatami y finaliza tu primera sesión.");
            error.getStyle().set("color", "var(--lumo-secondary-text-color)");
            error.getStyle().set("font-style", "italic");
            add(error);
        }
    }
}