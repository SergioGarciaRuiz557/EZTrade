package com.trading.platform.eztrade.user.adapter.mapper;

import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    @DisplayName("userDTOToUser mapea correctamente de UserDTO a User")
    void userDTOToUser_mapsCorrectly() {
        UserDTO dto = new UserDTO("John", "Doe", "john.doe@test.com", "pwd123");

        User user = UserMapper.userDTOToUser(dto);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getSurname()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(user.getPassword()).isEqualTo("pwd123");
    }

    @Test
    @DisplayName("userToUserDTO mapea correctamente de User a UserDTO")
    void userToUserDTO_mapsCorrectly() {
        User user = new User("John", "Doe", "john.doe@test.com", "pwd123");

        UserDTO dto = UserMapper.userToUserDTO(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getFirstname()).isEqualTo("John");
        assertThat(dto.getLastname()).isEqualTo("Doe");
        assertThat(dto.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(dto.getPassword()).isEqualTo("pwd123");
    }
}
