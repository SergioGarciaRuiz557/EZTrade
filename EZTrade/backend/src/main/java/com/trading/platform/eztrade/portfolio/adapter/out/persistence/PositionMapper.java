package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.PositionJpaEntity;
import com.trading.platform.eztrade.portfolio.domain.Position;

final class PositionMapper {

    private PositionMapper() {
    }

    static Position toDomain(PositionJpaEntity entity) {
        return Position.rehydrate(
                entity.getOwner(),
                entity.getSymbol(),
                entity.getQuantity(),
                entity.getAverageCost(),
                entity.getRealizedPnl(),
                entity.getUpdatedAt()
        );
    }

    static PositionJpaEntity toEntity(Position position) {
        PositionJpaEntity entity = new PositionJpaEntity();
        entity.setOwner(position.owner());
        entity.setSymbol(position.symbol());
        entity.setQuantity(position.quantity());
        entity.setAverageCost(position.averageCost());
        entity.setRealizedPnl(position.realizedPnl());
        entity.setUpdatedAt(position.updatedAt());
        return entity;
    }
}

