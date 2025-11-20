package com.RafaelDiaz.ClubJudoColombia.vista.component;

import com.vaadin.flow.component.page.Page;
import elemental.json.JsonObject; // ✅ IMPORT EXPLÍCITO
import elemental.json.JsonValue;  // ✅ IMPORT EXPLÍCITO
import java.util.function.Consumer;

/**
 * Helper para capturar coordenadas GPS del navegador de forma reutilizable.
 * Maneja timeouts y permisos denegados.
 *
 * @author RafaelDiaz
 * @version 1.4 (Corregida definitiva para Vaadin 24.8.4)
 * @since 2025-11-20
 */
public class GPSHelper {

    private GPSHelper() {}

    /**
     * Captura ubicación GPS del navegador.
     * @param page Instancia de Page de Vaadin
     * @param onSuccess Callback con (lat, lon)
     * @param onError Callback de error
     */
    public static void capturarUbicacion(Page page, Consumer<Double[]> onSuccess, Runnable onError) {
        page.executeJs(
                "return new Promise((resolve, reject) => {" +
                        "  navigator.geolocation.getCurrentPosition(" +
                        "    pos => resolve({lat: pos.coords.latitude, lon: pos.coords.longitude})," +
                        "    err => reject(err)," +
                        "    {timeout: 8000, enableHighAccuracy: true}" +
                        "  );" +
                        "});"
        ).then(jsonValue -> {
            if (jsonValue == null) {
                onError.run();
            } else {
                try {
                    // ✅ CORREGIDO: Conversión explícita y uso de getDouble()
                    JsonObject jsonObject = (JsonObject) jsonValue;

                    // ✅ getNumber() devuelve double directamente, no JsonNumber
                    double lat = jsonObject.getNumber("lat");
                    double lon = jsonObject.getNumber("lon");

                    onSuccess.accept(new Double[]{lat, lon});
                } catch (Exception e) {
                    onError.run();
                }
            }
        });
    }
}