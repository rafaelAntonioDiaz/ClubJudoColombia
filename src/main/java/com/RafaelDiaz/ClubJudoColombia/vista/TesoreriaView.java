package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConceptoFinanciero;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.MetodoPago;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoTransaccion;
import com.RafaelDiaz.ClubJudoColombia.servicio.FileStorageService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.JudokaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.PdfReciboService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Route(value = "tesoreria", layout = SenseiLayout.class)
@RolesAllowed({"ROLE_SENSEI", "ROLE_ADMIN"})
@PageTitle("Tesorería | Club Judo Colombia")
public class TesoreriaView extends VerticalLayout {

    private final FinanzasService finanzasService;
    private final JudokaService judokaService;
    private final FileStorageService fileStorageService;
    private final PdfReciboService pdfReciboService;
    private final TraduccionService traduccionService; // NUEVO: Servicio de traducción

    private Span lblIngresosMes;
    private Span lblEgresosMes;
    private Span lblBalanceMes;
    private Grid<MovimientoCaja> gridMovimientos;

    @Autowired
    public TesoreriaView(FinanzasService finanzasService,
                         JudokaService judokaService,
                         FileStorageService fileStorageService,
                         PdfReciboService pdfReciboService,
                         TraduccionService traduccionService) { // NUEVO: Parámetro añadido
        this.finanzasService = finanzasService;
        this.judokaService = judokaService;
        this.fileStorageService = fileStorageService;
        this.pdfReciboService = pdfReciboService;
        this.traduccionService = traduccionService; // NUEVO: Inicialización

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.add(new H2(traduccionService.get("tesoreria.titulo"))); // NUEVO: Texto traducido
        add(header);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        // NUEVO: Pestañas con textos traducidos
        tabSheet.add(traduccionService.get("tesoreria.tab.registrar_ingreso"),
                crearFormularioIngreso());
        tabSheet.add(traduccionService.get("tesoreria.tab.registrar_gasto"),
                crearFormularioEgreso());
        tabSheet.add(traduccionService.get("tesoreria.tab.balance_reportes"),
                crearVistaBalance());

        add(tabSheet);
    }

    private Component crearFormularioIngreso() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("600px");

        ComboBox<Judoka> judokaSelect = new ComboBox<>(traduccionService.get("tesoreria.alumno"));
        judokaSelect.setItems(judokaService.findAllJudokas());
        judokaSelect.setItemLabelGenerator(
                j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido());
        judokaSelect.setPlaceholder(traduccionService.get("tesoreria.buscar_alumno"));
        judokaSelect.setWidthFull();

        HorizontalLayout conceptoLayout = new HorizontalLayout();
        conceptoLayout.setWidthFull();
        conceptoLayout.setAlignItems(Alignment.BASELINE);

        ComboBox<ConceptoFinanciero> conceptoSelect = new ComboBox<>(traduccionService.get("tesoreria.concepto"));
        conceptoSelect.setItems(finanzasService.obtenerConceptosPorTipo(TipoTransaccion.INGRESO));
        conceptoSelect.setItemLabelGenerator(ConceptoFinanciero::getNombre);
        conceptoSelect.setWidthFull();

        Button btnNuevoConcepto = new Button(new Icon(VaadinIcon.PLUS));
        btnNuevoConcepto.addClickListener(
                e -> abrirDialogoNuevoConcepto(TipoTransaccion.INGRESO, conceptoSelect));
        conceptoLayout.add(conceptoSelect, btnNuevoConcepto);

