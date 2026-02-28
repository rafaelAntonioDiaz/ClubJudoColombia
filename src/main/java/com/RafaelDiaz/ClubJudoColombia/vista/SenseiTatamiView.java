package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.MicrocicloService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SesionEjecutadaService;
import com.RafaelDiaz.ClubJudoColombia.vista.component.TabataComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value = "tatami", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_MASTER", "ROLE_SENSEI"})
public class SenseiTatamiView extends VerticalLayout {

    private final MicrocicloService microcicloService;
    private final SesionEjecutadaService sesionEjecutadaService;
    private final Sensei senseiActual;
    private final JudokaRepository judokaRepository;
    private ComboBox<Microciclo> selectorPlan;
    private VerticalLayout contenedorEjercicios;

    // Cambiado a FlexLayout para envolver las tarjetas de asistencia
    private FlexLayout contenedorAsistencia;

    private List<Asistencia> asistenciasActuales = new ArrayList<>();

    private ProgressBar progresoClase;
    private Span labelProgreso;
    private int minutosTotalesPlan = 0;
    private int minutosCompletados = 0;

    public SenseiTatamiView(MicrocicloService microcicloService,
                            SesionEjecutadaService sesionEjecutadaService,
                            SecurityService securityService,
                            JudokaRepository judokaRepository) {
        this.microcicloService = microcicloService;
        this.sesionEjecutadaService = sesionEjecutadaService;
        this.senseiActual = securityService.getAuthenticatedSensei().orElseThrow();
        this.judokaRepository = judokaRepository;

        addClassName("modo-tatami-view");
        setSizeFull();
        setSpacing(true);

        configurarCabecera();
        configurarSelectorYAsistencia();

        contenedorEjercicios = new VerticalLayout();
        contenedorEjercicios.setPadding(false);
        add(contenedorEjercicios);

        // Botón Tabata (Flotante general)
        Button btnTabataRapido = new Button(new Icon(VaadinIcon.STOPWATCH));
        btnTabataRapido.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnTabataRapido.getStyle().set("position", "fixed");
        btnTabataRapido.getStyle().set("bottom", "80px"); // Subido para no chocar con el botón de finalizar
        btnTabataRapido.getStyle().set("right", "20px");
        btnTabataRapido.getStyle().set("border-radius", "50%");
        btnTabataRapido.getStyle().set("width", "60px");
        btnTabataRapido.getStyle().set("height", "60px");
        btnTabataRapido.getStyle().set("z-index", "100");
        btnTabataRapido.addClickListener(e -> abrirDialogoTabata());
        add(btnTabataRapido);

        // Botón Finalizar
        Button btnFinalizar = new Button("Finalizar Sesión", new Icon(VaadinIcon.CHECK_CIRCLE));
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        btnFinalizar.setWidthFull();
        btnFinalizar.addClickListener(e -> abrirDialogoCierre());

        // Contenedor fijo en la parte inferior para "Finalizar"
        HorizontalLayout footer = new HorizontalLayout(btnFinalizar);
        footer.setWidthFull();
        footer.getStyle().set("position", "sticky");
        footer.getStyle().set("bottom", "0");
        footer.getStyle().set("background", "white");
        footer.getStyle().set("padding", "10px 0");
        add(footer);
    }

    private void configurarCabecera() {
        HorizontalLayout cabecera = new HorizontalLayout(new Icon(VaadinIcon.TIMER), new H2("Modo Tatami"));
        cabecera.setAlignItems(Alignment.CENTER);

        progresoClase = new ProgressBar();
        progresoClase.setWidthFull();
        progresoClase.setMin(0);
        progresoClase.setMax(100);
        progresoClase.setValue(0);

        labelProgreso = new Span("Progreso: 0%");
        labelProgreso.getStyle().set("font-size", "var(--lumo-font-size-s)");
        labelProgreso.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(cabecera, progresoClase, labelProgreso);
    }

    private void configurarSelectorYAsistencia() {
        selectorPlan = new ComboBox<>("Seleccionar Plan de Hoy");
        selectorPlan.setItemLabelGenerator(Microciclo::getNombre);
        selectorPlan.setItems(microcicloService.obtenerHistorialDelSensei(senseiActual));
        selectorPlan.setWidthFull();

        selectorPlan.addValueChangeListener(e -> {
            if (e.getValue() != null) cargarDetallesDelPlan(e.getValue());
        });

        // Usamos FlexLayout para que las tarjetas de los alumnos se acomoden como cuadricula
        contenedorAsistencia = new FlexLayout();
        contenedorAsistencia.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contenedorAsistencia.getStyle().set("gap", "10px");
        contenedorAsistencia.getStyle().set("margin-top", "10px");
        contenedorAsistencia.getStyle().set("margin-bottom", "20px");

        Span tituloAsistencia = new Span("Asistencia (Toque para marcar falta)");
        tituloAsistencia.getStyle().set("font-weight", "bold");

        add(selectorPlan, tituloAsistencia, contenedorAsistencia);
    }

