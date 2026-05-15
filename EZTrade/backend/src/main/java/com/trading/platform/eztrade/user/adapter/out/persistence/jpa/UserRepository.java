package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia para usuarios.
 * <p>
 * Traduce entre el agregado de dominio puro y la entidad JPA usada por la
 * infraestructura de base de datos.
 */
@Repository
public class UserRepository implements com.trading.platform.eztrade.user.application.ports.out.UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepository(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findByEmail(String username) {
        return jpaUserRepository.findByEmail(username).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(UserJpaMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = jpaUserRepository.save(UserJpaMapper.toEntity(user));
        return UserJpaMapper.toDomain(saved);
    }
}
