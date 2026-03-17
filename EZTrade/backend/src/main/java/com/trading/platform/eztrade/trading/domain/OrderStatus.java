package com.trading.platform.eztrade.trading.domain;

/**
 * Estado del ciclo de vida de una orden.
 * <p>
 * El agregado {@link TradeOrder} transita entre estos estados segun
 * las reglas de negocio del modulo.
 */
public enum OrderStatus {

    /** Orden creada y pendiente de ejecucion/cancelacion. */
    PENDING,

    /** Orden ejecutada correctamente. */
    EXECUTED,

    /** Orden cancelada por su propietario. */
    CANCELLED
}
