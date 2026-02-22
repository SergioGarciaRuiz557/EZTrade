package com.trading.platform.eztrade.user.api;

import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Puerto de entrada para la carga de usuarios en el contexto de seguridad.
 * <p>
 * Este puerto es utilizado por la capa de infraestructura (por ejemplo,
 * adaptadores de Spring Security) para obtener los detalles de un usuario
 * a partir de su identificador.
 */
@NamedInterface
public interface LoadUserForSecurityPort {

    /**
     * Carga los detalles de un usuario a partir de su nombre de usuario.
     *
     * @param username identificador del usuario, normalmente el correo electrónico
     * @return una instancia de {@link UserDetails} con la información de seguridad del usuario
     */
    UserDetails loadByUsername(String username);
}

