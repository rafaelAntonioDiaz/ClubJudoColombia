package com.RafaelDiaz.ClubJudoColombia.vista.judoka;

import com.RafaelDiaz.ClubJudoColombia.modelo.*;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@PageTitle("Completar Perfil | Club Judo Colombia")
@Route("completar-perfil-judoka")
@RolesAllowed({"ROLE_JUDOKA_ADULTO", "ROLE_JUDOKA", "ROLE_ACUDIENTE", "ROLE_MASTER", "ROLE_SENSEI"})
public class CompletarPerfilJudokaView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final Logger log = LoggerFactory.getLogger(CompletarPerfilJudokaView.class);

    private final SecurityService securityService;
    private final AdmisionesService admisionesService;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;
    private final TraduccionService traduccionService;
    private final ConfiguracionService configuracionService;

    private Judoka judokaActual;
    private Long judokaId;

    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formDatos = new FormLayout();

    // Campos del formulario
    private final TextField nombre = new TextField();
    private final TextField apellido = new TextField();

    // ✅ FECHA DESGLOSADA: Solución para evitar errores de renderizado del DatePicker
    private final Span labelFecha = new Span();
    private final ComboBox<Integer> dia = new ComboBox<>();
    private final ComboBox<String> mes = new ComboBox<>();
    private final ComboBox<Integer> anio = new ComboBox<>();

    private final ComboBox<Sexo> sexo = new ComboBox<>();
    private final TextField peso = new TextField();
    private final TextField estatura = new TextField();
    private final TextField eps = new TextField();
    private final TextField nombreContactoEmergencia = new TextField();
    private final TextField telefonoEmergencia = new TextField();

    private final VerticalLayout seccionDocumentos = new VerticalLayout();
    private final VerticalLayout seccionPago = new VerticalLayout();

    private boolean waiverSubido = false;
    private boolean epsSubido = false;
    private boolean pagoSubido = false;
    private String urlWaiver = null;
    private String urlEps = null;
    private String urlComprobante = null;
    private final Paragraph textoMontoPago = new Paragraph();

    private final Button btnGuardar = new Button();

    public CompletarPerfilJudokaView(SecurityService securityService,
                                     AdmisionesService admisionesService,
                                     AlmacenamientoCloudService almacenamientoCloudService,
                                     JudokaRepository judokaRepository,
                                     TraduccionService traduccionService,
                                     ConfiguracionService configuracionService,
                                     FinanzasService finanzasService) {
        this.securityService = securityService;
        this.admisionesService = admisionesService;
        this.almacenamientoCloudService = almacenamientoCloudService;
        this.judokaRepository = judokaRepository;
        this.traduccionService = traduccionService;
        this.configuracionService = configuracionService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configuración de campos de solo lectura
        nombre.setLabel(traduccionService.get("label.nombre", "Nombre"));
        nombre.setReadOnly(true);
        apellido.setLabel(traduccionService.get("label.apellido", "Apellido"));
        apellido.setReadOnly(true);

        // ✅ Configuración de la etiqueta para la fecha de nacimiento
        labelFecha.setText(traduccionService.get("label.fecha_nacimiento", "Fecha de Nacimiento"));
        labelFecha.getStyle().set("font-weight", "bold");
        labelFecha.getStyle().set("color", "var(--lumo-secondary-text-color)");
        labelFecha.getStyle().set("margin-top", "0.5em");

        // ✅ Configuración de Fecha con 3 campos independientes
        dia.setLabel(traduccionService.get("label.dia", "Día"));
        dia.setItems(IntStream.rangeClosed(1, 31).boxed().collect(Collectors.toList()));
        dia.setWidthFull();

        mes.setLabel(traduccionService.get("label.mes", "Mes"));
        mes.setItems("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
        mes.setWidthFull();

        anio.setLabel(traduccionService.get("label.anio", "Año"));
        anio.setItems(IntStream.rangeClosed(1930, java.time.LocalDate.now().getYear()).boxed()
                .sorted(java.util.Collections.reverseOrder()).collect(Collectors.toList()));
        anio.setWidthFull();

        HorizontalLayout layoutFecha = new HorizontalLayout(dia, mes, anio);
        layoutFecha.setWidthFull();
        layoutFecha.setSpacing(true);
        layoutFecha.setFlexGrow(1, dia);
        layoutFecha.setFlexGrow(2, mes);
        layoutFecha.setFlexGrow(1, anio);

        sexo.setLabel(traduccionService.get("label.sexo"));
        sexo.setItems(Sexo.values());
        sexo.setItemLabelGenerator(s -> traduccionService.get("sexo." + s.name().toLowerCase()));
        sexo.setWidthFull();

        peso.setLabel(traduccionService.get("label.peso_kg"));
        peso.setWidthFull();
        estatura.setLabel(traduccionService.get("label.estatura_cm"));
        estatura.setWidthFull();
        eps.setLabel(traduccionService.get("label.eps"));
        eps.setWidthFull();
        nombreContactoEmergencia.setLabel(traduccionService.get("label.contacto_emergencia"));
        nombreContactoEmergencia.setWidthFull();
        telefonoEmergencia.setLabel(traduccionService.get("label.telefono_emergencia"));
        telefonoEmergencia.setWidthFull();

        // ✅ Configuración de FormLayout más tolerante para evitar colapso a 1 columna
        formDatos.setWidthFull();
        formDatos.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("300px", 2)
        );

        // Orden de los campos en el formulario
        formDatos.add(nombre, apellido);
        formDatos.add(labelFecha);
        formDatos.setColspan(labelFecha, 2);
        formDatos.add(layoutFecha);
        formDatos.setColspan(layoutFecha, 2);
        formDatos.add(sexo, peso, estatura, eps, nombreContactoEmergencia, telefonoEmergencia);
        btnGuardar.setText(traduccionService.get("boton.guardar_enviar_revision"));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> guardarYEnviarRevision());

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("800px");
        card.setMinWidth("350px"); // ✅ Forzar ancho mínimo
        card.setWidthFull();
        card.setPadding(true);
        card.setAlignItems(Alignment.STRETCH);

        card.add(titulo, descripcion, new H3(traduccionService.get("perfil.datos_personales")), formDatos);
        card.add(new H3(traduccionService.get("perfil.documentos")), seccionDocumentos);
        card.add(new H3(traduccionService.get("perfil.pago")), seccionPago);
        card.add(btnGuardar);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long judokaId) {
        if (judokaId == null) {
            judokaActual = securityService.getAuthenticatedJudoka()
                    .orElseThrow(() -> new RuntimeException("No se pudo identificar el judoka."));
        } else {
            this.judokaId = judokaId;
            judokaActual = judokaRepository.findByIdWithDetails(judokaId)
                    .orElseThrow(() -> new RuntimeException("Judoka no encontrado."));
        }
        cargarDatos();
    }

    private void cargarDatos() {
        Usuario usuarioActual = securityService.getAuthenticatedUsuario().orElse(null);
        if (usuarioActual == null) {
            getUI().ifPresent(ui -> ui.navigate(""));
            return;
        }

        boolean puedeEditar = (judokaActual.getAcudiente() != null && Objects.equals(usuarioActual.getId(), judokaActual.getAcudiente().getId()))
                || securityService.isMaster() || securityService.isSensei();

        if (!puedeEditar) {
            Notification.show("No tienes permiso para completar este perfil.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(""));
            return;
        }

        titulo.setText(traduccionService.get("perfil.completar.titulo", judokaActual.getNombre()));
        descripcion.setText(traduccionService.get("perfil.completar.descripcion"));

        // Cargar valores existentes
        nombre.setValue(judokaActual.getNombre() != null ? judokaActual.getNombre() : "");
        apellido.setValue(judokaActual.getApellido() != null ? judokaActual.getApellido() : "");

        if (judokaActual.getFechaNacimiento() != null) {
            java.time.LocalDate fn = judokaActual.getFechaNacimiento();
            dia.setValue(fn.getDayOfMonth());
            mes.setValue(obtenerNombreMes(fn.getMonthValue()));
            anio.setValue(fn.getYear());
        }

        if (judokaActual.getSexo() != null) sexo.setValue(judokaActual.getSexo());
        if (judokaActual.getPeso() != null) peso.setValue(String.valueOf(judokaActual.getPeso()));
        if (judokaActual.getEstatura() != null) estatura.setValue(String.valueOf(judokaActual.getEstatura()));
        eps.setValue(judokaActual.getEps() != null ? judokaActual.getEps() : "");
        nombreContactoEmergencia.setValue(judokaActual.getNombreContactoEmergencia() != null ? judokaActual.getNombreContactoEmergencia() : "");
        telefonoEmergencia.setValue(judokaActual.getTelefonoEmergencia() != null ? judokaActual.getTelefonoEmergencia() : "");

        seccionDocumentos.removeAll();
        seccionPago.removeAll();
        actualizarSeccionesDinamicas();
    }

    private void actualizarSeccionesDinamicas() {
        boolean esSaaS = judokaActual.getSensei() != null &&
                !judokaActual.getSensei().getUsuario().getUsername().equals("master_admin");

        if (!esSaaS) {
            seccionDocumentos.add(crearComponenteSubida(traduccionService.get("perfil.subir_waiver"), url -> { urlWaiver = url; waiverSubido = true; }));
            seccionDocumentos.add(crearComponenteSubida(traduccionService.get("perfil.subir_eps"), url -> { urlEps = url; epsSubido = true; }));
        } else {
            seccionDocumentos.add(new Span(traduccionService.get("perfil.saas.no_documentos")));
        }

        GrupoEntrenamiento grupo = judokaActual.getGrupoFacturacion();
        if (grupo != null && grupo.getTarifaMensual() != null && grupo.getTarifaMensual().compareTo(BigDecimal.ZERO) > 0) {
            String montoFormateado = configuracionService.obtenerFormatoMoneda().format(grupo.getTarifaMensual());
            textoMontoPago.setText(traduccionService.get("perfil.pago.monto_esperado", montoFormateado));
            seccionPago.add(textoMontoPago, crearComponenteSubida(traduccionService.get("perfil.subir_comprobante"), url -> { urlComprobante = url; pagoSubido = true; }));
        } else {
            textoMontoPago.setText(traduccionService.get("perfil.pago.sin_costo"));
            seccionPago.add(textoMontoPago);
        }
    }

    private String obtenerNombreMes(int numero) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return (numero >= 1 && numero <= 12) ? meses[numero - 1] : "Enero";
    }

    private int obtenerNumeroMes(String nombreMes) {
        if (nombreMes == null) return 1;
        switch (nombreMes) {
            case "Enero": return 1; case "Febrero": return 2; case "Marzo": return 3; case "Abril": return 4;
            case "Mayo": return 5; case "Junio": return 6; case "Julio": return 7; case "Agosto": return 8;
            case "Septiembre": return 9; case "Octubre": return 10; case "Noviembre": return 11; case "Diciembre": return 12;
            default: return 1;
        }
    }

    private Upload crearComponenteSubida(String etiqueta, java.util.function.Consumer<String> onSuccess) {
        Upload upload = new Upload();
        upload.setAcceptedFileTypes("application/pdf", ".pdf", "image/png", ".png", "image/jpeg", ".jpg");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setDropLabel(new Span(etiqueta));
        upload.setMaxFiles(1);
        upload.setUploadHandler(UploadHandler.inMemory((metadata, bytes) -> {
            try {
                InputStream in = new ByteArrayInputStream(bytes);
                String nombreArchivo = almacenamientoCloudService.subirArchivo(judokaActual.getId(), metadata.fileName(), in);
                onSuccess.accept("judokas/" + judokaActual.getId() + "/" + nombreArchivo);
                getUI().ifPresent(ui -> ui.access(() -> Notification.show("¡" + metadata.fileName() + " subido!", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS)));
            } catch (Exception ex) {
                log.error("Error subiendo archivo", ex);
                getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error al subir.", 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR)));
            }
        }));
        return upload;
    }

    private void guardarYEnviarRevision() {
        if (dia.isEmpty() || mes.isEmpty() || anio.isEmpty()) {
            Notification.show(traduccionService.get("error.fecha_nacimiento_requerida", "Fecha de nacimiento requerida")).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            judokaActual.setFechaNacimiento(java.time.LocalDate.of(anio.getValue(), obtenerNumeroMes(mes.getValue()), dia.getValue()));
            judokaActual.setSexo(sexo.getValue());
            if (!peso.isEmpty()) judokaActual.setPeso(Double.parseDouble(peso.getValue()));
            if (!estatura.isEmpty()) judokaActual.setEstatura(Double.parseDouble(estatura.getValue()));
            judokaActual.setEps(eps.getValue());
            judokaActual.setNombreContactoEmergencia(nombreContactoEmergencia.getValue());
            judokaActual.setTelefonoEmergencia(telefonoEmergencia.getValue());

            if (securityService.getAuthenticatedUserDetails().map(u -> u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_JUDOKA_ADULTO"))).orElse(false)) {
                judokaActual.setMayorEdad(true);
            }

            if (waiverSubido) admisionesService.cargarRequisito(judokaActual, TipoDocumento.WAIVER, urlWaiver);
            if (epsSubido) admisionesService.cargarRequisito(judokaActual, TipoDocumento.EPS, urlEps);
            if (pagoSubido) admisionesService.cargarRequisito(judokaActual, TipoDocumento.COMPROBANTE_PAGO, urlComprobante);

            judokaActual.setEstado(EstadoJudoka.EN_REVISION);
            judokaRepository.save(judokaActual);
            Notification.show(traduccionService.get("perfil.guardado_exito"), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("espera-autorizacion"));
        } catch (java.time.DateTimeException ex) {
            Notification.show("Fecha no válida", 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Error guardando perfil", e);
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}