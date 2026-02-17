package com.RafaelDiaz.ClubJudoColombia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class CloudflareR2Config {

    @Value("${cloudflare.r2.access-key:DUMMY_KEY}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key:DUMMY_SECRET}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint:https://dummy.r2.cloudflarestorage.com}")
    private String endpoint;

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // 1. Creamos la regla estricta de usar Path-Style (como le gusta a Cloudflare)
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .serviceConfiguration(serviceConfiguration) // 2. ¡Le inyectamos la regla al Presigner!
                .build();
    }

    @Bean
    public S3Client s3Client() {
        // AWS SDK fallará si las credenciales están vacías.
        // Si no las has configurado en el IDE, usará los DUMMY_KEY definidos arriba.
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .region(Region.US_EAST_1) // Cloudflare usa esto por compatibilidad
                .build();
    }
}