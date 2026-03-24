/**
 * Módulo <strong>Wallet</strong> (Cash/Ledger).
 * <p>
 * Este módulo implementa la lógica de <em>tesorería</em> de la plataforma: mantiene el efectivo disponible y el efectivo
 * reservado de cada usuario, y además persiste un <em>ledger</em> (libro mayor) inmutable/auditable con todos los
 * movimientos.
 * <p>
 * <strong>Responsabilidades principales</strong>:
 * <ul>
 *   <li><strong>Saldo</strong>: mantener {@code availableBalance} (saldo disponible) y {@code reservedBalance} (saldo reservado) por usuario.</li>
 *   <li><strong>Ledger</strong>: registrar cada operación en una entrada inmutable con motivo, referencia y balances post-operación.</li>
 *   <li><strong>Integración con trading</strong>: retener, liberar y liquidar fondos reaccionando a eventos de órdenes (placed/cancelled/executed).</li>
 *   <li><strong>Idempotencia</strong>: evitar aplicar dos veces el mismo movimiento detectándolo por (owner, referenceId, movementType).</li>
 * </ul>
 * <p>
 * <strong>Arquitectura</strong>:
 * <ul>
 *   <li>La capa de aplicación expone <em>puertos de entrada</em> (casos de uso) y depende de <em>puertos de salida</em> (repositorios y publicación de eventos).</li>
 *   <li>Las dependencias hacia infraestructura se implementan en adaptadores (persistencia JPA y publicación de eventos Spring).</li>
 *   <li>Para consistencia en concurrencia se usa bloqueo pesimista al cargar la cuenta ({@code findByOwnerForUpdate}).</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events"}
)
package com.trading.platform.eztrade.wallet;

import org.springframework.modulith.ApplicationModule;

