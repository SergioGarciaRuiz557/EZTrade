/**
 * <h2>UserExistsException</h2>
 *
 * <p><strong>Module:</strong> Domain</p>
 * <p><strong>Layer:</strong> Domain</p>
 *
 * <p><strong>Responsibility:</strong><br/>
 * Encapsulates pure domain business logic.</p>
 *
 * <p><strong>Architectural Role:</strong><br/>
 * Belongs to the module's hexagonal architecture, keeping domain,
 * application, and infrastructure separated through ports and adapters.
 * Managed by Spring Modulith.</p>
 */

package com.trading.platform.eztrade.user.domain.exceptions;

public class UserExistsException extends RuntimeException {
    /**
     * Creates an exception with the provided detail message.
     *
     * @param message detail message
     */
    public UserExistsException(String message) {
        super(message);
    }
}
