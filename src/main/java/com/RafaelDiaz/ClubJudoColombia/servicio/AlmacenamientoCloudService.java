package com.RafaelDiaz.ClubJudoColombia.servicio;

import java.io.OutputStream;

public interface AlmacenamientoCloudService {

    /**
     * Crea un canal de flujo de datos directo a la nube.
     * @param judokaId ID del judoka (se usa para crear carpetas, ej: /judokas/1/waiver.pdf)
     * @param nombreArchivo Nombre original del archivo
     * @return OutputStream al que Vaadin le inyectará los bytes en tiempo real.
     */
    OutputStream crearStreamDeSalida(Long judokaId, String nombreArchivo);

    /**
     * Devuelve la URL pública (o firmada) para ver el archivo después.
     */
    String obtenerUrl(Long judokaId, String nombreArchivo);
}