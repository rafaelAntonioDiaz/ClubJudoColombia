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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.util.Optional;

@PageTitle("Completar Perfil | Club Judo Colombia")
@Route("completar-perfil-judoka")
@RolesAllowed({"ROLE_JUDOKA_ADULTO", "ROLE_MASTER", "ROLE_SENSEI"})
public class CompletarPerfilJudokaView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final Logger log = LoggerFactory.getLogger(CompletarPerfilJudokaView.class);

    private final SecurityService securityService;
    private final AdmisionesService admisionesService;
    private final AlmacenamientoCloudService almacenamientoCloudService;
    private final JudokaRepository judokaRepository;
    private final TraduccionService traduccionService;
    private final ConfiguracionService configuracionService;
    private final FinanzasService finanzasService;

    private Judoka judokaActual;
    private Long judokaId;

    // Componentes UI
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formDatos = new FormLayout();

    private final DatePicker fechaNacimiento = new DatePicker();
    private final ComboBox<Sexo> sexo = new ComboBox<>();
    private final TextField peso = new TextField();
    private final TextField estatura = new TextField();
    private final TextField eps = new TextField();
    private final TextField nombreContactoEmergencia = new TextField();
    private final TextField telefonoEmergencia = new TextField();

    // Documentos
    private final VerticalLayout seccionDocumentos = new VerticalLayout();
    private boolean waiverSubido = false;
    private boolean epsSubido = false;
    private String urlWaiver = null;
    private String urlEps = null;

    // Pago
    private final VerticalLayout seccionPago = new VerticalLayout();
    private boolean pagoSubido = false;
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
        this.finanzasService = finanzasService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Configurar componentes
        fechaNacimiento.setLabel(traduccionService.get("label.fecha_nacimiento"));
        sexo.setLabel(traduccionService.get("label.sexo"));
        sexo.setItems(Sexo.values());
        sexo.setItemLabelGenerator(s -> traduccionService.get("sexo." + s.name().toLowerCase()));
        peso.setLabel(traduccionService.get("label.peso_kg"));
        estatura.setLabel(traduccionService.get("label.estatura_cm"));
        eps.setLabel(traduccionService.get("label.eps"));
        nombreContactoEmergencia.setLabel(traduccionService.get("label.contacto_emergencia"));
        telefonoEmergencia.setLabel(traduccionService.get("label.telefono_emergencia"));

        btnGuardar.setText(traduccionService.get("boton.guardar_enviar_revision"));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardar.addClickListener(e -> guardarYEnviarRevision());

        VerticalLayout card = new VerticalLayout();
        card.addClassName("card-blanca");
        card.setMaxWidth("800px");
        card.setPadding(true);
        card.add(titulo, descripcion, new H3(traduccionService.get("perfil.datos_personales")), formDatos);
        card.add(new H3(traduccionService.get("perfil.documentos")), seccionDocumentos);
        card.add(new H3(traduccionService.get("perfil.pago")), seccionPago);
        card.add(btnGuardar);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long judokaId) {
        if (judokaId == null) {
            // Si no se pasa ID, intentamos obtener el judoka autenticado
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
        // Verificar permisos (el propio judoka o master/sensei)
        Usuario usuarioActual = securityService.getAuthenticatedUsuario().orElse(null);
        if (usuarioActual == null) {
            getUI().ifPresent(ui -> ui.navigate(""));
            return;
        }
        boolean puedeEditar = usuarioActual.equals(judokaActual.getAcudiente()) ||
                (securityService.isMaster() || securityService.isSensei());
        if (!puedeEditar) {
            Notification.show("No tienes permiso para completar este perfil.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(""));
            return;
        }

        titulo.setText(traduccionService.get("perfil.completar.titulo", judokaActual.getNombre()));
        descripcion.setText(traduccionService.get("perfil.completar.descripcion"));

        // Cargar valores existentes
        fechaNacimiento.setValue(judokaActual.getFechaNacimiento());
        if (judokaActual.getSexo() != null) sexo.setValue(judokaActual.getSexo());
        if (judokaActual.getPeso() != null) peso.setValue(String.valueOf(judokaActual.getPeso()));
        if (judokaActual.getEstatura() != null) estatura.setValue(String.valueOf(judokaActual.getEstatura()));

        // Asignar cadena vacía si el valor es null
        eps.setValue(judokaActual.getEps() != null ? judokaActual.getEps() : "");
        nombreContactoEmergencia.setValue(judokaActual.getNombreContactoEmergencia() != null ? judokaActual.getNombreContactoEmergencia() : "");
        telefonoEmergencia.setValue(judokaActual.getTelefonoEmergencia() != null ? judokaActual.getTelefonoEmergencia() : "");

        formDatos.add(fechaNacimiento, sexo, peso, estatura, eps, nombreContactoEmergencia, telefonoEmergencia);
        formDatos.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Determinar si es SaaS (pertenece a sensei externo) o dojo principal
        boolean esSaaS = judokaActual.getSensei() != null &&
                !judokaActual.getSensei().getUsuario().getUsername().equals("master_admin");

        // Configurar sección de documentos (solo si no es SaaS)
        if (!esSaaS) {
            Upload uploadWaiver = crearComponenteSubida(traduccionService.get("perfil.subir_waiver"), url -> {
                urlWaiver = url;
                waiverSubido = true;
            });
            Upload uploadEps = crearComponenteSubida(traduccionService.get("perfil.subir_eps"), url -> {
                urlEps = url;
                epsSubido = true;
            });
            seccionDocumentos.add(uploadWaiver, uploadEps);
        } else {
            seccionDocumentos.add(new Span(traduccionService.get("perfil.saas.no_documentos")));
        }

        // Configurar sección de pago
        GrupoEntrenamiento grupo = judokaActual.getGrupoFacturacion();
        if (grupo == null) {
            textoMontoPago.setText(traduccionService.get("perfil.pago.sin_grupo"));
            seccionPago.add(textoMontoPago);
        } else {
            BigDecimal monto = grupo.getTarifaMensual();
            if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
                textoMontoPago.setText(traduccionService.get("perfil.pago.sin_costo"));
                seccionPago.add(textoMontoPago);
            } else {
                String montoFormateado = configuracionService.obtenerFormatoMoneda().format(monto);
                textoMontoPago.setText(traduccionService.get("perfil.pago.monto_esperado", montoFormateado));
                Upload uploadPago = crearComponenteSubida(traduccionService.get("perfil.subir_comprobante"), url -> {
                    urlComprobante = url;
                    pagoSubido = true;
                });
                seccionPago.add(textoMontoPago, uploadPago);
            }
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
                // subirArchivo devuelve solo el nombre del archivo (ej. "uuid_nombre.pdf")
                String nombreArchivo = almacenamientoCloudService.subirArchivo(judokaActual.getId(), metadata.fileName(), in);
                // Construimos la clave completa que se guardará en BD
                String claveCompleta = "judokas/" + judokaActual.getId() + "/" + nombreArchivo;
                onSuccess.accept(claveCompleta);  // ✅ Guardamos la clave completa

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("¡" + metadata.fileName() + " subido con éxito!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));
            } catch (Exception ex) {
                log.error("Error subiendo archivo", ex);
                getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error al subir archivo.", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)));
            }
        }));
        return upload;
    }

    private void guardarYEnviarRevision() {
        // Validaciones
        if (fechaNacimiento.getValue() == null) {
            Notification.show(traduccionService.get("error.fecha_nacimiento_requerida")).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        boolean esSaaS = judokaActual.getSensei() != null &&
                !judokaActual.getSensei().getUsuario().getUsername().equals("master_admin");

        if (!esSaaS) {
            if (!waiverSubido) {
                Notification.show(traduccionService.get("error.waiver_requerido")).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!epsSubido) {
                Notification.show(traduccionService.get("error.eps_requerido")).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        GrupoEntrenamiento grupo = judokaActual.getGrupoFacturacion();
        if (grupo != null && grupo.getTarifaMensual() != null && grupo.getTarifaMensual().compareTo(BigDecimal.ZERO) > 0) {
            if (!pagoSubido) {
                Notification.show(traduccionService.get("error.pago_requerido")).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        try {
            // Actualizar datos personales
            judokaActual.setFechaNacimiento(fechaNacimiento.getValue());
            judokaActual.setSexo(sexo.getValue());
            if (!peso.isEmpty()) judokaActual.setPeso(Double.parseDouble(peso.getValue()));
            if (!estatura.isEmpty()) judokaActual.setEstatura(Double.parseDouble(estatura.getValue()));
            judokaActual.setEps(eps.getValue());
            judokaActual.setNombreContactoEmergencia(nombreContactoEmergencia.getValue());
            judokaActual.setTelefonoEmergencia(telefonoEmergencia.getValue());

            // Guardar documentos en DocumentoRequisito
            if (waiverSubido) {
                admisionesService.cargarRequisito(judokaActual, TipoDocumento.WAIVER, urlWaiver);
            }
            if (epsSubido) {
                admisionesService.cargarRequisito(judokaActual, TipoDocumento.EPS, urlEps);
            }
            if (pagoSubido) {
                admisionesService.cargarRequisito(judokaActual, TipoDocumento.COMPROBANTE_PAGO, urlComprobante);
            }

            // Si aún no se ha generado la cuenta de cobro, la generamos
            boolean tieneCobro = finanzasService.obtenerDeudasPendientes(judokaActual.getAcudiente()).stream()
                    .anyMatch(c -> c.getJudokaBeneficiario().getId().equals(judokaActual.getId()));
            if (!tieneCobro && grupo != null && grupo.getTarifaMensual() != null && grupo.getTarifaMensual().compareTo(BigDecimal.ZERO) > 0) {
                finanzasService.generarCobroBienvenida(judokaActual);
            }

            // Cambiar estado a EN_REVISION
            judokaActual.setEstado(EstadoJudoka.EN_REVISION);
            judokaRepository.save(judokaActual);

            Notification.show(traduccionService.get("perfil.guardado_exito"), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Redirigir a la página de espera
            getUI().ifPresent(ui -> ui.navigate("espera-autorizacion"));

        } catch (Exception e) {
            log.error("Error guardando perfil", e);
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}