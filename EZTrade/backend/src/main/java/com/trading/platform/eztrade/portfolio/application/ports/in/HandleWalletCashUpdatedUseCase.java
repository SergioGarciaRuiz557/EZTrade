package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;

/**
 * Input port for updating the portfolio cash projection with wallet events.
 */
public interface HandleWalletCashUpdatedUseCase {

    void handle(AvailableCashUpdatedEvent event);
}

