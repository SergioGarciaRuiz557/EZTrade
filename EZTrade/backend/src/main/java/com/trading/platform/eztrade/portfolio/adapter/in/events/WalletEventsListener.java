package com.trading.platform.eztrade.portfolio.adapter.in.events;

import com.trading.platform.eztrade.portfolio.application.ports.in.HandleWalletCashUpdatedUseCase;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada que sincroniza el cash proyectado de portfolio con eventos del modulo wallet.
 */
@Component
public class WalletEventsListener {

    private final HandleWalletCashUpdatedUseCase handleWalletCashUpdatedUseCase;

    public WalletEventsListener(HandleWalletCashUpdatedUseCase handleWalletCashUpdatedUseCase) {
        this.handleWalletCashUpdatedUseCase = handleWalletCashUpdatedUseCase;
    }

    @EventListener
    public void on(AvailableCashUpdatedEvent event) {
        handleWalletCashUpdatedUseCase.handle(event);
    }
}

