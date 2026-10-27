package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutionRequestEvent;

/**
 * Input port (use case) for reacting when order execution is requested.
 * <p>
 * In the wallet, it is used to settle:
 * <ul>
 *   <li>BUY: consume reserved balance (debit).</li>
 *   <li>SELL: credit available balance (credit).</li>
 * </ul>
 */
public interface HandleOrderExecutedUseCase {

    /**
     * Handles the order-execution request event.
     *
     * @param event event published by the trading module
     */
    void handle(OrderExecutionRequestEvent event);
}
