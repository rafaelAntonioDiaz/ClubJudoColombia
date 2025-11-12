package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * Servicio que maneja la obtención de textos (i18n)
 * directamente desde la base de datos (tabla 'traducciones').
 * Esto reemplaza la necesidad de archivos .properties estáticos.
 */
@Service
public class TraduccionService {

    private final TraduccionRepository traduccionRepository;

    public TraduccionService(TraduccionRepository traduccionRepository) {
        this.traduccionRepository = traduccionRepository;
    }

    /**
     * Obtiene el texto traducido para una clave y un idioma específicos.
     * @param clave La clave i18n (ej. "ejercicio.sjft.nombre")
     * @param idioma El código de idioma (ej. "es", "en")
     * @return El texto traducido, o la clave si no se encuentra.
     */
    public String getTraduccion(String clave, String idioma) {
        // Buscamos en la BD
        Optional<Traduccion> traduccion = traduccionRepository.findByClaveAndIdioma(clave, idioma);

        // Si la encontramos, devolvemos el texto.
        // Si no, devolvemos la clave rodeada de [!] para
        // que sepamos que falta una traducción.
        return traduccion.map(Traduccion::getTexto)
                .orElse("[!" + clave + "!]");
    }

    /**
     * --- MÉTODO PRINCIPAL ---
     * Obtiene una traducción usando el idioma actual del usuario.
     * (Detecta automáticamente si el navegador está en 'es', 'en', etc.)
     *
     * @param clave La clave i18n (ej. "ejercicio.sjft.nombre")
     * @return El texto traducido.
     */
    /**
     * --- MÉTODO PRINCIPAL (VERSIÓN CORREGIDA CON "PLAN B") ---
     * Obtiene una traducción usando el idioma actual del usuario.
     *
     * (Cambiamos UI.getCurrentOptional() por Optional.ofNullable(UI.getCurrent())
     * para evitar el error de compilación del IDE)
     */
    public String get(String clave) {

        // 1. Obtener el idioma actual del navegador del usuario
        String idioma = Optional.ofNullable(UI.getCurrent()) // <-- ESTE ES EL CAMBIO
                .map(UI::getLocale)
                .map(Locale::getLanguage)
                .orElse("es"); // Default a 'es'

        // 2. Buscar la traducción
        if (!idioma.equals("es")) {
            idioma = "es"; // Forzar 'es' si no es español
        }

        return getTraduccion(clave, idioma);
    }
}