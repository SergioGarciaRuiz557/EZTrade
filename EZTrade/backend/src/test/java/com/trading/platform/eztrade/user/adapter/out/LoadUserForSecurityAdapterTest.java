package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoadUserForSecurityAdapterTest {

    @Mock
    private GetUserUserCase getUserUserCase;

    @InjectMocks
    private LoadUserForSecurityAdapter adapter;

    @Test
    @DisplayName("loadByUsername devuelve UserDetails con username, password y authorities")
    void loadByUsername_returnsUserDetails() {
        String email = "john.doe@test.com";
        User user = new User("John", "Doe", email, "encodedPwd");
        user.setRole(Role.USER);

        given(getUserUserCase.getUser(eq(email))).willReturn(user);

        UserDetails details = adapter.loadByUsername(email);

        assertThat(details.getUsername()).isEqualTo(email);
        assertThat(details.getPassword()).isEqualTo("encodedPwd");
        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertThat(authorities).extracting("authority").contains("ROLE_USER");
    }

    @Test
    @DisplayName("loadByUsername propaga excepción cuando el usuario no existe")
    void loadByUsername_propagatesExceptionWhenUserNotFound() {
        String email = "missing@test.com";

        given(getUserUserCase.getUser(eq(email))).willThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> adapter.loadByUsername(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }
}
