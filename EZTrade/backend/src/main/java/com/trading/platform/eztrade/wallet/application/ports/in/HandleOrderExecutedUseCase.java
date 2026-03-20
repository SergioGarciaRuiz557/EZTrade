package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;

/**
 * Puerto de entrada (caso de uso) para reaccionar cuando una orden se ejecuta.
 * <p>
 * En el wallet se utiliza para liquidar:
 * <ul>
 *   <li>BUY: consumir saldo reservado (debit).</li>
 *   <li>SELL: abonar saldo disponible (credit).</li>
 * </ul>
 */
public interface HandleOrderExecutedUseCase {

    /**
     * Maneja el evento de ejecución de orden.
     *
     * @param event evento publicado por el módulo de trading.
     */
    void handle(OrderExecutedEvent event);
}

