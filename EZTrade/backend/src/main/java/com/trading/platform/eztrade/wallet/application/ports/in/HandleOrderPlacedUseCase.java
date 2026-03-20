package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;

/**
 * Puerto de entrada (caso de uso) para reaccionar cuando se coloca una orden.
 * <p>
 * En el wallet se utiliza para reservar fondos en órdenes BUY.
 */
public interface HandleOrderPlacedUseCase {

    /**
     * Maneja el evento de orden colocada.
     *
     * @param event evento publicado por el módulo de trading.
     */
    void handle(OrderPlacedEvent event);
}

