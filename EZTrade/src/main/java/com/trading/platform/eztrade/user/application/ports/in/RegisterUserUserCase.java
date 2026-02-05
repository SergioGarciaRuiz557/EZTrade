package com.trading.platform.eztrade.user.application.ports.in;

import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;

/**
 * Caso de uso de aplicación para el registro de un nuevo {@link User}.
 * <p>
 * Define la operación de alta de usuario que puede ser invocada
 * desde los adaptadores de entrada (por ejemplo, controladores REST).
 */
public interface RegisterUserUserCase {

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * La implementación deberá verificar que no exista ya un usuario
     * con las mismas credenciales y lanzar una excepción en tal caso.
     *
     * @param user entidad de dominio {@link User} con los datos a registrar
     * @return el {@link User} registrado, incluyendo cualquier dato generado (por ejemplo, identificador)
     * @throws UserExistsException si ya existe un usuario que impide el registro
     */
    User registerUser(User user) throws UserExistsException;
}
