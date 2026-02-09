package com.trading.platform.eztrade.user.adapter.mapper;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.domain.User;

/**
 * Mapper para convertir entre la entidad de dominio {@link User}
 * y el DTO de entrada/salida {@link UserDTO}.
 * <p>
 * Se utiliza en la capa de adaptadores para aislar la representación
 * expuesta por la API de la entidad de dominio.
 */
public class UserMapper {

    /**
     * Convierte un objeto {@link UserDTO} en una entidad de dominio {@link User}.
     *
     * @param userDTO objeto DTO que contiene los datos del usuario
     * @return instancia de {@link User} construida a partir de los datos del DTO
     */
    public static User userDTOToUser(UserDTO userDTO) {
        return new User(
                userDTO.getFirstname(),
                userDTO.getLastname(),
                userDTO.getEmail(),
                userDTO.getPassword()
        );
    }

    /**
     * Convierte una entidad de dominio {@link User} en un objeto {@link UserDTO}.
     *
     * @param user entidad de dominio que representa al usuario
     * @return instancia de {@link UserDTO} construida a partir de la entidad de dominio
     */
    public static UserDTO userToUserDTO(User user) {
        return new UserDTO(
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPassword()
        );
    }
}
