package com.trading.platform.eztrade.trading.domain;

/**
 * Tipo de orden dentro del dominio de trading.
 * <p>
 * Define la direccion economica de la operacion:
 * compra ({@link #BUY}) o venta ({@link #SELL}).
 */
public enum OrderSide {

    /** Orden de compra de un activo. */
    BUY,

    /** Orden de venta de un activo. */
    SELL
}
