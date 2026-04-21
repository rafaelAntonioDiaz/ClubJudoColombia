package com.RafaelDiaz.ClubJudoColombia.controlador;

import com.RafaelDiaz.ClubJudoColombia.servicio.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @GetMapping("/descargar")
    public ResponseEntity<byte[]> descargarBackup() throws IOException {
        File backupFile = backupService.backupDatabase();
        byte[] contenido = Files.readAllBytes(backupFile.toPath());
        
        String extension = backupFile.getName().endsWith(".zip") ? ".zip" : ".sql";
        String contentType = backupFile.getName().endsWith(".zip") ? "application/zip" : "application/sql";
        
        String fileName = "backup_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + extension;

        // Eliminar archivo temporal después de leerlo
        backupFile.delete();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(contenido);
    }
}