package com.trading.platform.eztrade.user.application.services;

import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.application.ports.out.UserRepository;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements RegisterUserUserCase, GetUserUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User registerUser(User user) throws UserExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) throw new UserExistsException("User already exists");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);


        return userRepository.save(user);
    }

    @Override
    public User getUser(String username) throws UserNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(()->new UserNotFoundException("User not found"));
    }
}
