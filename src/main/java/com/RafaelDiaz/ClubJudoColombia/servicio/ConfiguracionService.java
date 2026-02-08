package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository repository;

    public ConfiguracionService(ConfiguracionRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene la configuración única del sistema.
     * Si no existe, la crea con valores por defecto (Nivel CLUB).
     */
    public ConfiguracionSistema obtenerConfiguracion() {
        return repository.findById(1L).orElseGet(() -> {
            ConfiguracionSistema config = new ConfiguracionSistema();
            config.setId(1L);
            config.setNombreOrganizacion("Club de Judo Demo");
            config.setNivel(NivelOrganizacional.CLUB);
            return repository.save(config);
        });
    }

    @Transactional
    public void guardarConfiguracion(ConfiguracionSistema config) {
        config.setId(1L); // Asegurar Singleton
        repository.save(config);
    }

    // Métodos Helper para usar en los Layouts
    public boolean esClub() { return obtenerConfiguracion().getNivel() == NivelOrganizacional.CLUB; }
    public boolean esLiga() { return obtenerConfiguracion().getNivel() == NivelOrganizacional.LIGA; }
    public boolean esFederacion() { return obtenerConfiguracion().getNivel() == NivelOrganizacional.FEDERACION; }
    // Reemplaza los métodos helper al final de ConfiguracionService.java
    public BigDecimal getAsBigDecimal(String clave) {
        ConfiguracionSistema config = obtenerConfiguracion();
        return switch (clave) {
            case "FIN_SAAS_CANON_FIJO" -> config.getFIN_SAAS_CANON_FIJO();
            case "FIN_SENSEI_MASTER_MENSUALIDAD" -> config.getFIN_SENSEI_MASTER_MENSUALIDAD();
            case "FIN_MATRICULA_ANUAL" -> config.getFIN_MATRICULA_ANUAL();
            case "COMISION_SENSEI_MENSUALIDAD" -> config.getCOMISION_SENSEI_MENSUALIDAD();
            default -> BigDecimal.ZERO;
        };
    }

    public NumberFormat obtenerFormatoMoneda() {
        ConfiguracionSistema config = obtenerConfiguracion();
        String codigoMoneda = config.getMoneda() != null ? config.getMoneda() : "COP";

        // 1. Intentamos obtener la moneda del sistema
        Currency currency;
        try {
            currency = Currency.getInstance(codigoMoneda);
        } catch (Exception e) {
            currency = Currency.getInstance("COP"); // Fallback seguro
        }

        // 2. Usamos el Locale del usuario o defecto (Podrías inyectar User Locale aquí)
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formato.setCurrency(currency);

        // 3. Regla de Negocio: Si es COP o CLP (Pesos sin centavos), quitamos decimales
        if ("COP".equals(codigoMoneda) || "CLP".equals(codigoMoneda) || "JPY".equals(codigoMoneda)) {
            formato.setMaximumFractionDigits(0);
        } else {
            formato.setMaximumFractionDigits(2); // USD, EUR usan centavos
        }

        return formato;
    }
    public int getDiaVencimiento() {
        return obtenerConfiguracion().getFIN_DIA_VENCIMIENTO().intValue();
    }
}