package com.trading.platform.eztrade.user.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExistsExceptionTest {

    @Test
    @DisplayName("UserExistsException almacena correctamente el mensaje")
    void userExistsException_storesMessage() {
        UserExistsException ex = new UserExistsException("User already exists");

        assertThat(ex)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists");
    }
}
