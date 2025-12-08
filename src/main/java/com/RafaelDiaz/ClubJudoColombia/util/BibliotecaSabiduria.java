package com.RafaelDiaz.ClubJudoColombia.util;

import com.vaadin.flow.server.VaadinSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * El Oráculo del Dojo.
 * Maneja la selección de frases con persistencia probabilística (18% cambio / 82% permanencia).
 */
public class BibliotecaSabiduria {

    private static final List<String> CLAVES = new ArrayList<>();
    private static final Random random = new Random();

    static {
        // Cargar claves disponibles (Sincronizado con DataInitializer)
        // Sun Tzu (9 frases)
        for (int i = 1; i <= 9; i++) CLAVES.add("sabiduria.suntzu." + i);
        // Musashi (8 frases)
        for (int i = 1; i <= 8; i++) CLAVES.add("sabiduria.musashi." + i);
        // Kano (8 frases)
        for (int i = 1; i <= 8; i++) CLAVES.add("sabiduria.kano." + i);
    }

    /**
     * Obtiene una frase del día.
     * Si ya hay una en sesión, hay un 82% de probabilidad de mantenerla.
     */
    public static String obtenerClaveDelDia() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) return CLAVES.get(0); // Fallback seguro sin UI

        String claveActual = (String) session.getAttribute("frase_sabiduria_actual");

        // Lógica del Dado: 0 a 99
        int dado = random.nextInt(100);
        boolean cambiar = (claveActual == null) || (dado < 18); // 18% probabilidad de cambio

        if (cambiar) {
            String nuevaClave = claveActual;
            // Intentar buscar una diferente a la actual
            int intentos = 0;
            while ((nuevaClave == null || nuevaClave.equals(claveActual)) && intentos < 5) {
                nuevaClave = CLAVES.get(random.nextInt(CLAVES.size()));
                intentos++;
            }
            session.setAttribute("frase_sabiduria_actual", nuevaClave);
            return nuevaClave;
        } else {
            return claveActual; // Mística: La frase persiste
        }
    }

    /**
     * Devuelve el nombre del autor basado en la clave.
     */
    public static String obtenerAutor(String clave) {
        if (clave.contains("suntzu")) return "Sun Tzu - El Estratega";
        if (clave.contains("musashi")) return "Miyamoto Musashi - El Ronin";
        if (clave.contains("kano")) return "Jigoro Kano - Shihan";
        return "Sabiduría Marcial";
    }
}