package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;


import com.trading.platform.eztrade.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository implements com.trading.platform.eztrade.user.application.ports.out.UserRepository {
    private final JpaUserRepository jpaUserRepository;

    public UserRepository(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }


    @Override
    public Optional<User> findByEmail(String username) {
        return jpaUserRepository.findByEmail(username);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
