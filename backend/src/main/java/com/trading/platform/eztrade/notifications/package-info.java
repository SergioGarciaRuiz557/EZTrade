/**
 * Notifications module.
 * <p>
 * This module consumes business events emitted by other modules and transforms
 * them into notification messages for different channels (email, push,
 * websocket, and in-app inbox).
 * <p>
 * Functional flow:
 * <ul>
 *   <li>Receives domain events (for example, order placed/executed/canceled).</li>
 *   <li>Builds a notification message with a user-readable title and body.</li>
 *   <li>Fans out the same message to several delivery channels.</li>
 * </ul>
 * <p>
 * Boundaries:
 * <ul>
 *   <li>It does not contain trading or portfolio logic.</li>
 *   <li>It does not decide business rules; it only formats and routes notifications.</li>
 *   <li>It depends only on event interfaces exposed by other modules.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events", "portfolio :: events", "wallet :: events"}
)
package com.trading.platform.eztrade.notifications;

import org.springframework.modulith.ApplicationModule;

