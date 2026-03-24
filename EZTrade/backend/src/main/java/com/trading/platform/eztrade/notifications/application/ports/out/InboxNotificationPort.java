package com.trading.platform.eztrade.notifications.application.ports.out;

import com.trading.platform.eztrade.notifications.domain.NotificationMessage;

public interface InboxNotificationPort {

    void save(NotificationMessage message);
}

