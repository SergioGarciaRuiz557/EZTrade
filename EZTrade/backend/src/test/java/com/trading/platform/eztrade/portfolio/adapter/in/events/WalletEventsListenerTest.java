package com.trading.platform.eztrade.portfolio.adapter.in.events;

import com.trading.platform.eztrade.portfolio.application.ports.in.HandleWalletCashUpdatedUseCase;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventsListenerTest {

    @Mock
    private HandleWalletCashUpdatedUseCase handleWalletCashUpdatedUseCase;

    private WalletEventsListener listener;

    @BeforeEach
    void setUp() {
        listener = new WalletEventsListener(handleWalletCashUpdatedUseCase);
    }

    @Test
    @DisplayName("delegates AvailableCashUpdatedEvent")
    void delegates_available_cash_updated_event() {
        AvailableCashUpdatedEvent event = new AvailableCashUpdatedEvent(
                "user@demo.com",
                new BigDecimal("320"),
                "ORDER_EXECUTED",
                "200",
                LocalDateTime.now()
        );

        listener.on(event);

        verify(handleWalletCashUpdatedUseCase).handle(event);
    }
}

