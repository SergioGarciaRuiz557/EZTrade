package com.trading.platform.eztrade.user.application.services;

import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.application.ports.out.UserRepository;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación para la gestión de usuarios.
 * <p>
 * Implementa los casos de uso de registro y obtención de usuarios,
 * orquestando el acceso al repositorio y la codificación de contraseñas.
 */
@Service
public class UserService implements RegisterUserUserCase, GetUserUserCase {

    /**
     * Puerto de salida para el acceso y persistencia de usuarios.
     */
    private final UserRepository userRepository;

    /**
     * Componente encargado de codificar las contraseñas de los usuarios.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea una nueva instancia del servicio de usuarios.
     *
     * @param userRepository repositorio de usuarios utilizado para las operaciones de persistencia
     * @param passwordEncoder codificador de contraseñas a aplicar antes de guardar el usuario
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Verifica previamente si ya existe un usuario con el mismo email,
     * codifica la contraseña en texto plano y asigna el rol por defecto
     * <strong>Role.USER</strong> antes de delegar el guardado en el repositorio.
     *
     * @param user entidad de dominio <strong>User</strong> a registrar
     * @return el <strong>User</strong> persistido, con la contraseña codificada y el rol establecido
     * @throws UserExistsException si ya existe un usuario con el mismo email
     */
    @Override
    public User registerUser(User user) throws UserExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserExistsException("User already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * Obtiene un usuario a partir de su identificador de autenticación.
     * <p>
     * Normalmente, el identificador corresponde al email del usuario.
     *
     * @param username identificador del usuario (por ejemplo, su correo electrónico)
     * @return la entidad de dominio <strong>User</strong> asociada al identificador
     * @throws UserNotFoundException si no se encuentra ningún usuario con el identificador dado
     */
    @Override
    public User getUser(String username) throws UserNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

