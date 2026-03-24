package com.trading.platform.eztrade.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "ZmFrZS1kZXYtand0LXNlY3JldC1yZXBsYWNlLWluLXByb2Qtb25seS0xMjM0NTY3ODkw");
        ReflectionTestUtils.setField(jwtService, "tokenExpirationMs", 86_400_000L);
        ReflectionTestUtils.setField(jwtService, "refreshWindowMs", 604_800_000L);
    }

    private UserDetails sampleUser() {
        return new User("john.doe@test.com", "pwd123",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("generateToken y extractUsername funcionan de forma coherente")
    void generateToken_and_extractUsername_areConsistent() {
        UserDetails user = sampleUser();

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("isTokenValid devuelve true cuando el token pertenece al usuario")
    void isTokenValid_returnsTrueForMatchingUser() {
        UserDetails user = sampleUser();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid devuelve false cuando el token no pertenece al usuario")
    void isTokenValid_returnsFalseForDifferentUser() {
        UserDetails user = sampleUser();
        UserDetails other = new User("other@test.com", "pwd123",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("renewToken lanza excepción si el token no puede renovarse")
    void renewToken_throwsWhenCannotBeRenewed() {
        UserDetails user = sampleUser();
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.renewToken(token, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The JWT couldn't be renewed");
    }
}
