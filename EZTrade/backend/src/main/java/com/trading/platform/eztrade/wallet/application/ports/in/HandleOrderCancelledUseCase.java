package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;

/**
 * Puerto de entrada (caso de uso) para reaccionar cuando una orden se cancela.
 * <p>
 * En el wallet se utiliza para liberar los fondos reservados asociados a esa orden.
 */
public interface HandleOrderCancelledUseCase {

    /**
     * Maneja el evento de cancelación de orden.
     *
     * @param event evento publicado por el módulo de trading.
     */
    void handle(OrderCancelledEvent event);
}

