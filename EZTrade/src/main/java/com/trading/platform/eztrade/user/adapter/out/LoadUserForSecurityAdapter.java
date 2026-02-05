package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida para la carga de usuarios utilizada por el módulo
 * de seguridad de Spring Security.
 * <p>
 * Implementa el puerto {@link LoadUserForSecurityPort} delegando la
 * obtención del usuario al caso de uso {@link GetUserUserCase}.
 */
@Component
class LoadUserForSecurityAdapter implements LoadUserForSecurityPort {

    private final GetUserUserCase getUserUserCase;

    /**
     * Crea una nueva instancia del adaptador de carga de usuarios para seguridad.
     *
     * @param getUserUserCase caso de uso encargado de recuperar usuarios por su email o username
     */
    LoadUserForSecurityAdapter(GetUserUserCase getUserUserCase) {
        this.getUserUserCase = getUserUserCase;
    }

    /**
     * Carga los detalles de un usuario a partir de su nombre de usuario.
     * <p>
     * Delegará en el caso de uso {@link GetUserUserCase} la obtención del usuario
     * y devolverá una instancia que implementa {@link UserDetails} para ser usada
     * por Spring Security en el proceso de autenticación.
     *
     * @param username identificador del usuario (normalmente email)
     * @return detalles del usuario para el contexto de seguridad
     */
    @Override
    public UserDetails loadByUsername(String username) {
        return getUserUserCase.getUser(username);
    }
}
