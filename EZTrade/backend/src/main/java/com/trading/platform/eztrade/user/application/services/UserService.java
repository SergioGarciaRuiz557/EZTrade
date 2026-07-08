package com.trading.platform.eztrade.user.application.services;

import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.application.ports.out.UserRepository;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application service for the user module.
 * <p>
 * Implements the registration and query use cases. Coordinates the user
 * repository, encodes the password before persistence, and applies basic rules
 * such as email/username uniqueness and the default role.
 */
@Service
public class UserService implements RegisterUserUserCase, GetUserUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the service with its application and infrastructure dependencies.
     *
     * @param userRepository output port for persistence and lookup
     * @param passwordEncoder security component for encoding passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * <p>
     * Validates that email and username are not already in use, encodes the
     * received password, and assigns the default functional role {@link Role#USER}.
     *
     * @throws UserExistsException if the email or username is already taken
     */
    @Override
    public User registerUser(User user) throws UserExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserExistsException("User already exists");
        }
        if (userRepository.findByUsername(user.getUsernameValue()).isPresent()) {
            throw new UserExistsException("User already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * Finds a user by email or username.
     *
     * @param username identifier entered by the user during login/query
     * @return found domain user
     * @throws UserNotFoundException if no user exists with that email or username
     */
    @Override
    public User getUser(String username) throws UserNotFoundException {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
