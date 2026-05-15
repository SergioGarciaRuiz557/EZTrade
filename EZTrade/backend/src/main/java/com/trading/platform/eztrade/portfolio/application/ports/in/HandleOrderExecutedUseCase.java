package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;

/**
 * Puerto de entrada que procesa ejecuciones confirmadas por wallet/trading.
 * <p>
 * Portfolio escucha este evento para abrir, aumentar, reducir o cerrar
 * posiciones sin acoplarse al servicio interno de trading.
 */
public interface HandleOrderExecutedUseCase {

    /** Aplica el efecto de la orden ejecutada sobre las posiciones del usuario. */
    void handle(OrderExecutedEvent event);
}

