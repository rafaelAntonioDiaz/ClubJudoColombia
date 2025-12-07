package com.RafaelDiaz.ClubJudoColombia.config;

import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

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
        // Por ahora, devolvemos los idiomas soportados estáticamente.
        // Más adelante, esto podría venir de la base de datos.
        return List.of(new Locale("es"), new Locale("en"));
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        return traduccionService.getTraduccion(key, locale.getLanguage());
    }
}

