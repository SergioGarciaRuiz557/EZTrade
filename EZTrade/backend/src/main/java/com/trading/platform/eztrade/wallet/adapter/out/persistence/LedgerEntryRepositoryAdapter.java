package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.SpringDataLedgerEntryRepository;
import com.trading.platform.eztrade.wallet.application.ports.out.LedgerEntryRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.LedgerEntry;
import com.trading.platform.eztrade.wallet.domain.MovementType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepositoryPort {

    private final SpringDataLedgerEntryRepository repository;

    public LedgerEntryRepositoryAdapter(SpringDataLedgerEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        return LedgerEntryMapper.toDomain(repository.save(LedgerEntryMapper.toEntity(entry)));
    }

    @Override
    public Optional<LedgerEntry> findByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType) {
        return repository.findByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType)
                .map(LedgerEntryMapper::toDomain);
    }

    @Override
    public boolean existsByOwnerAndReferenceIdAndMovementType(String owner, String referenceId, MovementType movementType) {
        return repository.existsByOwnerAndReferenceIdAndMovementType(owner, referenceId, movementType);
    }
}

