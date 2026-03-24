package com.trading.platform.eztrade.wallet.domain;

/**
 * Origen o categoría de la referencia asociada a un movimiento del wallet.
 * <p>
 * En el ledger cada entrada guarda un {@code referenceType} + {@code referenceId} para:
 * <ul>
 *   <li>facilitar auditoría/trazabilidad (de dónde viene el movimiento),</li>
 *   <li>y ayudar a mantener idempotencia en operaciones que puedan reintentarse.</li>
 * </ul>
 */
public enum ReferenceType {
    /** Movimiento asociado a una orden del módulo de trading (normalmente {@code referenceId = orderId}). */
    ORDER,
    /** Movimiento originado por un ajuste manual/administrativo (depósito/retiro/comisión). */
    MANUAL
}

