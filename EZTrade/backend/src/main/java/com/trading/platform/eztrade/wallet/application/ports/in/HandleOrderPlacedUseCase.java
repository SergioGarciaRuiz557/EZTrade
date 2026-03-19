package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;

public interface HandleOrderPlacedUseCase {

    void handle(OrderPlacedEvent event);
}

