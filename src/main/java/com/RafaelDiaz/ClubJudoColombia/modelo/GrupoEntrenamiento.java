package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalTime;
import java.time.DayOfWeek;
/**
 * Entidad que representa un "equipo" o "grupo de entrenamiento".
 * Ej. "Equipo Masculino Sub-13", "Equipo Femenino Mayores".
 */
@Entity
@Table(name = "grupos_entrenamiento")
public class GrupoEntrenamiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sensei", nullable = false)
    private Sensei sensei;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "lugar_practica", length = 150)
    private String lugarPractica;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "radio_permitido_metros")
    private Integer radioPermitidoMetros = 100;
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    /**
     * Guarda los días de la semana en los que entrena este grupo.
     * FetchType.EAGER asegura que los días carguen de inmediato para la vista.
     */
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "grupo_dias", joinColumns = @JoinColumn(name = "id_grupo"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia")
    private Set<DayOfWeek> diasSemana = new HashSet<>();

    /**
     * --- RELACIÓN (Muchos-a-Muchos con Judoka) ---
     * Un grupo tiene muchos Judokas.
     * Un Judoka puede pertenecer a varios grupos
     * (ej. "Sub-13" y "Equipo de Competencia").
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "judoka_grupos", // Tabla de unión
            joinColumns = @JoinColumn(name = "id_grupo"),
            inverseJoinColumns = @JoinColumn(name = "id_judoka")
    )
    private Set<Judoka> judokas = new HashSet<>();


    @ManyToMany(mappedBy = "gruposAsignados", fetch = FetchType.LAZY)
    private Set<Microciclo> microciclosAsignados = new HashSet<>();


    @Column(name = "tarifa_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaMensual;

    @Column(name = "comision_sensei", nullable = false, precision = 10, scale = 2)
    private BigDecimal comisionSensei;

    @Column(name = "incluye_matricula", nullable = false)
    private boolean incluyeMatricula = false;

    @Column(name = "monto_matricula", precision = 10, scale = 2)
    private BigDecimal montoMatricula; // puede ser null si incluyeMatricula es false

    @Column(name = "dias_gracia", nullable = false)
    private int diasGracia = 5;

    // Getters y setters
    // --- Constructores ---
    public GrupoEntrenamiento() {}

    // --- Getters y Setters ---

    public String getLugarPractica() {
        return lugarPractica;
    }

    public void setLugarPractica(String lugarPractica) {
        this.lugarPractica = lugarPractica;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Set<Judoka> getJudokas() { return judokas; }
    public void setJudokas(Set<Judoka> judokas) { this.judokas = judokas; }
   public Sensei getSensei() { return sensei; }
    public void setSensei(Sensei sensei) { this.sensei = sensei; }

    public Set<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(Set<DayOfWeek> diasSemana) {
        this.diasSemana = diasSemana;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getRadioPermitidoMetros() {
        return radioPermitidoMetros;
    }

    public void setRadioPermitidoMetros(Integer radioPermitidoMetros) {
        this.radioPermitidoMetros = radioPermitidoMetros;
    }

    public Set<Microciclo> getMicrociclosAsignados() {
        return microciclosAsignados;
    }

    public void setMicrociclosAsignados(Set<Microciclo> microciclosAsignados) {
        this.microciclosAsignados = microciclosAsignados;
    }

    public BigDecimal getTarifaMensual() {
        return tarifaMensual;
    }

    public void setTarifaMensual(BigDecimal tarifaMensual) {
        this.tarifaMensual = tarifaMensual;
    }

    public BigDecimal getComisionSensei() {
        return comisionSensei;
    }

    public void setComisionSensei(BigDecimal comisionSensei) {
        this.comisionSensei = comisionSensei;
    }

    public boolean isIncluyeMatricula() {
        return incluyeMatricula;
    }

    public void setIncluyeMatricula(boolean incluyeMatricula) {
        this.incluyeMatricula = incluyeMatricula;
    }

    public BigDecimal getMontoMatricula() {
        return montoMatricula;
    }

    public void setMontoMatricula(BigDecimal montoMatricula) {
        this.montoMatricula = montoMatricula;
    }

    public int getDiasGracia() {
        return diasGracia;
    }

    public void setDiasGracia(int diasGracia) {
        this.diasGracia = diasGracia;
    }

    // --- hashCode y equals ---
    @Override
    public int hashCode() {
        return nombre != null ? nombre.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GrupoEntrenamiento that = (GrupoEntrenamiento) obj;
        return nombre != null && nombre.equals(that.nombre);
    }
}