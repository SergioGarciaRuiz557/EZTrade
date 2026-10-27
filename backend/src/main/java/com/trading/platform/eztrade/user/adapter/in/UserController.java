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
 * REST controller for user management.
 * <p>
 * Exposes endpoints to register new users and query users by email or username.
 * The controller only adapts HTTP to use cases and uses {@link UserMapper} to
 * avoid exposing the domain model directly.
 */
@RestController
@RequestMapping("api/user")
public class UserController {

    private final RegisterUserUserCase registerUserUserCase;
    private final GetUserUserCase getUserUserCase;

    /**
     * Builds a new user controller instance.
     *
     * @param registerUserUserCase use case responsible for registering users
     * @param getUserUserCase use case responsible for retrieving users
     */
    public UserController(RegisterUserUserCase registerUserUserCase, GetUserUserCase getUserUserCase) {
        this.registerUserUserCase = registerUserUserCase;
        this.getUserUserCase = getUserUserCase;
    }

    /**
     * Registers a new user.
     * <p>
     * Receives a {@link UserDTO}, maps it to the domain model, delegates the
     * creation to the application layer, and returns the persisted user as a DTO.
     *
     * @param userDTO user data to register
     * @return HTTP response with the registered user and 201 Created status
     * @throws UserExistsException if a user with the same email or username already exists
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) throws UserExistsException {
        User user = UserMapper.userDTOToUser(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.userToUserDTO(registerUserUserCase.registerUser(user)));
    }

    /**
     * Retrieves a user by email or username.
     *
     * @param email search identifier received as a query parameter
     * @return HTTP response with the found user as a DTO
     */
    @GetMapping
    public ResponseEntity<UserDTO> getUser(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UserMapper.userToUserDTO(getUserUserCase.getUser(email)));
    }
}
