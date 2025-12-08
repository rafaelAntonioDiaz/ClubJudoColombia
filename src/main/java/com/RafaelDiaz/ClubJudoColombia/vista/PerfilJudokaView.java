package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Reflexion;
import com.RafaelDiaz.ClubJudoColombia.servicio.FileStorageService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.util.BibliotecaSabiduria;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
// --- IMPORTS LIMPIOS ---
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
// USAMOS LA NUEVA API DE VAADIN 24.8
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
// -----------------------
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("perfil-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Mi Santuario | Club Judo Colombia")
public class PerfilJudokaView extends JudokaLayout {

    private final SecurityService securityService;
    private final JudokaService judokaService;
    private final TraduccionService traduccionService;
    private final FileStorageService fileStorageService;

    private Judoka judokaActual;
    private Image avatarImage; // Usamos Image en lugar de Avatar para soportar DownloadHandler
    private Div avatarContainer;

    @Autowired
    public PerfilJudokaView(SecurityService securityService,
                            AccessAnnotationChecker accessChecker,
                            JudokaService judokaService,
                            TraduccionService traduccionService,
                            FileStorageService fileStorageService) {
        super(securityService, accessChecker);
        this.securityService = securityService;
        this.judokaService = judokaService;
        this.traduccionService = traduccionService;
        this.fileStorageService = fileStorageService;

        this.judokaActual = securityService.getAuthenticatedJudoka()
                .orElseThrow(() -> new RuntimeException("Judoka no encontrado"));

        addClassName("perfil-view");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMaxWidth("1000px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        mainLayout.add(crearSeccionSabiduria());

        FlexLayout contentLayout = new FlexLayout();
        contentLayout.setWidthFull();
        contentLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contentLayout.getStyle().set("gap", "30px");

        contentLayout.add(crearTarjetaIdentidad(), crearBitacoraReflexion());

        mainLayout.add(contentLayout);
        setContent(mainLayout);
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

        String nombre = judokaActual.getUsuario().getNombre();

        // 1. Contenedor de Avatar (Imagen Circular)
        avatarContainer = new Div();
        avatarContainer.setWidth("120px");
        avatarContainer.setHeight("120px");
        avatarContainer.getStyle()
                .set("border-radius", "50%") // Círculo perfecto
                .set("overflow", "hidden")
                .set("border", "4px solid white")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)")
                .set("background-color", "#eee")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        cargarImagenEnAvatar(); // Lógica con DownloadHandler

        // 2. Upload (Botón)
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/jpg");
        upload.setMaxFiles(1);
        upload.setDropAllowed(false);

        Button uploadBtn = new Button(new Icon(VaadinIcon.CAMERA));
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        uploadBtn.setTooltipText("Cambiar foto");
        upload.setUploadButton(uploadBtn);

        // --- HANDLER MODERNO ---
        upload.setUploadHandler(event -> {
            try {
                judokaService.actualizarFotoPerfil(judokaActual, event.getInputStream(), event.getFileName());
                getUI().ifPresent(ui -> ui.access(() -> {
                    cargarImagenEnAvatar(); // Recargar usando la nueva API
                    Notification.show("Foto actualizada", 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    upload.clearFileList();
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show("Error: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR)
                ));
            }
        });

        Div avatarWrapper = new Div(avatarContainer, upload);
        avatarWrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("align-items", "center");

        H2 nombreH2 = new H2(nombre + " " + judokaActual.getUsuario().getApellido());
        nombreH2.getStyle().set("margin-bottom", "0").set("text-align", "center");

        Span cinturonBadge = new Span(judokaActual.getGrado().toString());
        cinturonBadge.getElement().getThemeList().add("badge");
        cinturonBadge.getStyle().set("background-color", "#2C3E50").set("color", "white").set("padding", "0.5em 1em").set("font-size", "0.9rem");

        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);
        stats.getStyle().set("margin-top", "20px");

        stats.add(crearStatItem(VaadinIcon.SCALE, judokaActual.getPeso() + " kg", "Peso"));
        stats.add(crearStatItem(VaadinIcon.ARROWS_LONG_V, judokaActual.getEstatura() + " cm", "Altura"));
        stats.add(crearStatItem(VaadinIcon.CALENDAR_USER, String.valueOf(judokaActual.getEdad()), "Edad"));

        layout.add(avatarWrapper, nombreH2, cinturonBadge, stats);
        return layout;
    }

    /**
     * Carga la imagen usando la API moderna DownloadHandler de Vaadin 24.8.
     * Reemplaza StreamResource obsoleto.
     */
    private void cargarImagenEnAvatar() {
        avatarContainer.removeAll();
        String rutaFoto = judokaActual.getUrlFotoPerfil();

        if (rutaFoto != null && !rutaFoto.isEmpty()) {
            // Creamos el Handler para servir el archivo
            DownloadHandler handler = DownloadHandler.fromInputStream(context -> {
                try {
                    // Nota: Asegúrate que fileStorageService guarde en una ruta accesible
                    // Aquí asumimos "uploads/" relativo
                    Path path = Path.of("uploads", rutaFoto);
                    return new DownloadResponse(new FileInputStream(path.toFile()), rutaFoto, Files.probeContentType(path), Files.size(path));
                } catch (Exception e) {
                    return new DownloadResponse(new ByteArrayInputStream(new byte[0]), "error", "application/octet-stream", 0);
                }
            }).inline(); // .inline() es clave para mostrarla y no descargarla

            // Usamos Image que SÍ soporta DownloadHandler en 24.8
            avatarImage = new Image(handler, "Foto Perfil");
            avatarImage.setWidth("100%");
            avatarImage.setHeight("100%");
            avatarImage.getStyle().set("object-fit", "cover"); // Ajuste perfecto
            avatarContainer.add(avatarImage);
        } else {
            // Fallback: Icono o iniciales
            Avatar placeholder = new Avatar(judokaActual.getUsuario().getNombre());
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

    // --- BITÁCORA ---
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

        Button btnPublicar = new Button("Registrar Pensamiento", new Icon(VaadinIcon.PENCIL));
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
            Span empty = new Span("Tu diario está vacío. Empieza hoy.");
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
                editDialog.setHeaderTitle("Editar Reflexión");

                TextArea editArea = new TextArea();
                editArea.setValue(ref.getContenido());
                editArea.setWidth("100%");
                editArea.setHeight("200px");

                Button save = new Button("Guardar Cambios", ev -> {
                    try {
                        judokaService.editarReflexion(ref, editArea.getValue());
                        editDialog.close();
                        refrescarTimeline(containerPadre);
                        Notification.show("Entrada actualizada.");
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage());
                    }
                });
                save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                Button cancel = new Button("Cancelar", ev -> editDialog.close());

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
            lock.setTooltipText("Registro permanente");
            card.add(lock);
        }
        return card;
    }
}