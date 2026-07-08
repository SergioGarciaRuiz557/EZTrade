/**
 * Application market module.
 * <p>
 * Encapsulates logic related to financial instruments, market prices, and their
 * exposure through REST APIs and WebSocket/STOMP.
 * <p>
 * Dependency constraints (Spring Modulith):
 * <ul>
 *   <li>May depend on {@code security} for cross-cutting security concerns when necessary.</li>
 *   <li>Must not depend directly on the {@code user} module to keep coupling low.</li>
 * </ul>
 */
@ApplicationModule
package com.trading.platform.eztrade.market;

import org.springframework.modulith.ApplicationModule;

