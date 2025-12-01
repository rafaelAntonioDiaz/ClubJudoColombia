package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de Traducción ROBUSTO.
 * Si encuentra la traducción en BD, la usa.
 * Si no, "embellece" la clave técnica para mostrar un texto legible al usuario.
 */
@Service
public class TraduccionService {

    private final TraduccionRepository traduccionRepository;

    public TraduccionService(TraduccionRepository traduccionRepository) {
        this.traduccionRepository = traduccionRepository;
    }

    /**
     * Obtiene el texto traducido.
     * Si no existe en BD, genera un texto "bonito" basado en la clave.
     */
    public String getTraduccion(String clave, String idioma) {
        // 1. Intentar buscar en la Base de Datos
        Optional<Traduccion> traduccion = traduccionRepository.findByClaveAndIdioma(clave, idioma);

        if (traduccion.isPresent()) {
            return traduccion.get().getTexto();
        }

        // 2. PLAN B: Fallback Inteligente (Generar texto legible)
        return formatearClaveComoTexto(clave);
    }

    /**
     * Método principal: Detecta idioma del navegador o usa español por defecto.
     */
    public String get(String clave) {
        String idioma = "es"; // Idioma base

        // Intentamos detectar el idioma del navegador si hay una UI activa
        if (UI.getCurrent() != null) {
            Locale locale = UI.getCurrent().getLocale();
            if (locale != null && locale.getLanguage() != null) {
                // Si el idioma es inglés, usamos "en", si no, mantenemos "es"
                // (Puedes expandir esto a más idiomas luego)
                if (locale.getLanguage().equals("en")) {
                    idioma = "en";
                }
            }
        }

        return getTraduccion(clave, idioma);
    }

    /**
     * Convierte claves técnicas feas en texto legible para humanos.
     * Ej: "ejercicio.salto_horizontal_proesp.nombre" -> "Salto Horizontal"
     */
    private String formatearClaveComoTexto(String clave) {
        if (clave == null) return "";

        String texto = clave;

        // 1. Limpieza de prefijos y sufijos técnicos
        texto = texto.replace("ejercicio.", "")
                .replace("metrica.", "")
                .replace(".nombre", "")
                .replace("proesp", "") // Quitamos sufijo técnico común
                .replace("_", " ");    // Guiones a espacios

        // 2. Casos especiales (Acrónimos que deben ir en mayúsculas)
        if (texto.trim().equalsIgnoreCase("sjft")) return "Test SJFT";
        if (texto.contains("imc")) return "IMC";

        // 3. Capitalización (Primera letra de cada palabra en Mayúscula)
        return Arrays.stream(texto.split(" "))
                .filter(palabra -> !palabra.isBlank())
                .map(palabra -> Character.toUpperCase(palabra.charAt(0)) + palabra.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}