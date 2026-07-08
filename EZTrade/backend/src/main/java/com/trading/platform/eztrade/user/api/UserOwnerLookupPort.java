package com.trading.platform.eztrade.user.api;

import org.springframework.modulith.NamedInterface;

import java.util.Optional;

/**
 * Public API for resolving a user's canonical identifier.
 */
@NamedInterface
public interface UserOwnerLookupPort {

    /**
     * Resolves an email or username to a canonical owner for other modules.
     */
    Optional<String> findOwner(String identifier);
}
