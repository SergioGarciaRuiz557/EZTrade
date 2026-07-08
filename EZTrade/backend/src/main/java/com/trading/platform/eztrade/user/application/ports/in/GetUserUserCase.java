package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;

/**
 * Application use case for retrieving a {@link User}.
 * <p>
 * Defines the user read operation that can be invoked from input adapters such
 * as REST controllers or security components.
 */
public interface GetUserUserCase {

    /**
     * Retrieves a user from its authentication identifier.
     * <p>
     * The implementation must resolve the user (usually using email or username)
     * and throw an exception if it does not exist.
     *
     * @param username user identifier (for example, email address)
     * @return matching {@link User} domain entity
     * @throws UserNotFoundException if no user is found with the given identifier
     */
    User getUser(String username) throws UserNotFoundException;
}
