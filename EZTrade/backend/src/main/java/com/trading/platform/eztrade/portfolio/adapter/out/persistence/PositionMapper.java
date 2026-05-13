package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.PositionJpaEntity;
import com.trading.platform.eztrade.portfolio.domain.Position;

/**
 * Mapper interno entre el agregado de dominio {@link Position} y su entidad JPA.
 * <p>
 * Mantenerlo separado evita que el dominio tenga anotaciones de persistencia o
 * constructores publicos pensados solo para JPA.
 */
final class PositionMapper {

    private PositionMapper() {
    }

    static Position toDomain(PositionJpaEntity entity) {
        // Rehidrata el agregado pasando por la factoria del dominio para que se
        // mantengan sus validaciones basicas.
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

