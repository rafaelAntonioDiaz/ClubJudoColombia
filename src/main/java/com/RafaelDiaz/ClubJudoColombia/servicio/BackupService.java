package com.RafaelDiaz.ClubJudoColombia.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public File backupDatabase() throws IOException {
        // Crear archivo temporal
        Path tempFile = Files.createTempFile("backup_", ".zip");
        String sql = String.format("BACKUP TO '%s'", tempFile.toString().replace("\\", "\\\\"));
        jdbcTemplate.execute(sql);
        return tempFile.toFile();
    }
}