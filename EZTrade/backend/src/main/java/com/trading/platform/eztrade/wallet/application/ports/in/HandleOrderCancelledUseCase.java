package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;

public interface HandleOrderCancelledUseCase {

    void handle(OrderCancelledEvent event);
}

