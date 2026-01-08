package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
