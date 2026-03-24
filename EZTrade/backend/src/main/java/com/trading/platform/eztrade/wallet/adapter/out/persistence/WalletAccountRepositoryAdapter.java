package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.SpringDataWalletAccountRepository;
import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.WalletAccountJpaEntity;
import com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Adaptador de persistencia para {@link com.trading.platform.eztrade.wallet.domain.WalletAccount}.
 * <p>
 * Implementa el puerto {@link com.trading.platform.eztrade.wallet.application.ports.out.WalletAccountRepositoryPort}
 * delegando en un repositorio Spring Data JPA.
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
        // Al ser owner único, tratamos el guardado como un "upsert".
        // Si ya existe entidad, copiamos su id para que JPA haga update en lugar de insert.
        Optional<WalletAccountJpaEntity> existing = repository.findByOwner(walletAccount.owner());
        WalletAccountJpaEntity toSave = WalletAccountMapper.toEntity(walletAccount);
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return WalletAccountMapper.toDomain(repository.save(toSave));
    }
}

