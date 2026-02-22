package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlingAdviceTest {

    private final ExceptionHandlingAdvice advice = new ExceptionHandlingAdvice();

    @Test
    @DisplayName("UserExistsException devuelve ProblemDetail con 409 y mensaje correcto")
    void userExistsException_returnsProperProblemDetail() {
        UserExistsException ex = new UserExistsException("User already exists");

        ProblemDetail pd = advice.UserExistsException(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getTitle()).isEqualTo("User already exists");
        assertThat(pd.getDetail()).isEqualTo("User already exists");
    }
}
