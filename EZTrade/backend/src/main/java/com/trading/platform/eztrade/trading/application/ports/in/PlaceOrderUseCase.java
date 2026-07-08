package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Input port for registering a new trading order.
 * <p>
 * Defines the contract consumed by input adapters (REST, messaging, etc.).
 */
public interface PlaceOrderUseCase {

    /**
     * Creates a pending order in the domain.
     *
     * @param command input data for the order
     * @return created and persisted order
     */
    TradeOrder place(PlaceOrderCommand command);

    /**
     * Immutable command with the data required to create an order.
     *
     * @param owner order owner
     * @param symbol asset symbol
     * @param side order side
     * @param quantity requested quantity
     * @param price unit price
     */
    record PlaceOrderCommand(
            String owner,
            String symbol,
            OrderSide side,
            BigDecimal quantity,
            BigDecimal price
    ) {
    }
}
