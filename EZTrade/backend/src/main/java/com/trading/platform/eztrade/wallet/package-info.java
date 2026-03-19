/**
 * Modulo Wallet (Cash/Ledger).
 * <p>
 * Responsabilidades principales:
 * <ul>
 *   <li>Mantener saldo disponible y saldo reservado por usuario.</li>
 *   <li>Registrar todos los movimientos monetarios en un ledger auditable.</li>
 *   <li>Retener, liberar y liquidar fondos en reaccion a eventos de trading.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events"}
)
package com.trading.platform.eztrade.wallet;

import org.springframework.modulith.ApplicationModule;

