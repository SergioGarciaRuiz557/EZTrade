package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;

/**
 * Caso de uso de aplicación para la obtención de un {@link User}.
 * <p>
 * Define la operación de lectura de usuario que puede ser invocada
 * desde los adaptadores de entrada (por ejemplo, controladores REST
 * o componentes de seguridad).
 */
public interface GetUserUserCase {

    /**
     * Recupera un usuario a partir de su identificador de autenticación.
     * \<p\>
     * La implementación deberá resolver el usuario (normalmente usando
     * su email o username) y lanzar una excepción si no existe.
     *
     * @param username identificador del usuario (por ejemplo, correo electrónico)
     * @return la entidad de dominio {@link User} correspondiente
     * @throws UserNotFoundException si no se encuentra ningún usuario con el identificador dado
     */
    User getUser(String username) throws UserNotFoundException;
}
