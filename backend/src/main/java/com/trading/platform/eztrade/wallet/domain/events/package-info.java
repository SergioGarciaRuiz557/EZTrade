/**
 * Public interface of the Wallet module for <strong>domain events</strong>.
 * <p>
 * Spring Modulith uses {@link org.springframework.modulith.NamedInterface} to
 * declare which package is considered part of the module API. This package
 * contains <em>records</em> that represent relevant wallet-domain facts,
 * typically published after an operation (reserve/release/settlement) or after
 * a business failure (insufficient funds).
 * <p>
 * Note: events are modeled as simple objects (records) to make publication
 * through the Spring event bus and consumption by other modules easier.
 */
@NamedInterface("events")
package com.trading.platform.eztrade.wallet.domain.events;

import org.springframework.modulith.NamedInterface;

