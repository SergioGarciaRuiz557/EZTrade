package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.LedgerEntry;
import com.trading.platform.eztrade.wallet.domain.MovementType;

import java.util.Optional;

public interface LedgerEntryRepositoryPort {

    LedgerEntry save(LedgerEntry entry);

    Optional<LedgerEntry> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);

    boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);
}

