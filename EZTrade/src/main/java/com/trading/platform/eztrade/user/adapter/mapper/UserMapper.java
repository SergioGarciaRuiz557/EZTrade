package com.trading.platform.eztrade.user.adapter.mapper;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.domain.User;

public class UserMapper {
    public static User userDTOToUser(UserDTO userDTO) {
       return new User(userDTO.getFirstname(), userDTO.getLastname(), userDTO.getEmail(), userDTO.getPassword());

    }

    public static UserDTO userToUserDTO(User user) {
        return new UserDTO(user.getName(), user.getSurname(),  user.getEmail(), user.getPassword());
    }
}
