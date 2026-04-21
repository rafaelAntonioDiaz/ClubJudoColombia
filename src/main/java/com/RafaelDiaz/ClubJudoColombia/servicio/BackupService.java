package com.RafaelDiaz.ClubJudoColombia.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public File backupDatabase() throws IOException {
        if (dbUrl.contains("h2")) {
            return backupH2();
        } else if (dbUrl.contains("postgresql")) {
            return backupPostgres();
        } else {
            throw new IOException("Base de datos no soportada para backup automático");
        }
    }

    private File backupH2() throws IOException {
        Path tempFile = Files.createTempFile("backup_h2_", ".zip");
        String sql = String.format("BACKUP TO '%s'", tempFile.toString().replace("\\", "\\\\"));
        jdbcTemplate.execute(sql);
        return tempFile.toFile();
    }

    private File backupPostgres() throws IOException {
        Path tempFile = Files.createTempFile("backup_pg_", ".sql");
        
        // Intentar usar pg_dump. Requiere que postgresql-client esté instalado en el sistema.
        // Extraemos host, puerto y nombre de la DB del URL de conexión
        // jdbc:postgresql://localhost:5432/judo_db
        String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
        String[] parts = cleanUrl.split("/");
        String hostPort = parts[0];
        String dbName = parts[1].split("\\?")[0];
        
        String host = hostPort.split(":")[0];
        String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump",
            "-h", host,
            "-p", port,
            "-U", dbUsername,
            "-F", "p", // Plain text SQL
            "-f", tempFile.toString(),
            dbName
        );

        // Pasar contraseña vía variable de entorno (forma estándar para pg_dump)
        Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", dbPassword);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error ejecutando pg_dump. Asegúrese de que postgresql-client esté instalado. Código de salida: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("El proceso de backup fue interrumpido", e);
        }

        return tempFile.toFile();
    }
}