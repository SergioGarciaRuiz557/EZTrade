package com.trading.platform.eztrade.wallet.application.ports.out;

import com.trading.platform.eztrade.wallet.domain.WalletAccount;

import java.util.Optional;

public interface WalletAccountRepositoryPort {

    Optional<WalletAccount> findByOwner(String owner);

    Optional<WalletAccount> findByOwnerForUpdate(String owner);

    WalletAccount save(WalletAccount walletAccount);
}

