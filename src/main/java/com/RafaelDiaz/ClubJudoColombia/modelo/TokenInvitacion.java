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
    @JoinColumn(name = "id_sensei", nullable = true)
    private Sensei sensei;

    @Column(name = "es_club_propio")
    private Boolean esClubPropio;

    // Getter y setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = true)
    private Judoka judoka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuarioInvitado;

    @Column(name = "rol_esperado", length = 50)
    private String rolEsperado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private GrupoEntrenamiento grupo;

    public TokenInvitacion() {}

    public TokenInvitacion(Judoka judoka, int horasValidez) {
        this.judoka = judoka;
        this.token = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaCreacion.plusHours(horasValidez);
        this.sensei = judoka.getSensei();
    }
    public void generarToken(int horasValidez) {
        this.token = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaCreacion.plusHours(horasValidez);
        this.usado = false;
    }
    // --- Getters, Setters y Lógica ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Sensei getSensei() {
        return sensei;
    }

    public void setSensei(Sensei sensei) {
        this.sensei = sensei;
    }

    public void setJudoka(Judoka judoka) {
        this.judoka = judoka;
    }

    public Usuario getUsuarioInvitado() {
        return usuarioInvitado;
    }

    public void setUsuarioInvitado(Usuario usuarioInvitado) {
        this.usuarioInvitado = usuarioInvitado;
    }

    public String getRolEsperado() {
        return rolEsperado;
    }

    public void setRolEsperado(String rolEsperado) {
        this.rolEsperado = rolEsperado;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public boolean isValido() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public String getToken() { return token; }
    public Judoka getJudoka() { return judoka; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public Boolean getEsClubPropio() {
        return esClubPropio;
    }

    public void setEsClubPropio(Boolean esClubPropio) {
        this.esClubPropio = esClubPropio;
    }

    public GrupoEntrenamiento getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoEntrenamiento grupo) {
        this.grupo = grupo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}