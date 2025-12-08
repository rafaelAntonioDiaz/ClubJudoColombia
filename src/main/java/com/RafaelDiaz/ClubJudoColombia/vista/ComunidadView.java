package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Comentario;
import com.RafaelDiaz.ClubJudoColombia.modelo.MensajeChat;
import com.RafaelDiaz.ClubJudoColombia.modelo.Publicacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
// Import correcto para I18n
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
// --- IMPORTS CRÍTICOS VAADIN 24.8 ---
// La nueva API de handlers está en el paquete 'streams'
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
// ------------------------------------
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route("comunidad-judoka")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_COMPETIDOR"})
@PageTitle("Comunidad | Club Judo Colombia")
public class ComunidadView extends JudokaLayout {

    private static final Logger logger = LoggerFactory.getLogger(ComunidadView.class);
    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final FileStorageService fileStorageService;
    private final PublicacionService publicacionService;
    private final ChatService chatService; // --- 1. NUEVO SERVICIO INYECTADO ---

    private VerticalLayout feedLayout;
    private VerticalLayout listaPublicacionesLayout;
    private VerticalLayout chatLayout;
    private MessageList messageList;
    private String ultimoArchivoSubido;

    @Autowired
    public ComunidadView(SecurityService securityService,
                         AccessAnnotationChecker accessChecker,
                         TraduccionService traduccionService,
                         FileStorageService fileStorageService,
                         PublicacionService publicacionService,
                         ChatService chatService) { // --- AGREGAR AL CONSTRUCTOR ---
        super(securityService, accessChecker);
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.fileStorageService = fileStorageService;
        this.publicacionService = publicacionService;
        this.chatService = chatService; // --- ASIGNAR ---

        addClassName("comunidad-view");

        // Pestañas
        Tab tabMuro = new Tab(VaadinIcon.GRID_BIG.create(),
                new Span(" " + traduccionService.get("comunidad.tab.muro")));
        Tab tabChat = new Tab(VaadinIcon.CHAT.create(),
                new Span(" " + traduccionService.get("comunidad.tab.chat")));
        Tabs tabs = new Tabs(tabMuro, tabChat);
        tabs.setWidthFull();
        tabs.addThemeVariants(com.vaadin.flow.component.tabs.TabsVariant.LUMO_EQUAL_WIDTH_TABS);

        // Crear layouts
        crearFeedLayout();
        crearChatLayout(); // Este método ahora usa BD
        chatLayout.setVisible(false);

        // Lógica de cambio de pestaña
        tabs.addSelectedChangeListener(event -> {
            boolean esMuro = event.getSelectedTab().equals(tabMuro);
            feedLayout.setVisible(esMuro);
            chatLayout.setVisible(!esMuro);

            // Si entramos al chat, refrescamos los mensajes y hacemos scroll al final
            if (!esMuro) {
                cargarMensajesChat();
            }
        });

        VerticalLayout content = new VerticalLayout(tabs, feedLayout, chatLayout);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        setContent(content);
    }

