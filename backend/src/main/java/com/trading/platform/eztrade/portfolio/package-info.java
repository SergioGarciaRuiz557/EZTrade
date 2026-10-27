/**
 * Portfolio module (Holdings/Positions).
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Maintain positions by user and symbol (quantity, average cost, and realized PnL).</li>
 *   <li>Maintain a local available-cash projection from wallet events.</li>
 *   <li>React to order execution events emitted by trading.</li>
 *   <li>Publish position change and portfolio valuation events.</li>
 * </ul>
 * <p>
 * Boundaries:
 * <ul>
 *   <li>It does not execute orders (that belongs to the trading module).</li>
 *   <li>It queries market prices only through the market module public API.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events", "wallet :: events", "market :: api"}
)
package com.trading.platform.eztrade.portfolio;

import org.springframework.modulith.ApplicationModule;

