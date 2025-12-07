package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat; // <--- IMPORTANTE: No olvides este import
import java.util.List;
import java.util.Locale;

@Component
public class CustomI18NProvider implements I18NProvider {

    private final TraduccionService traduccionService;

    public CustomI18NProvider(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.of("es"), Locale.of("en"));
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        // 1. Obtener el texto base de la BD
        String texto = traduccionService.getTraduccion(key, locale.getLanguage());

        // 2. CORRECCIÓN: Aplicar formato si hay parámetros
        if (params != null && params.length > 0) {
            try {
                return MessageFormat.format(texto, params);
            } catch (Exception e) {
                return texto; // Fallback seguro
            }
        }

        return texto;
    }
}