package com.RafaelDiaz.ClubJudoColombia.vista.admin;

import com.RafaelDiaz.ClubJudoColombia.modelo.Sensei;
import com.RafaelDiaz.ClubJudoColombia.repositorio.SenseiRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.ConfiguracionService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "admin/senseis", layout = SenseiLayout.class)
@PageTitle("Gestión de Senseis | Admin")
@RolesAllowed("ROLE_MASTER")
public class GestionSenseisView extends VerticalLayout {

    private final SenseiRepository senseiRepository;
    private final TraduccionService traduccionService;
    private final FinanzasService finanzasService;
    private final ConfiguracionService configuracionService;

    private Grid<Sensei> grid = new Grid<>(Sensei.class, false);

    public GestionSenseisView(SenseiRepository senseiRepository,
                              TraduccionService traduccionService,
                              FinanzasService finanzasService,
                              ConfiguracionService configuracionService) {
        this.senseiRepository = senseiRepository;
        this.traduccionService = traduccionService;
        this.finanzasService = finanzasService;
        this.configuracionService = configuracionService;

        setSizeFull();
        configureGrid();
        loadData();
    }

    private void configureGrid() {
        grid.addColumn(Sensei::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(s -> s.getUsuario().getNombre() + " " + s.getUsuario().getApellido()).setHeader("Nombre");
        grid.addColumn(s -> s.getUsuario().getUsername()).setHeader("Email");
        grid.addColumn(new ComponentRenderer<>(sensei -> {
            Checkbox check = new Checkbox(sensei.isEsClubPropio());
            check.addValueChangeListener(e -> {
                sensei.setEsClubPropio(e.getValue());
                senseiRepository.save(sensei);
            });
            return check;
        })).setHeader("Club Propio").setWidth("120px");
        grid.addColumn(new ComponentRenderer<>(sensei -> {
            NumberField comisionField = new NumberField();
            comisionField.setValue(sensei.getComisionPorcentaje().doubleValue());
            comisionField.setStep(0.01);
            comisionField.setMin(0);
            comisionField.setMax(100);
            comisionField.setWidth("100px");
            comisionField.addValueChangeListener(e -> {
                sensei.setComisionPorcentaje(BigDecimal.valueOf(e.getValue()));
                senseiRepository.save(sensei);
                Notification.show("Comisión actualizada para " + sensei.getUsuario().getNombre())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadData(); // recargar
            });
            return comisionField;
        })).setHeader("Comisión %").setWidth("120px");
        grid.addColumn(s -> s.getGrado()).setHeader("Grado");

        // 🔹 Columna de saldo con badge
        grid.addColumn(new ComponentRenderer<>(this::crearBadgeSaldo))
                .setHeader("Saldo a liquidar")
                .setWidth("150px");

        // 🔹 Columna de acción (botón Liquidar)
        grid.addColumn(new ComponentRenderer<>(this::crearBotonLiquidar))
                .setHeader("Acción")
                .setWidth("120px");

        grid.setAllRowsVisible(true);
        add(grid);
    }

    private Span crearBadgeSaldo(Sensei sensei) {
        BigDecimal saldo = sensei.getSaldoWallet();
        String saldoFormateado = formatearMoneda(saldo);
        Span badge = new Span(saldoFormateado);
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            badge.getElement().getThemeList().add("badge success");
        } else {
            badge.getElement().getThemeList().add("badge contrast");
        }
        return badge;
    }

    private HorizontalLayout crearBotonLiquidar(Sensei sensei) {
        Button btnLiquidar = new Button("Liquidar", VaadinIcon.MONEY.create());
        btnLiquidar.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnLiquidar.setEnabled(sensei.getSaldoWallet().compareTo(BigDecimal.ZERO) > 0);
        btnLiquidar.addClickListener(e -> {
            // Aquí podrías abrir un diálogo para pedir comprobante o usar uno genérico
            finanzasService.liquidarSaldoSensei(sensei, "Liquidación manual desde interfaz");
            Notification.show("Liquidación registrada para " + sensei.getUsuario().getNombre())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadData(); // Recargar grid
        });
        return new HorizontalLayout(btnLiquidar);
    }

    private void loadData() {
        List<Sensei> senseis = senseiRepository.findAllWithUsuario();
        grid.setItems(senseis);

        // Verificar si hay senseis con saldo pendiente y mostrar notificación
        boolean hayPendientes = senseis.stream()
                .anyMatch(s -> s.getSaldoWallet().compareTo(BigDecimal.ZERO) > 0);
        if (hayPendientes) {
            Notification notification = Notification.show(
                    "Hay senseis con saldo pendiente de liquidación.",
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) return configuracionService.obtenerFormatoMoneda().format(BigDecimal.ZERO);
        return configuracionService.obtenerFormatoMoneda().format(valor);
    }
}