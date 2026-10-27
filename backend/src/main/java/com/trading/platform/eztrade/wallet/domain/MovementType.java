package com.trading.platform.eztrade.wallet.domain;

/**
 * Movement types supported by the wallet.
 * <p>
 * Used both in the domain and in the ledger (persistence) to:
 * <ul>
 *   <li>Classify operations.</li>
 *   <li>Provide idempotency: the combination (owner, referenceId, movementType) identifies a unique movement.</li>
 * </ul>
 * Typical semantics by type:
 * <ul>
 *   <li>{@link #DEPOSIT}: increases available balance.</li>
 *   <li>{@link #WITHDRAWAL}: decreases available balance.</li>
 *   <li>{@link #TRANSFER_OUT}: decreases available balance for an outgoing transfer.</li>
 *   <li>{@link #TRANSFER_IN}: increases available balance for an incoming transfer.</li>
 *   <li>{@link #RESERVE}: moves funds from available to reserved before executing a BUY order.</li>
 *   <li>{@link #RELEASE}: moves funds from reserved to available after cancellation or lower-amount execution.</li>
 *   <li>{@link #SETTLEMENT_DEBIT}: consumes reserved balance for BUY settlement.</li>
 *   <li>{@link #SETTLEMENT_CREDIT}: increases available balance for SELL settlement credit.</li>
 *   <li>{@link #FEE}: fee charge, currently modeled as a withdrawal from available balance.</li>
 * </ul>
 */
public enum MovementType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_OUT,
    TRANSFER_IN,
    RESERVE,
    RELEASE,
    SETTLEMENT_DEBIT,
    SETTLEMENT_CREDIT,
    FEE
}

