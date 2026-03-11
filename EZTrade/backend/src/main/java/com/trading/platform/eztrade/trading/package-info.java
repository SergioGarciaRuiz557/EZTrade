/**
 * Modulo Trading.
 * <p>
 * Este modulo encapsula la gestion del ciclo de vida de ordenes de compra y venta,
 * asi como su ejecucion y cancelacion.
 * <p>
 * Arquitectura y limites:
 * <ul>
 *   <li>Arquitectura hexagonal: dominio, aplicacion (puertos/casos de uso) y adaptadores.</li>
 *   <li>El dominio es puro: sin dependencias de Spring ni detalles de infraestructura.</li>
 *   <li>La aplicacion orquesta reglas de negocio a traves de puertos de entrada/salida.</li>
 *   <li>La comunicacion con otros modulos se realiza mediante eventos de dominio.</li>
 * </ul>
 */
@ApplicationModule
package com.trading.platform.eztrade.trading;

import org.springframework.modulith.ApplicationModule;
