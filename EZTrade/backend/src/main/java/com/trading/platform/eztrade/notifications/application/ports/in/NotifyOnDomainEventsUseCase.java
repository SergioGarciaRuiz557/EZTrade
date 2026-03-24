package com.trading.platform.eztrade.notifications.application.ports.in;

import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;

/**
 * Puerto de entrada de notifications para procesar eventos de dominio.
 * <p>
 * Este contrato define que eventos externos pueden disparar notificaciones.
 * Las implementaciones deben limitarse a construir y despachar mensajes,
 * sin introducir logica de negocio de los modulos emisores.
 */
public interface NotifyOnDomainEventsUseCase {

    /**
     * Procesa el alta de una orden y genera la notificacion correspondiente.
     *
     * @param event evento de orden registrada
     */
    void handle(OrderPlacedEvent event);

    /**
     * Procesa la ejecucion de una orden y genera la notificacion correspondiente.
     *
     * @param event evento de orden ejecutada
     */
    void handle(OrderExecutedEvent event);

    /**
     * Procesa la cancelacion de una orden y genera la notificacion correspondiente.
     *
     * @param event evento de orden cancelada
     */
    void handle(OrderCancelledEvent event);

    /**
     * Procesa una actualizacion de valoracion de cartera.
     *
     * @param event evento de portfolio con metricas agregadas
     */
    void handle(PortfolioValuationUpdatedEvent event);
}

