package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.SpringDataWalletAccountRepository;
import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.WalletAccountJpaEntity;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Persistence adapter for {@link com.trading.platform.eztrade.wallet.domain.WalletAccount}.
 * <p>
 * Implements the {@link com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort}
 * port by delegating to a Spring Data JPA repository.
 */
public class WalletAccountRepositoryAdapter implements WalletAccountRepositoryPort {

    private final SpringDataWalletAccountRepository repository;

    public WalletAccountRepositoryAdapter(SpringDataWalletAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<WalletAccount> findByOwner(String owner) {
        return repository.findByOwner(owner).map(WalletAccountMapper::toDomain);
    }

    @Override
    public Optional<WalletAccount> findByOwnerForUpdate(String owner) {
        return repository.findByOwnerForUpdate(owner).map(WalletAccountMapper::toDomain);
    }

    @Override
    public WalletAccount save(WalletAccount walletAccount) {
        // Since owner is unique, treat the save as an "upsert".
        // If an entity already exists, copy its id so JPA updates instead of inserting.
        Optional<WalletAccountJpaEntity> existing = repository.findByOwner(walletAccount.owner());
        WalletAccountJpaEntity toSave = WalletAccountMapper.toEntity(walletAccount);
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return WalletAccountMapper.toDomain(repository.save(toSave));
    }
}

