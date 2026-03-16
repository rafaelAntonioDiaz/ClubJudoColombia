package com.RafaelDiaz.ClubJudoColombia.vista;

import com.RafaelDiaz.ClubJudoColombia.servicio.BackupService;
import com.RafaelDiaz.ClubJudoColombia.vista.layout.SenseiLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;

@Route(value = "backup", layout = SenseiLayout.class)
@RolesAllowed("ROLE_MASTER")
@PageTitle("Back Up | Club Judo Colombia")

public class BackupView extends VerticalLayout {


    private final BackupService backupService;

    public BackupView(BackupService backupService) {
        this.backupService = backupService;

        TextField rutaField = new TextField("Ruta de destino (incluye nombre del archivo .zip)");
        rutaField.setValue("/media/disco_externo/backup_" + LocalDate.now() + ".zip");
        rutaField.setWidthFull();

        Button btnBackup = new Button("Realizar Backup", e -> {
            try {
                backupService.backupDatabase(rutaField.getValue());
                Notification.show("Backup completado exitosamente en: " + rutaField.getValue());
            } catch (Exception ex) {
                Notification.show("Error al hacer backup: " + ex.getMessage());
            }
        });

        add(rutaField, btnBackup);
    }
}