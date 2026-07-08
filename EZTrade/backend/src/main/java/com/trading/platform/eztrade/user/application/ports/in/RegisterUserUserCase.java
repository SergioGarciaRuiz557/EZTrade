package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;

/**
 * Application use case for registering a new {@link User}.
 * <p>
 * Defines the user creation operation that can be invoked from input adapters
 * such as REST controllers.
 */
public interface RegisterUserUserCase {

    /**
     * Registers a new user in the system.
     * <p>
     * The implementation must verify that no user already exists with the same
     * credentials and throw an exception when that happens.
     *
     * @param user {@link User} domain entity with the data to register
     * @return registered {@link User}, including generated data such as the identifier
     * @throws UserExistsException if an existing user prevents registration
     */
    User registerUser(User user) throws UserExistsException;
}
