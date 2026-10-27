package com.trading.platform.eztrade.user.adapter.mapper;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.domain.User;

/**
 * Mapper for converting between the {@link User} domain entity and the
 * {@link UserDTO} input/output DTO.
 * <p>
 * Used in the adapter layer to isolate the API representation from the domain
 * entity.
 */
public class UserMapper {

    /**
     * Converts a {@link UserDTO} object into a {@link User} domain entity.
     *
     * @param userDTO DTO object containing the user data
     * @return {@link User} instance built from the DTO data
     */
    public static User userDTOToUser(UserDTO userDTO) {
        return new User(
                userDTO.getFirstname(),
                userDTO.getLastname(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getPassword()
        );
    }

    /**
     * Converts a {@link User} domain entity into a {@link UserDTO} object.
     *
     * @param user domain entity representing the user
     * @return {@link UserDTO} instance built from the domain entity
     */
    public static UserDTO userToUserDTO(User user) {
        return new UserDTO(
                user.getName(),
                user.getSurname(),
                user.getUsernameValue(),
                user.getEmail(),
                user.getPassword()
        );
    }
}
