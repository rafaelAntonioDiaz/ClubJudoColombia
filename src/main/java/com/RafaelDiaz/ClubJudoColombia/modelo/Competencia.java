package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelCompetencia;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "competencias")
public class Competencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String lugar;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelCompetencia nivel;

    // --- REQUISITOS DOCUMENTALES (EL CORAZÓN DE TU SOLICITUD) ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competencia_requisitos", joinColumns = @JoinColumn(name = "id_competencia"))
    @Column(name = "nombre_requisito")
    private Set<String> documentosRequeridos = new HashSet<>();

    public Competencia() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public NivelCompetencia getNivel() { return nivel; }
    public void setNivel(NivelCompetencia nivel) { this.nivel = nivel; }
    public Set<String> getDocumentosRequeridos() { return documentosRequeridos; }
    public void setDocumentosRequeridos(Set<String> documentosRequeridos) { this.documentosRequeridos = documentosRequeridos; }
}