package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService; // <--- INYECCIÓN
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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
import java.util.List;
import java.util.Optional;

@Route(value = "admisiones", layout = SenseiLayout.class)
@RolesAllowed("ROLE_SENSEI")
@PageTitle("Admisiones | Club Judo Colombia")
public class ValidacionIngresoView extends VerticalLayout {

    private final JudokaRepository judokaRepository;
    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService; // <--- I18n

    private Grid<Judoka> gridAspirantes;

    @Autowired
    public ValidacionIngresoView(JudokaRepository judokaRepository,
                                 AdmisionesService admisionesService,
                                 TraduccionService traduccionService) {
        this.judokaRepository = judokaRepository;
        this.admisionesService = admisionesService;
        this.traduccionService = traduccionService;

        addClassName("admisiones-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2(traduccionService.get("admisiones.titulo")));
        add(new Span(traduccionService.get("admisiones.descripcion")));

        configureGrid();
        actualizarGrid();

        add(gridAspirantes);
    }

    private void configureGrid() {
        gridAspirantes = new Grid<>(Judoka.class, false);
        gridAspirantes.addClassName("admisiones-grid");
        gridAspirantes.setSizeFull();
        gridAspirantes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gridAspirantes.addColumn(j -> j.getUsuario().getNombre() + " " + j.getUsuario().getApellido())
                .setHeader(traduccionService.get("generic.aspirante"))
                .setAutoWidth(true)
                .setSortable(true);

        gridAspirantes.addColumn(new LocalDateTimeRenderer<>(Judoka::getFechaPreRegistro, "dd/MM/yyyy HH:mm"))
                .setHeader(traduccionService.get("admisiones.grid.registrado"))
                .setAutoWidth(true);

        gridAspirantes.addColumn(new ComponentRenderer<>(this::crearComponenteWaiver))
                .setHeader(traduccionService.get("admisiones.grid.documentos"))
                .setAutoWidth(true);
        gridAspirantes.addColumn(new ComponentRenderer<>(this::crearComponenteEps))
                .setHeader(traduccionService.get("admisiones.grid.documentos.eps")) // "Certificado EPS"
                .setAutoWidth(true);
        gridAspirantes.addColumn(new ComponentRenderer<>(this::crearComponentePago))
                .setHeader(traduccionService.get("admisiones.grid.pago"))
                .setAutoWidth(true);

        gridAspirantes.addColumn(new ComponentRenderer<>(this::crearBotonesAccion))
                .setHeader(traduccionService.get("generic.decision"))
                .setAutoWidth(true);
    }

    private Component crearComponenteWaiver(Judoka judoka) {
        Optional<DocumentoRequisito> waiverOpt = judoka.getDocumentos().stream()
                .filter(d -> d.getTipo() == TipoDocumento.WAIVER)
                .findFirst();

        if (waiverOpt.isPresent()) {
            String urlEnLaNube = waiverOpt.get().getUrlArchivo();

            // Creamos un link directo a la URL de Amazon S3
            Anchor link = new Anchor(urlEnLaNube, traduccionService.get("btn.ver_pdf"));
            link.getElement().setAttribute("target", "_blank"); // Abre en nueva pestaña

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
                .filter(d -> d.getTipo() == TipoDocumento.EPS) // Asegúrate que coincida con tu Enum
                .findFirst();

        if (epsOpt.isPresent()) {
            String urlEnLaNube = epsOpt.get().getUrlArchivo();

            // Link directo a la nube (AWS S3)
            Anchor link = new Anchor(urlEnLaNube, traduccionService.get("btn.ver_pdf"));
            link.getElement().setAttribute("target", "_blank");

            Button btnVer = new Button(new Icon(VaadinIcon.AMBULANCE)); // Icono de medicina para EPS
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
    private Component crearComponentePago(Judoka judoka) {
        if (judoka.isMatriculaPagada()) {
            Span span = new Span(traduccionService.get("generic.pagado"));
            span.getElement().getThemeList().add("badge success");
            return span;
        } else {
            Button btnMarcar = new Button(traduccionService.get("admisiones.btn.marcar_pago"));
            btnMarcar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnMarcar.addClickListener(e -> {
                admisionesService.registrarPagoMatricula(judoka);
                Notification.show(traduccionService.get("msg.success.payment_manual")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                actualizarGrid();
            });

            HorizontalLayout hl = new HorizontalLayout(new Span(traduccionService.get("generic.no_registrado")), btnMarcar);
            hl.setAlignItems(Alignment.CENTER);
            return hl;
        }
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

    private void actualizarGrid() {
        List<Judoka> pendientes = judokaRepository.findByEstadoWithDetails(EstadoJudoka.PENDIENTE);
        gridAspirantes.setItems(pendientes);
    }
}