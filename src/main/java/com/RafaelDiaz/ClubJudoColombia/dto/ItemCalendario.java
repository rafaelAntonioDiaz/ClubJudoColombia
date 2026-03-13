package com.RafaelDiaz.ClubJudoColombia.dto;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoItem;
import java.time.LocalDateTime;

public class ItemCalendario {
    private Long id;
    private String titulo;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private TipoItem tipo;
    private String estado;          // programada, completada, pendiente, asistio, falto, en_curso
    private String ubicacionEsperada;
    private Double latitudEsperada;
    private Double longitudEsperada;
    private Double latitudRegistrada;
    private Double longitudRegistrada;
    private Long entidadId;          // ID de sesión o ejercicio
    private String grupoNombre;
    private String judokaNombre;     // para tareas individuales
    private String color;             // para UI
    private boolean requiereGps;
    private boolean completado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public TipoItem getTipo() {
        return tipo;
    }

    public void setTipo(TipoItem tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacionEsperada() {
        return ubicacionEsperada;
    }

    public void setUbicacionEsperada(String ubicacionEsperada) {
        this.ubicacionEsperada = ubicacionEsperada;
    }

    public Double getLatitudEsperada() {
        return latitudEsperada;
    }

    public void setLatitudEsperada(Double latitudEsperada) {
        this.latitudEsperada = latitudEsperada;
    }

    public Double getLongitudEsperada() {
        return longitudEsperada;
    }

    public void setLongitudEsperada(Double longitudEsperada) {
        this.longitudEsperada = longitudEsperada;
    }

    public Double getLongitudRegistrada() {
        return longitudRegistrada;
    }

    public void setLongitudRegistrada(Double longitudRegistrada) {
        this.longitudRegistrada = longitudRegistrada;
    }

    public Double getLatitudRegistrada() {
        return latitudRegistrada;
    }

    public void setLatitudRegistrada(Double latitudRegistrada) {
        this.latitudRegistrada = latitudRegistrada;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }

    public void setGrupoNombre(String grupoNombre) {
        this.grupoNombre = grupoNombre;
    }

    public String getJudokaNombre() {
        return judokaNombre;
    }

    public void setJudokaNombre(String judokaNombre) {
        this.judokaNombre = judokaNombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isRequiereGps() {
        return false;
    }

    public void setRequiereGps(boolean requiereGps) {
        this.requiereGps = requiereGps;
    }

    public void setRequiereSupervision(boolean requiereSupervision) {
    }
}