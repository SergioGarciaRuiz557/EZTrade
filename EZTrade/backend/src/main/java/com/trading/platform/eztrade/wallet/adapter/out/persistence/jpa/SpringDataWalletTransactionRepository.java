package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link WalletTransactionJpaEntity}.
 */
public interface SpringDataWalletTransactionRepository extends JpaRepository<WalletTransactionJpaEntity, Long> {

    Optional<WalletTransactionJpaEntity> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);

    boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType);
}

