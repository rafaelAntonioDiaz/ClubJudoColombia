package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.modelo.Competencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import com.RafaelDiaz.ClubJudoColombia.servicio.CompetenciaService;
import com.RafaelDiaz.ClubJudoColombia.servicio.SecurityService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.JudokaLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;


import java.io.File;
import java.nio.file.Files;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Map;

@Route(value = "mis-competencias", layout = JudokaLayout.class)
@PageTitle("Mis Torneos | Club Judo")
@RolesAllowed({"ROLE_JUDOKA", "ROLE_ACUDIENTE"})
public class MisCompetenciasView extends VerticalLayout {

    private final CompetenciaService competenciaService;
    private final SecurityService securityService;
    private final AlmacenamientoCloudService cloudService;

    public MisCompetenciasView(CompetenciaService competenciaService,
                               SecurityService securityService,
                               AlmacenamientoCloudService cloudService) {
        this.competenciaService = competenciaService;
        this.securityService = securityService;
        this.cloudService = cloudService;
        configurarUI();
    }

    private void configurarUI() {
        setSpacing(true);
        setPadding(true);

        securityService.getAuthenticatedJudoka().ifPresentOrElse(judoka -> {
            add(new H3("Mis Próximos Campeonatos"));
            Grid<Competencia> grid = new Grid<>(Competencia.class, false);
            grid.addColumn(Competencia::getNombre).setHeader("Torneo").setAutoWidth(true);
            grid.addColumn(Competencia::getFechaInicio).setHeader("Fecha");

            grid.addComponentColumn(comp -> {
                Button btnDocs = new Button("Documentos", new Icon(VaadinIcon.FILE_TEXT));
                btnDocs.addClickListener(e -> abrirDialogoEntrega(judoka, comp));
                return btnDocs;
            }).setHeader("Acción");

            grid.setItems(competenciaService.obtenerInscripcionesDeJudoka(judoka));
            add(grid);
        }, () -> add(new Span("No se encontró perfil de Judoka.")));
    }

    private void abrirDialogoEntrega(Judoka judoka, Competencia competencia) {
        com.vaadin.flow.component.dialog.Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Requisitos: " + competencia.getNombre());
        dialog.setWidth("600px");

        VerticalLayout lista = new VerticalLayout();
        Map<String, String> estadoDocs = competenciaService.obtenerEstadoDocumentos(judoka, competencia);

        estadoDocs.forEach((requisito, urlExistente) -> {
            HorizontalLayout fila = new HorizontalLayout();
            fila.setAlignItems(FlexComponent.Alignment.CENTER);
            fila.setWidthFull();
            fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            fila.add(new Span(requisito));

            if (urlExistente != null) {
                Anchor link = new Anchor(urlExistente, "Ver");
                link.setTarget("_blank");
                fila.add(new HorizontalLayout(new Icon(VaadinIcon.CHECK_CIRCLE), link));
            } else {
                // --- IMPLEMENTACIÓN PURA CON UPLOADHANDLER ---
                Upload upload = new Upload();
                upload.setAcceptedFileTypes("application/pdf", "image/jpeg", "image/png");

                // UploadHandler.toTempFile es el método moderno que reemplaza Receivers y Listeners
                upload.setUploadHandler(UploadHandler.toTempFile((request, tempFile) -> {
                    // El procesamiento ocurre en un hilo de background, usamos ui.access()
                    getUI().ifPresent(ui -> ui.access(() -> {
                        try (FileInputStream fis = new FileInputStream(tempFile)) {
                            // 1. Subir a la nube usando el nombre del archivo del request

                            String url = cloudService.subirArchivo(judoka.getId(), request.fileName(), fis);

                            // 2. Persistir en base de datos
                            competenciaService.recibirDocumentoTorneo(judoka, competencia, requisito, url);

                            Notification.show("¡Recibido!")
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                            // 3. Actualizar UI
                            dialog.close();
                            abrirDialogoEntrega(judoka, competencia);

                        } catch (Exception ex) {
                            Notification.show("Error: " + ex.getMessage())
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        } finally {
                            // Limpieza atómica del archivo temporal
                            try { Files.deleteIfExists(tempFile.toPath()); } catch (Exception ignored) {}
                        }
                    }));
                }));

                fila.add(upload);
            }
            lista.add(fila);
        });

        dialog.add(lista);
        dialog.open();
    }
}