package com.trading.platform.eztrade.user.application.services;

import com.trading.platform.eztrade.user.application.ports.out.UserRepository;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("registerUser crea un usuario nuevo cuando el email no existe")
    void registerUser_createsNewUser_whenEmailDoesNotExist() {
        User user = new User("John", "Doe", "john.doe@test.com", "plainPwd");

        given(userRepository.findByEmail(eq("john.doe@test.com"))).willReturn(Optional.empty());
        given(passwordEncoder.encode("plainPwd")).willReturn("encodedPwd");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(user);

        verify(userRepository).findByEmail("john.doe@test.com");
        verify(passwordEncoder).encode("plainPwd");
        verify(userRepository).save(user);

        assertThat(result.getPassword()).isEqualTo("encodedPwd");
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("registerUser lanza UserExistsException cuando el email ya existe")
    void registerUser_throwsUserExistsException_whenEmailAlreadyExists() {
        User existing = new User("John", "Doe", "john.doe@test.com", "pwd");
        User incoming = new User("John", "Doe", "john.doe@test.com", "plainPwd");

        given(userRepository.findByEmail("john.doe@test.com")).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.registerUser(incoming))
                .isInstanceOf(UserExistsException.class)
                .hasMessage("User already exists");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUser devuelve el usuario cuando existe")
    void getUser_returnsUser_whenExists() {
        String email = "john.doe@test.com";
        User user = new User("John", "Doe", email, "pwd");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User result = userService.getUser(email);

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUser lanza UserNotFoundException cuando el usuario no existe")
    void getUser_throwsUserNotFoundException_whenUserDoesNotExist() {
        String email = "does.not.exist@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(email);
    }
}
