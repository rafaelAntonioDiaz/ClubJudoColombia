package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.CuentaCobro;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.Mecenas;
import com.RafaelDiaz.ClubJudoColombia.modelo.Usuario;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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

@Route(value = "panel-mecenas", layout = JudokaLayout.class)
@PageTitle("Impacto Social | Club Judo Colombia")
@RolesAllowed({"ROLE_MECENAS", "ROLE_ADMIN"})
public class MecenasDashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final JudokaService judokaService;
    private final FinanzasService finanzasService;

    // UI Components para refrescar
    private Grid<CuentaCobro> gridPagos;
    private H3 kpiDeudaValor;

    public MecenasDashboardView(SecurityService securityService,
                                JudokaService judokaService,
                                FinanzasService finanzasService) { // <--- Inyectamos Finanzas
        this.securityService = securityService;
        this.judokaService = judokaService;
        this.finanzasService = finanzasService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        addClassName("mecenas-dashboard-view");

        configurarInterfaz();
    }

    private void configurarInterfaz() {
        Optional<Usuario> user = securityService.getAuthenticatedUsuario();
        if (user.isEmpty() || user.get().getPerfilMecenas() == null) {
            add(new H2("Error: Perfil de Mecenas no encontrado"));
            return;
        }

        Usuario usuarioLogueado = user.get();
        Mecenas mecenas = usuarioLogueado.getPerfilMecenas();

        // 1. Cabecera
        Header header = new Header();
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        H2 saludo = new H2("Hola, " + usuarioLogueado.getNombre());
        saludo.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.TextColor.PRIMARY);
        Span subtitulo = new Span("Gracias por construir el futuro del Judo.");
        subtitulo.addClassNames(LumoUtility.TextColor.SECONDARY);
        header.add(saludo, subtitulo);

        // 2. Datos Financieros y KPIs
        List<Judoka> misAtletas = judokaService.findByMecenas(mecenas);
        BigDecimal deudaTotal = finanzasService.calcularDeudaTotal(usuarioLogueado);

        FlexLayout kpis = new FlexLayout();
        kpis.setWidthFull();

        kpis.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        kpis.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        kpis.add(
                crearKpiCard("Atletas Apoyados", String.valueOf(misAtletas.size()), VaadinIcon.USERS, "var(--lumo-primary-color)"),
                crearKpiDeuda(deudaTotal), // KPI Dinámico
                crearKpiCard("Estado", "Activo", VaadinIcon.HEART, "#E74C3C")
        );

        // 3. Sección de Pagos Pendientes (¡El Core del Negocio!)
        H3 tituloPagos = new H3("Compromisos Pendientes");
        tituloPagos.addClassNames(LumoUtility.Margin.Top.XLARGE);

        configurarGridPagos(usuarioLogueado);

        // 4. Sección de Atletas (Visual)
        H3 tituloAtletas = new H3("Tus Ahijados");
        tituloAtletas.addClassNames(LumoUtility.Margin.Top.LARGE);
        FlexLayout containerAtletas = new FlexLayout();
        containerAtletas.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        kpis.getStyle().set("gap", "20px");

        if (misAtletas.isEmpty()) {
            containerAtletas.add(new Span("No tienes atletas asignados actualmente."));
        } else {
            misAtletas.forEach(j -> containerAtletas.add(crearTarjetaAtleta(j)));
        }

        add(header, kpis, tituloPagos, gridPagos, tituloAtletas, containerAtletas);
    }

    private VerticalLayout crearKpiDeuda(BigDecimal deuda) {
        kpiDeudaValor = new H3(formatoMoneda(deuda));
        String color = deuda.compareTo(BigDecimal.ZERO) > 0 ? "#E74C3C" : "var(--lumo-success-color)";

        VerticalLayout card = new VerticalLayout();
        card.setWidth("220px");
        card.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        card.setSpacing(false);
        card.setAlignItems(Alignment.CENTER);

        Icon i = VaadinIcon.INVOICE.create();
        i.setColor(color);

        kpiDeudaValor.addClassNames(LumoUtility.Margin.NONE);
        kpiDeudaValor.getStyle().set("color", color);

        Span t = new Span("Total a Pagar");
        t.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        card.add(i, kpiDeudaValor, t);
        return card;
    }

    private void configurarGridPagos(Usuario usuario) {
        gridPagos = new Grid<>(CuentaCobro.class, false);
        gridPagos.addClassName("grid-pagos");
        gridPagos.setAllRowsVisible(true);

        gridPagos.addColumn(c -> c.getJudokaBeneficiario().getNombre() + " " + c.getJudokaBeneficiario().getApellido())
                .setHeader("Atleta")
                .setAutoWidth(true);

        gridPagos.addColumn(CuentaCobro::getConcepto)
                .setHeader("Concepto")
                .setFlexGrow(1);

        gridPagos.addColumn(c -> c.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Vence");

        gridPagos.addColumn(c -> formatoMoneda(c.getValorTotal()))
                .setHeader("Valor")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Columna de Acción (Botón Pagar)
        gridPagos.addComponentColumn(cuenta -> {
            Button btnPagar = new Button("Pagar", VaadinIcon.DOLLAR.create());
            btnPagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btnPagar.addClickListener(e -> abrirDialogoPago(cuenta));
            return btnPagar;
        });

        actualizarGrid(usuario);
    }

    private void abrirDialogoPago(CuentaCobro cuenta) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registrar Pago");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Paragraph info = new Paragraph("Vas a pagar: " + cuenta.getConcepto() +
                " por " + formatoMoneda(cuenta.getValorTotal()));

        Select<MetodoPago> metodoSelect = new Select<>();
        metodoSelect.setLabel("Método de Pago");
        metodoSelect.setItems(MetodoPago.values());
        metodoSelect.setValue(MetodoPago.NEQUI);

        TextField comprobanteField = new TextField("Referencia / URL Comprobante");
        comprobanteField.setPlaceholder("Ej: M123456 o Link de imagen");

        layout.add(info, metodoSelect, comprobanteField);

        Button btnConfirmar = new Button("Confirmar Pago", e -> {
            try {
                // LLAMADA AL MOTOR FINANCIERO
                finanzasService.pagarCuentaCobro(
                        cuenta.getId(),
                        metodoSelect.getValue(),
                        comprobanteField.getValue()
                );

                Notification.show("Pago registrado con éxito. ¡Gracias!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Refrescar UI
                securityService.getAuthenticatedUsuario().ifPresent(this::actualizarGrid);
                dialog.close();

            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        btnConfirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(btnCancelar, btnConfirmar);
        dialog.open();
    }

    private void actualizarGrid(Usuario usuario) {
        List<CuentaCobro> pendientes = finanzasService.obtenerDeudasPendientes(usuario);
        gridPagos.setItems(pendientes);

        // Actualizar KPI visualmente
        BigDecimal total = pendientes.stream()
                .map(CuentaCobro::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (kpiDeudaValor != null) {
            kpiDeudaValor.setText(formatoMoneda(total));
            String color = total.compareTo(BigDecimal.ZERO) > 0 ? "#E74C3C" : "var(--lumo-success-color)";
            kpiDeudaValor.getStyle().set("color", color);
        }
    }

    // --- Helpers Visuales (Cartas y Formatos) ---

    private Component crearTarjetaAtleta(Judoka j) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("250px");
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10, LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);
        card.setAlignItems(Alignment.CENTER);

        // Avatar
        Div avatar = new Div();
        avatar.setWidth("60px");
        avatar.setHeight("60px");
        avatar.getStyle().set("background-color", "#f0f0f0").set("border-radius", "50%")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center");
        Icon icon = VaadinIcon.USER.create();
        icon.setSize("30px");
        icon.setColor("gray");
        avatar.add(icon);

        H4 nombre = new H4(j.getNombre() + " " + j.getApellido());
        nombre.addClassNames(LumoUtility.Margin.Vertical.SMALL, LumoUtility.FontSize.MEDIUM);

        Span cinturon = new Span("Cinturón " + j.getGrado());
        cinturon.getElement().getThemeList().add("badge");

        // Estado financiero del atleta (Visual simple)
        boolean alDia = j.getFechaVencimientoSuscripcion() != null &&
                j.getFechaVencimientoSuscripcion().isAfter(java.time.LocalDate.now());
        Span estado = new Span(alDia ? "Al día" : "Pago Pendiente");
        estado.getElement().getThemeList().add(alDia ? "badge success" : "badge error");

        card.add(avatar, nombre, cinturon, estado);
        return card;
    }

    private VerticalLayout crearKpiCard(String titulo, String valor, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("220px");
        card.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        card.setSpacing(false);
        card.setAlignItems(Alignment.CENTER);

        Icon i = icon.create();
        i.setColor(color);

        H3 v = new H3(valor);
        v.addClassNames(LumoUtility.Margin.NONE);

        Span t = new Span(titulo);
        t.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        card.add(i, v, t);
        return card;
    }

    private String formatoMoneda(BigDecimal valor) {
        if (valor == null) return "$ 0";
        NumberFormat formater = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        formater.setMaximumFractionDigits(0);
        return formater.format(valor);
    }
}