package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Input port for cancelling an order.
 */
public interface CancelOrderUseCase {

    /**
     * Cancels a pending order after validating the owner.
     *
     * @param orderId order identifier
     * @param requestedBy user requesting cancellation
     * @return cancelled order
     */
    TradeOrder cancel(OrderId orderId, String requestedBy);
}
