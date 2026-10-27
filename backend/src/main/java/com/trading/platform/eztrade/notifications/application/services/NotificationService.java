package com.trading.platform.eztrade.notifications.application.services;

import com.trading.platform.eztrade.notifications.application.ports.in.NotifyOnDomainEventsUseCase;
import com.trading.platform.eztrade.notifications.application.ports.out.EmailNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.InboxNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.PushNotificationPort;
import com.trading.platform.eztrade.notifications.application.ports.out.WebSocketNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import com.trading.platform.eztrade.notifications.domain.NotificationType;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Application service for the notifications module.
 * <p>
 * Implements the input use case by consuming domain events, transforming them
 * into {@link NotificationMessage}, and distributing them to all configured
 * channels through output ports.
 */
@Service
public class NotificationService implements NotifyOnDomainEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailNotificationPort emailNotificationPort;
    private final PushNotificationPort pushNotificationPort;
    private final WebSocketNotificationPort webSocketNotificationPort;
    private final InboxNotificationPort inboxNotificationPort;

    public NotificationService(EmailNotificationPort emailNotificationPort,
                               PushNotificationPort pushNotificationPort,
                               WebSocketNotificationPort webSocketNotificationPort,
                               InboxNotificationPort inboxNotificationPort) {
        this.emailNotificationPort = emailNotificationPort;
        this.pushNotificationPort = pushNotificationPort;
        this.webSocketNotificationPort = webSocketNotificationPort;
        this.inboxNotificationPort = inboxNotificationPort;
    }

    /**
     * Builds the order-placed notification and dispatches it through all channels.
     *
     * @param event order placement event
     */
    @Override
    public void handle(OrderPlacedEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.owner(),
                NotificationType.ORDER_PLACED,
                "Orden registrada",
                "Se registro la orden #" + event.orderId() + " (" + event.side() + " " + event.quantity().toPlainString() + " " + event.symbol() + " @ " + event.price().toPlainString() + ").",
                event.occurredAt()
        );
        dispatch(message);
    }

    /**
     * Builds the order-executed notification and dispatches it through all channels.
     *
     * @param event order execution event
     */
    @Override
    public void handle(OrderExecutedEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.owner(),
                NotificationType.ORDER_EXECUTED,
                "Orden ejecutada",
                "La orden #" + event.orderId() + " fue ejecutada: " + event.side() + " " + event.quantity().toPlainString() + " " + event.symbol() + " @ " + event.price().toPlainString() + ".",
                event.occurredAt()
        );
        dispatch(message);
    }

    /**
     * Builds the order-canceled notification and dispatches it through all channels.
     *
     * @param event order cancellation event
     */
    @Override
    public void handle(OrderCancelledEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.owner(),
                NotificationType.ORDER_CANCELLED,
                "Orden cancelada",
                "La orden #" + event.orderId() + " para " + event.symbol() + " fue cancelada.",
                event.occurredAt()
        );
        dispatch(message);
    }

    /**
     * Builds the insufficient-funds notification and dispatches it through all channels.
     *
     * @param event insufficient-funds event
     */
    @Override
    public void handle(InsufficientFundsEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.owner(),
                NotificationType.INSUFFICIENT_FUNDS,
                "Fondos insuficientes",
                "No se pudo completar la orden #" + event.orderId()
                        + ". Motivo: " + event.reason()
                        + ". Disponible=" + event.availableBalance().toPlainString()
                        + ", reservado=" + event.reservedBalance().toPlainString()
                        + ", solicitado=" + event.requestedAmount().toPlainString() + ".",
                event.occurredAt()
        );
        dispatch(message);
    }

    /**
     * Builds the portfolio-update notification and dispatches it through all channels.
     *
     * @param event portfolio aggregate valuation event
     */
    @Override
    public void handle(PortfolioValuationUpdatedEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.owner(),
                NotificationType.PORTFOLIO_VALUATION_UPDATED,
                "Cartera actualizada",
                "Valoracion actualizada: cash=" + event.cashAvailable().toPlainString()
                        + ", coste=" + event.totalCostBasis().toPlainString()
                        + ", pnl realizado=" + event.totalRealizedPnl().toPlainString() + ".",
                event.occurredAt()
        );
        dispatch(message);
    }

    /**
     * Fans out the message to all output adapters.
     *
     * @param message normalized message ready to send/persist
     */
    private void dispatch(NotificationMessage message) {
        deliver("email", message, emailNotificationPort::send);
        deliver("push", message, pushNotificationPort::send);
        deliver("websocket", message, webSocketNotificationPort::send);
        deliver("inbox", message, inboxNotificationPort::save);
    }

    private void deliver(String channel, NotificationMessage message, Consumer<NotificationMessage> sender) {
        try {
            sender.accept(message);
        } catch (RuntimeException ex) {
            log.warn("Notification channel '{}' failed for recipient '{}'", channel, message.recipient(), ex);
        }
    }
}

