package com.trading.platform.eztrade.user.application.ports.out;

import com.trading.platform.eztrade.user.domain.User;

import java.util.Optional;

/**
 * Puerto de salida para el acceso y gestión de la entidad de dominio {@link User}.
 * <p>
 * Define las operaciones de persistencia que debe ofrecer la capa de
 * adapter (por ejemplo, un repositorio JPA) hacia la capa de aplicación.
 */
public interface UserRepository {

    /**
     * Busca un usuario por su correo electrónico o nombre de usuario.
     *
     * @param username identificador del usuario (normalmente correo electrónico)
     * @return un {@link Optional} que contiene el {@link User} si existe,
     *         o vacío si no se encuentra ningún usuario con ese identificador
     */
    Optional<User> findByEmail(String username);

    /**
     * Persiste un usuario en el sistema.
     * <p>
     * Si el usuario ya existe, se actualiza su información; en caso contrario,
     * se crea un nuevo registro.
     *
     * @param user entidad de dominio {@link User} a guardar
     * @return la entidad {@link User} resultante tras la operación de guardado
     */
    User save(User user);
}
