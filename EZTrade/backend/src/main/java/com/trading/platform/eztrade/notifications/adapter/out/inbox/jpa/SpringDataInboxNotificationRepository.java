package com.trading.platform.eztrade.notifications.adapter.out.inbox.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de la bandeja persistida de notificaciones.
 */
public interface SpringDataInboxNotificationRepository extends JpaRepository<InboxNotificationJpaEntity, Long> {
}

