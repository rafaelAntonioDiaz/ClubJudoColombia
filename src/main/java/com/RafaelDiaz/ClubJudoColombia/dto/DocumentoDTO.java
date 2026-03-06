package com.RafaelDiaz.ClubJudoColombia.dto;

public class DocumentoDTO {
    private Long id;
    private String tipo;       // texto traducido (ej. "Exoneración de responsabilidad")
    private String nombreArchivo;
    private String url;

    public DocumentoDTO() {}

    public DocumentoDTO(Long id, String tipo, String nombreArchivo, String url) {
        this.id = id;
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.url = url;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}