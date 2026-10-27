package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Output port for sending email notifications.
 * <p>
 * The application only knows this contract; the concrete implementation can be
 * a real mail provider or, as in development, a logging adapter.
 */
public interface EmailNotificationPort {

    /** Sends the already formatted message to the email channel. */
    void send(NotificationMessage message);
}

