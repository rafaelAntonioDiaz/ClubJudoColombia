package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.CuentaCobro;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.servicio.*;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Route(value = "mi-familia", layout = JudokaLayout.class)
@PageTitle("Mi Familia | Club Judo Colombia")
@RolesAllowed({"ROLE_ACUDIENTE", "ROLE_ADMIN", "ROLE_MASTER"})
public class PerfilAcudienteView extends VerticalLayout {

    private final SecurityService securityService;
    private final JudokaService judokaService;
    private final FinanzasService finanzasService;
    private final AccesoDojoService accesoDojoService;
    private final ConfiguracionService configuracionService;
    // UI
    private FlexLayout contenedorHijos;
    private Grid<CuentaCobro> gridDeudas;
    private H3 kpiTotalPagar;

    public PerfilAcudienteView(SecurityService securityService,
                               JudokaService judokaService,
                               FinanzasService finanzasService,
                               AccesoDojoService accesoDojoService, ConfiguracionService configuracionService) {
        this.securityService = securityService;
        this.judokaService = judokaService;
        this.finanzasService = finanzasService;
        this.accesoDojoService = accesoDojoService;
        this.configuracionService = configuracionService;

        addClassName("perfil-acudiente-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configurarVista();
    }

    private void configurarVista() {
        Optional<Usuario> usuarioOpt = securityService.getAuthenticatedUsuario();
        if (usuarioOpt.isEmpty()) {
            add(new H2("Sesión finalizada."));
            return;
        }
        Usuario acudiente = usuarioOpt.get();

        // 1. Cabecera
        crearCabecera(acudiente);

        // 2. Sección Mis Hijos
        add(new H3("Mis Deportistas"));
        contenedorHijos = new FlexLayout();
        contenedorHijos.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        contenedorHijos.getStyle().set("gap", "20px"); // CSS Gap
        contenedorHijos.setWidthFull();
        add(contenedorHijos);

        // 3. Sección Finanzas
        H3 tituloFinanzas = new H3("Estado de Cuenta");
        tituloFinanzas.addClassNames(LumoUtility.Margin.Top.LARGE);
        add(tituloFinanzas);

        configurarGridDeudas(acudiente);

        cargarDatos(acudiente);
    }

    private void crearCabecera(Usuario acudiente) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        VerticalLayout titulos = new VerticalLayout();
        titulos.setSpacing(false);
        titulos.setPadding(false);

        H2 saludo = new H2("Familia " + acudiente.getApellido());
        saludo.addClassNames(LumoUtility.Margin.Bottom.NONE);
        Span rol = new Span("Panel de Acudiente");
        rol.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        titulos.add(saludo, rol);

        // KPI Deuda
        kpiTotalPagar = new H3("$ 0");
        kpiTotalPagar.addClassNames(LumoUtility.TextColor.SUCCESS);

        VerticalLayout boxDeuda = new VerticalLayout(new Span("Total a Pagar:"), kpiTotalPagar);
        boxDeuda.setSpacing(false);
        boxDeuda.setAlignItems(Alignment.END);

        header.add(titulos, boxDeuda);
        add(header);
    }

    private void cargarDatos(Usuario acudiente) {
        List<Judoka> hijos = judokaService.findByAcudiente(acudiente);
        contenedorHijos.removeAll();

        if (hijos.isEmpty()) {
            contenedorHijos.add(new Span("No hay deportistas vinculados."));
        } else {
            hijos.forEach(h -> contenedorHijos.add(crearTarjetaHijo(h)));
        }

        actualizarFinanzas(acudiente);
    }

