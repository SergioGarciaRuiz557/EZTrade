package com.trading.platform.eztrade.trading.adapter.out.persistence;

import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.TradeOrderJpaEntity;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Utilidad de mapeo entre modelo de persistencia y modelo de dominio.
 * <p>
 * Mantiene el dominio desacoplado de anotaciones JPA.
 */
final class TradeOrderMapper {

    private TradeOrderMapper() {
    }

    /**
     * Convierte una entidad JPA en agregado de dominio.
     *
     * @param entity entidad de persistencia
     * @return agregado de dominio rehidratado
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
     * Convierte un agregado de dominio en entidad JPA.
     *
     * @param order agregado de dominio
     * @return entidad lista para persistencia
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
