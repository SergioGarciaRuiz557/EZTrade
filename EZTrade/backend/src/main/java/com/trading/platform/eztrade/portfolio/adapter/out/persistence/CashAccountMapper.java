package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.CashAccountJpaEntity;
import com.trading.platform.eztrade.portfolio.domain.CashAccount;

final class CashAccountMapper {

    private CashAccountMapper() {
    }

    static CashAccount toDomain(CashAccountJpaEntity entity) {
        return CashAccount.rehydrate(entity.getOwner(), entity.getAvailableCash());
    }

    static CashAccountJpaEntity toEntity(CashAccount cashAccount) {
        CashAccountJpaEntity entity = new CashAccountJpaEntity();
        entity.setOwner(cashAccount.owner());
        entity.setAvailableCash(cashAccount.availableCash());
        return entity;
    }
}

