package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicaciones")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Eager para cargar el nombre/avatar rápido
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String imagenUrl; // Guardaremos el nombre del archivo (ej: "foto-123.jpg")

    private LocalDateTime fecha;

    private int likes;

    // Constructor vacío obligatorio para JPA
    public Publicacion() {
        this.fecha = LocalDateTime.now();
        this.likes = 0;
    }

    public Publicacion(Usuario autor, String contenido, String imagenUrl) {
        this();
        this.autor = autor;
        this.contenido = contenido;
        this.imagenUrl = imagenUrl;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
}