        BigDecimalField montoField = new BigDecimalField(traduccionService.get("tesoreria.valor"));
        montoField.setWidthFull();
        conceptoSelect.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().getValorSugerido() != null) {
                montoField.setValue(e.getValue().getValorSugerido());
            }
        });

        Select<MetodoPago> metodoPagoSelect = new Select<>();
        metodoPagoSelect.setLabel(traduccionService.get("tesoreria.metodo_pago"));
        metodoPagoSelect.setItems(MetodoPago.values());
        // NUEVO: Usar traducción de Enums
        metodoPagoSelect.setItemLabelGenerator(mp -> traduccionService.get(mp));
        metodoPagoSelect.setValue(MetodoPago.EFECTIVO);
        metodoPagoSelect.setWidthFull();

        TextArea observacion = new TextArea(traduccionService.get("tesoreria.observacion"));
        observacion.setWidthFull();

        Button btnCobrar = new Button(traduccionService.get("tesoreria.boton.registrar_generar_recibo"),
                new Icon(VaadinIcon.DIPLOMA_SCROLL));
        btnCobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCobrar.setWidthFull();

        btnCobrar.addClickListener(e -> {
            if (conceptoSelect.getValue() == null || montoField.getValue() == null) {
                Notification.show(traduccionService.get("tesoreria.validacion.concepto_monto"))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            MovimientoCaja mov = new MovimientoCaja();
            mov.setTipo(TipoTransaccion.INGRESO);
            mov.setConcepto(conceptoSelect.getValue());
            mov.setMonto(montoField.getValue());
            mov.setMetodoPago(metodoPagoSelect.getValue());
            mov.setJudoka(judokaSelect.getValue());
            mov.setObservacion(observacion.getValue());

            MovimientoCaja guardado = finanzasService.registrarMovimiento(mov);

            Notification.show(traduccionService.get("tesoreria.notificacion.ingreso_exitoso"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Descarga automática del PDF
            descargarRecibo(guardado);

            montoField.clear();
            observacion.clear();
            actualizarBalance();
        });

        layout.add(judokaSelect, conceptoLayout, montoField, metodoPagoSelect, observacion, btnCobrar);
        return layout;
    }

    private Component crearFormularioEgreso() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("600px");

        HorizontalLayout conceptoLayout = new HorizontalLayout();
        conceptoLayout.setWidthFull();
        conceptoLayout.setAlignItems(Alignment.BASELINE);

        ComboBox<ConceptoFinanciero> conceptoSelect = new ComboBox<>(traduccionService.get("tesoreria.categoria_gasto"));
        conceptoSelect.setItems(finanzasService.obtenerConceptosPorTipo(TipoTransaccion.EGRESO));
        conceptoSelect.setItemLabelGenerator(ConceptoFinanciero::getNombre);
        conceptoSelect.setWidthFull();

        Button btnNuevoConcepto = new Button(new Icon(VaadinIcon.PLUS));
        btnNuevoConcepto.addClickListener(
                e -> abrirDialogoNuevoConcepto(TipoTransaccion.EGRESO, conceptoSelect));
        conceptoLayout.add(conceptoSelect, btnNuevoConcepto);

        BigDecimalField montoField = new BigDecimalField(traduccionService.get("tesoreria.valor_pagado"));
        montoField.setWidthFull();

        Select<MetodoPago> metodoPagoSelect = new Select<>();
        metodoPagoSelect.setLabel(traduccionService.get("tesoreria.metodo_pago"));
        metodoPagoSelect.setItems(MetodoPago.values());
        // NUEVO: Usar traducción de Enums
        metodoPagoSelect.setItemLabelGenerator(mp -> traduccionService.get(mp));
        metodoPagoSelect.setValue(MetodoPago.EFECTIVO);
        metodoPagoSelect.setWidthFull();

        TextArea observacion = new TextArea(traduccionService.get("tesoreria.detalle_proveedor"));
        observacion.setWidthFull();

        Upload upload = new Upload();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf");
        upload.setMaxFiles(1);

        Button uploadBtn = new Button(traduccionService.get("tesoreria.foto_factura"),
                new Icon(VaadinIcon.CAMERA));
        upload.setUploadButton(uploadBtn);

        final String[] rutaSoporte = {null};

        upload.setUploadHandler(event -> {
            try {
                String path = fileStorageService.save(event.getInputStream(), "factura_" + System.currentTimeMillis() + "_" + event.getFileName());
                rutaSoporte[0] = path;
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("tesoreria.notificacion.soporte_cargado"))
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS)
                ));
            } catch (Exception ex) {
                getUI().ifPresent(ui -> ui.access(() ->
                        Notification.show(traduccionService.get("tesoreria.notificacion.error_subir") + ex.getMessage())
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)
                ));
            }
        });

        Button btnPagar = new Button(traduccionService.get("tesoreria.boton.registrar_salida"),
                new Icon(VaadinIcon.MINUS_CIRCLE));
        btnPagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btnPagar.setWidthFull();

        btnPagar.addClickListener(e -> {
            if (conceptoSelect.getValue() == null || montoField.getValue() == null) {
                Notification.show(traduccionService.get("tesoreria.validacion.categoria_monto"))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            MovimientoCaja mov = new MovimientoCaja();
            mov.setTipo(TipoTransaccion.EGRESO);
            mov.setConcepto(conceptoSelect.getValue());
            mov.setMonto(montoField.getValue());
            mov.setMetodoPago(metodoPagoSelect.getValue());
            mov.setObservacion(observacion.getValue());
            mov.setUrlSoporte(rutaSoporte[0]);

            finanzasService.registrarMovimiento(mov);

            Notification.show(traduccionService.get("tesoreria.notificacion.gasto_registrado"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            montoField.clear();
            observacion.clear();
            upload.clearFileList();
            rutaSoporte[0] = null;
            actualizarBalance();
        });

        layout.add(conceptoLayout, montoField, metodoPagoSelect, observacion,
                new H4(traduccionService.get("tesoreria.soporte")), upload, btnPagar);
        return layout;
    }

    private Component crearVistaBalance() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout kpis = new HorizontalLayout();
        kpis.setWidthFull();

        lblIngresosMes = crearKpiCard(traduccionService.get("tesoreria.kpi.ingresos_mes"),
                "var(--lumo-success-text-color)");
        lblEgresosMes = crearKpiCard(traduccionService.get("tesoreria.kpi.egresos_mes"),
                "var(--lumo-error-text-color)");
        lblBalanceMes = crearKpiCard(traduccionService.get("tesoreria.kpi.balance"),
                "var(--lumo-body-text-color)");

        kpis.add(lblIngresosMes, lblEgresosMes, lblBalanceMes);

        gridMovimientos = new Grid<>(MovimientoCaja.class, false);

        gridMovimientos.addColumn(new LocalDateTimeRenderer<>(
                MovimientoCaja::getFecha,
                "dd/MM/yyyy HH:mm"
        )).setHeader(traduccionService.get("tesoreria.grid.fecha")).setAutoWidth(true);

        // NUEVO: Usar traducción de Enums para TipoTransaccion
        gridMovimientos.addColumn(m -> traduccionService.get(m.getTipo()))
                .setHeader(traduccionService.get("tesoreria.grid.tipo")).setAutoWidth(true);

        gridMovimientos.addColumn(m -> m.getConcepto().getNombre())
                .setHeader(traduccionService.get("tesoreria.grid.concepto")).setAutoWidth(true);

        gridMovimientos.addColumn(m -> m.getMonto())
                .setHeader(traduccionService.get("tesoreria.grid.monto")).setAutoWidth(true);

        gridMovimientos.addColumn(m -> m.getJudoka() != null ? m.getJudoka().getUsuario().getNombre() : "-")
                .setHeader(traduccionService.get("tesoreria.grid.judoka"));

        gridMovimientos.addColumn(new ComponentRenderer<>(mov -> {
            if (mov.getTipo() == TipoTransaccion.INGRESO) {
                Button btnPdf = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
                btnPdf.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                btnPdf.addClickListener(e -> descargarRecibo(mov));
                return btnPdf;
            }
            return new Span();
        })).setHeader(traduccionService.get("tesoreria.grid.soporte"));

        gridMovimientos.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        actualizarBalance();

        layout.add(kpis, gridMovimientos);
        return layout;
    }

    private Span crearKpiCard(String titulo, String color) {
        Span val = new Span("$ 0");
        val.getStyle().set("font-size", "1.5rem").set("font-weight", "bold").set("color", color);
        VerticalLayout v = new VerticalLayout(new Span(titulo), val);
        v.addClassName("card-blanca");
        v.setSpacing(false);
        v.setAlignItems(Alignment.CENTER);
        return val;
    }

    private void actualizarBalance() {
        lblIngresosMes.setText("$ " + finanzasService.calcularTotalIngresosMes());
        lblEgresosMes.setText("$ " + finanzasService.calcularTotalEgresosMes());
        lblBalanceMes.setText("$ " + finanzasService.calcularBalanceMes());
        gridMovimientos.setItems(finanzasService.obtenerMovimientosDelMes());
    }

    /**
     * Método Definitivo para Descarga en Vaadin 24.8
     * Soluciona los problemas de Page.open y DownloadHandler.fromByteArray
     */
    private void descargarRecibo(MovimientoCaja movimiento) {
        // 1. Generar bytes del PDF
        byte[] pdfBytes = pdfReciboService.generarReciboPdf(movimiento);
        String filename = "Recibo_" + movimiento.getId() + ".pdf";

        // 2. Crear Handler usando fromInputStream
        DownloadHandler handler = DownloadHandler.fromInputStream(context ->
                new com.vaadin.flow.server.streams.DownloadResponse(
                        new ByteArrayInputStream(pdfBytes),
                        filename,
                        "application/pdf",
                        pdfBytes.length
                )
        );

        // 3. Crear Anchor Invisible para disparar la descarga
        Anchor anchor = new Anchor(handler, "");
        anchor.getElement().setAttribute("download", true);
        anchor.getStyle().set("display", "none");

        add(anchor); // El anchor debe estar en el DOM para funcionar

        // 4. Click programático
        getUI().ifPresent(ui -> ui.getPage().executeJs("arguments[0].click()", anchor.getElement()));
    }

    private void abrirDialogoNuevoConcepto(TipoTransaccion tipo, ComboBox<ConceptoFinanciero> comboRefresco) {
        Dialog d = new Dialog();
        d.setHeaderTitle(traduccionService.get("tesoreria.dialog.nuevo_concepto.titulo") +
                " " + traduccionService.get(tipo));

        TextField nombre = new TextField(traduccionService.get("tesoreria.dialog.nuevo_concepto.nombre"));
        BigDecimalField valor = new BigDecimalField(traduccionService.get("tesoreria.dialog.nuevo_concepto.valor_sugerido"));

        Button guardar = new Button(traduccionService.get("tesoreria.boton.guardar"), e -> {
            if (!nombre.isEmpty()) {
                ConceptoFinanciero c = new ConceptoFinanciero(nombre.getValue(), tipo, valor.getValue());
                finanzasService.guardarConcepto(c);
                comboRefresco.setItems(finanzasService.obtenerConceptosPorTipo(tipo));
                d.close();
                Notification.show(traduccionService.get("tesoreria.notificacion.concepto_creado"));
            }
        });

        d.add(new VerticalLayout(nombre, valor, guardar));
        d.open();
    }
}