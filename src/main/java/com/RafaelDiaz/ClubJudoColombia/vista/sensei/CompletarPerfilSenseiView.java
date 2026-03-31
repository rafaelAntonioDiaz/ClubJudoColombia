package com.RafaelDiaz.ClubJudoColombia.vista.sensei;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.modelo.TokenInvitacion;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.GradoCinturon;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.UsuarioRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.SenseiDashboardView;
import com.RafaelDiaz.ClubJudoColombia.vista.util.NotificationHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

// CORRECCIÓN: Se eliminó la importación de NumberField (no se usa)
// CORRECCIÓN: Se reemplazó TextField por IntegerField para el campo de comisión
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@PageTitle("Completar Perfil de Profesor")
@Route("completar-perfil-sensei")
@RolesAllowed({"ROLE_SENSEI", "ROLE_MASTER"})
public class CompletarPerfilSenseiView extends VerticalLayout implements HasUrlParameter<String> {

    // ──────────────────────────────────────────────────────────────────────────
    // Dependencias inyectadas por constructor
    // ──────────────────────────────────────────────────────────────────────────
    private final UsuarioRepository usuarioRepository;
    private final SenseiRepository senseiRepository;
    // TokenInvitacionRepository eliminado de la vista: el servicio AdmisionesService
    // es el único responsable del ciclo de vida del token (validar, consumir).
    private final TraduccionService traduccionService;
    private final AdmisionesService admisionesService;
    private final SecurityService securityService;

    // ──────────────────────────────────────────────────────────────────────────
    // Estado de la vista
    // ──────────────────────────────────────────────────────────────────────────
    private TokenInvitacion token;
    private boolean modoEdicion;
    private Usuario usuario;
    private Sensei sensei;

    // ──────────────────────────────────────────────────────────────────────────
    // Componentes del formulario (declarados a nivel de clase para ser
    // accesibles tanto en construirFormulario() como en guardarPerfil())
    // ──────────────────────────────────────────────────────────────────────────
    private final H2 titulo = new H2();
    private final Paragraph descripcion = new Paragraph();
    private final FormLayout formLayout = new FormLayout();

    // Campos de datos del sensei
    private final TextField nombreDojoField = new TextField();
    private final ComboBox<GradoCinturon> gradoField = new ComboBox<>();
    private final IntegerField anosPractica = new IntegerField();
    private final TextArea biografiaField = new TextArea();

    // Upload: Vaadin 24.8 no tiene setLabel() en Upload.
    // Se usa un Span externo como etiqueta visible y se agrega junto al Upload al formulario.
    private final Span labelCertificaciones = new Span();
    private final Upload uploadCertificaciones = new Upload();

    private final Button btnGuardar = new Button();
    private final Checkbox esClubPropioCheckbox = new Checkbox();

