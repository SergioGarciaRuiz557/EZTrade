package com.trading.platform.eztrade.notifications.adapter.out.ws;

import com.trading.platform.eztrade.notifications.application.ports.out.WebSocketNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador websocket para notificaciones near real-time al usuario autenticado.
 */
@Component
public class WebSocketNotificationAdapter implements WebSocketNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationAdapter.class);
    private static final String USER_QUEUE_DESTINATION = "/queue/notifications";

    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    public WebSocketNotificationAdapter(ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider) {
        this.messagingTemplateProvider = messagingTemplateProvider;
    }

    @Override
    public void send(NotificationMessage message) {
        SimpMessagingTemplate template = messagingTemplateProvider.getIfAvailable();
        if (template == null) {
            log.info("WS disabled. Notification queued only for inbox user={} type={}", message.recipient(), message.type());
            return;
        }

        template.convertAndSendToUser(
                message.recipient(),
                USER_QUEUE_DESTINATION,
                Map.of(
                        "type", message.type().name(),
                        "title", message.title(),
                        "body", message.body(),
                        "occurredAt", message.occurredAt()
                )
        );
    }
}

