package com.RafaelDiaz.ClubJudoColombia.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para gestionar el almacenamiento de archivos en el sistema de archivos.
 * Basado en la solución robusta con UUID y manejo de directorios.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    // Directorio donde se guardarán los archivos (configurable en application.properties)
    // Por defecto usa una carpeta "uploads" en la raíz del proyecto si no se configura nada.
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public FileStorageService() {
    }

    /**
     * Guarda un archivo en el sistema de archivos y devuelve su nombre de archivo único.
     *
     * @param inputStream Flujo de entrada del archivo a guardar
     * @param originalName Nombre original del archivo
     * @return El nombre del archivo guardado (ej: 550e8400-e29b....jpg)
     * @throws IOException si ocurre un error
     */
    public String save(InputStream inputStream, String originalName) throws IOException {
        try {
            // 1. Asegurar que el directorio de subida existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Directorio de subida creado: {}", uploadPath.toAbsolutePath());
            }

            // 2. Generar nombre único (UUID) para evitar colisiones
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 3. Ruta destino completa
            Path filePath = uploadPath.resolve(fileName);

            // 4. Guardar el archivo (sobrescribiendo si por milagro existe el UUID)
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado: {}", fileName);

            // Devolvemos solo el nombre del archivo.
            // La vista o un controlador de recursos se encargará de construir la URL completa.
            return fileName;

        } catch (IOException ex) {
            log.error("Error al guardar archivo: {}", originalName, ex);
            throw ex;
        }
    }
}