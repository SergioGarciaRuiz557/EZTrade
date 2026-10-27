/**
 * Trading module.
 * <p>
 * This module encapsulates the lifecycle management of buy and sell orders,
 * including execution and cancellation.
 * <p>
 * Architecture and boundaries:
 * <ul>
 *   <li>Hexagonal architecture: domain, application (ports/use cases), and adapters.</li>
 *   <li>The domain is pure: no Spring dependencies or infrastructure details.</li>
 *   <li>The application orchestrates business rules through input/output ports.</li>
 *   <li>Communication with other modules is performed through domain events.</li>
 *   <li>To validate marketplace prices, trading only depends on the public
 *       {@code market :: api}; it does not query adapters or internal market
 *       services directly.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"market :: api"}
)
package com.trading.platform.eztrade.trading;

import org.springframework.modulith.ApplicationModule;
