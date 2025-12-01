package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoSesion;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una sesión de entrenamiento programada.
 * Ej. "Entrenamiento Sub-13 - Lunes 6:00 AM".
 *
 * <p><b>Características:</b>
 * <ul>
 *   <li>Lazy loading seguro con @EntityGraph</li>
 *   <li>Optimistic locking con @Version</li>
 *   <li>Relación bidireccional con Asistencia</li>
 * </ul>
 *
 * @author RafaelDiaz
 * @version 1.1 (Armonizada)
 * @since 2025-11-20
 */
@Entity
@Table(name = "sesiones_programadas")
@NamedEntityGraph(
        name = "SesionProgramada.completo",
        attributeNodes = {
                @NamedAttributeNode("grupo"),
                @NamedAttributeNode("sensei"),
                @NamedAttributeNode("asistencias")
        }
)
public class SesionProgramada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "fecha_hora_inicio", nullable = false) // Corrección para V6
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)   // Corrección para V6
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sesion", nullable = false)      // Corrección para V5/V6
    private TipoSesion tipoSesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private GrupoEntrenamiento grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Asistencia> asistencias = new HashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // --- Constructores ---
    public SesionProgramada() {}

    public SesionProgramada(String nombre, TipoSesion tipoSesion, LocalDateTime inicio, LocalDateTime fin, GrupoEntrenamiento grupo, Sensei sensei) {
        this.nombre = nombre;
        this.tipoSesion = tipoSesion;
        this.fechaHoraInicio = inicio;
        this.fechaHoraFin = fin;
        this.grupo = grupo;
        this.sensei = sensei;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoSesion getTipoSesion() { return tipoSesion; }
    public void setTipoSesion(TipoSesion tipo) { this.tipoSesion = tipo; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public GrupoEntrenamiento getGrupo() { return grupo; }
    public void setGrupo(GrupoEntrenamiento grupo) { this.grupo = grupo; }
    public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }
    public Set<Asistencia> getAsistencias() { return asistencias; }
    public void setAsistencias(Set<Asistencia> asistencias) { this.asistencias = asistencias; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // --- Helpers de Negocio ---
    @Transient
    public boolean esFutura() {
        return this.fechaHoraInicio.isAfter(LocalDateTime.now());
    }

    @Transient
    public boolean estaEnCurso() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(this.fechaHoraInicio) && !ahora.isAfter(this.fechaHoraFin);
    }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SesionProgramada that = (SesionProgramada) obj;
        return id != null && id.equals(that.id);
    }
}