package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.dto.DatosAntropometricosDTO;
import com.RafaelDiaz.ClubJudoColombia.dto.DocumentoDTO;
import com.RafaelDiaz.ClubJudoColombia.dto.ResultadoPruebaDTO;
import com.RafaelDiaz.ClubJudoColombia.dto.TareaEjecutadaDTO;
import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.repositorio.InsigniaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.MetricaRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.PruebaEstandarRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.util.BibliotecaSabiduria;
import com.RafaelDiaz.ClubJudoColombia.vista.component.MiDoWidget;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;

import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.netty.util.concurrent.FastThreadLocal.removeAll;

@Route(value = "perfil-judoka", layout = JudokaLayout.class)
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Mi Santuario | Club Judo Colombia")
public class PerfilJudokaView extends JudokaLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final JudokaService judokaService;
    private final TraduccionService traduccionService;
    private final PerfilJudokaService perfilService;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository  judokaRepository;
    private final InsigniaRepository insigniaRepository;
    private final PruebaEstandarRepository pruebaEstandarRepository;
    private final MetricaRepository metricaRepository;
    private Judoka judokaActual;
    private Image avatarImage;
    private Div avatarContainer;
    private Long parametroId;

    @Autowired
    public PerfilJudokaView(SecurityService securityService,
                            AccessAnnotationChecker accessChecker,
                            JudokaService judokaService,
                            TraduccionService traduccionService, PerfilJudokaService perfilService,
                            AlmacenamientoCloudService almacenamientoCloudService,
                            JudokaRepository judokaRepository,
                            InsigniaRepository insigniaRepository,
                            PruebaEstandarRepository pruebaEstandarRepository,
                            MetricaRepository metricaRepository) {
        super(securityService, accessChecker, traduccionService, judokaRepository);
        this.securityService = securityService;
        this.judokaService = judokaService;
        this.traduccionService = traduccionService;
        this.perfilService = perfilService;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;
        this.insigniaRepository = insigniaRepository;
        this.pruebaEstandarRepository = pruebaEstandarRepository;
        this.metricaRepository = metricaRepository;

        addClassName("perfil-view");
    }

    private Component crearSeccionSabiduria() {
        Div card = new Div();
        card.setWidthFull();
        card.addClassName("card-blanca");
        card.getStyle()
                .set("background", "linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)")
                .set("border-left", "5px solid #8E44AD")
                .set("padding", "20px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.05)");

        Icon quoteIcon = VaadinIcon.QUOTE_LEFT.create();
        quoteIcon.setColor("#8E44AD");
        quoteIcon.getStyle().set("opacity", "0.3").set("margin-right", "10px");

        String claveFrase = BibliotecaSabiduria.obtenerClaveDelDia();
        String textoTraducido = traduccionService.get(claveFrase);
        String autor = BibliotecaSabiduria.obtenerAutor(claveFrase);

        Span textoFrase = new Span(textoTraducido);
        textoFrase.getStyle().set("font-style", "italic").set("font-size", "1.1rem").set("color", "#555").set("line-height", "1.5");

        Span lblAutor = new Span("- " + autor);
        lblAutor.getStyle().set("display", "block").set("text-align", "right").set("font-weight", "bold").set("font-size", "0.9rem").set("margin-top", "10px").set("color", "#333");

        card.add(quoteIcon, textoFrase, lblAutor);
        return card;
    }

    private Component crearTarjetaIdentidad() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("card-blanca");
        layout.setWidth("350px");
        layout.setMinWidth("300px");
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);

        // ✅ Usar los datos del judoka, no del usuario
        String nombreCompleto = judokaActual.getNombre() + " " + judokaActual.getApellido();
        // 1. Contenedor de Avatar
        avatarContainer = new Div();
        avatarContainer.setWidth("120px");
        avatarContainer.setHeight("120px");
        avatarContainer.getStyle()
                .set("border-radius", "50%")
                .set("overflow", "hidden")
                .set("border", "4px solid white")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)")
                .set("background-color", "#eee")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        cargarImagenEnAvatar();

        // 2. Upload (Usa el UploadHandler Moderno)
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/jpg");
        upload.setMaxFiles(1);
        upload.setDropAllowed(false);

        Button uploadBtn = new Button(new Icon(VaadinIcon.CAMERA));
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        uploadBtn.setTooltipText(traduccionService.get("tooltip.cambiar.foto"));
        upload.setUploadButton(uploadBtn);

        upload.setUploadHandler(event -> {
            try {
                judokaService.actualizarFotoPerfil(judokaActual, event.getInputStream(), event.getFileName());
                getUI().ifPresent(ui -> ui.access(() -> {
                    cargarImagenEnAvatar();
                    Notification.show(traduccionService.get("msg.foto.actualizada"), 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    upload.clearFileList();
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("msg.error.general") + ": " + e.getMessage())
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)
                ));
            }
        });

        Div avatarWrapper = new Div(avatarContainer, upload);
        avatarWrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("align-items", "center");

        H2 nombreH2 = new H2(nombreCompleto);
        nombreH2.getStyle().set("margin-bottom", "0").set("text-align", "center");

        Span cinturonBadge = new Span(traduccionService.get(judokaActual.getGrado()));
        cinturonBadge.getElement().getThemeList().add("badge");
        cinturonBadge.getStyle().set("background-color", "#2C3E50").set("color", "white").set("padding", "0.5em 1em").set("font-size", "0.9rem");

        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);
        stats.getStyle().set("margin-top", "20px");

        stats.add(crearStatItem(VaadinIcon.SCALE, judokaActual.getPeso() + " kg", traduccionService.get("lbl.peso")));
        stats.add(crearStatItem(VaadinIcon.ARROWS_LONG_V, judokaActual.getEstatura() + " cm", traduccionService.get("lbl.altura")));
        stats.add(crearStatItem(VaadinIcon.CALENDAR_USER, String.valueOf(judokaActual.getEdad()), traduccionService.get("lbl.edad")));

        layout.add(avatarWrapper, nombreH2, cinturonBadge, stats);
        return layout;
    }

    private void cargarImagenEnAvatar() {
        avatarContainer.removeAll();
        String urlFotoCloud = judokaActual.getUrlFotoPerfil();

        if (urlFotoCloud != null && !urlFotoCloud.isEmpty()) {
            avatarImage = new Image(urlFotoCloud, traduccionService.get("alt.foto.perfil"));
            avatarImage.setWidth("100%");
            avatarImage.setHeight("100%");
            avatarImage.getStyle().set("object-fit", "cover");
            avatarContainer.add(avatarImage);
        } else {
            // ✅ Usar el nombre del judoka
            Avatar placeholder = new Avatar(judokaActual.getNombre() + " " + judokaActual.getApellido());
            placeholder.setWidth("100%");
            placeholder.setHeight("100%");
            avatarContainer.add(placeholder);
        }
    }

    private VerticalLayout crearStatItem(VaadinIcon icon, String valor, String label) {
        VerticalLayout v = new VerticalLayout();
        v.setSpacing(false);
        v.setPadding(false);
        v.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon i = icon.create();
        i.setColor("var(--lumo-primary-color)");
        i.setSize("20px");

        Span val = new Span(valor);
        val.getStyle().set("font-weight", "bold").set("font-size", "1.1rem");
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "0.8rem").set("color", "gray");

        v.add(i, val, lbl);
        return v;
    }

    private Component crearBitacoraReflexion() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("card-blanca");
        layout.setWidth("600px");
        layout.setMinWidth("300px");
        layout.setFlexGrow(1);
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 titulo = new H3(traduccionService.get("perfil.notas.titulo"));
        titulo.getStyle().set("margin-top", "0").set("color", "#2C3E50");

        TextArea nuevaNotaArea = new TextArea();
        nuevaNotaArea.setWidthFull();
        nuevaNotaArea.setPlaceholder(traduccionService.get("perfil.notas.placeholder"));
        nuevaNotaArea.setMinHeight("100px");

        Button btnPublicar = new Button(traduccionService.get("btn.registrar.pensamiento"), new Icon(VaadinIcon.PENCIL));
        btnPublicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublicar.getStyle().set("background-color", "#27AE60");

        VerticalLayout timelineLayout = new VerticalLayout();
        timelineLayout.setPadding(false);
        timelineLayout.setSpacing(true);

        btnPublicar.addClickListener(e -> {
            if (!nuevaNotaArea.isEmpty()) {
                judokaService.crearReflexion(judokaActual, nuevaNotaArea.getValue());
                nuevaNotaArea.clear();
                Notification.show(traduccionService.get("perfil.msg.guardado"), 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refrescarTimeline(timelineLayout);
            }
        });

        refrescarTimeline(timelineLayout);

        layout.add(titulo, nuevaNotaArea, btnPublicar, new Hr(), timelineLayout);
        return layout;
    }

    private void refrescarTimeline(VerticalLayout container) {
        container.removeAll();
        List<Reflexion> historial = judokaService.obtenerHistorialReflexiones(judokaActual);

        if (historial.isEmpty()) {
            Span empty = new Span(traduccionService.get("msg.diario.vacio"));
            empty.getStyle().set("color", "gray").set("font-style", "italic").set("margin-top", "20px");
            container.add(empty);
            return;
        }

        for (Reflexion ref : historial) {
            container.add(crearTarjetaReflexion(ref, container));
        }
    }

    private Component crearTarjetaReflexion(Reflexion ref, VerticalLayout containerPadre) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle().set("background-color", "#fcfcfc").set("border", "1px solid #eee").set("border-radius", "8px").set("padding", "15px").set("position", "relative");

        String fechaStr = ref.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"));
        Span fecha = new Span(fechaStr);
        fecha.getStyle().set("font-weight", "bold").set("font-size", "0.8rem").set("color", "#8E44AD");

        Paragraph texto = new Paragraph(ref.getContenido());
        texto.getStyle().set("margin", "10px 0").set("white-space", "pre-wrap");

        card.add(fecha, texto);

        if (ref.esEditable()) {
            Button btnEditar = new Button(new Icon(VaadinIcon.EDIT));
            btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            btnEditar.getStyle().set("position", "absolute").set("top", "10px").set("right", "10px");

            btnEditar.addClickListener(e -> {
                Dialog editDialog = new Dialog();
                editDialog.setHeaderTitle(traduccionService.get("title.editar.reflexion"));

                TextArea editArea = new TextArea();
                editArea.setValue(ref.getContenido());
                editArea.setWidth("100%");
                editArea.setHeight("200px");

                Button save = new Button(traduccionService.get("btn.guardar.cambios"), ev -> {
                    try {
                        judokaService.editarReflexion(ref, editArea.getValue());
                        editDialog.close();
                        refrescarTimeline(containerPadre);
                        Notification.show(traduccionService.get("msg.entrada.actualizada"));
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage());
                    }
                });
                save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                Button cancel = new Button(traduccionService.get("btn.cancelar"), ev -> editDialog.close());

                editDialog.add(editArea);
                editDialog.getFooter().add(cancel, save);
                editDialog.open();
            });
            card.add(btnEditar);
        } else {
            Icon lock = VaadinIcon.LOCK.create();
            lock.setSize("14px");
            lock.setColor("#ccc");
            lock.getStyle().set("position", "absolute").set("top", "10px").set("right", "10px");
            lock.setTooltipText(traduccionService.get("tooltip.registro.permanente"));
            card.add(lock);
        }
        return card;
    }
    private Component crearPruebasTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        // Filtros
        HorizontalLayout filtros = new HorizontalLayout();
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.END);

        ComboBox<PruebaEstandar> comboPrueba = new ComboBox<>(traduccionService.get("perfil.filtro.prueba", "Prueba"));
        comboPrueba.setItems(pruebaEstandarRepository.findGlobalesYDelSensei(judokaActual.getSensei()));
        comboPrueba.setItemLabelGenerator(p -> p.getNombreMostrar(traduccionService));

        DatePicker fechaDesde = new DatePicker(traduccionService.get("perfil.filtro.desde", "Desde"));
        DatePicker fechaHasta = new DatePicker(traduccionService.get("perfil.filtro.hasta", "Hasta"));

        Button btnFiltrar = new Button(traduccionService.get("perfil.filtrar", "Filtrar"), new Icon(VaadinIcon.SEARCH));
        btnFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        filtros.add(comboPrueba, fechaDesde, fechaHasta, btnFiltrar);

        // Grid de resultados
        Grid<ResultadoPruebaDTO> grid = new Grid<>(ResultadoPruebaDTO.class, false);
        grid.setEmptyStateComponent(new Span(traduccionService.get("perfil.sin_resultados", "No hay resultados registrados")));
        grid.addColumn(ResultadoPruebaDTO::getFecha).setHeader(traduccionService.get("perfil.grid.fecha", "Fecha"));
        grid.addColumn(ResultadoPruebaDTO::getPruebaNombre).setHeader(traduccionService.get("perfil.grid.prueba", "Prueba"));
        grid.addColumn(ResultadoPruebaDTO::getMetricaNombre).setHeader(traduccionService.get("perfil.grid.metrica", "Métrica"));
        grid.addColumn(ResultadoPruebaDTO::getValor).setHeader(traduccionService.get("perfil.grid.valor", "Valor"));
        grid.addColumn(ResultadoPruebaDTO::getClasificacion).setHeader(traduccionService.get("perfil.grid.clasificacion", "Clasificación"));
        grid.addColumn(ResultadoPruebaDTO::getPuntos).setHeader(traduccionService.get("perfil.grid.puntos", "Puntos"));
        grid.setWidthFull();

        // Cargar datos iniciales
        cargarResultadosGrid(grid, null, null, null);

        btnFiltrar.addClickListener(e -> {
            Long pruebaId = comboPrueba.getValue() != null ? comboPrueba.getValue().getId() : null;
            cargarResultadosGrid(grid, pruebaId, fechaDesde.getValue(), fechaHasta.getValue());
        });

        layout.add(filtros, grid);
        return layout;
    }

    private void cargarResultadosGrid(Grid<ResultadoPruebaDTO> grid,
                                      Long pruebaId, LocalDate desde, LocalDate hasta) {
        List<ResultadoPruebaDTO> datos = perfilService.getResultadosPruebas(
                judokaActual,
                Optional.ofNullable(pruebaId),
                Optional.ofNullable(desde),
                Optional.ofNullable(hasta)
        );
        grid.setItems(datos);
    }

    private Component crearTareasTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Grid<TareaEjecutadaDTO> grid = new Grid<>(TareaEjecutadaDTO.class, false);
        grid.setEmptyStateComponent(new Span(traduccionService.get("perfil.sin_tareas", "No hay tareas ejecutadas")));
        grid.addColumn(TareaEjecutadaDTO::getFecha).setHeader(traduccionService.get("perfil.grid.fecha", "Fecha"));
        grid.addColumn(TareaEjecutadaDTO::getTareaNombre).setHeader(traduccionService.get("perfil.grid.tarea", "Tarea"));
        grid.addColumn(t -> t.isCompletada() ? "✅" : "❌").setHeader(traduccionService.get("perfil.grid.completada", "Completada"));
        grid.setWidthFull();

        List<TareaEjecutadaDTO> tareas = perfilService.getUltimasTareas(judokaActual, 20);
        grid.setItems(tareas);

        layout.add(grid);
        return layout;
    }

    private Component crearInsigniasTab() {
        // Usar MiDoWidget (ya inyectado insigniaRepository)
        List<Insignia> todas = insigniaRepository.findAll();
        List<JudokaInsignia> misLogros = perfilService.getInsignias(judokaActual);
        MiDoWidget widget = new MiDoWidget(todas, misLogros, traduccionService);
        widget.setWidthFull();
        return widget;
    }

    private Component crearPalmaresTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Grid<ParticipacionCompetencia> grid = new Grid<>(ParticipacionCompetencia.class, false);
        grid.setEmptyStateComponent(new Span(traduccionService.get("perfil.sin_palmares", "Sin participación en competiciones")));
        grid.addColumn(ParticipacionCompetencia::getFecha).setHeader(traduccionService.get("perfil.grid.fecha", "Fecha"));
        grid.addColumn(ParticipacionCompetencia::getNombreCampeonato).setHeader(traduccionService.get("perfil.grid.evento", "Evento"));
        grid.addColumn(ParticipacionCompetencia::getSede).setHeader(traduccionService.get("perfil.grid.lugar", "Lugar"));
        grid.addColumn(p -> traduccionService.get(p.getResultado())).setHeader(traduccionService.get("perfil.grid.resultado", "Resultado"));
        grid.addColumn(ParticipacionCompetencia::getPuntosCalculados).setHeader(traduccionService.get("perfil.grid.puntos", "Puntos"));
        grid.setWidthFull();

        List<ParticipacionCompetencia> palmares = perfilService.getPalmares(judokaActual);
        grid.setItems(palmares);
        layout.add(grid);
        return layout;
    }

    private Component crearDocumentosTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Grid<DocumentoDTO> grid = new Grid<>(DocumentoDTO.class, false);
        grid.setEmptyStateComponent(new Span(traduccionService.get("perfil.sin_documentos", "No hay documentos subidos")));
        grid.addColumn(DocumentoDTO::getTipo).setHeader(traduccionService.get("perfil.grid.tipo", "Tipo"));
        grid.addColumn(DocumentoDTO::getNombreArchivo).setHeader(traduccionService.get("perfil.grid.archivo", "Archivo"));
        grid.addComponentColumn(d -> {
                Anchor link = new Anchor(d.getUrl(), traduccionService.get("perfil.ver", "Ver"));
                link.setTarget("_blank");
                return link;
            }).setHeader(traduccionService.get("perfil.grid.accion", "Acción"));
        grid.setWidthFull();

        List<DocumentoDTO> docs = perfilService.getDocumentos(judokaActual);
        grid.setItems(docs);
        layout.add(grid);
        return layout;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
        this.parametroId = parameter;

        // Caso 1: Hay parámetro en la URL (ej: /perfil-judoka/2)
        if (parametroId != null) {
            Optional<Judoka> opt = judokaRepository.findByIdWithDetails(parametroId);
            if (opt.isPresent()) {
                Judoka j = opt.get();
                // Verificar permisos (acudiente, sensei, master)
                Usuario usuarioActual = securityService.getAuthenticatedUsuario().orElse(null);
                if (usuarioActual == null) {
                    event.rerouteTo("login");
                    return;
                }
                boolean puedeVer = false;
                if (securityService.isSensei()) {
                    puedeVer = j.getSensei().getUsuario().equals(usuarioActual);
                } else if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_ACUDIENTE"))) {
                    puedeVer = j.getAcudiente().equals(usuarioActual);
                } else if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("ROLE_MASTER"))) {
                    puedeVer = true;
                } else {
                    puedeVer = j.getUsuario().equals(usuarioActual);
                }
                if (!puedeVer) {
                    Notification.show("No tienes permiso para ver este perfil");
                    event.rerouteTo("dashboard-judoka");
                    return;
                }
                judokaActual = j;
                construirVista();
                return;
            } else {
                Notification.show("Judoka no encontrado");
                event.rerouteTo("dashboard-judoka");
                return;
            }
        }

        // Caso 2: Sin parámetro, intentar con ID de sesión (magic link)
        Long sessionId = (Long) VaadinSession.getCurrent().getAttribute("JUDOKA_ACTUAL_ID");
        if (sessionId != null) {
            Optional<Judoka> opt = judokaRepository.findByIdWithDetails(sessionId);
            if (opt.isPresent()) {
                judokaActual = opt.get();
                construirVista();
                return;
            } else {
                // ID inválido, limpiar y continuar
                VaadinSession.getCurrent().setAttribute("JUDOKA_ACTUAL_ID", null);
            }
        }

        // Caso 3: Usuario autenticado normal
        judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("No hay judoka autenticado"));
        construirVista();
    }

    private void construirVista() {
        removeAll(); // Limpiar por si acaso

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMaxWidth("1000px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        mainLayout.add(crearSeccionSabiduria());

        // Crear TabSheet
        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();

        // Pestaña Resumen (contenido actual)
        VerticalLayout resumenLayout = new VerticalLayout();
        resumenLayout.setSpacing(true);
        resumenLayout.setPadding(false);

        FlexLayout contentLayout = new FlexLayout();
        contentLayout.setWidthFull();
        contentLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contentLayout.getStyle().set("gap", "30px");

        contentLayout.add(crearTarjetaIdentidad(), crearBitacoraReflexion());

        // Sección de antropometría
        Div antropometriaSection = new Div();
        antropometriaSection.setWidthFull();
        antropometriaSection.add(new H3(traduccionService.get("perfil.antropometria", "Antropometría histórica")));

        List<DatosAntropometricosDTO> historial = perfilService.getHistorialAntropometrico(judokaActual);
        if (historial.isEmpty()) {
            antropometriaSection.add(new Span(traduccionService.get("perfil.sin_datos", "Sin datos antropométricos")));
        } else {
            ApexCharts chart = crearGraficoAntropometria(historial);
            chart.setWidthFull();
            chart.setHeight("300px");
            antropometriaSection.add(chart);
        }

        resumenLayout.add(contentLayout, antropometriaSection);
        tabSheet.add(traduccionService.get("perfil.tab.resumen", "Resumen"), resumenLayout);

        // Añadir las demás pestañas (solo si hay datos o siempre)
        tabSheet.add(traduccionService.get("perfil.tab.pruebas", "Pruebas"), crearPruebasTab());
        tabSheet.add(traduccionService.get("perfil.tab.tareas", "Tareas"), crearTareasTab());
        tabSheet.add(traduccionService.get("perfil.tab.insignias", "Insignias"), crearInsigniasTab());
        tabSheet.add(traduccionService.get("perfil.tab.palmares", "Palmarés"), crearPalmaresTab());
        tabSheet.add(traduccionService.get("perfil.tab.documentos", "Documentos"), crearDocumentosTab());

        mainLayout.add(tabSheet);
        setContent(mainLayout);
    }
    private ApexCharts crearGraficoAntropometria(List<DatosAntropometricosDTO> historial) {
        List<String> fechas = historial.stream().map(d -> d.getFecha().toString()).collect(Collectors.toList());
        List<Double> pesos = historial.stream().map(DatosAntropometricosDTO::getPeso).collect(Collectors.toList());
        List<Double> estaturas = historial.stream().map(DatosAntropometricosDTO::getEstatura).collect(Collectors.toList());
        List<Double> imcs = historial.stream().map(DatosAntropometricosDTO::getImc).collect(Collectors.toList());

        // Crear título para el eje Y
        com.github.appreciated.apexcharts.config.yaxis.Title yTitle = new com.github.appreciated.apexcharts.config.yaxis.Title();
        yTitle.setText("Valores");

        return ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withHeight("300px")
                        .withToolbar(ToolbarBuilder.get().withShow(true).build())
                        .build())
                .withSeries(
                        new Series<>("Peso (kg)", pesos.toArray(new Double[0])),
                        new Series<>("Estatura (cm)", estaturas.toArray(new Double[0])),
                        new Series<>("IMC", imcs.toArray(new Double[0]))
                )
                .withXaxis(XAxisBuilder.get().withCategories(fechas.toArray(new String[0])).build())
                .withYaxis(YAxisBuilder.get().withTitle(yTitle).build())
                .withLegend(LegendBuilder.get().withPosition(Position.TOP).build())
                .build();
    }
}