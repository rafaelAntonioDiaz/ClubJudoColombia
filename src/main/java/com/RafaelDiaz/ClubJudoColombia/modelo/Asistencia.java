package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.EstadoAsistencia;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que registra la asistencia (o ausencia) de un
 * Judoka a una SesionProgramada específica.
 */
@Entity
@Table(name = "asistencias", indexes = {
    @Index(name = "idx_asistencia_judoka", columnList = "id_judoka"),
    @Index(name = "idx_asistencia_sesion", columnList = "id_sesion_ejecutada"),
    @Index(name = "idx_asistencia_estado", columnList = "estado")
})
public class Asistencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long id;

    /**
     * El Judoka que asiste.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    private Judoka judoka;

    /**
     * La Sesión a la que se asiste.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion_ejecutada", nullable = false)
    private SesionEjecutada sesion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAsistencia estado;


    /**
     * (Opcional) Notas del sensei, ej. "Llegó tarde", "Justificado".
     */
    @Column(name = "notas")
    private String notas;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    public Asistencia() {}
    public Asistencia(Judoka judoka, EstadoAsistencia estado) {
        this.judoka = judoka;
        this.estado = estado;
    }

    public EstadoAsistencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsistencia estado) {
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public SesionEjecutada getSesion() { return sesion; }
    public void setSesion(SesionEjecutada sesion) { this.sesion = sesion; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Asistencia that = (Asistencia) obj;
        return id != null && id.equals(that.id);
    }
}