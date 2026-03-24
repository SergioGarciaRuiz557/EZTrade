/**
 * Modulo Notifications.
 * <p>
 * Este modulo consume eventos de negocio emitidos por otros modulos y los transforma
 * en mensajes de notificacion para distintos canales (email, push, websocket e inbox in-app).
 * <p>
 * Flujo funcional:
 * <ul>
 *   <li>Recibe eventos de dominio (por ejemplo, orden registrada/ejecutada/cancelada).</li>
 *   <li>Construye un mensaje de notificacion con titulo y cuerpo legibles para el usuario.</li>
 *   <li>Hace fan-out del mismo mensaje a varios canales de entrega.</li>
 * </ul>
 * <p>
 * Limites:
 * <ul>
 *   <li>No contiene logica de trading ni de portfolio.</li>
 *   <li>No decide reglas de negocio; solo formatea y enruta notificaciones.</li>
 *   <li>Depende unicamente de interfaces de eventos expuestas por otros modulos.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events", "portfolio :: events"}
)
package com.trading.platform.eztrade.notifications;

import org.springframework.modulith.ApplicationModule;

