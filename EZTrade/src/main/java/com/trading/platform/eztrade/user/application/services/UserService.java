package com.trading.platform.eztrade.user.application.services;

import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.application.ports.out.UserRepository;
import com.trading.platform.eztrade.user.application.security.JwtService;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements RegisterUserUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Override
    public String registerUser(User user) throws ChangeSetPersister.NotFoundException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) throw new ChangeSetPersister.NotFoundException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);


        return jwtService.generateToken(saved);
    }
}
