package com.RafaelDiaz.ClubJudoColombia.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Realiza un backup de la base de datos H2 en la ruta especificada.
     * @param fullPath Ruta completa donde se guardará el archivo .zip (ej: /media/externo/backup_20250316.zip)
     */
    public void backupDatabase(String fullPath) {
        // La ruta debe ser absoluta y el usuario de la aplicación debe tener permisos de escritura
        String sql = String.format("BACKUP TO '%s'", fullPath);
        jdbcTemplate.execute(sql);
    }
}