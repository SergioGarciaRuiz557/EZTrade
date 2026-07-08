package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;

/**
 * Input port for order queries.
 */
public interface GetOrdersUseCase {

    /**
     * Gets an order by its identifier.
     *
     * @param orderId order id
     * @return found order
     */
    TradeOrder getById(OrderId orderId);

    /**
     * Gets all orders for an owner.
     *
     * @param owner order owner
     * @return owner's order list
     */
    List<TradeOrder> getByOwner(String owner);
}
