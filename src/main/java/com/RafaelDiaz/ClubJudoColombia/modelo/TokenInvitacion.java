package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens_invitacion")
public class TokenInvitacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    public TokenInvitacion() {}

    public TokenInvitacion(Judoka judoka, int horasValidez) {
        this.judoka = judoka;
        this.token = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaCreacion.plusHours(horasValidez);
    }

    // --- Getters, Setters y Lógica ---

    public boolean isValido() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public String getToken() { return token; }
    public Judoka getJudoka() { return judoka; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}