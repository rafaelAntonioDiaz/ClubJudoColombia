package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.NivelCompetencia;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ResultadoCompetencia;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "palmares")
public class ParticipacionCompetencia {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    private String nombreCampeonato;
    private String sede;
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private NivelCompetencia nivel;

    @Enumerated(EnumType.STRING)
    private ResultadoCompetencia resultado;

    // Multimedia
    private String urlFoto;  // Ruta relativa o URL
    private String urlVideo; // Enlace a YouTube

    public ParticipacionCompetencia() {}

    // Constructor conveniente
    public ParticipacionCompetencia(Judoka judoka, String nombre, String sede, LocalDate fecha,
                                    NivelCompetencia nivel, ResultadoCompetencia res, String video) {
        this.judoka = judoka;
        this.nombreCampeonato = nombre;
        this.sede = sede;
        this.fecha = fecha;
        this.nivel = nivel;
        this.resultado = res;
        this.urlVideo = video;
    }

    // Lógica de Puntos
    public int getPuntosCalculados() {
        if (nivel == null || resultado == null) return 0;
        return (int) (nivel.getPuntosBase() * resultado.getMultiplicador());
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public String getNombreCampeonato() { return nombreCampeonato; }
    public void setNombreCampeonato(String nombreCampeonato) { this.nombreCampeonato = nombreCampeonato; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public NivelCompetencia getNivel() { return nivel; }
    public void setNivel(NivelCompetencia nivel) { this.nivel = nivel; }
    public ResultadoCompetencia getResultado() { return resultado; }
    public void setResultado(ResultadoCompetencia resultado) { this.resultado = resultado; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
}