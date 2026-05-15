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
 * Servicio de aplicacion del modulo user.
 * <p>
 * Implementa los casos de uso de registro y consulta. Coordina el repositorio
 * de usuarios, codifica la password antes de persistir y aplica reglas basicas
 * como unicidad de email/username y rol por defecto.
 */
@Service
public class UserService implements RegisterUserUserCase, GetUserUserCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea el servicio con sus dependencias de aplicacion e infraestructura.
     *
     * @param userRepository puerto de salida para persistencia y busqueda
     * @param passwordEncoder componente de seguridad para codificar passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un usuario nuevo.
     * <p>
     * Valida que email y username no existan, codifica la password recibida y
     * asigna el rol funcional por defecto {@link Role#USER}.
     *
     * @throws UserExistsException si email o username ya estan ocupados
     */
    @Override
    public User registerUser(User user) throws UserExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserExistsException("User already exists");
        }
        if (userRepository.findByUsername(user.getUsernameValue()).isPresent()) {
            throw new UserExistsException("User already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * Busca un usuario por email o username.
     *
     * @param username identificador escrito por el usuario en login/consulta
     * @return usuario de dominio encontrado
     * @throws UserNotFoundException si no existe ningun usuario con ese email o username
     */
    @Override
    public User getUser(String username) throws UserNotFoundException {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
