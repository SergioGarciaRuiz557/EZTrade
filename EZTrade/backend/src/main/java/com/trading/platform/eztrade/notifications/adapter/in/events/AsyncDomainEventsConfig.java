package com.trading.platform.eztrade.notifications.adapter.in.events;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuracion que habilita listeners de eventos asincronos en notifications.
 * <p>
 * El modulo puede procesar avisos sin bloquear el flujo principal que publico
 * el evento, siempre que los listeners se anoten como asincronos.
 */
@Configuration
@EnableAsync
class AsyncDomainEventsConfig {
}
