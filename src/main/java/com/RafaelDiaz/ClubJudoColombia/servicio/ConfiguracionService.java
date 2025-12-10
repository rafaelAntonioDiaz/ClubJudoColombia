package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelOrganizacional;
import com.RafaelDiaz.ClubJudoColombia.repositorio.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}