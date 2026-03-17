package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.BackupService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "backup", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Backup | Club Judo Colombia")
public class BackupView extends VerticalLayout {

    private final BackupService backupService;

    public BackupView(BackupService backupService) {
        this.backupService = backupService;

        Button btnBackup = new Button("Generar y Descargar Backup", e -> {
            // Redirigir al endpoint de descarga
            UI.getCurrent().getPage().setLocation("/api/backup/descargar");
            Notification.show("Generando backup... La descarga comenzará en breve.");
        });
        btnBackup.setWidthFull();

        add(btnBackup);
    }
}