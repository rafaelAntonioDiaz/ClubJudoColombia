package com.RafaelDiaz.ClubJudoColombia.modelo;

import com.RafaelDiaz.ClubJudoColombia.modelo.enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_requisitos")
public class DocumentoRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_judoka", nullable = false)
    @NotNull(message = "{validation.doc.judoka_obligatorio}")
    private Judoka judoka;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "{validation.doc.tipo_obligatorio}")
    private TipoDocumento tipo;

    @Column(nullable = false)
    @NotBlank(message = "{validation.doc.archivo_obligatorio}")
    private String urlArchivo; // Ruta donde se guardó el PDF/Imagen
    /**
     * Opcional: Nombre o ID del evento si es un documento efímero.
     * Ej. "Campeonato Nacional 2026"
     * Si es null, es un documento base (EPS, Waiver Anual).
     */
    @Column(name = "evento_asociado")
    private String eventoAsociado;

    private boolean validadoPorSensei = false; // El Sensei debe darle visto bueno

    private LocalDateTime fechaCarga;

    public DocumentoRequisito() {
        this.fechaCarga = LocalDateTime.now();
    }

    public DocumentoRequisito(Judoka judoka, TipoDocumento tipo, String urlArchivo) {
        this.judoka = judoka;
        this.tipo = tipo;
        this.urlArchivo = urlArchivo;
        this.fechaCarga = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Judoka getJudoka() { return judoka; }
    public void setJudoka(Judoka judoka) { this.judoka = judoka; }
    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }
    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }
    public boolean isValidadoPorSensei() { return validadoPorSensei; }
    public void setValidadoPorSensei(boolean validadoPorSensei) { this.validadoPorSensei = validadoPorSensei; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
    public String getEventoAsociado() { return eventoAsociado; }
    public void setEventoAsociado(String eventoAsociado) { this.eventoAsociado = eventoAsociado; }
}