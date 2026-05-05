package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutionRequestEvent;

/**
 * Puerto de entrada (caso de uso) para reaccionar cuando se solicita ejecutar una orden.
 * <p>
 * En el wallet se utiliza para liquidar:
 * <ul>
 *   <li>BUY: consumir saldo reservado (debit).</li>
 *   <li>SELL: abonar saldo disponible (credit).</li>
 * </ul>
 */
public interface HandleOrderExecutedUseCase {

    /**
     * Maneja el evento de solicitud de ejecucion de orden.
     *
     * @param event evento publicado por el modulo de trading.
     */
    void handle(OrderExecutionRequestEvent event);
}
