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
 * Servicio de Traducción ROBUSTO V2.
 * [cite_start]Ahora soporta traducción directa de Enums[cite: 7].
 */
@Service
public class TraduccionService {

    private final TraduccionRepository traduccionRepository;

    public TraduccionService(TraduccionRepository traduccionRepository) {
        this.traduccionRepository = traduccionRepository;
    }

    /**
     * Obtiene el texto traducido para una clave.
     */
    @org.springframework.cache.annotation.Cacheable("traducciones")
    public String getTraduccion(String clave, String idioma) {
        if (clave == null) return "";

        // 1. Buscar en BD
        Optional<Traduccion> traduccion = traduccionRepository.
                findByClaveAndIdioma(clave, idioma);
        if (traduccion.isPresent()) {
            return traduccion.get().getTexto();
        }

        // 2. Fallback Inteligente (Embellecer clave)
        return formatearClaveComoTexto(clave);
    }

    /**
     * Método principal para Strings: Detecta idioma del navegador.
     */
    public String get(String clave) {
        return getTraduccion(clave, getIdiomaActual());
    }

    /**
     * --- NUEVO: Método Especializado para ENUMS ---
     * Permite llamar a traduccionService.get(EstadoJudoka.ACTIVO) directamente.
     */
    public String get(Enum<?> enumValue) {
        if (enumValue == null) return "";

        // Estrategia 1: Si el Enum tiene un método getDescripcion() que devuelve una clave (Patrón aplicado en EstadoJudoka)
        try {
            java.lang.reflect.Method metodo = enumValue.
                    getClass().getMethod("getDescripcion");
            Object resultado = metodo.invoke(enumValue);
            if (resultado instanceof String) {
                String clave = (String) resultado;
                // Si parece una clave (contiene puntos), la traducimos
                if (clave.contains(".")) {
                    return get(clave);
                }
                // Si no, asumimos que es texto legacy y lo devolvemos tal cual (hasta migrar todo)
                return clave;
            }
        } catch (Exception e) {
            // No tiene getDescripcion o falló, seguimos a la estrategia 2
        }

        // Estrategia 2: Generación automática de clave por convención
        // Ej: EstadoJudoka.PENDIENTE -> "enum.estadojudoka.pendiente"
        String claveGenerada = "enum." +
                enumValue.getClass().getSimpleName().toLowerCase() + "." +
                enumValue.name().toLowerCase();

        return get(claveGenerada);
    }

    private String getIdiomaActual() {
        if (UI.getCurrent() != null) {
            Locale locale = UI.getCurrent().getLocale();
            if (locale != null && "en".equals(locale.getLanguage())) {
                return "en";
            }
        }
        return "es";
    }

    private String formatearClaveComoTexto(String clave) {
        if (clave == null) return "";
        String texto = clave.replace("enum.", "")
                .replace("ejercicio.", "")
                .replace("metrica.", "")
                .replace(".nombre", "")
                .replace("_", " ");

        // Eliminar nombre de la clase enum del inicio si existe (para limpieza visual)
        if (texto.contains(".")) {
            texto = texto.substring(texto.lastIndexOf(".") + 1);
        }

        return Arrays.stream(texto.split(" "))
                .filter(p -> !p.isBlank())
                .map(p -> Character.toUpperCase(p.charAt(0))
                        + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}