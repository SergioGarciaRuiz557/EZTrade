package com.trading.platform.eztrade.notifications.adapter.out.inbox.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for the persisted notification inbox.
 */
public interface SpringDataInboxNotificationRepository extends JpaRepository<InboxNotificationJpaEntity, Long> {
}

