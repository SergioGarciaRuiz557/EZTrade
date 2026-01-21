package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.adapter.mapper.UserMapper;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("api/user")
public class UserController {
    private final RegisterUserUserCase registerUserUserCase;

    public UserController(RegisterUserUserCase registerUserUserCase) {
        this.registerUserUserCase = registerUserUserCase;
    }
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) throws UserExistsException {
        User user = UserMapper.userDTOToUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.userToUserDTO(registerUserUserCase.registerUser(user)));
    }
}
