package com.RafaelDiaz.ClubJudoColombia.servicio;

import java.io.InputStream;

public interface AlmacenamientoCloudService {
    /**
     * Sube el archivo directamente a la nube usando el flujo moderno de Vaadin.
     */
    String subirArchivo(Long judokaId, String nombreArchivo, InputStream inputStream);

    String obtenerUrl(Long judokaId, String nombreArchivo);
    /**
     * Elimina un archivo de la nube dado su URL o Key.
     */
    boolean eliminarArchivo(String urlArchivoEnLaNube);
}