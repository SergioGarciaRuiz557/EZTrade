package com.trading.platform.eztrade.user.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotFoundExceptionTest {

    @Test
    @DisplayName("UserNotFoundException almacena correctamente el mensaje")
    void userNotFoundException_storesMessage() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        assertThat(ex)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
