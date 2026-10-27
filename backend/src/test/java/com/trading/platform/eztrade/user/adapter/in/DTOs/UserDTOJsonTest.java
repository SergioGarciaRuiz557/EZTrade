package com.trading.platform.eztrade.user.adapter.in.DTOs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDTOJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Se serializa UserDTO a JSON con los campos esperados")
    void serializeUserDTO() throws Exception {
        UserDTO dto = new UserDTO("John", "Doe", "johnny", "john.doe@test.com", "pwd123");

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"firstname\":\"John\"");
        assertThat(json).contains("\"lastname\":\"Doe\"");
        assertThat(json).contains("\"username\":\"johnny\"");
        assertThat(json).contains("\"email\":\"john.doe@test.com\"");
        assertThat(json).contains("\"password\":\"pwd123\"");
    }

    @Test
    @DisplayName("Se deserializa JSON a UserDTO con los valores correctos")
    void deserializeUserDTO() throws Exception {
        String json = "{\"firstname\":\"John\",\"lastname\":\"Doe\",\"username\":\"johnny\",\"email\":\"john.doe@test.com\",\"password\":\"pwd123\"}";

        UserDTO dto = mapper.readValue(json, UserDTO.class);

        assertThat(dto.getFirstname()).isEqualTo("John");
        assertThat(dto.getLastname()).isEqualTo("Doe");
        assertThat(dto.getUsername()).isEqualTo("johnny");
        assertThat(dto.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(dto.getPassword()).isEqualTo("pwd123");
    }
}