    private Component crearTarjetaHijo(Judoka hijo) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("320px");
        card.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);

        // Cabecera: Avatar + Datos
        HorizontalLayout top = new HorizontalLayout();
        top.setAlignItems(Alignment.CENTER);
        top.setWidthFull();

        Avatar avatar = new Avatar(hijo.getNombre());
        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);

        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);
        info.add(new H4(hijo.getNombre()), new Span("Cinturón " + hijo.getGrado().name()));

        top.add(avatar, info);
        top.setFlexGrow(1, info);

        // --- ESTADO Y PATROCINIO (CORREGIDO) ---
        HorizontalLayout badges = new HorizontalLayout();

        boolean activo = hijo.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.ACTIVO;
        boolean enRevision = hijo.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.EN_REVISION;

        Span badgeEstado = new Span();
        if (activo) {
            badgeEstado.setText("Activo");
            badgeEstado.getElement().getThemeList().add("badge success");
        } else if (enRevision) {
            badgeEstado.setText("En Revisión (Auditoría)");
            badgeEstado.getElement().getThemeList().add("badge contrast");
        } else {
            badgeEstado.setText("Pago Pendiente");
            badgeEstado.getElement().getThemeList().add("badge error");
        }
        badges.add(badgeEstado);

        if (hijo.getMecenas() != null) {
            Span badgeMecenas = new Span("Patrocinado");
            badgeMecenas.getElement().getThemeList().add("badge contrast");
            badgeMecenas.setTitle("Soportado por: " + (hijo.getMecenas().getNombreEmpresa() != null ?
                    hijo.getMecenas().getNombreEmpresa() : "Padrino Anónimo"));
            badges.add(badgeMecenas);
        }

        // --- ACCIONES (CORREGIDO Y PROTEGIDO) ---
        HorizontalLayout acciones = new HorizontalLayout();
        acciones.setWidthFull();
        acciones.addClassNames(LumoUtility.Margin.Top.SMALL);

        Button btnMagicLink = new Button("Pase QR", VaadinIcon.QRCODE.create());
        btnMagicLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnMagicLink.addClickListener(e -> generarMagicLink(hijo));

        Button btnDetalle = new Button("Progreso", VaadinIcon.LINE_CHART.create());
        btnDetalle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnDetalle.addClassNames(LumoUtility.Margin.Left.AUTO);

        // Bloqueamos el botón si el niño aún no es admitido oficialmente
        if (!activo) {
            btnDetalle.setEnabled(false);
            btnDetalle.getElement().setProperty("title", "Disponible cuando el Master apruebe el ingreso");
        } else {
            btnDetalle.addClickListener(e -> {
                // Aquí pondrás la navegación a la vista real de Progreso cuando la construyas
                Notification.show("Navegando al progreso de " + hijo.getNombre());
            });
        }

        acciones.add(btnMagicLink, btnDetalle);
        card.add(top, badges, new com.vaadin.flow.component.html.Hr(), acciones);
        return card;}

    // --- LÓGICA DE MAGIC LINK (OPTIMIZADA) ---
