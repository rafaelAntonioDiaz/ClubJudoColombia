package com.RafaelDiaz.ClubJudoColombia.dto;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.BloqueAgudelo;
import java.util.List;

public class BloqueConPruebasDTO {
    private BloqueAgudelo bloque;
    private String nombreBloque; // Traducido
    private List<PruebaResumenDTO> pruebas;
    private Long pruebaSeleccionadaId; // ID de la prueba por defecto (la más reciente o primera con datos)

    public BloqueConPruebasDTO() {}

    public BloqueConPruebasDTO(BloqueAgudelo bloque, String nombreBloque,
                               List<PruebaResumenDTO> pruebas, Long pruebaSeleccionadaId) {
        this.bloque = bloque;
        this.nombreBloque = nombreBloque;
        this.pruebas = pruebas;
        this.pruebaSeleccionadaId = pruebaSeleccionadaId;
    }

    // Getters y Setters
    public BloqueAgudelo getBloque() { return bloque; }
    public void setBloque(BloqueAgudelo bloque) { this.bloque = bloque; }
    public String getNombreBloque() { return nombreBloque; }
    public void setNombreBloque(String nombreBloque) { this.nombreBloque = nombreBloque; }
    public List<PruebaResumenDTO> getPruebas() { return pruebas; }
    public void setPruebas(List<PruebaResumenDTO> pruebas) { this.pruebas = pruebas; }
    public Long getPruebaSeleccionadaId() { return pruebaSeleccionadaId; }
    public void setPruebaSeleccionadaId(Long pruebaSeleccionadaId) { this.pruebaSeleccionadaId = pruebaSeleccionadaId; }
}