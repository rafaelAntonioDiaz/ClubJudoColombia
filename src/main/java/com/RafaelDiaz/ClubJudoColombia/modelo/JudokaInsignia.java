package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "judoka_insignias")
public class JudokaInsignia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLogro;

    @ManyToOne
    @JoinColumn(name = "id_judoka")
    private Judoka judoka;

    @ManyToOne
    @JoinColumn(name = "id_insignia")
    private Insignia insignia;

    private LocalDateTime fechaObtencion = LocalDateTime.now();

    public JudokaInsignia () {

    }


    public void setJudoka(Judoka judoka) {
        this.judoka = judoka;
    }

    public void setInsignia(Insignia insignia) {
        this.insignia = insignia;
    }

    public void setFechaObtencion(LocalDateTime fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }
    public void setIdLogro(Long idLogro) {
        this.idLogro = idLogro;
    }
    public Insignia getInsignia() {
        return insignia;
    }
    public LocalDateTime getFechaObtencion() {
        return fechaObtencion;
    }

    public Long getIdLogro() {
        return idLogro;
    }
    public Judoka getJudoka() {
        return judoka;
    }
}