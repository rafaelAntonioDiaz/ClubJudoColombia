package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.Traduccion;
import com.RafaelDiaz.ClubJudoColombia.repositorio.TraduccionRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
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


    public String get(String clave) {
        // 1. Obtener el idioma actual de la sesión de Vaadin
        Locale locale = null;
        if (UI.getCurrent() != null) {
            locale = UI.getCurrent().getLocale();
        } else if (VaadinSession.getCurrent() != null) {
            locale = VaadinSession.getCurrent().getLocale();
        }

        // 2. Si por alguna razón no hay sesión, por defecto a español
        String idioma = (locale != null) ? locale.getLanguage() : "es";

        // 3. Llamar al método que va a la base de datos
        return getTraduccion(clave, idioma);
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
        // Prioridad: atributo de sesión
        Locale sessionLocale = (Locale) VaadinSession.getCurrent().getAttribute("locale");
        if (sessionLocale != null) {
            return sessionLocale.getLanguage();
        }
        // Fallback: locale de UI
        if (UI.getCurrent() != null) {
            Locale locale = UI.getCurrent().getLocale();
            String lang = locale.getLanguage();
            if (lang.equals("en")) return "en";
            if (lang.equals("pt")) return "pt";
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
    /**
     * --- NUEVO MÉTODO PARA PARÁMETROS ---
     * Permite: traduccionService.get("dashboard.welcome", "Rafael")
     */
    public String get(String clave, Object... params) {
        String texto = get(clave);
        if (params != null && params.length > 0) {
            try {
                return MessageFormat.format(texto, params);
            } catch (Exception e) {
                return texto; // Si falla el formato, devolvemos el texto original
            }
        }
        return texto;
    }
    // Añade este import arriba si no lo tienes:
    // import org.springframework.transaction.annotation.Transactional;

    /**
     * --- NUEVO: MÉTODO DE ESCRITURA ---
     * Guarda una traducción solo si no existe en la base de datos.
     */
    @Transactional
    public void guardarTraduccionSiNoExiste(String idioma, String clave, String texto) {
        if (traduccionRepository.findByClaveAndIdioma(clave, idioma).isEmpty()) {
            Traduccion nuevaTraduccion = new Traduccion();
            nuevaTraduccion.setIdioma(idioma);
            nuevaTraduccion.setClave(clave);
            nuevaTraduccion.setTexto(texto);
            traduccionRepository.save(nuevaTraduccion);
        }
    }

}