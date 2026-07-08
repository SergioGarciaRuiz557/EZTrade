package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

/**
 * Input port for manual wallet fund adjustments.
 * <p>
 * Used for administrative operations or explicit user actions (deposit/withdrawal),
 * as well as fee charges. Each operation must be identified with a
 * {@code referenceId} to prevent duplicates on retries.
 */
public interface AdjustWalletFundsUseCase {

    /** Deposits available balance. */
    void deposit(AdjustCommand command);

    /** Withdraws available balance. */
    void withdraw(AdjustCommand command);

    /** Charges a fee (currently modeled as a withdrawal from available balance). */
    void chargeFee(AdjustCommand command);

    /**
     * Adjustment command.
     *
     * @param owner wallet owner
     * @param amount amount (must be &gt; 0)
     * @param referenceId idempotent operation identifier (for example, external id or uuid)
     * @param description optional description for auditing
     */
    record AdjustCommand(String owner, BigDecimal amount, String referenceId, String description) {
    }
}

