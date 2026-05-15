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
import static org.mockito.ArgumentMatchers.any;
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
        UserJpaEntity entity = new UserJpaEntity(1L, "John", "Doe", "johnny", email, "pwd", null);

        given(jpaUserRepository.findByEmail(eq(email))).willReturn(Optional.of(entity));

        Optional<User> result = userRepositoryAdapter.findByEmail(email);

        assertThat(result).map(User::getEmail).contains(email);
        verify(jpaUserRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByUsername delega en JpaUserRepository")
    void findByUsername_delegatesToJpaRepository() {
        String username = "johnny";
        UserJpaEntity entity = new UserJpaEntity(1L, "John", "Doe", username, "john.doe@test.com", "pwd", null);

        given(jpaUserRepository.findByUsername(eq(username))).willReturn(Optional.of(entity));

        Optional<User> result = userRepositoryAdapter.findByUsername(username);

        assertThat(result).map(User::getUsernameValue).contains(username);
        verify(jpaUserRepository).findByUsername(username);
    }

    @Test
    @DisplayName("save delega en JpaUserRepository")
    void save_delegatesToJpaRepository() {
        User user = new User("John", "Doe", "johnny", "john.doe@test.com", "pwd");
        UserJpaEntity saved = new UserJpaEntity(1L, "John", "Doe", "johnny", "john.doe@test.com", "pwd", null);

        given(jpaUserRepository.save(any(UserJpaEntity.class))).willReturn(saved);

        User result = userRepositoryAdapter.save(user);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
        verify(jpaUserRepository).save(any(UserJpaEntity.class));
    }
}
