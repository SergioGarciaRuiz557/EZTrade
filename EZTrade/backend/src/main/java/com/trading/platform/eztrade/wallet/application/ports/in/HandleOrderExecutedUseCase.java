package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;

public interface HandleOrderExecutedUseCase {

    void handle(OrderExecutedEvent event);
}

