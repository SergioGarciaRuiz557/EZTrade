package com.trading.platform.eztrade.wallet.domain;

/**
 * Source or category of the reference associated with a wallet movement.
 * <p>
 * In the ledger, each entry stores a {@code referenceType} + {@code referenceId} to:
 * <ul>
 *   <li>support auditing/traceability (where the movement comes from),</li>
 *   <li>and help preserve idempotency for operations that may be retried.</li>
 * </ul>
 */
public enum ReferenceType {
    /** Movement associated with a trading module order (usually {@code referenceId = orderId}). */
    ORDER,
    /** Movement originated by a manual/administrative adjustment (deposit/withdrawal/fee). */
    MANUAL
}

