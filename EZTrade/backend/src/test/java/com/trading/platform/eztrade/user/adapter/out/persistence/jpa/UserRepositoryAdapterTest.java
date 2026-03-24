package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @InjectMocks
    private UserRepository userRepositoryAdapter;

    @Test
    @DisplayName("findByEmail delega en JpaUserRepository")
    void findByEmail_delegatesToJpaRepository() {
        String email = "john.doe@test.com";
        User user = new User("John", "Doe", "johnny", email, "pwd");

        given(jpaUserRepository.findByEmail(eq(email))).willReturn(Optional.of(user));

        Optional<User> result = userRepositoryAdapter.findByEmail(email);

        assertThat(result).contains(user);
        verify(jpaUserRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByUsername delega en JpaUserRepository")
    void findByUsername_delegatesToJpaRepository() {
        String username = "johnny";
        User user = new User("John", "Doe", username, "john.doe@test.com", "pwd");

        given(jpaUserRepository.findByUsername(eq(username))).willReturn(Optional.of(user));

        Optional<User> result = userRepositoryAdapter.findByUsername(username);

        assertThat(result).contains(user);
        verify(jpaUserRepository).findByUsername(username);
    }

    @Test
    @DisplayName("save delega en JpaUserRepository")
    void save_delegatesToJpaRepository() {
        User user = new User("John", "Doe", "johnny", "john.doe@test.com", "pwd");

        given(jpaUserRepository.save(user)).willReturn(user);

        User result = userRepositoryAdapter.save(user);

        assertThat(result).isSameAs(user);
        verify(jpaUserRepository).save(user);
    }
}
