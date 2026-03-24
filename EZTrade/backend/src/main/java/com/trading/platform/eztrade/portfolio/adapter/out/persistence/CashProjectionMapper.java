package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.CashProjectionJpaEntity;
import com.trading.platform.eztrade.portfolio.domain.CashProjection;

final class CashProjectionMapper {

    private CashProjectionMapper() {
    }

    static CashProjection toDomain(CashProjectionJpaEntity entity) {
        return new CashProjection(entity.getOwner(), entity.getAvailableCash(), entity.getUpdatedAt());
    }

    static CashProjectionJpaEntity toEntity(CashProjection projection) {
        CashProjectionJpaEntity entity = new CashProjectionJpaEntity();
        entity.setOwner(projection.owner());
        entity.setAvailableCash(projection.availableCash());
        entity.setUpdatedAt(projection.updatedAt());
        return entity;
    }
}

