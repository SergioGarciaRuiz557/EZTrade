package com.trading.platform.eztrade.user.application.ports.out;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String username);

    User save(User user);
}
