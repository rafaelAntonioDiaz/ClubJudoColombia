package com.RafaelDiaz.ClubJudoColombia.servicio.impl;

import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
public class CloudflareR2AlmacenamientoService implements AlmacenamientoCloudService {

    @Autowired
    private S3Presigner s3Presigner;

    private static final Logger log = LoggerFactory.getLogger(CloudflareR2AlmacenamientoService.class);

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public CloudflareR2AlmacenamientoService(S3Client s3Client) {

        this.s3Client = s3Client;
        System.out.println(">>> CloudflareR2AlmacenamientoService: publicUrl = " + publicUrl);
    }

    @Override
    public String subirArchivo(Long judokaId, String nombreOriginal, InputStream inputStream) {
        String uniqueFileName = UUID.randomUUID() + "_" + nombreOriginal;
        String rutaEnNube = "judokas/" + judokaId + "/" + uniqueFileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(rutaEnNube)
                    .contentType("image/jpeg") // o detecta el tipo real
                    .build();

            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            // ✅ Devuelve solo el nombre (¡esto es clave!)
            return uniqueFileName;
        } catch (Exception e) {
            throw new RuntimeException("Fallo al subir archivo", e);
        }
    }

    @Override
    public boolean eliminarArchivo(String urlArchivoEnLaNube) {
        if (urlArchivoEnLaNube == null || urlArchivoEnLaNube.isEmpty()) return false;

        // 1. Extraemos la "Llave" (Key) de S3 quitando el dominio público de la URL
        String s3Key = urlArchivoEnLaNube.replace(publicUrl + "/", "");

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // 2. Ejecutamos el borrado en Cloudflare
            s3Client.deleteObject(deleteRequest);
            log.info("Archivo eliminado de la nube con éxito: {}", s3Key);
            return true;

        } catch (Exception e) {
            log.error("Error al intentar borrar el archivo de la nube: {}", s3Key, e);
            return false;
        }
    }

    @Override
    public String obtenerUrl(Long judokaId, String nombreArchivo) {
        String key = "judokas/" + judokaId + "/" + nombreArchivo;
        return generarUrlSegura(key);
    }

    public String generarUrlSegura(String objectKey) {
        try {
            // Validaciones críticas
            if (bucketName == null) {
                throw new IllegalStateException("bucketName no está inyectado. Revisa la propiedad cloudflare.r2.bucket-name");
            }
            if (objectKey == null || objectKey.isBlank()) {
                throw new IllegalArgumentException("objectKey no puede ser nulo o vacío");
            }
            if (s3Presigner == null) {
                throw new IllegalStateException("s3Presigner no está inyectado. Revisa la configuración de AWS SDK");
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    // No incluyas responseContentType ni responseContentDisposition a menos que sean estrictamente necesarios
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7)) // 7 días de validez
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Fallo al firmar el documento: " + objectKey, e);
        }
    }
}