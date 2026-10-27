package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

/**
 * Output port for persisting notifications in the internal inbox.
 * <p>
 * Acts as an auditable history and as a fallback when the user is not connected
 * to the WebSocket channel.
 */
public interface InboxNotificationPort {

    /** Saves the message as an unread notification in the user's inbox. */
    void save(NotificationMessage message);
}

