/**
 * <strong>Wallet</strong> module (Cash/Ledger).
 * <p>
 * This module implements the platform treasury logic: it keeps each user's
 * available and reserved cash, and persists an immutable/auditable
 * <em>ledger</em> with every movement.
 * <p>
 * <strong>Main responsibilities</strong>:
 * <ul>
 *   <li><strong>Balance</strong>: keep {@code availableBalance} and {@code reservedBalance} per user.</li>
 *   <li><strong>Ledger</strong>: record each operation as an immutable entry with reason, reference, and post-operation balances.</li>
 *   <li><strong>Trading integration</strong>: reserve, release, and settle funds in reaction to order events (placed/cancelled/executed).</li>
 *   <li><strong>Idempotency</strong>: prevent applying the same movement twice by detecting it through (owner, referenceId, movementType).</li>
 * </ul>
 * <p>
 * <strong>Architecture</strong>:
 * <ul>
 *   <li>The application layer exposes <em>input ports</em> (use cases) and depends on <em>output ports</em> (repositories and event publishing).</li>
 *   <li>Infrastructure dependencies are implemented through adapters (JPA persistence and Spring event publishing).</li>
 *   <li>Pessimistic locking is used when loading the account ({@code findByOwnerForUpdate}) to preserve consistency under concurrency.</li>
 * </ul>
 */
@ApplicationModule(
        allowedDependencies = {"trading :: events", "user :: api"}
)
package com.trading.platform.eztrade.wallet;

import org.springframework.modulith.ApplicationModule;

