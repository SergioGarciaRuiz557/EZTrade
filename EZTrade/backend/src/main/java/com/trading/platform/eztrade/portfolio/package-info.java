/**
 * Modulo Portfolio (Cartera/Posiciones).
 * <p>
 * Responsabilidades principales:
 * <ul>
 *   <li>Mantener posiciones por usuario y simbolo (cantidad, coste medio y PnL realizado).</li>
 *   <li>Mantener una proyeccion local del cash disponible a partir de eventos de wallet.</li>
 *   <li>Reaccionar a eventos de ejecucion de ordenes emitidos por trading.</li>
 *   <li>Publicar eventos de cambios de posicion y valoracion de cartera.</li>
 * </ul>
 * <p>
 * Limites:
 * <ul>
 *   <li>No ejecuta ordenes (eso pertenece al modulo trading).</li>
 *   <li>No consulta precios de mercado directamente.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events", "wallet :: events"}
)
package com.trading.platform.eztrade.portfolio;

import org.springframework.modulith.ApplicationModule;

