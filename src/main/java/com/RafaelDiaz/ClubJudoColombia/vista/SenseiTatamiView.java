package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.component.TabataComponent;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private final TraduccionService traduccionService;
    private final Sensei senseiActual;
    private final JudokaRepository judokaRepository;
    private ComboBox<Microciclo> selectorPlan;
    private VerticalLayout contenedorEjercicios;
    private final GrupoEntrenamientoService grupoService;
    private ComboBox<GrupoEntrenamiento> selectorGrupo;
    // Cambiado a FlexLayout para envolver las tarjetas de asistencia
    private FlexLayout contenedorAsistencia;

    private List<Asistencia> asistenciasActuales = new ArrayList<>();
    private final GamificationService gamificationService;
    private ProgressBar progresoClase;
    private Span labelProgreso;
    private int minutosTotalesPlan = 0;
    private int minutosCompletados = 0;

    public SenseiTatamiView(MicrocicloService microcicloService,
                            SesionEjecutadaService sesionEjecutadaService, TraduccionService traduccionService,
                            SecurityService securityService,
                            JudokaRepository judokaRepository,
                            GrupoEntrenamientoService grupoService, GamificationService gamificationService) {
        this.microcicloService = microcicloService;
        this.sesionEjecutadaService = sesionEjecutadaService;
        this.traduccionService = traduccionService;
        this.senseiActual = securityService.getAuthenticatedSensei().orElseThrow();
        this.judokaRepository = judokaRepository;
        this.grupoService = grupoService;
        this.gamificationService = gamificationService;

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
        // --- 1. SELECTOR DE GRUPO ---
        selectorGrupo = new ComboBox<>("1. Seleccionar Grupo");
        selectorGrupo.setItemLabelGenerator(GrupoEntrenamiento::getNombre);
        // Usamos el método en GrupoService para traer los grupos del Sensei
        selectorGrupo.setItems(grupoService.findAllBySenseiId(senseiActual.getId()));
        selectorGrupo.setWidthFull();

        // --- 2. SELECTOR DE MICROCICLO ---
        selectorPlan = new ComboBox<>("2. Seleccionar Plan (Microciclo)");
        selectorPlan.setItemLabelGenerator(Microciclo::getNombre);
        selectorPlan.setItems(microcicloService.obtenerHistorialDelSensei(senseiActual));
        selectorPlan.setWidthFull();
        selectorPlan.setEnabled(false); // Deshabilitado hasta que elija grupo

        // --- CONTENEDOR DE SELECTORES ---
        HorizontalLayout layoutSelectores = new HorizontalLayout(selectorGrupo, selectorPlan);
        layoutSelectores.setWidthFull();

        // --- EVENTOS (La Magia) ---
        selectorGrupo.addValueChangeListener(e -> {
            boolean grupoSeleccionado = e.getValue() != null;
            selectorPlan.setEnabled(grupoSeleccionado);
            if (grupoSeleccionado) {
                // Si cambia el grupo, redibujamos la asistencia de inmediato
                dibujarListaAsistencia(e.getValue());
                // Si ya había un plan seleccionado, recargamos los ejercicios para limpiar progreso
                if (selectorPlan.getValue() != null) {
                    cargarDetallesDelPlan(selectorPlan.getValue());
                }
            } else {
                contenedorAsistencia.removeAll();
                contenedorEjercicios.removeAll();
                asistenciasActuales.clear();
            }
        });

        selectorPlan.addValueChangeListener(e -> {
            if (e.getValue() != null && selectorGrupo.getValue() != null) {
                cargarDetallesDelPlan(e.getValue());
            }
        });
// --- BOTÓN TINDER (MODO RÁFAGA) ---
        Button btnTomarLista = new Button("Pasar Lista (Modo Rápido)", new Icon(VaadinIcon.USERS));
        btnTomarLista.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnTomarLista.setWidthFull();
        btnTomarLista.getStyle().set("margin-top", "10px");
        btnTomarLista.addClickListener(e -> abrirTinderAsistencia()); // <-- Llamamos al nuevo método



        // --- CONTENEDOR ASISTENCIA ---
        contenedorAsistencia = new FlexLayout();
        contenedorAsistencia.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contenedorAsistencia.getStyle().set("gap", "10px");
        contenedorAsistencia.getStyle().set("margin-top", "10px");
        contenedorAsistencia.getStyle().set("margin-bottom", "20px");

        Span tituloAsistencia = new Span("Resumen de Asistencia:");
        tituloAsistencia.getStyle().set("font-weight", "bold");

        add(layoutSelectores, btnTomarLista, tituloAsistencia, contenedorAsistencia);
    }

    private void cargarDetallesDelPlan(Microciclo plan) {
        contenedorEjercicios.removeAll();
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

            String nombreTareaStr = ej.getTareaDiaria().getNombre().toLowerCase();
            if (nombreTareaStr.contains("randori") || nombreTareaStr.contains("combate") || nombreTareaStr.contains("uchikomi")) {
                Button btnLanzarTabataLocal = new Button("Iniciar Cronómetro", new Icon(VaadinIcon.PLAY));
                btnLanzarTabataLocal.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                btnLanzarTabataLocal.addClickListener(ev -> abrirDialogoTabata());
                card.add(btnLanzarTabataLocal);
            }

            contenedorEjercicios.add(card);
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
        List<Judoka> listaJudokas = judokaRepository.findByGrupoWithAcudiente(grupo);

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
            // --- BOTÓN DE PÁNICO (SOS) ---
            Button btnSOS = new Button(new Icon(VaadinIcon.AMBULANCE));
            btnSOS.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            btnSOS.setWidth("40px");
            btnSOS.setHeight("40px");
            btnSOS.getStyle().set("border-radius", "50%");

            // 1. Lógica Servidor: Abrir diálogo
            btnSOS.addClickListener(e -> mostrarDialogoSOS(j));

            // 2. Lógica Cliente: Detener propagación del clic para no activar la tarjeta
            btnSOS.getElement().executeJs("this.addEventListener('click', function(e) { e.stopPropagation(); });");
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

            cardJudoka.add(btnSOS, avatar, nombreCorto, iconEstado);

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
    private void mostrarDialogoSOS(Judoka alumno) {
        Dialog d = new Dialog();
        d.setHeaderTitle(traduccionService.get("asistencia.dialog.sos.titulo"));

        VerticalLayout layout = new VerticalLayout();

        layout.add(new H4(alumno.getUsuario().getNombre() + " " + alumno.getUsuario().getApellido()));

        // Datos de emergencia con labels traducidos
        layout.add(crearFilaInfo(VaadinIcon.PHONE,
                traduccionService.get("asistencia.dialog.sos.acudiente_movil"),
                alumno.getTelefonoAcudiente()));
        layout.add(crearFilaInfo(VaadinIcon.ENVELOPE,
                traduccionService.get("asistencia.dialog.sos.email"),
                alumno.getUsuario().getEmail()));
        layout.add(crearFilaInfo(VaadinIcon.USER_HEART,
                traduccionService.get("asistencia.dialog.sos.eps"),
                alumno.getEps()));
        layout.add(crearFilaInfo(VaadinIcon.FAMILY,
                traduccionService.get("asistencia.dialog.sos.nombre_acudiente"),
                alumno.getNombreAcudiente()));

        Button btnLlamar = new Button(traduccionService.get("asistencia.dialog.sos.llamar_ahora"),
                new Icon(VaadinIcon.PHONE));

        btnLlamar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btnLlamar.setWidthFull();

        // Enlace tel: para móviles
        String telefonoLlamada = alumno.getTelefonoAcudiente() != null ? alumno.getTelefonoAcudiente() : alumno.getCelular();
        if (telefonoLlamada != null && !telefonoLlamada.isEmpty()) {
            Anchor link = new Anchor("tel:" + telefonoLlamada, btnLlamar);
            link.setWidthFull();
            layout.add(link);
        } else {
            btnLlamar.setEnabled(false);
            btnLlamar.setText(traduccionService.get("asistencia.dialog.sos.sin_telefono"));
            layout.add(btnLlamar);
        }

        Button cerrar = new Button(traduccionService.get("asistencia.boton.cerrar"), e -> d.close());

        d.add(layout);
        d.getFooter().add(cerrar);
        d.open();
    }
    private HorizontalLayout crearFilaInfo(VaadinIcon icon, String label, String valor) {
        HorizontalLayout h = new HorizontalLayout();
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon i = icon.create();
        i.setColor("gray");
        i.setSize("16px");

        Span lbl = new Span(label + ": ");
        lbl.getStyle().set("font-weight", "bold");

        Span val = new Span(valor != null ? valor : "---");

        h.add(i, lbl, val);
        return h;
    }


    private void abrirDialogoCierre() {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Retroalimentación de la Clase");

        TextArea notasR = new TextArea("Dicta o escribe tus observaciones (Fase R)");
        notasR.setPlaceholder("Ej: El grupo falló mucho en el agarre...");
        notasR.setWidthFull();
        notasR.setHeight("200px");

        Button btnGuardar = new Button("Guardar y Finalizar", e -> {
            // Validaciones rápidas de seguridad
            if(selectorGrupo.getValue() == null || selectorPlan.getValue() == null) {
                Notification.show("Debes seleccionar un Grupo y un Plan antes de finalizar.");
                return;
            }

            SesionEjecutada sesion = new SesionEjecutada();
            sesion.setSensei(senseiActual);
            sesion.setMicrociclo(selectorPlan.getValue());

            sesion.setGrupo(selectorGrupo.getValue());

            sesion.setNotasRetroalimentacion(notasR.getValue());

            asistenciasActuales.forEach(sesion::addAsistencia);

            sesionEjecutadaService.guardarSesion(sesion);

            // 🎮 GAMIFICATION: Verificar logros de asistencia para los presentes
            asistenciasActuales.stream()
                    .filter(a -> a.getEstado() == EstadoAsistencia.PRESENTE)
                    .map(Asistencia::getJudoka)
                    .forEach(judoka -> gamificationService.verificarLogrosAsistencia(judoka));

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
        tabataDialog.setHeaderTitle("Configurar Cronómetro");

        // SECCIÓN 1: Botones Rápidos (Presets)
        Span lblRapidos = new Span("Ajustes Rápidos:");
        lblRapidos.getStyle().set("font-weight", "bold").set("font-size", "0.9rem");

        Button btnRandori = new Button("Randori Oficial (4' x 1')", e -> lanzarCronometro(tabataDialog, 240, 60, 5));
        btnRandori.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnUchikomi = new Button("Uchikomi (30'' x 10'')", e -> lanzarCronometro(tabataDialog, 30, 10, 10));
        btnUchikomi.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        VerticalLayout layoutRapido = new VerticalLayout(lblRapidos, btnRandori, btnUchikomi);
        layoutRapido.setPadding(false);

        // SECCIÓN 2: Configuración Manual (Custom)
        Span lblManual = new Span("Configuración Libre:");
        lblManual.getStyle().set("font-weight", "bold").set("font-size", "0.9rem").set("margin-top", "15px");

        com.vaadin.flow.component.textfield.IntegerField fieldTrabajo = new com.vaadin.flow.component.textfield.IntegerField("Trabajo (Segundos)");
        fieldTrabajo.setValue(120); // 2 min por defecto
        fieldTrabajo.setStepButtonsVisible(true);
        fieldTrabajo.setMin(5);

        com.vaadin.flow.component.textfield.IntegerField fieldDescanso = new com.vaadin.flow.component.textfield.IntegerField("Descanso (Segundos)");
        fieldDescanso.setValue(30);
        fieldDescanso.setStepButtonsVisible(true);
        fieldDescanso.setMin(0);

        com.vaadin.flow.component.textfield.IntegerField fieldSeries = new com.vaadin.flow.component.textfield.IntegerField("Series/Rounds");
        fieldSeries.setValue(3);
        fieldSeries.setStepButtonsVisible(true);
        fieldSeries.setMin(1);

        Button btnIniciarManual = new Button("Iniciar Personalizado", new Icon(VaadinIcon.PLAY));
        btnIniciarManual.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btnIniciarManual.setWidthFull();
        btnIniciarManual.addClickListener(e -> {
            lanzarCronometro(tabataDialog, fieldTrabajo.getValue(), fieldDescanso.getValue(), fieldSeries.getValue());
        });

        VerticalLayout layoutManual = new VerticalLayout(lblManual, fieldTrabajo, fieldDescanso, fieldSeries, btnIniciarManual);
        layoutManual.setPadding(false);
        layoutManual.getStyle().set("border-top", "1px solid var(--lumo-contrast-20pct)");
        layoutManual.getStyle().set("padding-top", "15px");

        VerticalLayout mainLayout = new VerticalLayout(layoutRapido, layoutManual);
        tabataDialog.add(mainLayout);

        tabataDialog.getFooter().add(new Button("Cancelar", i -> tabataDialog.close()));
        tabataDialog.open();
    }

    private void lanzarCronometro(com.vaadin.flow.component.dialog.Dialog padre, int t, int d, int s) {
        padre.close(); // Cerramos el configurador

        com.vaadin.flow.component.dialog.Dialog pantallaCompleta = new com.vaadin.flow.component.dialog.Dialog();
        pantallaCompleta.setWidth("100vw");
        pantallaCompleta.setHeight("100vh");
        pantallaCompleta.getElement().getThemeList().add("no-padding");
        pantallaCompleta.setCloseOnOutsideClick(false);
        pantallaCompleta.setCloseOnEsc(false);
        TabataComponent tabata = new TabataComponent(() -> pantallaCompleta.close());
        pantallaCompleta.add(tabata);

        pantallaCompleta.open();
        tabata.iniciarTabata(t, d, s);
    }
    // =========================================================
    // MODO TINDER: ASISTENCIA EN RÁFAGA
    // =========================================================
    private int indiceAsistenciaActual = 0;

    private void abrirTinderAsistencia() {
        if (asistenciasActuales.isEmpty()) {
            Notification.show("No hay alumnos en este grupo para tomar lista.");
            return;
        }

        indiceAsistenciaActual = 0; // Reiniciamos el contador

        com.vaadin.flow.component.dialog.Dialog tinderDialog = new com.vaadin.flow.component.dialog.Dialog();
        tinderDialog.setWidth("100vw");
        tinderDialog.setHeight("100vh");
        tinderDialog.getElement().getThemeList().add("no-padding");

        VerticalLayout layoutPrincipal = new VerticalLayout();
        layoutPrincipal.setSizeFull();
        layoutPrincipal.setAlignItems(Alignment.CENTER);
        layoutPrincipal.setJustifyContentMode(JustifyContentMode.CENTER);
        layoutPrincipal.getStyle().set("background-color", "#f4f4f4");

        tinderDialog.add(layoutPrincipal);
        tinderDialog.open();

        mostrarSiguienteTarjeta(tinderDialog, layoutPrincipal);
    }

    private void mostrarSiguienteTarjeta(com.vaadin.flow.component.dialog.Dialog dialog, VerticalLayout layoutPrincipal) {
        layoutPrincipal.removeAll();

        if (indiceAsistenciaActual >= asistenciasActuales.size()) {
            dialog.close();
            Notification.show("¡Lista terminada!");
            // Refrescamos visualmente la cuadrícula pequeña de atrás
            dibujarListaAsistencia(selectorGrupo.getValue());
            return;
        }

        Asistencia asistencia = asistenciasActuales.get(indiceAsistenciaActual);
        Judoka judoka = asistencia.getJudoka();

        // 1. LA TARJETA GIGANTE
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.getStyle().set("background", "white");
        card.getStyle().set("border-radius", "20px");
        card.getStyle().set("box-shadow", "0 10px 20px rgba(0,0,0,0.1)");
        card.getStyle().set("width", "85vw");
        card.getStyle().set("height", "60vh");
        card.getStyle().set("max-width", "400px");

        // Animación de entrada suave
        card.getStyle().set("animation", "fadein 0.3s");
        layoutPrincipal.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.innerHTML = '@keyframes fadein { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }';" +
                        "document.head.appendChild(style);"
        );

        Avatar avatarGigante = new Avatar(judoka.getNombre() + " " + judoka.getApellido());
        avatarGigante.getStyle().set("width", "150px").set("height", "150px");
        if (judoka.getUrlFotoPerfil() != null && !judoka.getUrlFotoPerfil().isEmpty()) {
            avatarGigante.setImage(judoka.getUrlFotoPerfil());
        }

        H2 nombreText = new H2(judoka.getNombre());
        nombreText.getStyle().set("margin-bottom", "0");

        Span apellidoText = new Span(judoka.getApellido());
        apellidoText.getStyle().set("font-size", "1.2rem").set("color", "gray");

        card.add(avatarGigante, nombreText, apellidoText);

        // 2. LOS BOTONES DE ACCIÓN (Izquierda Rojo / Derecha Verde)
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setWidth("85vw");
        botonesLayout.getStyle().set("max-width", "400px");
        botonesLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        botonesLayout.getStyle().set("margin-top", "30px");

        Button btnFalto = new Button(new Icon(VaadinIcon.CLOSE));
        btnFalto.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnFalto.getStyle().set("width", "80px").set("height", "80px").set("border-radius", "50%");

        Button btnPresente = new Button(new Icon(VaadinIcon.CHECK));
        btnPresente.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnPresente.getStyle().set("width", "80px").set("height", "80px").set("border-radius", "50%");

        // Acciones al hacer clic
        btnFalto.addClickListener(e -> {
            asistencia.setEstado(EstadoAsistencia.AUSENTE);
            indiceAsistenciaActual++;
            mostrarSiguienteTarjeta(dialog, layoutPrincipal);
        });

        btnPresente.addClickListener(e -> {
            asistencia.setEstado(EstadoAsistencia.PRESENTE);
            indiceAsistenciaActual++;
            mostrarSiguienteTarjeta(dialog, layoutPrincipal);
        });

        botonesLayout.add(btnFalto, btnPresente);

        layoutPrincipal.add(card, botonesLayout);
    }
}