    private void cargarDetallesDelPlan(Microciclo plan) {
        contenedorEjercicios.removeAll();
        asistenciasActuales.clear();

        minutosTotalesPlan = 0;
        minutosCompletados = 0;
        progresoClase.setValue(0);
        labelProgreso.setText("Progreso: 0%");

        Span tituloEjercicios = new Span("Ejercicios del Plan");
        tituloEjercicios.getStyle().set("font-weight", "bold");
        contenedorEjercicios.add(tituloEjercicios);

        for (EjercicioPlanificado ej : plan.getEjerciciosPlanificados()) {
            int duracion = ej.getDuracionMinutos() != null ? ej.getDuracionMinutos() : 0;
            minutosTotalesPlan += duracion;

            VerticalLayout card = new VerticalLayout();
            card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
            card.getStyle().set("border-radius", "8px");
            card.getStyle().set("padding", "15px");
            card.getStyle().set("margin-bottom", "10px");
            card.getStyle().set("transition", "all 0.3s ease");
            card.setSpacing(true);

            Span textoTarea = new Span(ej.getTareaDiaria().getNombre() + " (" + duracion + " min)");
            textoTarea.getStyle().set("font-weight", "bold");
            textoTarea.getStyle().set("font-size", "var(--lumo-font-size-l)");

            Span dosis = new Span(ej.getNotaAjuste() != null ? ej.getNotaAjuste() : "");
            dosis.getStyle().set("font-size", "var(--lumo-font-size-s)");
            dosis.getStyle().set("color", "var(--lumo-secondary-text-color)");

            Checkbox chkCompletado = new Checkbox("Hecho");
            chkCompletado.addValueChangeListener(event -> {
                if (event.getValue()) {
                    minutosCompletados += duracion;
                    textoTarea.getStyle().set("text-decoration", "line-through");
                    textoTarea.getStyle().set("color", "var(--lumo-disabled-text-color)");
                    card.getStyle().set("background-color", "var(--lumo-success-color-10pct)");
                    card.getStyle().set("border-color", "var(--lumo-success-color)");
                } else {
                    minutosCompletados -= duracion;
                    textoTarea.getStyle().remove("text-decoration");
                    textoTarea.getStyle().set("color", "var(--lumo-body-text-color)");
                    card.getStyle().remove("background-color");
                    card.getStyle().set("border-color", "var(--lumo-contrast-20pct)");
                }
                actualizarBarraProgreso();
            });

            HorizontalLayout filaSuperior = new HorizontalLayout(textoTarea, chkCompletado);
            filaSuperior.setWidthFull();
            filaSuperior.setJustifyContentMode(JustifyContentMode.BETWEEN);
            filaSuperior.setAlignItems(Alignment.CENTER);

            card.add(filaSuperior, dosis);

            // --- INYECCIÓN DEL TABATA EN LA TARJETA ---
            // Si el nombre de la tarea sugiere combate o técnica, añadimos botón rápido
            String nombreTareaStr = ej.getTareaDiaria().getNombre().toLowerCase();
            if (nombreTareaStr.contains("randori") || nombreTareaStr.contains("combate") || nombreTareaStr.contains("uchikomi")) {
                Button btnLanzarTabataLocal = new Button("Iniciar Cronómetro", new Icon(VaadinIcon.PLAY));
                btnLanzarTabataLocal.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                btnLanzarTabataLocal.addClickListener(ev -> abrirDialogoTabata());
                card.add(btnLanzarTabataLocal);
            }

            contenedorEjercicios.add(card);
        }

        if (!plan.getGruposAsignados().isEmpty()) {
            GrupoEntrenamiento grupo = plan.getGruposAsignados().iterator().next();
            dibujarListaAsistencia(grupo);
        }
    }

    private void actualizarBarraProgreso() {
        if (minutosTotalesPlan > 0) {
            double porcentaje = (double) minutosCompletados / minutosTotalesPlan * 100;
            progresoClase.setValue(porcentaje);
            labelProgreso.setText(String.format("Progreso: %.0f%% (%d/%d min)", porcentaje, minutosCompletados, minutosTotalesPlan));
        }
    }

