package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Asistencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.AsistenciaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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

@Route(value = "mis-clases", layout = JudokaLayout.class)
@PageTitle("Mi Progreso | Club Judo Colombia")
@RolesAllowed({"ROLE_JUDOKA"})
public class JudokaHistorialView extends VerticalLayout {

    private final AsistenciaRepository asistenciaRepository;
    private final Judoka judokaActual;

    public JudokaHistorialView(AsistenciaRepository asistenciaRepository, SecurityService securityService) {
        this.asistenciaRepository = asistenciaRepository;
        this.judokaActual = securityService.getAuthenticatedJudoka().orElseThrow();

        setSizeFull();
        setSpacing(true);

        HorizontalLayout cabecera = new HorizontalLayout(new Icon(VaadinIcon.ACADEMY_CAP), new H2("Mi Progreso"));
        cabecera.setAlignItems(Alignment.CENTER);
        add(cabecera);

        cargarHistorialPersonal();
    }

    private void cargarHistorialPersonal() {
        try {
            List<Asistencia> misAsistencias = asistenciaRepository.findHistorialByJudokaId(judokaActual.getId());

            if (misAsistencias == null || misAsistencias.isEmpty()) {
                Span vacio = new Span("Aún no tienes registros de clases. ¡Anímate a ir a tu primer entrenamiento!");
                vacio.getStyle().set("color", "var(--lumo-secondary-text-color)");
                add(vacio);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");

            for (Asistencia a : misAsistencias) {
                VerticalLayout card = new VerticalLayout();
                card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
                card.getStyle().set("border-radius", "8px");
                card.getStyle().set("padding", "15px");
                card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)");
                card.setWidthFull();
                card.setSpacing(false);

                // --- 1. CABECERA: Fecha y Estado de Asistencia ---
                HorizontalLayout filaTop = new HorizontalLayout();
                filaTop.setWidthFull();
                filaTop.setJustifyContentMode(JustifyContentMode.BETWEEN);
                filaTop.setAlignItems(Alignment.CENTER);

                H3 tituloFecha = new H3(a.getSesion().getFechaHoraEjecucion().format(formatter));
                tituloFecha.getStyle().set("margin", "0");
                tituloFecha.getStyle().set("font-size", "var(--lumo-font-size-m)");

                Icon iconoEstado;
                Span txtEstado;
                if (a.getEstado() == EstadoAsistencia.PRESENTE) {
                    iconoEstado = new Icon(VaadinIcon.CHECK_CIRCLE);
                    iconoEstado.setColor("var(--lumo-success-color)");
                    txtEstado = new Span("Presente");
                    txtEstado.getStyle().set("color", "var(--lumo-success-color)");
                    txtEstado.getStyle().set("font-weight", "bold");
                } else {
                    iconoEstado = new Icon(VaadinIcon.CLOSE_CIRCLE);
                    iconoEstado.setColor("var(--lumo-error-color)");
                    txtEstado = new Span("Ausente");
                    txtEstado.getStyle().set("color", "var(--lumo-error-color)");
                    txtEstado.getStyle().set("font-weight", "bold");
                }

                HorizontalLayout badgeEstado = new HorizontalLayout(iconoEstado, txtEstado);
                badgeEstado.setAlignItems(Alignment.CENTER);
                badgeEstado.setSpacing(true);

                filaTop.add(tituloFecha, badgeEstado);

                // --- 2. CUERPO: Datos de la clase ---
                Span lblTema = new Span("Microciclo: " + a.getSesion().getMicrociclo().getNombre());
                lblTema.getStyle().set("font-size", "var(--lumo-font-size-s)");
                lblTema.getStyle().set("color", "var(--lumo-secondary-text-color)");

                // CORRECCIÓN APLICADA AQUÍ: Atravesando a Usuario para llegar al Nombre
                String nombreSensei = a.getSesion().getSensei().getUsuario().getNombre();
                Span lblSensei = new Span("Sensei: " + nombreSensei);
                lblSensei.getStyle().set("font-size", "var(--lumo-font-size-s)");

                card.add(filaTop, lblTema, lblSensei);

                // --- 3. FASE R: Feedback del Sensei ---
                String notasClase = a.getSesion().getNotasRetroalimentacion();
                if (notasClase != null && !notasClase.trim().isEmpty()) {
                    VerticalLayout cajaNotas = new VerticalLayout();
                    cajaNotas.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
                    cajaNotas.getStyle().set("border-left", "4px solid var(--lumo-primary-color)");
                    cajaNotas.getStyle().set("padding", "10px");
                    cajaNotas.getStyle().set("margin-top", "10px");
                    cajaNotas.setSpacing(false);

                    Span tituloNotas = new Span("Feedback del Sensei:");
                    tituloNotas.getStyle().set("font-weight", "bold");
                    tituloNotas.getStyle().set("font-size", "var(--lumo-font-size-xs)");

                    Span contenidoNotas = new Span(notasClase);
                    contenidoNotas.getStyle().set("font-style", "italic");
                    contenidoNotas.getStyle().set("font-size", "var(--lumo-font-size-s)");

                    cajaNotas.add(tituloNotas, contenidoNotas);
                    card.add(cajaNotas);
                }

                add(card);
            }

        } catch (Exception e) {
            Span error = new Span("No pudimos cargar tu historial. Revisa más tarde.");
            error.getStyle().set("color", "var(--lumo-error-text-color)");
            add(error);
        }
    }
}