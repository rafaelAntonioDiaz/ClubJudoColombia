package com.RafaelDiaz.ClubJudoColombia.vista.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper estático para mostrar notificaciones consistentes en toda la aplicación.
 *
 * <p><b>Tipos de notificaciones:</b>
 * <ul>
 *   <li><b>Success:</b> Operaciones exitosas (3s, verde)</li>
 *   <li><b>Error:</b> Operaciones fallidas (5s, rojo)</li>
 *   <li><b>Info:</b> Información general (3s, azul)</li>
 *   <li><b>Warning:</b> Advertencias (4s, amarillo)</li>
 * </ul>
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
public final class NotificationHelper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHelper.class);

    // Duraciones por defecto (ms)
    private static final int DURATION_SUCCESS = 3000;
    private static final int DURATION_INFO = 3000;
    private static final int DURATION_WARNING = 4000;
    private static final int DURATION_ERROR = 5000;

    private NotificationHelper() {
        // Clase de utilidad, no se instancia
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    /**
     * Muestra una notificación de éxito.
     *
     * @param mensaje Mensaje a mostrar
     */
    public static void success(String mensaje) {
        showNotification(mensaje, NotificationVariant.LUMO_SUCCESS, DURATION_SUCCESS, Position.MIDDLE);
        logger.info("Notificación de éxito: {}", mensaje);
    }

    /**
     * Muestra una notificación de error.
     *
     * @param mensaje Mensaje a mostrar
     */
    public static void error(String mensaje) {
        showNotification(mensaje, NotificationVariant.LUMO_ERROR, DURATION_ERROR, Position.MIDDLE);
        logger.error("Notificación de error: {}", mensaje);
    }

    /**
     * Muestra una notificación de información.
     *
     * @param mensaje Mensaje a mostrar
     */
    public static void info(String mensaje) {
        showNotification(mensaje, NotificationVariant.LUMO_PRIMARY, DURATION_INFO, Position.BOTTOM_START);
        logger.info("Notificación de info: {}", mensaje);
    }

    /**
     * Muestra una notificación de advertencia.
     *
     * @param mensaje Mensaje a mostrar
     */
    public static void warning(String mensaje) {
        showNotification(mensaje, NotificationVariant.LUMO_CONTRAST, DURATION_WARNING, Position.MIDDLE);
        logger.warn("Notificación de warning: {}", mensaje);
    }

    /**
     * Muestra una notificación personalizada.
     *
     * @param mensaje Mensaje a mostrar
     * @param variant Variante de estilo (success, error, etc.)
     * @param duration Duración en milisegundos
     * @param position Posición en pantalla
     */
    private static void showNotification(String mensaje, NotificationVariant variant, int duration, Position position) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            logger.warn("Intento de mostrar notificación vacía");
            return;
        }

        Notification notification = Notification.show(mensaje, duration, position);
        notification.addThemeVariants(variant);
        notification.addClassName("sensei-notification");
    }

    /**
     * Muestra una notificación con duración corta (2s) para acciones rápidas.
     *
     * @param mensaje Mensaje a mostrar
     */
    public static void quick(String mensaje) {
        Notification.show(mensaje, 2000, Position.BOTTOM_CENTER);
        logger.debug("Notificación rápida: {}", mensaje);
    }
}