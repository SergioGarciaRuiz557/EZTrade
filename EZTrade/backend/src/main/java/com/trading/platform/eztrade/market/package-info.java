/**
 * Módulo de mercado de la aplicación.
 * <p>
 * Encapsula la lógica relacionada con instrumentos financieros, precios
 * de mercado y su exposición a través de APIs REST y WebSocket/STOMP.
 * <p>
 * Restricciones de dependencias (Spring Modulith):
 * <ul>
 *   <li>Puede depender de {@code security} para aspectos transversales
 *       de seguridad, si es necesario.</li>
 *   <li>No debe depender directamente del módulo {@code user} para
 *       mantener un bajo acoplamiento.</li>
 * </ul>
 */
@ApplicationModule
package com.trading.platform.eztrade.market;

import org.springframework.modulith.ApplicationModule;

