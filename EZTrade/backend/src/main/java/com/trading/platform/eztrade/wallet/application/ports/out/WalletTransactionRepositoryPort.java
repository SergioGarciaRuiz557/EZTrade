package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;

import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar el histórico de movimientos del wallet.
 * <p>
 * Anteriormente se llamaba "Ledger"; se renombra a "WalletTransaction" para ser más intuitivo sin cambiar el
 * comportamiento: sigue representando entradas auditable/idempotentes.
 */
public interface WalletTransactionRepositoryPort {

    /** Persiste una transacción (movimiento) del wallet. */
    WalletTransaction save(WalletTransaction entry);

    /** Busca una transacción por (owner, referencia, tipo de movimiento). */
    Optional<WalletTransaction> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);

    /**
     * Indica si ya existe una transacción por (owner, referencia, tipo de movimiento).
     * <p>
     * Se usa como guard clause para que el caso de uso sea idempotente.
     */
    boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);
}

