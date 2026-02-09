package com.trading.platform.eztrade.security.jwt;

import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Proveedor de autenticación basado en JWT.
 * <p>
 * Encapsula el acceso al puerto de carga de usuarios para integrarlo
 * con la infraestructura de seguridad de Spring.
 */
@Component
public class JwtAuthenticationProvider {

    /**
     * Puerto de aplicación encargado de cargar los datos de usuario
     * necesarios para la autenticación.
     */
    private final LoadUserForSecurityPort userPort;

    /**
     * Crea una nueva instancia del proveedor de autenticación JWT.
     *
     * @param userPort puerto usado para obtener los detalles del usuario
     */
    public JwtAuthenticationProvider(LoadUserForSecurityPort userPort) {
        this.userPort = userPort;
    }

    /**
     * Carga los detalles de un usuario a partir de su nombre de usuario.
     *
     * @param username identificador del usuario (por ejemplo, email)
     * @return detalles del usuario necesarios para la autenticación
     */
    public UserDetails loadByUsername(String username) {
        return userPort.loadByUsername(username);
    }
}

