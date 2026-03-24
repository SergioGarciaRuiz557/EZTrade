package com.trading.platform.eztrade.notifications.application.services;

import com.trading.platform.eztrade.notifications.application.ports.out.EmailNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.InboxNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.PushNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.WebSocketNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import com.trading.platform.eztrade.notifications.domain.NotificationType;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailNotificationPort emailNotificationPort;

    @Mock
    private PushNotificationPort pushNotificationPort;

    @Mock
    private WebSocketNotificationPort webSocketNotificationPort;

    @Mock
    private InboxNotificationPort inboxNotificationPort;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("OrderExecutedEvent dispara fan-out a todos los canales")
    void order_executed_fans_out_all_channels() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                99L,
                "user@demo.com",
                "IBM",
                "BUY",
                new BigDecimal("2"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        notificationService.handle(event);

        verify(emailNotificationPort, times(1)).send(any(NotificationMessage.class));
        verify(pushNotificationPort, times(1)).send(any(NotificationMessage.class));
        verify(webSocketNotificationPort, times(1)).send(any(NotificationMessage.class));

        ArgumentCaptor<NotificationMessage> inboxCaptor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(inboxNotificationPort, times(1)).save(inboxCaptor.capture());

        NotificationMessage saved = inboxCaptor.getValue();
        assertThat(saved.recipient()).isEqualTo("user@demo.com");
        assertThat(saved.type()).isEqualTo(NotificationType.ORDER_EXECUTED);
        assertThat(saved.title()).isEqualTo("Orden ejecutada");
        assertThat(saved.body()).contains("#99").contains("IBM");
    }

    @Test
    @DisplayName("PortfolioValuationUpdatedEvent tambien genera notificacion")
    void portfolio_event_generates_notification() {
        PortfolioValuationUpdatedEvent event = new PortfolioValuationUpdatedEvent(
                "user@demo.com",
                new BigDecimal("500"),
                new BigDecimal("200"),
                new BigDecimal("35"),
                LocalDateTime.now()
        );

        notificationService.handle(event);

        verify(emailNotificationPort, times(1)).send(any(NotificationMessage.class));
        verify(pushNotificationPort, times(1)).send(any(NotificationMessage.class));
        verify(webSocketNotificationPort, times(1)).send(any(NotificationMessage.class));

        ArgumentCaptor<NotificationMessage> inboxCaptor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(inboxNotificationPort, times(1)).save(inboxCaptor.capture());

        NotificationMessage saved = inboxCaptor.getValue();
        assertThat(saved.type()).isEqualTo(NotificationType.PORTFOLIO_VALUATION_UPDATED);
        assertThat(saved.body()).contains("cash=500").contains("pnl realizado=35");
    }

}

