package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Output port for sending push notifications.
 * <p>
 * Allows {@code NotificationService} to publish to a mobile channel without
 * depending on SDKs or external providers.
 */
public interface PushNotificationPort {

    /** Sends the already built message to the push channel. */
    void send(NotificationMessage message);
}

