package com.RafaelDiaz.ClubJudoColombia.servicio.impl;

import com.RafaelDiaz.ClubJudoColombia.servicio.AlmacenamientoCloudService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AwsS3AlmacenamientoService implements AlmacenamientoCloudService {

    private final S3Client s3Client;
    // Un hilo secundario para que AWS no congele la pantalla del usuario mientras sube
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // El nombre de tu "caja fuerte" gratuita en AWS
    @Value("${aws.s3.bucket:club-judo-docs-gratis}")
    private String bucketName;

    public AwsS3AlmacenamientoService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public OutputStream crearStreamDeSalida(Long judokaId, String nombreArchivo) {
        // Estructura de carpetas: /judokas/1/waiver.pdf
        String rutaEnNube = "judokas/" + judokaId + "/" + nombreArchivo;

        try {
            // 1. Creamos la Tubería
            PipedInputStream entradaAWS = new PipedInputStream();
            PipedOutputStream salidaVaadin = new PipedOutputStream(entradaAWS);

            // 2. AWS debe "chupar" los datos en un hilo separado
            executor.submit(() -> {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(rutaEnNube)
                        .contentType("application/pdf")
                        .build();

                // Streaming puro: Sube los bytes directo a la nube mientras llegan
                s3Client.putObject(request, RequestBody.fromInputStream(entradaAWS, -1));
            });

            // 3. Le damos la "boca" de la tubería a Vaadin para que escriba ahí
            return salidaVaadin;

        } catch (IOException e) {
            throw new RuntimeException("Error fatal de I/O creando el stream", e);
        }
    }

    @Override
    public String obtenerUrl(Long judokaId, String nombreArchivo) {
        // En producción esta puede ser una URL firmada (con caducidad) para mayor seguridad
        return "https://" + bucketName + ".s3.amazonaws.com/judokas/" + judokaId + "/" + nombreArchivo;
    }
}