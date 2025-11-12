package com.RafaelDiaz.ClubJudoColombia.modelo.enums;

/**
 * Define el estado de una transacción de pago.
 */
public enum EstadoPago {
    PENDIENTE, // El pago fue creado pero no completado
    PAGADO,    // El pago fue exitoso
    FALLIDO    // El pago fue rechazado
}