package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {

    Optional<LedgerEntryJpaEntity> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);

    boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);
}

