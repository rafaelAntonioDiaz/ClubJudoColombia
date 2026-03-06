package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.servicio.inicializacion.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final SeguridadInicializer seguridadInicializer;
    private final CatalogoInicializer catalogoInicializer;
    private final TraduccionInicializer traduccionInicializer;
    private final NormaInicializer normaInicializer;
    private final InsigniaInicializer insigniaInicializer;
    private final DatosMuestraInicializer datosMuestraInicializer;

    public DataInitializer(SeguridadInicializer seguridadInicializer,
                           CatalogoInicializer catalogoInicializer,
                           NormaInicializer normaInicializer, TraduccionInicializer traduccionInicializer,
                           InsigniaInicializer insigniaInicializer,
                           DatosMuestraInicializer datosMuestraInicializer) {
        this.seguridadInicializer = seguridadInicializer;
        this.catalogoInicializer = catalogoInicializer;
        this.traduccionInicializer = traduccionInicializer;
        this.normaInicializer = normaInicializer;
        this.insigniaInicializer = insigniaInicializer;
        this.datosMuestraInicializer = datosMuestraInicializer;

    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> INICIANDO INICIALIZACIÓN DE DATOS...");

        // 1. Seguridad (roles y master)
        var master = seguridadInicializer.inicializar();

        // 2. Catálogos (pruebas, métricas)
        catalogoInicializer.inicializar();

        traduccionInicializer.inicializar();
        // 3. Normas de evaluación
        normaInicializer.inicializar();

        // 4. Insignias
        insigniaInicializer.inicializar();

        // 5. Datos de muestra (senseis, judokas, resultados)
        datosMuestraInicializer.inicializar(master);

        System.out.println(">>> INICIALIZACIÓN COMPLETADA.");
    }
}