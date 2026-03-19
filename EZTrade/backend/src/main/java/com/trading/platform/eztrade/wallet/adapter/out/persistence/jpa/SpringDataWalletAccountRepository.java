package com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface SpringDataWalletAccountRepository extends JpaRepository<WalletAccountJpaEntity, Long> {

    Optional<WalletAccountJpaEntity> findByOwner(String owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletAccountJpaEntity> findByOwnerForUpdate(String owner);
}

