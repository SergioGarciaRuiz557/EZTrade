package com.trading.platform.eztrade.security.service;

import com.trading.platform.eztrade.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login autentica al usuario y devuelve el token generado")
    void login_authenticatesAndReturnsToken() {
        String email = "john.doe@test.com";
        String password = "pwd123";
        String expectedToken = "token-jwt";

        UserDetails userDetails = new User(email, password, List.of());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtService.generateToken(userDetails)).willReturn(expectedToken);

        String token = authService.login(email, password);

        assertThat(token).isEqualTo(expectedToken);
    }
}
