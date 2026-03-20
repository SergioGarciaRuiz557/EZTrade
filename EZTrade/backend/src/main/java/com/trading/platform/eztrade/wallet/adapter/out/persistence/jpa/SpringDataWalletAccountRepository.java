package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link WalletAccountJpaEntity}.
 * <p>
 * Provee un método con {@link Lock} en modo {@link LockModeType#PESSIMISTIC_WRITE} para serializar actualizaciones de
 * balance por owner.
 */
public interface SpringDataWalletAccountRepository extends JpaRepository<WalletAccountJpaEntity, Long> {

    Optional<WalletAccountJpaEntity> findByOwner(String owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletAccountJpaEntity> findByOwnerForUpdate(String owner);
}

