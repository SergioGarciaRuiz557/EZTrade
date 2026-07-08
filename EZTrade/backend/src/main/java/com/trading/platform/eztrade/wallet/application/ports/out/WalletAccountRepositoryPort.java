package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.WalletAccount;

import java.util.Optional;

/**
 * Output port for persisting and retrieving {@link WalletAccount}.
 * <p>
 * Defined as an interface to decouple the application from the persistence
 * mechanism (JPA or another implementation).
 */
public interface WalletAccountRepositoryPort {

    /** Returns the owner's account if it exists (without locking). */
    Optional<WalletAccount> findByOwner(String owner);

    /**
     * Returns the owner's account while applying a mutual-exclusion mechanism
     * (lock) if the adapter supports it.
     * <p>
     * Used when balances are about to be modified to avoid race conditions.
     */
    Optional<WalletAccount> findByOwnerForUpdate(String owner);

    /** Saves the account (insert/update) and returns the persisted state. */
    WalletAccount save(WalletAccount walletAccount);
}

