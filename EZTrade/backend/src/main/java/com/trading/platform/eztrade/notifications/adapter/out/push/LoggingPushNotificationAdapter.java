package com.trading.platform.eztrade.notifications.adapter.out.push;

import com.trading.platform.eztrade.notifications.application.ports.out.PushNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador push basico. Punto de extension para FCM/APNS u otro proveedor.
 */
@Component
public class LoggingPushNotificationAdapter implements PushNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushNotificationAdapter.class);

    @Override
    public void send(NotificationMessage message) {
        log.info("PUSH to={} type={} title={} body={}",
                message.recipient(),
                message.type(),
                message.title(),
                message.body());
    }
}

