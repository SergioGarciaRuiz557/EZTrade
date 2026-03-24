package com.trading.platform.eztrade.wallet.domain;

/**
 * Tipos de movimientos soportados por el wallet.
 * <p>
 * Se utilizan tanto en el dominio como en el ledger (persistencia) para:
 * <ul>
 *   <li>Clasificar operaciones.</li>
 *   <li>Proveer idempotencia: la combinación (owner, referenceId, movementType) identifica un movimiento único.</li>
 * </ul>
 * La semántica típica por tipo es:
 * <ul>
 *   <li>{@link #DEPOSIT}: incrementa saldo disponible.</li>
 *   <li>{@link #WITHDRAWAL}: decrementa saldo disponible.</li>
 *   <li>{@link #RESERVE}: mueve fondos de disponible a reservado (previo a ejecutar una orden BUY).</li>
 *   <li>{@link #RELEASE}: mueve fondos de reservado a disponible (cancelación o ejecución por menor importe).</li>
 *   <li>{@link #SETTLEMENT_DEBIT}: consume saldo reservado (liquidación de BUY).</li>
 *   <li>{@link #SETTLEMENT_CREDIT}: incrementa saldo disponible (liquidación de SELL con abono).</li>
 *   <li>{@link #FEE}: cargo de comisión, actualmente modelado como un retiro del disponible.</li>
 * </ul>
 */
public enum MovementType {
    DEPOSIT,
    WITHDRAWAL,
    RESERVE,
    RELEASE,
    SETTLEMENT_DEBIT,
    SETTLEMENT_CREDIT,
    FEE
}

