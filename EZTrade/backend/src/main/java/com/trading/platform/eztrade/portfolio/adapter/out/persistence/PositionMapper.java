package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.PositionJpaEntity;
import com.trading.platform.eztrade.portfolio.domain.Position;

/**
 * Internal mapper between the {@link Position} domain aggregate and its JPA entity.
 * <p>
 * Keeping it separate prevents the domain from having persistence annotations
 * or public constructors intended only for JPA.
 */
final class PositionMapper {

    private PositionMapper() {
    }

    static Position toDomain(PositionJpaEntity entity) {
        // Rehydrate the aggregate through the domain factory so its basic
        // validations are preserved.
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

