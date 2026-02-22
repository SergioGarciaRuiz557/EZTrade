/**
 * <h2>UserService</h2>
 *
 * <p><strong>Módulo:</strong> user</p>
 * <p><strong>Capa:</strong> Application</p>
 *
 * <p><strong>Responsabilidad:</strong><br/>
 * Orquesta casos de uso y coordina reglas de negocio para 'User'.</p>
 *
 * <p><strong>Rol arquitectónico:</strong><br/>
 * Forma parte de la arquitectura hexagonal del módulo, manteniendo separación
 * entre dominio, aplicación e infraestructura mediante puertos y adaptadores.
 * Está gestionado por Spring Modulith.</p>
 */

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

@Service
public class UserService implements RegisterUserUserCase, GetUserUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Ejecuta la operación principal asociada al caso de uso.
     * @param userRepository dato de entrada requerido por la operación.
     * @param passwordEncoder dato de entrada requerido por la operación.
     * @return resultado devuelto por la operación.
     */

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    /**
     * Crea una nueva instancia o recurso en el sistema.
     * @param user dato de entrada requerido por la operación.
     * @return resultado devuelto por la operación.
     */
    public User registerUser(User user) throws UserExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) throw new UserExistsException("User already exists");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);


        return userRepository.save(user);
    }

    @Override
    /**
     * Obtiene información del estado interno del objeto.
     * @param username dato de entrada requerido por la operación.
     * @return resultado devuelto por la operación.
     */
    public User getUser(String username) throws UserNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(()->new UserNotFoundException("User not found"));
    }
}
