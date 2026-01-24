package com.RafaelDiaz.ClubJudoColombia.vista.aspirante;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@PageTitle("Completar Perfil | Club Judo Colombia")
@Route("completar-perfil")
@RolesAllowed("ROLE_JUDOKA")
public class AsistenteAdmisionView extends VerticalLayout {

    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService;
    private final AlmacenamientoCloudService almacenamientoCloudService;

    private final Judoka judokaActual;

    private final VerticalLayout contenidoPaso = new VerticalLayout();
    private int pasoActual = 1;

    private final DatePicker fechaNacimientoField = new DatePicker();
    private final NumberField pesoField = new NumberField();

    public AsistenteAdmisionView(AdmisionesService admisionesService,
                                 TraduccionService traduccionService,
                                 AlmacenamientoCloudService almacenamientoCloudService) {
        this.admisionesService = admisionesService;
        this.traduccionService = traduccionService;
        this.almacenamientoCloudService = almacenamientoCloudService;

        this.judokaActual = new Judoka();
        this.judokaActual.setId(1L);

        configurarVista();
        renderizarPaso();
    }

    private void configurarVista() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        contenidoPaso.setMaxWidth("600px");
        add(new H2(traduccionService.get("vista.wizard.titulo")), contenidoPaso);
    }

    private void renderizarPaso() {
        contenidoPaso.removeAll();

        switch (pasoActual) {
            case 1 -> mostrarPaso1DatosPersonales();
            case 2 -> mostrarPaso2Documentos();
            case 3 -> mostrarPaso3Confirmacion();
        }
    }

    private void mostrarPaso1DatosPersonales() {
        H3 tituloPaso = new H3("Paso 1: Datos Físicos");
        Paragraph desc = new Paragraph("Para competir, necesitamos conocer tu categoría.");

        fechaNacimientoField.setLabel(traduccionService.get("label.fecha_nacimiento"));
        pesoField.setLabel(traduccionService.get("label.peso_kg"));
        pesoField.setSuffixComponent(new Span("kg"));

        FormLayout formLayout = new FormLayout(fechaNacimientoField, pesoField);

        Button btnSiguiente = new Button("Siguiente Paso", VaadinIcon.ARROW_RIGHT.create());
        btnSiguiente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSiguiente.addClickListener(e -> {
            if(pesoField.isEmpty() || fechaNacimientoField.isEmpty()){
                Notification.show("Por favor completa los campos", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            pasoActual = 2;
            renderizarPaso();
        });

        contenidoPaso.add(tituloPaso, desc, formLayout, btnSiguiente);
    }

    // --- PASO 2: DOCUMENTACIÓN CORREGIDO (Vaadin 24.8+) ---
// --- PASO 2: DOCUMENTACIÓN (Waiver + EPS) ---
    private void mostrarPaso2Documentos() {
        H3 tituloPaso = new H3(traduccionService.get("vista.wizard.paso2.titulo")); // "Paso 2: Documentos Obligatorios"
        Paragraph desc = new Paragraph(traduccionService.get("vista.wizard.paso2.desc")); // "Sube tu Exoneración (Waiver) y Certificado de EPS en PDF."

        // 1. UPLOAD: WAIVER (Exoneración de Responsabilidad)
        Upload uploadWaiver = configurarUploadComponente(
                "msg.waiver.instruccion",
                TipoDocumento.WAIVER
        );

        // 2. UPLOAD: EPS (Certificado Médico/Salud)
        // Asumimos que tienes TipoDocumento.EPS_CERTIFICADO o CERTIFICADO_MEDICO en tu Enum
        Upload uploadEps = configurarUploadComponente(
                "msg.eps.instruccion",
                TipoDocumento.CERTIFICADO_MEDICO
        );

        // Maquetación en dos columnas para pantallas grandes
        FormLayout gridDocumentos = new FormLayout();
        gridDocumentos.add(uploadWaiver, uploadEps);
        gridDocumentos.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Botones de Navegación
        HorizontalLayout botonesNavegacion = new HorizontalLayout();
        Button btnAtras = new Button(traduccionService.get("btn.atras"), VaadinIcon.ARROW_LEFT.create(), e -> { pasoActual = 1; renderizarPaso(); });
        Button btnFinalizar = new Button(traduccionService.get("btn.finalizar"), VaadinIcon.CHECK.create(), e -> { pasoActual = 3; renderizarPaso(); });
        btnFinalizar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        botonesNavegacion.add(btnAtras, btnFinalizar);

        contenidoPaso.add(tituloPaso, desc, gridDocumentos, botonesNavegacion);
    }

    private void mostrarPaso3Confirmacion() {
        VaadinIcon icon = VaadinIcon.CHECK_CIRCLE;
        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", "var(--lumo-success-color)").set("font-size", "48px");

        H3 titulo = new H3("¡Solicitud Completada!");
        Paragraph mensaje = new Paragraph("Has terminado. El Sensei revisará tu documentación y te notificará cuando estés ACTIVO para entrenar.");

        Button btnDashboard = new Button("Ir al Dashboard", e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));

        contenidoPaso.setHorizontalComponentAlignment(Alignment.CENTER, iconSpan, titulo, mensaje, btnDashboard);
        contenidoPaso.add(iconSpan, titulo, mensaje, btnDashboard);
    }
    /**
     * Método Helper para no repetir código. Crea un Upload moderno y lo conecta a la Nube.
     */
    private Upload configurarUploadComponente(String i18nLabelKey, TipoDocumento tipoDoc) {
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("application/pdf");
        upload.setDropLabel(new Span(traduccionService.get(i18nLabelKey)));

        // Manejador moderno (Vaadin 24.8+)
        upload.setUploadHandler((UploadEvent event) -> {
            String fileName = event.getFileName();

            try (InputStream in = event.getInputStream();
                 OutputStream out = almacenamientoCloudService.crearStreamDeSalida(judokaActual.getId(), fileName)) {

                in.transferTo(out);

                // Guardamos la URL en la base de datos
                String urlEnLaNube = almacenamientoCloudService.obtenerUrl(judokaActual.getId(), fileName);
                admisionesService.cargarRequisito(judokaActual, tipoDoc, urlEnLaNube);

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show(traduccionService.get("msg.exito.archivo_subido"), 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));

            } catch (IOException ex) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show(traduccionService.get("error.upload") + ": " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        });

        return upload;
    }
}