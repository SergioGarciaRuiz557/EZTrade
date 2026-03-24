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
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicacion del modulo notifications.
 * <p>
 * Implementa el caso de uso de entrada consumiendo eventos de dominio,
 * transformandolos en {@link NotificationMessage} y distribuyendolos a
 * todos los canales configurados mediante puertos de salida.
 */
@Service
public class NotificationService implements NotifyOnDomainEventsUseCase {

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
     * Construye la notificacion de orden registrada y la despacha por todos los canales.
     *
     * @param event evento de alta de orden
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
     * Construye la notificacion de orden ejecutada y la despacha por todos los canales.
     *
     * @param event evento de ejecucion de orden
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
     * Construye la notificacion de orden cancelada y la despacha por todos los canales.
     *
     * @param event evento de cancelacion de orden
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
     * Construye la notificacion de actualizacion de cartera y la despacha por todos los canales.
     *
     * @param event evento de valoracion agregada de portfolio
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
     * Hace fan-out del mensaje a todos los adaptadores de salida.
     *
     * @param message mensaje normalizado listo para enviar/persistir
     */
    private void dispatch(NotificationMessage message) {
        emailNotificationPort.send(message);
        pushNotificationPort.send(message);
        webSocketNotificationPort.send(message);
        inboxNotificationPort.save(message);
    }
}

