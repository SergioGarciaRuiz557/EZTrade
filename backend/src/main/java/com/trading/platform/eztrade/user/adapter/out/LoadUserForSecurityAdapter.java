package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.domain.Role;
import com.trading.platform.eztrade.user.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter that translates the domain user into the representation required by
 * Spring Security. The domain model does not implement {@code UserDetails}.
 */
@Component
class LoadUserForSecurityAdapter implements LoadUserForSecurityPort {

    private final GetUserUserCase getUserUserCase;

    LoadUserForSecurityAdapter(GetUserUserCase getUserUserCase) {
        this.getUserUserCase = getUserUserCase;
    }

    @Override
    public UserDetails loadByUsername(String username) {
        User user = getUserUserCase.getUser(username);
        Role role = user.getRole() == null ? Role.USER : user.getRole();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
