package com.trading.platform.eztrade.trading.adapter.out.persistence;

import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.TradeOrderJpaEntity;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Mapping utility between the persistence model and the domain model.
 * <p>
 * Keeps the domain decoupled from JPA annotations.
 */
final class TradeOrderMapper {

    private TradeOrderMapper() {
    }

    /**
     * Converts a JPA entity into a domain aggregate.
     *
     * @param entity persistence entity
     * @return rehydrated domain aggregate
     */
    static TradeOrder toDomain(TradeOrderJpaEntity entity) {
        return TradeOrder.rehydrate(
                new OrderId(entity.getId()),
                entity.getOwner(),
                entity.getSymbol(),
                entity.getSide(),
                new Quantity(entity.getQuantity()),
                new Money(entity.getPrice()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExecutedAt()
        );
    }

    /**
     * Converts a domain aggregate into a JPA entity.
     *
     * @param order domain aggregate
     * @return entity ready for persistence
     */
    static TradeOrderJpaEntity toEntity(TradeOrder order) {
        TradeOrderJpaEntity entity = new TradeOrderJpaEntity();
        if (order.id() != null) {
            entity.setId(order.id().value());
        }
        entity.setOwner(order.owner());
        entity.setSymbol(order.symbol());
        entity.setSide(order.side());
        entity.setQuantity(order.quantity().value());
        entity.setPrice(order.price().value());
        entity.setStatus(order.status());
        entity.setCreatedAt(order.createdAt());
        entity.setExecutedAt(order.executedAt());
        return entity;
    }
}