    private void dibujarListaAsistencia(GrupoEntrenamiento grupo) {
        contenedorAsistencia.removeAll();
        List<Judoka> listaJudokas = judokaRepository.findByGrupo(grupo);

        for (Judoka j : listaJudokas) {
            final Asistencia asist = new Asistencia(j, EstadoAsistencia.PRESENTE);
            asistenciasActuales.add(asist);

            // Construir la tarjeta del Judoka (Visual)
            VerticalLayout cardJudoka = new VerticalLayout();
            cardJudoka.setAlignItems(Alignment.CENTER);
            cardJudoka.getStyle().set("border", "2px solid var(--lumo-success-color)"); // Verde por defecto
            cardJudoka.getStyle().set("border-radius", "8px");
            cardJudoka.getStyle().set("padding", "10px");
            cardJudoka.getStyle().set("width", "90px");
            cardJudoka.getStyle().set("cursor", "pointer"); // Indica que es clicable
            cardJudoka.getStyle().set("background-color", "white");

            // El Avatar (Foto o Iniciales)
            Avatar avatar = new Avatar(j.getNombre() + " " + j.getApellido());
            if (j.getUrlFotoPerfil() != null && !j.getUrlFotoPerfil().isEmpty()) {
                avatar.setImage(j.getUrlFotoPerfil());
            }

            // Nombre corto
            Span nombreCorto = new Span(j.getNombre().split(" ")[0]);
            nombreCorto.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            nombreCorto.getStyle().set("font-weight", "bold");
            nombreCorto.getStyle().set("text-align", "center");

            // Ícono indicador
            Icon iconEstado = new Icon(VaadinIcon.CHECK_CIRCLE);
            iconEstado.setColor("var(--lumo-success-color)");

            cardJudoka.add(avatar, nombreCorto, iconEstado);

            // Lógica del clic para cambiar estado
            cardJudoka.addClickListener(e -> {
                if (asist.getEstado() == EstadoAsistencia.PRESENTE) {
                    asist.setEstado(EstadoAsistencia.AUSENTE);
                    cardJudoka.getStyle().set("border-color", "var(--lumo-error-color)");
                    cardJudoka.getStyle().set("background-color", "var(--lumo-error-color-10pct)");
                    cardJudoka.getStyle().set("opacity", "0.6");
                    iconEstado.getElement().setAttribute("icon", "vaadin:close-circle");
                    iconEstado.setColor("var(--lumo-error-color)");
                } else {
                    asist.setEstado(EstadoAsistencia.PRESENTE);
                    cardJudoka.getStyle().set("border-color", "var(--lumo-success-color)");
                    cardJudoka.getStyle().set("background-color", "white");
                    cardJudoka.getStyle().set("opacity", "1");
                    iconEstado.getElement().setAttribute("icon", "vaadin:check-circle");
                    iconEstado.setColor("var(--lumo-success-color)");
                }
            });

            contenedorAsistencia.add(cardJudoka);
        }
    }

    private void abrirDialogoCierre() {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Retroalimentación de la Clase");

        TextArea notasR = new TextArea("Dicta o escribe tus observaciones (Fase R)");
        notasR.setPlaceholder("Ej: El grupo falló mucho en el agarre...");
        notasR.setWidthFull();
        notasR.setHeight("200px");

        Button btnGuardar = new Button("Guardar y Finalizar", e -> {
            SesionEjecutada sesion = new SesionEjecutada();
            sesion.setSensei(senseiActual);
            sesion.setMicrociclo(selectorPlan.getValue());
            sesion.setGrupo(selectorPlan.getValue().getGruposAsignados().iterator().next());
            sesion.setNotasRetroalimentacion(notasR.getValue());

            asistenciasActuales.forEach(sesion::addAsistencia);

            sesionEjecutadaService.guardarSesion(sesion);
            Notification.show("Sesión guardada. ¡Buen trabajo, Sensei!");
            dialog.close();
            getUI().ifPresent(ui -> ui.navigate(SenseiDashboardView.class));
        });

        dialog.add(notasR);
        dialog.getFooter().add(new Button("Cancelar", i -> dialog.close()), btnGuardar);
        dialog.open();
    }

    private void abrirDialogoTabata() {
        com.vaadin.flow.component.dialog.Dialog tabataDialog = new com.vaadin.flow.component.dialog.Dialog();
        tabataDialog.setHeaderTitle("Cronómetro de Combate");

        Button btnRandori = new Button("Randori Oficial (4'x1')", e -> lanzarCronometro(tabataDialog, 240, 60, 5));
        Button btnUchikomi = new Button("Uchikomi (30''x10'')", e -> lanzarCronometro(tabataDialog, 30, 10, 10));

        VerticalLayout layout = new VerticalLayout(btnRandori, btnUchikomi);
        tabataDialog.add(layout);

        tabataDialog.getFooter().add(new Button("Cerrar", i -> tabataDialog.close()));
        tabataDialog.open();
    }

    private void lanzarCronometro(com.vaadin.flow.component.dialog.Dialog padre, int t, int d, int s) {
        padre.close();

        com.vaadin.flow.component.dialog.Dialog pantallaCompleta = new com.vaadin.flow.component.dialog.Dialog();

        pantallaCompleta.setWidth("100vw");
        pantallaCompleta.setHeight("100vh");
        pantallaCompleta.getElement().getThemeList().add("no-padding");

        TabataComponent tabata = new TabataComponent();
        pantallaCompleta.add(tabata);

        pantallaCompleta.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                getElement().executeJs("document.body.style.backgroundColor = '';");
            }
        });

        pantallaCompleta.open();
        tabata.iniciarTabata(t, d, s);
    }
}