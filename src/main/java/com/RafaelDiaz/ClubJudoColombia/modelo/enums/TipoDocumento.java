package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

public enum TipoDocumento {
    WAIVER("enum.tipo_documento.exoneracion_responsabilidad"),
    CERTIFICADO_MEDICO("enum.tipo_documento.certificado_medico"),
    EPS("enum.tipo_documento.certificado_afiliacion_eps"),
    DOCUMENTO_IDENTIDAD("enum.tipo_documento.documento_identidad"),
    COMPROBANTE_PAGO("enum.tipo_documento.comprobante_pago");


    private final String clave;

    TipoDocumento(String clave) {
        this.clave = clave;
    }

    /**
     * Método estándar para I18n.
     */
    public String getDescripcion() {
        return clave;
    }

    public String getNombre() { // Compatibilidad
        return clave;
    }
}