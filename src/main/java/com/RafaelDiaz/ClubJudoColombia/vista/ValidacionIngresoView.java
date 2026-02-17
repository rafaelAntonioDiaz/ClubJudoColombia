package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.servicio.impl.CloudflareR2AlmacenamientoService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "admisiones", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Admisiones | Club Judo Colombia")
public class ValidacionIngresoView extends VerticalLayout {

    private final JudokaRepository judokaRepository;
    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService;
    private final FinanzasService finanzasService;
    private final CloudflareR2AlmacenamientoService cloudflareR2AlmacenamientoService;
    // Dividimos en dos tablas
    private Grid<Judoka> gridDojoPrincipal;
    private Grid<Judoka> gridSaaS;

    private static final String MASTER_ADMIN_USERNAME = "master_admin";

    @Autowired
    public ValidacionIngresoView(JudokaRepository judokaRepository,
                                 AdmisionesService admisionesService,
                                 TraduccionService traduccionService,
                                 FinanzasService finanzasService,
                                 CloudflareR2AlmacenamientoService cloudflareR2AlmacenamientoService) {
        this.judokaRepository = judokaRepository;
        this.admisionesService = admisionesService;
        this.traduccionService = traduccionService;
        this.finanzasService = finanzasService;
        this.cloudflareR2AlmacenamientoService = cloudflareR2AlmacenamientoService;

        addClassName("admisiones-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2(traduccionService.get("admisiones.titulo")));
        add(new Span(traduccionService.get("admisiones.descripcion")));

        configurarGrids();
        actualizarGrid();
    }

    private void configurarGrids() {
        // --- 1. GRID DOJO PRINCIPAL ---
        gridDojoPrincipal = new Grid<>(Judoka.class, false);
        gridDojoPrincipal.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gridDojoPrincipal.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.aspirante")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponenteWaiver))
                .setHeader(traduccionService.get("admisiones.grid.documentos")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponenteEps))
                .setHeader(traduccionService.get("admisiones.grid.documentos.eps")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponentePago))
                .setHeader(traduccionService.get("admisiones.grid.pago")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearBotonesAccion))
                .setHeader(traduccionService.get("generic.decision")).setAutoWidth(true);

        // --- 2. GRID SAAS (Solo muestra Comprobante de Pago) ---
        gridSaaS = new Grid<>(Judoka.class, false);
        gridSaaS.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gridSaaS.addColumn(j -> j.getSensei().getUsuario().getNombre() + " " + j.getSensei().getUsuario().getApellido())
                .setHeader("Sensei a Cargo").setAutoWidth(true); // Puedes añadir esto al TraduccionService luego
        gridSaaS.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.aspirante")).setAutoWidth(true);
        gridSaaS.addColumn(new ComponentRenderer<>(this::crearComponentePago))
                .setHeader(traduccionService.get("admisiones.grid.pago")).setAutoWidth(true);
        gridSaaS.addColumn(new ComponentRenderer<>(this::crearBotonesAccion))
                .setHeader(traduccionService.get("generic.decision")).setAutoWidth(true);

        // Añadimos a la vista con títulos separados
        add(new H3("Alumnos Dojo Principal"));
        add(gridDojoPrincipal);
        add(new H3("Nuevas Suscripciones SaaS (Otros Dojos)"));
        add(gridSaaS);
    }



    private Component crearBotonesAccion(Judoka judoka) {
        Button btnAprobar = new Button(traduccionService.get("btn.activar"), new Icon(VaadinIcon.USER_CHECK));
        btnAprobar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button btnRechazar = new Button(traduccionService.get("btn.rechazar"), new Icon(VaadinIcon.TRASH));
        btnRechazar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        btnAprobar.addClickListener(e -> {
            try {
                admisionesService.activarJudoka(judoka);
                Notification.show(traduccionService.get("admisiones.msg.activado") + " " + judoka.getUsuario().getNombre()).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                actualizarGrid();
            } catch (RuntimeException ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        btnRechazar.addClickListener(e -> {
            admisionesService.rechazarAspirante(judoka, "Rechazado por el Sensei");
            Notification.show(traduccionService.get("admisiones.msg.rechazado")).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            actualizarGrid();
        });

        return new HorizontalLayout(btnAprobar, btnRechazar);
    }

    // --- SEPARACIÓN DE LAS LISTAS ---
    private void actualizarGrid() {
        List<Judoka> todosPendientes = judokaRepository.findByEstadoWithDetails(EstadoJudoka.PENDIENTE);

        // Alumnos del Dojo Principal (El Sensei es master_admin)
        List<Judoka> dojoPrincipal = todosPendientes.stream()
                .filter(j -> j.getSensei().getUsuario().getUsername().equals(MASTER_ADMIN_USERNAME))
                .collect(Collectors.toList());

        // Suscripciones SaaS (El Sensei es cualquier otro)
        List<Judoka> saas = todosPendientes.stream()
                .filter(j -> !j.getSensei().getUsuario().getUsername().equals(MASTER_ADMIN_USERNAME))
                .collect(Collectors.toList());

        gridDojoPrincipal.setItems(dojoPrincipal);
        gridSaaS.setItems(saas);
    }

    // MUESTRA EL COMPROBANTE DE PAGO ---
    private Component crearComponentePago(Judoka judoka) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.CENTER);

        // 1. Mostrar el enlace del documento a la Nube (AWS S3 / R2)
        Optional<DocumentoRequisito> pagoOpt = judoka.getDocumentos().stream()
                .filter(d -> d.getTipo() == TipoDocumento.COMPROBANTE_PAGO)
                .findFirst();

        if (pagoOpt.isPresent()) {
            String urlSegura = cloudflareR2AlmacenamientoService.generarUrlSegura(pagoOpt.get().getUrlArchivo());
            Anchor link = new Anchor(urlSegura, traduccionService.get("btn.ver_pdf"));
            link.getElement().setAttribute("target", "_blank");
            Button btnVer = new Button(new Icon(VaadinIcon.DOLLAR));
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            link.add(btnVer);
            layout.add(link);
        } else {
            Span sinDoc = new Span("Sin Comprobante");
            sinDoc.getElement().getThemeList().add("badge contrast");
            layout.add(sinDoc);
        }

        // 2. Estado de pago y Botón de Marcar
        if (judoka.isMatriculaPagada()) {
            Span span = new Span(traduccionService.get("generic.pagado"));
            span.getElement().getThemeList().add("badge success");
            layout.add(span);
        } else {
            Button btnMarcar = new Button(traduccionService.get("admisiones.btn.marcar_pago"));
            btnMarcar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnMarcar.addClickListener(e -> {
                admisionesService.registrarPagoMatricula(judoka);
                Notification.show(traduccionService.get("msg.success.payment_manual")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                finanzasService.procesarPagoMensualidad(judoka,"EFECTIVO");
                judoka.setMatriculaPagada(true);
                judoka.setFechaVencimientoSuscripcion(LocalDate.now());
                actualizarGrid();
            });
            layout.add(btnMarcar);
        }

        return layout;
    }

    private Component crearComponenteWaiver(Judoka judoka) {
        Optional<DocumentoRequisito> waiverOpt = judoka.getDocumentos().stream()
                .filter(d -> d.getTipo() == TipoDocumento.WAIVER)
                .findFirst();

        if (waiverOpt.isPresent()) {
            String urlSegura = cloudflareR2AlmacenamientoService.generarUrlSegura(waiverOpt.get().getUrlArchivo());
            Anchor link = new Anchor(urlSegura, traduccionService.get("btn.ver_pdf"));
            link.getElement().setAttribute("target", "_blank");
            Button btnVer = new Button(new Icon(VaadinIcon.FILE_TEXT));
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            link.add(btnVer);
            HorizontalLayout hl = new HorizontalLayout(new Icon(VaadinIcon.CHECK_CIRCLE), link);
            hl.setAlignItems(Alignment.CENTER);
            hl.getElement().getThemeList().add("badge success");
            return hl;
        } else {
            Span span = new Span(traduccionService.get("generic.pendiente"));
            span.getElement().getThemeList().add("badge error");
            return span;
        }
    }

    private Component crearComponenteEps(Judoka judoka) {
        Optional<DocumentoRequisito> epsOpt = judoka.getDocumentos().stream()
                .filter(d -> d.getTipo() == TipoDocumento.EPS)
                .findFirst();

        if (epsOpt.isPresent()) {
// Pedimos la URL temporal justo en el momento que se pinta la tabla
            String urlSegura = cloudflareR2AlmacenamientoService.generarUrlSegura(epsOpt.get().getUrlArchivo());
            Anchor link = new Anchor(urlSegura, traduccionService.get("btn.ver_pdf"));
            link.getElement().setAttribute("target", "_blank");
            Button btnVer = new Button(new Icon(VaadinIcon.AMBULANCE));
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            link.add(btnVer);
            HorizontalLayout hl = new HorizontalLayout(new Icon(VaadinIcon.CHECK_CIRCLE), link);
            hl.setAlignItems(Alignment.CENTER);
            hl.getElement().getThemeList().add("badge success");
            return hl;
        } else {
            Span span = new Span(traduccionService.get("generic.pendiente"));
            span.getElement().getThemeList().add("badge error");
            return span;
        }
    }
}