    // CORRECCIÓN: TextField no tiene setMin/setMax/setStep ni acepta setValue(double).
    // Se reemplaza por IntegerField para manejar porcentajes enteros (ej. 12%).
    private final IntegerField comisionPorcentajeField = new IntegerField();
    private final AlmacenamientoCloudService almacenamientoCloudService;
    // ──────────────────────────────────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────────────────────────────────
    public CompletarPerfilSenseiView(UsuarioRepository usuarioRepository,
                                     SenseiRepository senseiRepository,
                                     TraduccionService traduccionService,
                                     AdmisionesService admisionesService,
                                     SecurityService securityService, AlmacenamientoCloudService almacenamientoCloudService) {
        this.usuarioRepository = usuarioRepository;
        this.senseiRepository = senseiRepository;
        this.traduccionService = traduccionService;
        this.admisionesService = admisionesService;
        this.securityService = securityService;
        this.almacenamientoCloudService = almacenamientoCloudService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Resolución del parámetro de URL (token de invitación o modo edición)
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String tokenParam) {
        try {
            if (tokenParam == null || tokenParam.isEmpty()) {
                // Modo edición: sensei autenticado actualizando su perfil
                sensei = securityService.getAuthenticatedSensei()
                        .orElseThrow(() -> new RuntimeException("No se encontró perfil de sensei"));
                usuario = sensei.getUsuario();
                modoEdicion = true;
            } else {
                // Nuevo sensei llegando con token de invitación
                TokenInvitacion tokenInvitacion = admisionesService.validarTokenInvitacion(tokenParam);
                usuario = tokenInvitacion.getUsuarioInvitado();
                this.token = tokenInvitacion; // para consumirlo al final
                modoEdicion = false;

                // 🔧 Crear un nuevo Sensei para el usuario invitado (si no existe)
                sensei = senseiRepository.findByUsuario(usuario).orElse(new Sensei());
                if (sensei.getId() == null) {
                    sensei.setUsuario(usuario);
                }
            }
            construirFormulario();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }
    // ──────────────────────────────────────────────────────────────────────────
    // Construcción del formulario con los valores actuales del sensei
    // ──────────────────────────────────────────────────────────────────────────
    private void construirFormulario() {

        // Título y descripción traducidos
        titulo.setText(traduccionService.get("sensei.perfil.titulo") + " " + usuario.getNombre());
        descripcion.setText(traduccionService.get("sensei.perfil.descripcion"));

        // ── Nombre del dojo ──────────────────────────────────────────────────
        // NOTA: Se unifica en un solo campo (nombreDojo). En el código original
        // se asignaba setLabel() dos veces sobre el mismo field, lo que pisaba
        // el primer valor. Si el modelo tiene ambos campos (nombreClub y nombreDojo)
        // deberá separarse en dos TextField distintos.
        nombreDojoField.setLabel(traduccionService.get("sensei.nombre_dojo"));
        nombreDojoField.setValue(sensei.getNombreDojo() != null ? sensei.getNombreDojo() : "");
        nombreDojoField.setRequired(true);
        nombreDojoField.setWidthFull();

        // ── Grado cinturón ───────────────────────────────────────────────────
        gradoField.setLabel(traduccionService.get("sensei.grado_cinturon"));
        gradoField.setItems(GradoCinturon.values());
        gradoField.setItemLabelGenerator(
                grado -> traduccionService.get("grado." + grado.name().toLowerCase())
        );
        gradoField.setValue(sensei.getGrado() != null ? sensei.getGrado() : GradoCinturon.BLANCO);
        gradoField.setRequired(true);
        gradoField.setWidthFull();

        // ── Años de práctica ─────────────────────────────────────────────────
        anosPractica.setLabel(traduccionService.get("sensei.anos_practica"));
        anosPractica.setMin(0);
        anosPractica.setMax(60);
        anosPractica.setStep(1);
        // getAnosPractica() puede ser Long, Integer o Number según el modelo;
        // se convierte a int de forma segura
        anosPractica.setValue(
                sensei.getAnosPractica() != null ? sensei.getAnosPractica().intValue() : 0
        );
        anosPractica.setRequired(true);
        anosPractica.setWidthFull();

        // ── Biografía ────────────────────────────────────────────────────────
        biografiaField.setLabel(traduccionService.get("sensei.biografia"));
        biografiaField.setValue(sensei.getBiografia() != null ? sensei.getBiografia() : "");
        biografiaField.setHeight("150px");
        biografiaField.setWidthFull();

        // ── Upload de certificaciones ────────────────────────────────────────
        // CORRECCIÓN: Upload en Vaadin 24.8 no tiene setLabel().
        // Se agrega un Span como etiqueta visible separada del componente Upload.
        labelCertificaciones.setText(traduccionService.get("sensei.certificaciones"));
        uploadCertificaciones.setAcceptedFileTypes("application/pdf", "image/png", "image/jpeg");
        uploadCertificaciones.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        uploadCertificaciones.setMaxFiles(1);
        uploadCertificaciones.setUploadHandler(com.vaadin.flow.server.streams.UploadHandler.inMemory((metadata, bytes) -> {
            try {
                // 1. Crear el stream desde los bytes recibidos
                java.io.InputStream in = new java.io.ByteArrayInputStream(bytes);

                // 2. Subir al servicio de nube (usamos el ID del usuario como carpeta)
                String nombreArchivo = almacenamientoCloudService.subirArchivo(usuario.getId(), metadata.fileName(), in);

                // 3. Construir la ruta completa para la base de datos
                String rutaCompleta = "senseis/" + usuario.getId() + "/certificaciones/" + nombreArchivo;
                sensei.setRutaCertificaciones(rutaCompleta);

                // 4. Notificar éxito en el hilo de la UI
                getUI().ifPresent(ui -> ui.access(() -> {
                    NotificationHelper.success(
                            traduccionService.get("sensei.certificaciones.subida_ok") + ": " + metadata.fileName()
                    );
                }));
            } catch (Exception ex) {
                getUI().ifPresent(ui -> ui.access(() ->
                        NotificationHelper.error("Error al procesar archivo: " + ex.getMessage())
                ));
            }
        }));

        // ── Campos visibles solo para MASTER ─────────────────────────────────
        boolean isMaster = securityService.isMaster();

        if (isMaster && !modoEdicion && sensei.getId() == null) {
            // CORRECCIÓN: Se reemplazó TextField por IntegerField.
            // TextField no tiene setMin/setMax/setStep ni acepta setValue(double).
            // El porcentaje de comisión es un entero (0–100).
            comisionPorcentajeField.setLabel(traduccionService.get("sensei.comision_porcentaje"));
            comisionPorcentajeField.setMin(0);
            comisionPorcentajeField.setMax(100);
            comisionPorcentajeField.setStep(1);
            comisionPorcentajeField.setValue(0);
            comisionPorcentajeField.setRequired(true);
            comisionPorcentajeField.setWidthFull();
            formLayout.add(comisionPorcentajeField);
        }

        if (isMaster) {
            esClubPropioCheckbox.setLabel(traduccionService.get("sensei.es_club_propio"));
            esClubPropioCheckbox.setValue(sensei.isEsClubPropio());
            formLayout.add(esClubPropioCheckbox);
        }

        // ── Botón guardar ────────────────────────────────────────────────────
        btnGuardar.setText(traduccionService.get("btn.guardar"));
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardar.setWidthFull();
        btnGuardar.addClickListener(e -> guardarPerfil());

        // ── Ensamblar formulario ─────────────────────────────────────────────
        // CORRECCIÓN: Se eliminó la duplicación de nombreDojoField que existía
        // en el formLayout.add() original.
        // labelCertificaciones se agrega antes del Upload como etiqueta visible.
        formLayout.add(
                nombreDojoField,
                gradoField,
                anosPractica,
                biografiaField,
                labelCertificaciones,
                uploadCertificaciones,
                btnGuardar
        );
        formLayout.setMaxWidth("600px");

        add(titulo, descripcion, formLayout);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistencia del perfil del sensei
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    private void guardarPerfil() {

        // Validación básica de campos obligatorios
        if (nombreDojoField.isEmpty() || gradoField.isEmpty()) {
            NotificationHelper.error(traduccionService.get("validacion.campos_obligatorios"));
            return;
        }

        try {
            if (modoEdicion) {
                // ── Modo edición: actualizar sensei existente ────────────────
                sensei.setNombreDojo(nombreDojoField.getValue().trim());
                sensei.setGrado(gradoField.getValue());
                sensei.setAnosPractica(anosPractica.getValue() != null ? anosPractica.getValue() : 0);
                sensei.setBiografia(biografiaField.getValue().trim());

                senseiRepository.saveAndFlush(sensei);
                NotificationHelper.success(traduccionService.get("sensei.perfil.actualizado"));

            } else {
                // ── Modo creación con token de invitación ────────────────────
                if (usuario == null) {
                    throw new RuntimeException("Usuario no encontrado en el token");
                }

                // Evitar duplicados: verificar que no exista ya un sensei para este usuario
                if (senseiRepository.findByUsuario(usuario).isPresent()) {
                    NotificationHelper.error(traduccionService.get("sensei.ya_existe"));
                    return;
                }

                // Poblar el sensei con los datos del formulario
                sensei.setUsuario(usuario);
                sensei.setNombreDojo(nombreDojoField.getValue().trim());
                sensei.setGrado(gradoField.getValue());
                sensei.setAnosPractica(anosPractica.getValue() != null ? anosPractica.getValue() : 0);
                sensei.setBiografia(biografiaField.getValue().trim());

                // Datos que vienen del token (no editables por el usuario)
                sensei.setEsClubPropio(
                        token.getEsClubPropio() != null ? token.getEsClubPropio() : false
                );
                sensei.setComisionPorcentaje(
                        token.getPorcentajeComision() != null
                                ? token.getPorcentajeComision()
                                : BigDecimal.ZERO
                );

                Sensei guardado = senseiRepository.saveAndFlush(sensei);

                // Registrar el ID del sensei en la sesión de Vaadin para uso posterior
                VaadinSession.getCurrent().setAttribute("CURRENT_SENSEI_ID", guardado.getId());

                // Marcar el token como consumido para que no pueda reutilizarse.
                // consumirToken() ya hace setUsado(true) + save() internamente.
                // NO se repite aquí para evitar doble consumo sobre el objeto
                // detachado de Hibernate, que causaba el error "Token Expirado"
                // al releer el token como ya-usado en la segunda operación.
                admisionesService.consumirToken(token.getToken());

                NotificationHelper.success(
                        "¡" + nombreDojoField.getValue() + " " + traduccionService.get("sensei.configurado_exito") + "!"
                );
            }

            // Navegar al dashboard del sensei tras guardar exitosamente
            UI.getCurrent().navigate(SenseiDashboardView.class);

        } catch (Exception ex) {
            NotificationHelper.error(
                    traduccionService.get("error.guardar_perfil") + ": " + ex.getMessage()
            );
            ex.printStackTrace();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Pantalla de error genérica (token inválido, sensei no encontrado, etc.)
    // ──────────────────────────────────────────────────────────────────────────
    private void mostrarError(String mensaje) {
        removeAll();
        add(new H2(traduccionService.get("error.titulo_ops")));
        add(new Paragraph(mensaje));
    }
}