    private void crearFeedLayout() {
        feedLayout = new VerticalLayout();
        feedLayout.setSizeFull();
        feedLayout.getStyle().set("background-color", "var(--judo-gris-fondo)");
        feedLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        feedLayout.getStyle().set("overflow-y", "auto");

        Div crearPostCard = new Div();
        crearPostCard.addClassName("card-blanca");
        crearPostCard.setWidth("100%");
        crearPostCard.setMaxWidth("600px");
        crearPostCard.getStyle().set("padding", "20px").set("margin-top", "20px");

        TextArea textoPost = new TextArea();
        textoPost.setPlaceholder(traduccionService.get("comunidad.post.placeholder"));
        textoPost.setWidthFull();
        textoPost.setMaxHeight("150px");

        // --- UPLOAD HANDLER 24.8 ---
        Upload upload = new Upload();

        // Implementamos UploadHandler. La lógica de éxito va AQUÍ DENTRO.
        upload.setUploadHandler(event -> {
            try {
                // 1. Guardar el archivo usando el stream del evento
                String fileName = fileStorageService.save(event.getInputStream(),
                        event.getFileName());

                // 2. Actualizar UI (Necesario UI.access porque esto corre en otro hilo)
                getUI().ifPresent(ui -> ui.access(() -> {
                    this.ultimoArchivoSubido = fileName;
                    Notification.show(traduccionService.get(
                            "comunidad.msg.archivo_listo") + ": " + fileName)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));
            } catch (IOException e) {
                logger.error("Error subiendo archivo", e);
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("error.upload")
                                + ": " + e.getMessage())
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)));
                // Importante: No lanzar RuntimeException aquí para no romper el hilo de Vaadin silenciosamente
            }
        });

        upload.setAcceptedFileTypes("image/*", "video/*");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(traduccionService.get(
                "comunidad.btn.subir_foto"), new Icon(VaadinIcon.CAMERA)));
        upload.setDropLabel(new Span(traduccionService.get(
                "comunidad.label.drop")));

        Button btnPublicar = new Button(traduccionService.get(
                "comunidad.btn.publicar"), new Icon(VaadinIcon.PAPERPLANE));
        btnPublicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublicar.setWidthFull();

        btnPublicar.addClickListener(e -> {
            if (!textoPost.isEmpty() || ultimoArchivoSubido != null) {
                try {
                    Usuario autor = securityService.getAuthenticatedUsuario()
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                    Publicacion nuevaPublicacion = new Publicacion(autor,
                            textoPost.getValue(), ultimoArchivoSubido);
                    publicacionService.guardar(nuevaPublicacion);

                    Notification.show(traduccionService.get("comunidad.msg.publicado"),
                                    3000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    textoPost.clear();
                    upload.clearFileList();
                    ultimoArchivoSubido = null;

                    cargarPublicaciones();

                } catch (Exception ex) {
                    Notification.show("Error al publicar: " + ex.getMessage(),
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show(traduccionService.get("comunidad.warn.empty_post"),
                        3000, Notification.Position.MIDDLE);            }
        });

        VerticalLayout layoutTarjeta = new VerticalLayout(textoPost, upload, btnPublicar);
        layoutTarjeta.setPadding(false);
        crearPostCard.add(layoutTarjeta);

        feedLayout.add(crearPostCard);

        listaPublicacionesLayout = new VerticalLayout();
        listaPublicacionesLayout.setWidthFull();
        listaPublicacionesLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        listaPublicacionesLayout.setPadding(false);

        feedLayout.add(listaPublicacionesLayout);

        cargarPublicaciones();
    }

    private void cargarPublicaciones() {
        listaPublicacionesLayout.removeAll();
        List<Publicacion> publicaciones = publicacionService.obtenerTodas();
        for (Publicacion p : publicaciones) {
            listaPublicacionesLayout.add(crearTarjetaPublicacion(p));
        }
    }

    private Component crearTarjetaPublicacion(Publicacion post) {
        Div card = new Div();
        card.addClassName("card-blanca");
        card.setWidth("100%");
        card.setMaxWidth("600px");
        card.getStyle().set("padding", "20px").set("margin-bottom", "20px");

        // --- Cabecera ---
        String nombreAutor = post.getAutor().getNombre() + " " + post.getAutor().getApellido();
        Avatar avatar = new Avatar(nombreAutor);
        avatar.setColorIndex(Math.abs(nombreAutor.hashCode()) % 7);
        Span nombre = new Span(nombreAutor);
        nombre.getStyle().set("font-weight", "bold").set("margin-left", "10px");

        String fechaStr = post.getFecha().format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
        Span fecha = new Span(fechaStr);
        fecha.addClassName("text-muted");
        fecha.getStyle().set("font-size", "0.8rem").set("margin-left", "auto");

        HorizontalLayout header = new HorizontalLayout(avatar, nombre, fecha);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        // --- Contenido ---
        Span texto = new Span(post.getContenido());
        texto.getStyle().set("display", "block").set("margin", "15px 0");

        Div mediaContainer = new Div();
        if (post.getImagenUrl() != null && !post.getImagenUrl().isEmpty()) {
            DownloadHandler imageHandler = DownloadHandler.
                    fromInputStream(context -> {
                try {
                    File file = new File("uploads/" + post.getImagenUrl());
                    String mimeType = Files.probeContentType(file.toPath());
                    if (mimeType == null) mimeType = "application/octet-stream";
                    return new DownloadResponse(new FileInputStream(file),
                            post.getImagenUrl(), mimeType, file.length());
                } catch (Exception e) {
                    return new DownloadResponse(new ByteArrayInputStream(new byte[0]), "error", "application/octet-stream", 0);
                }
            }).inline();
            Image img = new Image(imageHandler, traduccionService.get(
                    "comunidad.label.image_of") + " " + nombreAutor);
            img.setWidth("100%");
            img.getStyle().set("border-radius", "12px");
            mediaContainer.add(img);
        }

        // --- Botones de Acción ---
        Button btnLike = new Button(String.valueOf(post.getLikes()), new Icon(VaadinIcon.HEART));
        btnLike.addClickListener(e -> {
            publicacionService.darLike(post);
            btnLike.setText(String.valueOf(post.getLikes()));
            btnLike.addThemeVariants(ButtonVariant.LUMO_ERROR);
        });

        Button btnComentar = new Button(traduccionService.get("comunidad.btn.comentar"),
                new Icon(VaadinIcon.COMMENT_O));

        // --- SECCIÓN DE COMENTARIOS (LÓGICA NUEVA) ---
        VerticalLayout comentariosLayout = new VerticalLayout();
        comentariosLayout.setVisible(false); // Oculto por defecto
        comentariosLayout.setPadding(false);
        comentariosLayout.setSpacing(true);
        comentariosLayout.getStyle().set("background-color",
                "#f5f5f5").set("border-radius", "10px").set("padding", "10px");

        // 1. Contenedor para la lista de comentarios existentes
        VerticalLayout listaComentarios = new VerticalLayout();
        listaComentarios.setPadding(false);
        listaComentarios.setSpacing(false);

        // Cargar comentarios iniciales
        List<Comentario> comentariosExistentes =
                publicacionService.obtenerComentarios(post);
        for (Comentario c : comentariosExistentes) {
            listaComentarios.add(crearFilaComentario(c));
        }

        // 2. Input para nuevo comentario
        TextField inputComentario = new TextField();
        inputComentario.setPlaceholder(traduccionService.get("comunidad.comment.placeholder"));
        inputComentario.setWidthFull();
        Button btnEnviar = new Button(new Icon(VaadinIcon.PAPERPLANE));
        btnEnviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Lógica de envío real
        btnEnviar.addClickListener(ev -> {
            if (!inputComentario.isEmpty()) {
                try {
                    Usuario yo = securityService.getAuthenticatedUsuario().orElseThrow();

                    // Guardar en BD
                    Comentario nuevo = publicacionService.comentar(post,
                            yo, inputComentario.getValue());

                    // Actualizar UI inmediatamente (sin recargar todo)
                    listaComentarios.add(crearFilaComentario(nuevo));
                    inputComentario.clear();
                    Notification.show(traduccionService.get("comunidad.msg.comment_sent"),
                            2000, Notification.Position.BOTTOM_CENTER);
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            }
        });

        HorizontalLayout inputLayout = new HorizontalLayout(inputComentario, btnEnviar);
        inputLayout.setWidthFull();

        comentariosLayout.add(listaComentarios, inputLayout);

        // Toggle visibilidad
        btnComentar.addClickListener(e -> {
            boolean visible = !comentariosLayout.isVisible();
            comentariosLayout.setVisible(visible);
            if (visible) inputComentario.focus();
        });

        HorizontalLayout actions = new HorizontalLayout(btnLike, btnComentar);

        card.add(header, texto, mediaContainer, actions, comentariosLayout);
        return card;
    }

    // Método auxiliar para dibujar un comentario individual bonito
    private Component crearFilaComentario(Comentario c) {
        String autor = c.getAutor().getNombre();
        Span autorSpan = new Span(autor + ": ");
        autorSpan.getStyle().set("font-weight", "bold").set("font-size", "0.9em");

        Span textoSpan = new Span(c.getContenido());
        textoSpan.getStyle().set("font-size", "0.9em");

        Div fila = new Div(autorSpan, textoSpan);
        fila.getStyle().set("margin-bottom", "5px");
        return fila;
    }
    // --- SECCIÓN CHAT GRUPAL CON PERSISTENCIA ---
    private void crearChatLayout() {
        chatLayout = new VerticalLayout();
        chatLayout.setSizeFull();

        messageList = new MessageList();
        messageList.setSizeFull();

        // Carga inicial
        cargarMensajesChat();

        MessageInput input = new MessageInput();
        input.setWidthFull();

        // I18n para el input del chat (Alineado con traducción)
        MessageInputI18n i18n = new MessageInputI18n();
        i18n.setMessage(traduccionService.get("comunidad.chat.escribir"));
        i18n.setSend(traduccionService.get("comunidad.chat.enviar"));
        input.setI18n(i18n);

        // Listener de envío
        input.addSubmitListener(e -> {
            String texto = e.getValue();
            if (texto == null || texto.isBlank()) return;

            try {
                Usuario autor = securityService.getAuthenticatedUsuario()
                        .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));

                // 1. Guardar en BD
                chatService.enviarMensaje(autor, texto);

                // 2. Refrescar lista (simula tiempo real al recargar)
                cargarMensajesChat();

            } catch (Exception ex) {
                Notification.show("Error al enviar mensaje: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        chatLayout.add(messageList, input);
        chatLayout.expand(messageList);
    }

    /**
     * Carga el historial de mensajes desde la base de datos y los convierte
     * a objetos MessageListItem de Vaadin.
     */
    private void cargarMensajesChat() {
        List<MensajeChat> historial = chatService.obtenerHistorialChat();

        List<MessageListItem> itemsUi = historial.stream().map(msg -> {
            String nombreAutor = msg.getAutor().getNombre();
            Instant fechaInstant =
                    msg.getFecha().toInstant(ZoneOffset.UTC);// Ajusta zona horaria si es necesario

            MessageListItem item = new MessageListItem(
                    msg.getContenido(), fechaInstant, nombreAutor);

            // Asignar color de avatar basado en el nombre (para consistencia visual)
            item.setUserColorIndex(Math.abs(nombreAutor.hashCode()) % 7);

            // Opcional: Si el autor es el usuario actual,
            // podrías marcarlo visualmente
            // (Vaadin lo maneja automáticamente
            // si seteas el currentUser en el componente, pero esto basta por ahora)

            return item;
        }).collect(Collectors.toList());

        messageList.setItems(itemsUi);
    }}