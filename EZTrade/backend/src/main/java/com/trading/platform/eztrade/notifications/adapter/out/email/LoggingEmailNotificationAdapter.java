package com.trading.platform.eztrade.notifications.adapter.out.email;

import com.trading.platform.eztrade.notifications.application.ports.out.EmailNotificationPort;
import com.trading.platform.eztrade.notifications.domain.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador email basico. Punto de extension para SMTP/proveedor externo.
 */
@Component
public class LoggingEmailNotificationAdapter implements EmailNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationAdapter.class);

    @Override
    public void send(NotificationMessage message) {
        log.info("EMAIL to={} type={} title={} body={}",
                message.recipient(),
                message.type(),
                message.title(),
                message.body());
    }
}

