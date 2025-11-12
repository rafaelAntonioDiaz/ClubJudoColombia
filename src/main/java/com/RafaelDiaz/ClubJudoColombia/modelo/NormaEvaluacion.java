package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.ClasificacionRendimiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.enums.Sexo;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad que almacena una norma de evaluación (benchmark).
 * (Actualizada para apuntar a 'PruebaEstandar' en lugar de 'Ejercicio')
 */
@Entity
@Table(name = "normas_evaluacion")
public class NormaEvaluacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_norma")
    private Long id;

    @Column(name = "fuente", nullable = false, length = 100)
    private String fuente;

    /**
     * --- CAMPO REFACTORIZADO ---
     * Ahora se vincula a la 'PruebaEstandar' (el nuevo nombre de 'Ejercicio').
     * El @JoinColumn(name = "id_ejercicio") sigue siendo correcto porque
     * la tabla 'pruebas_estandar' mantiene ese nombre de columna.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private PruebaEstandar pruebaEstandar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metrica", nullable = false)
    private Metrica metrica;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Column(name = "edad_min", nullable = false)
    private Integer edadMin;

    @Column(name = "edad_max", nullable = false)
    private Integer edadMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false)
    private ClasificacionRendimiento clasificacion;

    @Column(name = "valor_min")
    private Double valorMin;

    @Column(name = "valor_max")
    private Double valorMax;

    // --- Constructores ---
    public NormaEvaluacion() {}

    // --- Getters y Setters (Actualizados) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }

    // --- MÉTODOS ACTUALIZADOS ---
    public PruebaEstandar getPruebaEstandar() { return pruebaEstandar; }
    public void setPruebaEstandar(PruebaEstandar pruebaEstandar) { this.pruebaEstandar = pruebaEstandar; }

    public Metrica getMetrica() { return metrica; }
    public void setMetrica(Metrica metrica) { this.metrica = metrica; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public Integer getEdadMin() { return edadMin; }
    public void setEdadMin(Integer edadMin) { this.edadMin = edadMin; }
    public Integer getEdadMax() { return edadMax; }
    public void setEdadMax(Integer edadMax) { this.edadMax = edadMax; }
    public ClasificacionRendimiento getClasificacion() { return clasificacion; }
    public void setClasificacion(ClasificacionRendimiento clasificacion) { this.clasificacion = clasificacion; }
    public Double getValorMin() { return valorMin; }
    public void setValorMin(Double valorMin) { this.valorMin = valorMin; }
    public Double getValorMax() { return valorMax; }
    public void setValorMax(Double valorMax) { this.valorMax = valorMax; }

    // --- hashCode y equals (Sin cambios) ---
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NormaEvaluacion that = (NormaEvaluacion) obj;
        return id != null && id.equals(that.id);
    }
}