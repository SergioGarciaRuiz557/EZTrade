package com.trading.platform.eztrade.user.application.ports.out;

import com.trading.platform.eztrade.user.domain.User;

import java.util.Optional;

/**
 * Output port for accessing and managing the {@link User} domain entity.
 * <p>
 * Defines the persistence operations that the adapter layer (for example, a JPA
 * repository) must provide to the application layer.
 */
public interface UserRepository {

    /**
     * Finds a user by email address or username.
     *
     * @param username user identifier (usually an email address)
     * @return {@link Optional} containing the {@link User} if it exists, or empty
     *         if no user is found with that identifier
     */
    Optional<User> findByEmail(String username);

    /**
     * Finds a user by username.
     *
     * @param username username
     * @return {@link Optional} with the user if it exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Persists a user in the system.
     * <p>
     * If the user already exists, its information is updated; otherwise a new
     * record is created.
     *
     * @param user {@link User} domain entity to save
     * @return resulting {@link User} entity after the save operation
     */
    User save(User user);
}
