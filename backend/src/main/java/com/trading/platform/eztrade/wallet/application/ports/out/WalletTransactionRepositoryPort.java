package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting and querying the wallet movement history.
 * <p>
 * Previously named "Ledger"; renamed to "WalletTransaction" to be more
 * intuitive without changing behavior: it still represents auditable/idempotent
 * entries.
 */
public interface WalletTransactionRepositoryPort {

    /** Persists a wallet transaction (movement). */
    WalletTransaction save(WalletTransaction entry);

    /** Finds a transaction by (owner, reference, movement type). */
    Optional<WalletTransaction> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);

    /** Returns the wallet history, newest movements first. */
    List<WalletTransaction> findByOwnerOrderByOccurredAtDesc(String owner);

    /**
     * Indicates whether a transaction already exists for (owner, reference, movement type).
     * <p>
     * Used as a guard clause to keep the use case idempotent.
     */
    boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);
}

