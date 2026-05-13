package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.User;

/**
 * Mapper interno entre la entidad JPA de usuario y el modelo de dominio.
 * <p>
 * Centraliza la traduccion para que repositorios y adaptadores no dupliquen el
 * conocimiento de campos como username/email/password/role.
 */
final class UserJpaMapper {

    private UserJpaMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getSurname(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole()
        );
    }

    static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsernameValue(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}
