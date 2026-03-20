package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.DocumentoRequisito;
import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoJudoka;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import com.RafaelDiaz.ClubJudoColombia.repositorio.GrupoEntrenamientoRepository;
import com.RafaelDiaz.ClubJudoColombia.repositorio.JudokaRepository;
import com.RafaelDiaz.ClubJudoColombia.servicio.AdmisionesService;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.FinanzasService;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "admisiones", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Admisiones | Club Judo Colombia")
public class ValidacionIngresoView extends VerticalLayout {

    private final JudokaRepository judokaRepository;
    private final GrupoEntrenamientoRepository grupoEntrenamientoRepository;
    private final AdmisionesService admisionesService;
    private final TraduccionService traduccionService;
    private final FinanzasService finanzasService;
    private final CloudflareR2AlmacenamientoService cloudflareR2AlmacenamientoService;

    private Grid<Judoka> gridDojoPrincipal;
    private Grid<Judoka> gridSaaS;

    private List<Judoka> judokasPendientes;

    private static final String MASTER_ADMIN_USERNAME = "master_admin";

    @Autowired
    public ValidacionIngresoView(JudokaRepository judokaRepository, GrupoEntrenamientoRepository grupoEntrenamientoRepository,
                                 AdmisionesService admisionesService,
                                 TraduccionService traduccionService,
                                 FinanzasService finanzasService,
                                 CloudflareR2AlmacenamientoService cloudflareR2AlmacenamientoService) {
        this.judokaRepository = judokaRepository;
        this.grupoEntrenamientoRepository = grupoEntrenamientoRepository;
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

        gridDojoPrincipal.addColumn(this::obtenerNombreAspirante)
                .setHeader(traduccionService.get("generic.aspirante")).setAutoWidth(true);

        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponenteWaiver))
                .setHeader(traduccionService.get("admisiones.grid.documentos")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponenteEps))
                .setHeader(traduccionService.get("admisiones.grid.documentos.eps")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearComponentePago))
                .setHeader(traduccionService.get("admisiones.grid.pago")).setAutoWidth(true);
        gridDojoPrincipal.addColumn(new ComponentRenderer<>(this::crearBotonesAccion))
                .setHeader(traduccionService.get("generic.decision")).setAutoWidth(true);

        // --- 2. GRID SAAS ---
        gridSaaS = new Grid<>(Judoka.class, false);
        gridSaaS.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        gridSaaS.addColumn(j -> j.getSensei().getUsuario().getNombre() + " " + j.getSensei().getUsuario().getApellido())
                .setHeader("Sensei a Cargo").setAutoWidth(true);

        gridSaaS.addColumn(this::obtenerNombreAspirante)
                .setHeader(traduccionService.get("generic.aspirante")).setAutoWidth(true);

        gridSaaS.addColumn(new ComponentRenderer<>(this::crearComponentePago))
                .setHeader(traduccionService.get("admisiones.grid.pago")).setAutoWidth(true);
        gridSaaS.addColumn(new ComponentRenderer<>(this::crearBotonesAccion))
                .setHeader(traduccionService.get("generic.decision")).setAutoWidth(true);

        // AÑADIDO UNA SOLA VEZ A LA VISTA (Limpiando duplicados visuales)
        add(new H3("Alumnos Dojo Principal"));
        add(gridDojoPrincipal);
        add(new H3("Nuevas Suscripciones SaaS (Otros Dojos)"));
        add(gridSaaS);
    }

    private void actualizarGrid() {
        // 1. Escudo Anti-Clones (Agrupamos estrictamente por ID)
        Map<Long, Judoka> mapaUnicos = new java.util.LinkedHashMap<>();
        for (Judoka j : admisionesService.obtenerJudokasParaValidacion()) {
            if (j.getFechaNacimiento() != null) {
                mapaUnicos.put(j.getId(), j);
            }
        }
        judokasPendientes = new java.util.ArrayList<>(mapaUnicos.values());

        List<Judoka> dojoPrincipal = judokasPendientes.stream()
                .filter(j -> j.getSensei() != null && j.getSensei().getUsuario().getUsername().equals(MASTER_ADMIN_USERNAME))
                .collect(Collectors.toList());

        List<Judoka> saas = judokasPendientes.stream()
                .filter(j -> j.getSensei() != null && !j.getSensei().getUsuario().getUsername().equals(MASTER_ADMIN_USERNAME))
                .collect(Collectors.toList());

        // 2. Escudo Anti-Sobrescritura de Vaadin (Forzamos a la tabla a reconocer a los hermanos)
        com.vaadin.flow.data.provider.ListDataProvider<Judoka> dpPrincipal = new com.vaadin.flow.data.provider.ListDataProvider<>(dojoPrincipal) {
            @Override
            public Object getId(Judoka item) {
                return item.getId(); // ¡Esto evita que Vaadin crea que los hermanos son la misma persona!
            }
        };
        gridDojoPrincipal.setDataProvider(dpPrincipal);

        com.vaadin.flow.data.provider.ListDataProvider<Judoka> dpSaas = new com.vaadin.flow.data.provider.ListDataProvider<>(saas) {
            @Override
            public Object getId(Judoka item) {
                return item.getId();
            }
        };
        gridSaaS.setDataProvider(dpSaas);
    }

    private Component crearBotonesAccion(Judoka judoka) {
        Button btnAprobar = new Button("Aprobar Grupo", new Icon(VaadinIcon.USER_CHECK));
        btnAprobar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button btnRechazar = new Button("Rechazar", new Icon(VaadinIcon.TRASH));
        btnRechazar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        btnAprobar.addClickListener(e -> aprobarFamiliar(judoka));

        btnRechazar.addClickListener(e -> {
            judoka.setEstado(EstadoJudoka.RECHAZADO);
            judokaRepository.save(judoka);
            admisionesService.rechazarAspirante(judoka, "Rechazado por el Master");
            Notification.show("Ingreso rechazado.").addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            actualizarGrid();
        });

        return new HorizontalLayout(btnAprobar, btnRechazar);
    }

    private void procesarPagoFamiliar(Judoka judokaPivot) {
        try {
            List<Judoka> hermanos = obtenerHermanosPendientes(judokaPivot);
            Optional<DocumentoRequisito> pagoOpt = obtenerDocumentoCompartido(judokaPivot, TipoDocumento.COMPROBANTE_PAGO);
            String urlComprobante = pagoOpt.map(DocumentoRequisito::getUrlArchivo).orElse("Aprobado_Por_Master_Sin_Soporte");

            for (Judoka j : hermanos) {
                if (!j.isMatriculaPagada()) {
                    finanzasService.procesarPagoOnboarding(j, urlComprobante);
                }
            }

            Notification.show("Pago validado y registrado en Caja para toda la familia.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            actualizarGrid();
        } catch (Exception ex) {
            Notification.show("Error al registrar pago: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void aprobarFamiliar(Judoka judokaPivot) {
        try {
            List<Judoka> hermanos = obtenerHermanosPendientes(judokaPivot);
            Optional<DocumentoRequisito> pagoOpt = obtenerDocumentoCompartido(judokaPivot, TipoDocumento.COMPROBANTE_PAGO);
            String urlComprobante = pagoOpt.map(DocumentoRequisito::getUrlArchivo).orElse("Aprobado_Por_Master_Sin_Soporte");

            for (Judoka j : hermanos) {
                if (!j.isMatriculaPagada()) {
                    finanzasService.procesarPagoOnboarding(j, urlComprobante);
                }
                j.setEstado(EstadoJudoka.ACTIVO);
                judokaRepository.save(j);
                admisionesService.activarJudoka(j);
            }

            Notification.show("¡Grupo familiar activado y finanzas procesadas exitosamente!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            actualizarGrid();
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Component crearComponenteWaiver(Judoka judoka) {
        Optional<DocumentoRequisito> waiverOpt = obtenerDocumentoCompartido(judoka, TipoDocumento.WAIVER);
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

// En ValidacionIngresoView, en el método crearComponentePago

    private Component crearComponentePago(Judoka judoka) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.CENTER);

        // Obtener el grupo (primer grupo)
        grupoEntrenamientoRepository.findByJudokas_Id(judoka.getId());
        String tarifaEsperada = "";
        if (grupoEntrenamientoRepository.findByJudokas_Id(judoka.getId()) != null && !grupoEntrenamientoRepository.findByJudokas_Id(judoka.getId()).isEmpty()) {
            GrupoEntrenamiento grupo = grupoEntrenamientoRepository.findByJudokas_Id(judoka.getId()).iterator().next();
            tarifaEsperada = "Esperado: $" + grupo.getTarifaMensual();
        }

        Optional<DocumentoRequisito> pagoOpt = obtenerDocumentoCompartido(judoka, TipoDocumento.COMPROBANTE_PAGO);

        if (pagoOpt.isPresent()) {
            String urlSegura = cloudflareR2AlmacenamientoService.generarUrlSegura(pagoOpt.get().getUrlArchivo());
            Anchor link = new Anchor(urlSegura, "Ver Recibo Nequi");
            link.getElement().setAttribute("target", "_blank");
            Button btnVer = new Button(new Icon(VaadinIcon.PICTURE));
            btnVer.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            link.add(btnVer);
            layout.add(link);
            if (!tarifaEsperada.isEmpty()) {
                Span esperado = new Span(tarifaEsperada);
                esperado.getElement().getThemeList().add("badge contrast");
                layout.add(esperado);
            }
        } else {
            Span sinDoc = new Span("Pendiente de Pago " + tarifaEsperada);
            sinDoc.getElement().getThemeList().add("badge error contrast");
            layout.add(sinDoc);
        }

        if (judoka.isMatriculaPagada()) {
            Span span = new Span(traduccionService.get("generic.pagado"));
            span.getElement().getThemeList().add("badge success");
            layout.add(span);
        } else {
            Button btnMarcar = new Button(traduccionService.get("admisiones.btn.marcar_pago"));
            btnMarcar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnMarcar.addClickListener(e -> procesarPagoFamiliar(judoka));
            layout.add(btnMarcar);
        }
        return layout;
    }

    private Component crearComponenteEps(Judoka judoka) {
        Optional<DocumentoRequisito> epsOpt = obtenerDocumentoCompartido(judoka, TipoDocumento.EPS);
        if (epsOpt.isPresent()) {
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

    private List<Judoka> obtenerHermanosPendientes(Judoka judoka) {
        if (judoka.getAcudiente() == null || judokasPendientes == null) return List.of(judoka);
        return judokasPendientes.stream()
                .filter(j -> j.getAcudiente() != null &&
                        j.getAcudiente().getId().equals(judoka.getAcudiente().getId()))
                .collect(Collectors.toList());
    }

    private Optional<DocumentoRequisito> obtenerDocumentoCompartido(Judoka judoka, TipoDocumento tipo) {
        List<DocumentoRequisito> misDocs = judoka.getDocumentos() != null ? judoka.getDocumentos() : java.util.Collections.emptyList();
        Optional<DocumentoRequisito> docPropio = misDocs.stream()
                .filter(d -> d.getTipo() == tipo)
                .findFirst();

        if (docPropio.isPresent()) {
            return docPropio;
        }

        // --- REGLA ESTRICTA ---
        // SÓLO compartimos el comprobante de pago con los hermanos.
        // EPS y Waiver NO se comparten porque pertenecen a cada niño.
        if (tipo != TipoDocumento.COMPROBANTE_PAGO || judoka.getAcudiente() == null || judokasPendientes == null) {
            return Optional.empty();
        }

        return judokasPendientes.stream()
                .filter(j -> j.getAcudiente() != null &&
                        j.getAcudiente().getId().equals(judoka.getAcudiente().getId()) &&
                        !j.getId().equals(judoka.getId()))
                .flatMap(j -> j.getDocumentos() != null ? j.getDocumentos().stream() : java.util.stream.Stream.empty())
                .filter(d -> d != null && d.getTipo() == TipoDocumento.COMPROBANTE_PAGO)
                .findFirst();
    }

    private String obtenerNombreAspirante(Judoka j) {
        if (j.getNombre() != null && !j.getNombre().isEmpty()) {
            return j.getNombre() + " " + j.getApellido();
        } else if (j.getUsuario() != null) {
            return j.getUsuario().getNombre() + " " + j.getUsuario().getApellido();
        }
        return "Aspirante Sin Nombre";
    }
}