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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei")
    private Sensei senseiOtorgante;
    private LocalDateTime fechaObtencion = LocalDateTime.now();

    public JudokaInsignia () {

    }
    // Constructor útil para el DataInitializer
    public JudokaInsignia(Judoka judoka, Insignia insignia, Sensei sensei) {
        this.judoka = judoka;
        this.insignia = insignia;
        this.senseiOtorgante = sensei;
        this.fechaObtencion = LocalDateTime.now();
    }

    public Long getIdLogro() { return idLogro; }
    public void setIdLogro(Long idLogro) { this.idLogro = idLogro; }

    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }

    public Insignia getInsignia() { return insignia; }
    public void setInsignia(Insignia insignia) { this.insignia = insignia; }

    public Sensei getSenseiOtorgante() { return senseiOtorgante; }
    public void setSenseiOtorgante(Sensei senseiOtorgante) { this.senseiOtorgante = senseiOtorgante; }

    public LocalDateTime getFechaObtencion() { return fechaObtencion; }
    public void setFechaObtencion(LocalDateTime fechaObtencion) { this.fechaObtencion = fechaObtencion; }
}