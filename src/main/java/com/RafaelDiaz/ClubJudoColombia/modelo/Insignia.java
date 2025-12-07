package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.CategoriaInsignia;
import jakarta.persistence.*;

@Entity
@Table(name = "insignias")
public class Insignia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInsignia;

    private String clave;
    private String nombre;
    private String descripcion;
    private String iconoVaadin;

    @Enumerated(EnumType.STRING)
    private CategoriaInsignia categoria;

    private Integer nivelRequerido;

    // Getters y Setters (imprescindibles)
    public String getClave() {
        return clave;
    }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getIconoVaadin() { return iconoVaadin; }
    public CategoriaInsignia getCategoria() { return categoria; }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdInsignia() {
        return idInsignia;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setCategoria(CategoriaInsignia categoria) {
        this.categoria = categoria;
    }
    public void setClave(String clave) {
        this.clave = clave;
    }
    public void setIconoVaadin(String iconoVaadin) {
        this.iconoVaadin = iconoVaadin;
    }
    public Integer getNivelRequerido() {
        return nivelRequerido;
    }

    public void setNivelRequerido(Integer nivelRequerido) {
        this.nivelRequerido = nivelRequerido;
    }
}