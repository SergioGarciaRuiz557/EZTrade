package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad de dominio {@link User}.
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}
 * y métodos específicos para el acceso a datos de usuarios.
 */
public interface JpaUserRepository extends JpaRepository<User, Integer> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email correo electrónico del usuario a buscar
     * @return un {@link Optional} que contiene el usuario si existe,
     *         o vacío en caso contrario
     */
    Optional<User> findByEmail(String email);
}
