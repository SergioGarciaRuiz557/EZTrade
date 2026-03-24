package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;

/**
 * Puerto de entrada para actualizar la proyeccion de cash de portfolio con eventos de wallet.
 */
public interface HandleWalletCashUpdatedUseCase {

    void handle(AvailableCashUpdatedEvent event);
}

