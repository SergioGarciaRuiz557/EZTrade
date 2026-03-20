package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.SpringDataWalletTransactionRepository;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletTransactionRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.WalletTransaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia para transacciones del wallet.
 * <p>
 * Implementa {@link WalletTransactionRepositoryPort} usando Spring Data JPA.
 */
@Repository
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepositoryPort {

    private final SpringDataWalletTransactionRepository repository;

    public WalletTransactionRepositoryAdapter(SpringDataWalletTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public WalletTransaction save(WalletTransaction entry) {
        return WalletTransactionMapper.toDomain(repository.save(WalletTransactionMapper.toEntity(entry)));
    }

    @Override
    public Optional<WalletTransaction> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType) {
        return repository.findByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType)
                .map(WalletTransactionMapper::toDomain);
    }

    @Override
    public boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType) {
        return repository.existsByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType);
    }
}

