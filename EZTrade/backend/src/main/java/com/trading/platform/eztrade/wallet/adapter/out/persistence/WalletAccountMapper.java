package com.trading.platform.eztrade.wallet.adapter.out.persistence;

import com.trading.platform.eztrade.wallet.adapter.out.persistence.jpa.WalletAccountJpaEntity;
import com.trading.platform.eztrade.wallet.domain.WalletAccount;

final class WalletAccountMapper {

    private WalletAccountMapper() {
    }

    static WalletAccount toDomain(WalletAccountJpaEntity entity) {
        return WalletAccount.rehydrate(
                entity.getOwner(),
                entity.getAvailableBalance(),
                entity.getReservedBalance()
        );
    }

    static WalletAccountJpaEntity toEntity(WalletAccount account) {
        WalletAccountJpaEntity entity = new WalletAccountJpaEntity();
        entity.setOwner(account.owner());
        entity.setAvailableBalance(account.availableBalance());
        entity.setReservedBalance(account.reservedBalance());
        return entity;
    }
}

