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
    }

    @Override
    public String subirArchivo(Long judokaId, String nombreOriginal, InputStream inputStream) { // <-- YA NO PEDIMOS EL TAMAÑO
        String uniqueFileName = UUID.randomUUID() + "_" + nombreOriginal;
        String rutaEnNube = "judokas/" + judokaId + "/" + uniqueFileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(rutaEnNube)
                    .contentType("application/pdf")
                    .build();

            // LA MAGIA MODERNA: Leemos el flujo en RAM y AWS obtiene el tamaño automáticamente
            byte[] bytesDelArchivo = inputStream.readAllBytes();

            s3Client.putObject(request, RequestBody.fromBytes(bytesDelArchivo));

            return uniqueFileName; // Devolvemos el nombre final

        } catch (Exception e) {
            throw new RuntimeException("Fallo al subir archivo a la nube", e);
        }
    }    @Override
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
        return publicUrl + "/judokas/" + judokaId + "/" + nombreArchivo;
    }

    public String generarUrlSegura(String objectKey) {
        try {
            // 1. Limpieza y extracción de la llave exacta
            if (objectKey != null && objectKey.startsWith("http")) {
                java.net.URL url = new java.net.URL(objectKey);
                // Decodificamos %20 (espacios) para evitar errores
                objectKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

                if (objectKey.startsWith("/")) {
                    objectKey = objectKey.substring(1);
                }

                // Blindaje extra: Si la ruta incluyó el bucket, lo cortamos
                if (objectKey.startsWith("judo-assets/")) {
                    objectKey = objectKey.substring("judo-assets/".length());
                }
            }

            // 2. MAGIA PURA: Detectamos el tipo real analizando la extensión
            String tipoContenido = "application/pdf"; // Asumimos PDF por defecto
            String nombreMinusculas = objectKey.toLowerCase();

            if (nombreMinusculas.endsWith(".png")) {
                tipoContenido = "image/png";
            } else if (nombreMinusculas.endsWith(".jpg") || nombreMinusculas.endsWith(".jpeg")) {
                tipoContenido = "image/jpeg";
            }

            // 3. Construimos la petición obligando al navegador a obedecer
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("judo-assets")
                    .key(objectKey)
                    .responseContentType(tipoContenido) // <--- ¡Corrige el pantallazo negro!
                    .responseContentDisposition("inline") // <--- ¡Lo muestra en la pestaña sí o sí!
                    .build();

            // 4. Firmamos la URL por 15 minutos
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (Exception e) {
            throw new RuntimeException("Fallo crítico al firmar el documento: " + objectKey, e);
        }
    }}