// --- LÓGICA DE MAGIC LINK (CORREGIDA) ---
    private void generarMagicLink(Judoka judoka) {
        // ✅ Comparación correcta con enum
        if (judoka.getEstado() == com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka.INACTIVO) {
            Notification.show("Debes estar al día para generar el pase.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // ✅ Solo una llamada al servicio
        String token = accesoDojoService.generarNuevoPase(judoka);

        // Construimos la URL absoluta
        var request = com.vaadin.flow.server.VaadinServletRequest.getCurrent().getHttpServletRequest();
        String urlBase = request.getRequestURL().toString().replace(request.getRequestURI(), "") + request.getContextPath();
        String linkCompleto = urlBase + "/acceso-dojo/" + token;

        // Resto del diálogo igual...
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pase de Acceso: " + judoka.getNombre());

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Paragraph("Comparte este enlace con tu hijo o escanea el QR al llegar al club."));

        Icon qrIcon = VaadinIcon.QRCODE.create();
        qrIcon.setSize("100px");
        qrIcon.getStyle().set("align-self", "center");

        TextArea linkField = new TextArea("Enlace de Acceso Directo");
        linkField.setValue(linkCompleto);
        linkField.setReadOnly(true);
        linkField.setWidthFull();

        Button btnCopiar = new Button("Copiar Enlace", VaadinIcon.COPY.create(), e -> {
            // Simulación de copia (puedes implementar copia real con JavaScript)
            Notification.show("Enlace copiado al portapapeles");
        });

        layout.add(qrIcon, linkField, btnCopiar);
        dialog.add(layout);
        dialog.getFooter().add(new Button("Cerrar", e -> dialog.close()));
        dialog.open();
    }

    private void configurarGridDeudas(Usuario acudiente) {
        gridDeudas = new Grid<>(CuentaCobro.class, false);
        gridDeudas.addClassName("grid-deudas");

        gridDeudas.addColumn(c -> c.getJudokaBeneficiario().getNombre())
                .setHeader("Beneficiario")
                .setAutoWidth(true);

        gridDeudas.addColumn(CuentaCobro::getConcepto).setHeader("Concepto");

        gridDeudas.addColumn(c -> c.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Vence");

        // USO DEL FORMATO DINÁMICO
        gridDeudas.addColumn(c -> formatearMoneda(c.getValorTotal()))
                .setHeader("Valor")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        gridDeudas.addComponentColumn(cuenta -> {
            Button btnPagar = new Button("Pagar", VaadinIcon.DOLLAR.create());
            btnPagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            btnPagar.addClickListener(e -> abrirDialogoPago(cuenta));
            return btnPagar;
        });

        add(gridDeudas);
    }

    private void abrirDialogoPago(CuentaCobro cuenta) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reportar Pago");

        VerticalLayout layout = new VerticalLayout();

        Select<MetodoPago> metodo = new Select<>();
        metodo.setLabel("Método de Pago");
        metodo.setItems(MetodoPago.values());
        metodo.setValue(MetodoPago.NEQUI);

        // Si prefieres usar un uploader real aquí en el futuro, inyectas el servicio Cloud.
        // Por ahora mantenemos tu TextField para la referencia / URL.
        TextField referencia = new TextField("Referencia Nequi o URL del Comprobante");
        referencia.setWidthFull();

        // USO DEL FORMATO DINÁMICO
        layout.add(new Paragraph("Monto a transferir: " + formatearMoneda(cuenta.getValorTotal())), metodo, referencia);

        Button btnConfirmar = new Button("Enviar Soporte", e -> {
            try {
                if (referencia.getValue().isEmpty()) {
                    Notification.show("Debes ingresar la referencia o comprobante.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // LLAMADA AL NUEVO BUZÓN (No entra a caja todavía)
                finanzasService.reportarPagoParaRevision(cuenta.getId(), metodo.getValue(), referencia.getValue());

                Notification.show("Soporte enviado. En espera de aprobación del Master.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                actualizarFinanzas(cuenta.getResponsablePago());
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(layout);
        dialog.getFooter().add(new Button("Cancelar", e -> dialog.close()), btnConfirmar);
        dialog.open();
    }

    private void actualizarFinanzas(Usuario acudiente) {
        List<CuentaCobro> pendientes = finanzasService.obtenerDeudasPendientes(acudiente);
        gridDeudas.setItems(pendientes);

        BigDecimal total = pendientes.stream()
                .map(CuentaCobro::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // USO DEL FORMATO DINÁMICO
        kpiTotalPagar.setText(formatearMoneda(total));

        kpiTotalPagar.getClassNames().clear();
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            kpiTotalPagar.addClassNames(LumoUtility.TextColor.ERROR);
        } else {
            kpiTotalPagar.addClassNames(LumoUtility.TextColor.SUCCESS);
        }

        contenedorHijos.removeAll();
        judokaService.findByAcudiente(acudiente).forEach(h -> contenedorHijos.add(crearTarjetaHijo(h)));
    }

    // --- MÉTODO HELPER CORREGIDO ---
    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return configuracionService.obtenerFormatoMoneda().format(0);
        return configuracionService.obtenerFormatoMoneda().format(valor);
    }
}