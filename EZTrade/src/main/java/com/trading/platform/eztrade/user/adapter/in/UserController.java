package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.adapter.mapper.UserMapper;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Expone endpoints para registrar nuevos usuarios y obtener usuarios por email.
 * Utiliza casos de uso de la capa de aplicación y un mapper para convertir
 * entre entidades de dominio y DTOs.
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
     * @param getUserUserCase      caso de uso responsable de obtener usuarios por email
     */
    public UserController(RegisterUserUserCase registerUserUserCase, GetUserUserCase getUserUserCase) {
        this.registerUserUserCase = registerUserUserCase;
        this.getUserUserCase = getUserUserCase;
    }

    /**
     * Registra un nuevo usuario.
     * <p>
     * Recibe un <strong>UserDTO</strong>> en el cuerpo de la petición, lo mapea a entidad de dominio,
     * delega el registro al caso de uso y devuelve el usuario registrado en formato DTO.
     *
     * @param userDTO datos del usuario a registrar
     * @return respuesta HTTP con el usuario registrado en formato DTO y estado <strong>201 CREATED</strong>
     * @throws UserExistsException si ya existe un usuario con los mismos datos (por ejemplo, email)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) throws UserExistsException {
        User user = UserMapper.userDTOToUser(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.userToUserDTO(registerUserUserCase.registerUser(user)));
    }

    /**
     * Obtiene un usuario a partir de su email.
     * <p>
     * Delegará la obtención del usuario al caso de uso correspondiente y
     * devolverá los datos en formato DTO.
     *
     * @param email email del usuario a buscar
     * @return respuesta HTTP con el usuario encontrado en formato DTO y estado <strong>200 OK</strong>`
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUser(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.userToUserDTO(getUserUserCase.getUser(email)));
    }
}
