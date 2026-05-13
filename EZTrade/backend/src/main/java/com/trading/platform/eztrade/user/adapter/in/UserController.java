package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.adapter.mapper.UserMapper;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestion de usuarios.
 * <p>
 * Expone endpoints para registrar nuevos usuarios y consultar usuarios por
 * email o username. El controlador solo adapta HTTP a casos de uso y usa
 * {@link UserMapper} para no exponer directamente el modelo de dominio.
 */
@RestController
@RequestMapping("api/user")
public class UserController {

    private final RegisterUserUserCase registerUserUserCase;
    private final GetUserUserCase getUserUserCase;

    /**
     * Construye una nueva instancia del controlador de usuarios.
     *
     * @param registerUserUserCase caso de uso responsable de registrar usuarios
     * @param getUserUserCase caso de uso responsable de obtener usuarios
     */
    public UserController(RegisterUserUserCase registerUserUserCase, GetUserUserCase getUserUserCase) {
        this.registerUserUserCase = registerUserUserCase;
        this.getUserUserCase = getUserUserCase;
    }

    /**
     * Registra un nuevo usuario.
     * <p>
     * Recibe un {@link UserDTO}, lo transforma a dominio, delega el alta en la
     * aplicacion y devuelve el usuario persistido en formato DTO.
     *
     * @param userDTO datos del usuario a registrar
     * @return respuesta HTTP con el usuario registrado y estado 201 Created
     * @throws UserExistsException si ya existe un usuario con el mismo email o username
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) throws UserExistsException {
        User user = UserMapper.userDTOToUser(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.userToUserDTO(registerUserUserCase.registerUser(user)));
    }

    /**
     * Obtiene un usuario por email o username.
     *
     * @param email identificador de busqueda recibido como query param
     * @return respuesta HTTP con el usuario encontrado en formato DTO
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUser(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.userToUserDTO(getUserUserCase.getUser(email)));
    }
}
