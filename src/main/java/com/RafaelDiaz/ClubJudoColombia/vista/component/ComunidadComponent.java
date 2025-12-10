package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.RafaelDiaz.ClubJudoColombia.modelo.Comentario;
import com.RafaelDiaz.ClubJudoColombia.modelo.MensajeChat;
import com.RafaelDiaz.ClubJudoColombia.modelo.Publicacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
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
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Componente reutilizable que contiene toda la lógica de la Comunidad.
 * Se puede incrustar en cualquier Layout (Sensei o Judoka).
 */
public class ComunidadComponent extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(ComunidadComponent.class);

    private final SecurityService securityService;
    private final TraduccionService traduccionService;
    private final FileStorageService fileStorageService;
    private final PublicacionService publicacionService;
    private final ChatService chatService;

    private VerticalLayout feedLayout;
    private VerticalLayout listaPublicacionesLayout;
    private VerticalLayout chatLayout;
    private MessageList messageList;
    private String ultimoArchivoSubido;

    public ComunidadComponent(SecurityService securityService,
                              TraduccionService traduccionService,
                              FileStorageService fileStorageService,
                              PublicacionService publicacionService,
                              ChatService chatService) {
        this.securityService = securityService;
        this.traduccionService = traduccionService;
        this.fileStorageService = fileStorageService;
        this.publicacionService = publicacionService;
        this.chatService = chatService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // --- PESTAÑAS DE NAVEGACIÓN ---
        Tab tabMuro = new Tab(VaadinIcon.GRID_BIG.create(), new Span(" " + traduccionService.get("comunidad.tab.muro")));
        Tab tabChat = new Tab(VaadinIcon.CHAT.create(), new Span(" " + traduccionService.get("comunidad.tab.chat")));
        Tabs tabs = new Tabs(tabMuro, tabChat);
        tabs.setWidthFull();
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);

        // --- CONTENEDORES ---
        crearFeedLayout();
        crearChatLayout();
        chatLayout.setVisible(false);

        // --- LÓGICA DE CAMBIO ---
        tabs.addSelectedChangeListener(event -> {
            boolean esMuro = event.getSelectedTab().equals(tabMuro);
            feedLayout.setVisible(esMuro);
            chatLayout.setVisible(!esMuro);
            if (!esMuro) cargarMensajesChat();
        });

        add(tabs, feedLayout, chatLayout);
    }

    // ================================================================
    // SECCIÓN MURO (FEED)
    // ================================================================

    private void crearFeedLayout() {
        feedLayout = new VerticalLayout();
        feedLayout.setSizeFull();
        feedLayout.getStyle().set("background-color", "var(--judo-gris-fondo)");
        feedLayout.setAlignItems(Alignment.CENTER);
        feedLayout.getStyle().set("overflow-y", "auto");

        // --- TARJETA DE CREAR POST ---
        Div crearPostCard = new Div();
        crearPostCard.addClassName("card-blanca");
        crearPostCard.setWidth("100%");
        crearPostCard.setMaxWidth("600px");
        crearPostCard.getStyle().set("padding", "20px").set("margin-top", "20px");

        TextArea textoPost = new TextArea();
        textoPost.setPlaceholder(traduccionService.get("comunidad.post.placeholder"));
        textoPost.setWidthFull();
        textoPost.setMaxHeight("150px");

        // Configuración de Upload (Vaadin 24.8 Handler)
        Upload upload = new Upload();
        upload.setUploadHandler(event -> {
            try {
                String fileName = fileStorageService.save(event.getInputStream(), event.getFileName());
                getUI().ifPresent(ui -> ui.access(() -> {
                    this.ultimoArchivoSubido = fileName;
                    Notification.show(traduccionService.get("comunidad.msg.archivo_listo") + ": " + fileName)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));
            } catch (IOException e) {
                logger.error("Error subiendo archivo", e);
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("error.upload")).addThemeVariants(NotificationVariant.LUMO_ERROR)));
            }
        });
        upload.setAcceptedFileTypes("image/*", "video/*");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(traduccionService.get("comunidad.btn.subir_foto"), new Icon(VaadinIcon.CAMERA)));
        upload.setDropLabel(new Span(traduccionService.get("comunidad.label.drop")));

        Button btnPublicar = new Button(traduccionService.get("comunidad.btn.publicar"), new Icon(VaadinIcon.PAPERPLANE));
        btnPublicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublicar.setWidthFull();

        btnPublicar.addClickListener(e -> publicarPost(textoPost, upload));

        VerticalLayout layoutTarjeta = new VerticalLayout(textoPost, upload, btnPublicar);
        layoutTarjeta.setPadding(false);
        crearPostCard.add(layoutTarjeta);

        // --- LISTA DE PUBLICACIONES ---
        listaPublicacionesLayout = new VerticalLayout();
        listaPublicacionesLayout.setWidthFull();
        listaPublicacionesLayout.setAlignItems(Alignment.CENTER);
        listaPublicacionesLayout.setPadding(false);

        feedLayout.add(crearPostCard, listaPublicacionesLayout);
        cargarPublicaciones();
    }

    private void publicarPost(TextArea textoPost, Upload upload) {
        if (!textoPost.isEmpty() || ultimoArchivoSubido != null) {
            try {
                Usuario autor = securityService.getAuthenticatedUsuario().orElseThrow();
                Publicacion nueva = new Publicacion(autor, textoPost.getValue(), ultimoArchivoSubido);
                publicacionService.guardar(nueva);

                Notification.show(traduccionService.get("comunidad.msg.publicado")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                textoPost.clear();
                upload.clearFileList();
                ultimoArchivoSubido = null;
                cargarPublicaciones();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show(traduccionService.get("comunidad.warn.empty_post"));
        }
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

        // Header Post
        String nombreAutor = post.getAutor().getNombre() + " " + post.getAutor().getApellido();
        Avatar avatar = new Avatar(nombreAutor);
        avatar.setColorIndex(Math.abs(nombreAutor.hashCode()) % 7);
        Span nombre = new Span(nombreAutor);
        nombre.getStyle().set("font-weight", "bold").set("margin-left", "10px");
        Span fecha = new Span(post.getFecha().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
        fecha.addClassName("text-muted");
        fecha.getStyle().set("font-size", "0.8rem").set("margin-left", "auto");

        HorizontalLayout header = new HorizontalLayout(avatar, nombre, fecha);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        // Contenido
        Span texto = new Span(post.getContenido());
        texto.getStyle().set("display", "block").set("margin", "15px 0");

        Div mediaContainer = new Div();
        if (post.getImagenUrl() != null && !post.getImagenUrl().isEmpty()) {
            // Manejo de imagen con DownloadHandler 24.8
            DownloadHandler imageHandler = DownloadHandler.fromInputStream(context -> {
                try {
                    File file = new File("uploads/" + post.getImagenUrl());
                    String mimeType = Files.probeContentType(file.toPath());
                    if (mimeType == null) mimeType = "application/octet-stream";
                    return new DownloadResponse(new FileInputStream(file), post.getImagenUrl(), mimeType, file.length());
                } catch (Exception e) {
                    return new DownloadResponse(new ByteArrayInputStream(new byte[0]), "error", "application/octet-stream", 0);
                }
            }).inline();
            Image img = new Image(imageHandler, "Imagen Post");
            img.setWidth("100%");
            img.getStyle().set("border-radius", "12px");
            mediaContainer.add(img);
        }

        // Acciones
        Button btnLike = new Button(String.valueOf(post.getLikes()), new Icon(VaadinIcon.HEART));
        btnLike.addClickListener(e -> {
            publicacionService.darLike(post);
            btnLike.setText(String.valueOf(post.getLikes()));
            btnLike.addThemeVariants(ButtonVariant.LUMO_ERROR);
        });

        Button btnComentar = new Button(traduccionService.get("comunidad.btn.comentar"), new Icon(VaadinIcon.COMMENT_O));

        // Sección Comentarios
        VerticalLayout comentariosLayout = crearSeccionComentarios(post);
        btnComentar.addClickListener(e -> comentariosLayout.setVisible(!comentariosLayout.isVisible()));

        HorizontalLayout actions = new HorizontalLayout(btnLike, btnComentar);
        card.add(header, texto, mediaContainer, actions, comentariosLayout);
        return card;
    }

    private VerticalLayout crearSeccionComentarios(Publicacion post) {
        VerticalLayout layout = new VerticalLayout();
        layout.setVisible(false);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("background-color", "#f5f5f5").set("border-radius", "10px").set("padding", "10px");

        VerticalLayout lista = new VerticalLayout();
        lista.setPadding(false);
        lista.setSpacing(false);

        // Cargar comentarios
        publicacionService.obtenerComentarios(post).forEach(c -> lista.add(crearFilaComentario(c)));

        TextField input = new TextField();
        input.setPlaceholder(traduccionService.get("comunidad.comment.placeholder"));
        input.setWidthFull();
        Button btnEnviar = new Button(new Icon(VaadinIcon.PAPERPLANE));
        btnEnviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        btnEnviar.addClickListener(ev -> {
            if (!input.isEmpty()) {
                try {
                    Usuario yo = securityService.getAuthenticatedUsuario().orElseThrow();
                    Comentario nuevo = publicacionService.comentar(post, yo, input.getValue());
                    lista.add(crearFilaComentario(nuevo));
                    input.clear();
                } catch (Exception ex) { Notification.show(ex.getMessage()); }
            }
        });

        layout.add(lista, new HorizontalLayout(input, btnEnviar));
        return layout;
    }

    private Component crearFilaComentario(Comentario c) {
        Span autor = new Span(c.getAutor().getNombre() + ": ");
        autor.getStyle().set("font-weight", "bold");
        return new Div(autor, new Span(c.getContenido()));
    }

    // ================================================================
    // SECCIÓN CHAT
    // ================================================================

    private void crearChatLayout() {
        chatLayout = new VerticalLayout();
        chatLayout.setSizeFull();
        messageList = new MessageList();
        messageList.setSizeFull();

        MessageInput input = new MessageInput();
        input.setWidthFull();
        MessageInputI18n i18n = new MessageInputI18n();
        i18n.setMessage(traduccionService.get("comunidad.chat.escribir"));
        i18n.setSend(traduccionService.get("comunidad.chat.enviar"));
        input.setI18n(i18n);

        input.addSubmitListener(e -> {
            if (e.getValue() != null && !e.getValue().isBlank()) {
                try {
                    Usuario autor = securityService.getAuthenticatedUsuario().orElseThrow();
                    chatService.enviarMensaje(autor, e.getValue());
                    cargarMensajesChat();
                } catch (Exception ex) { Notification.show("Error envío").addThemeVariants(NotificationVariant.LUMO_ERROR); }
            }
        });

        chatLayout.add(messageList, input);
        chatLayout.expand(messageList);
    }

    private void cargarMensajesChat() {
        List<MensajeChat> historial = chatService.obtenerHistorialChat();
        List<MessageListItem> items = historial.stream().map(msg -> {
            MessageListItem item = new MessageListItem(msg.getContenido(),
                    msg.getFecha().toInstant(ZoneOffset.UTC),
                    msg.getAutor().getNombre());
            item.setUserColorIndex(Math.abs(msg.getAutor().getNombre().hashCode()) % 7);
            return item;
        }).collect(Collectors.toList());
        messageList.setItems(items);
    }
}