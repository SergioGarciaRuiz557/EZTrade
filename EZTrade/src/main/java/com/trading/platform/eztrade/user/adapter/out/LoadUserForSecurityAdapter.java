package com.trading.platform.eztrade.user.adapter.out;

import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
class LoadUserForSecurityAdapter implements LoadUserForSecurityPort {
    private final GetUserUserCase getUserUserCase;

    LoadUserForSecurityAdapter(GetUserUserCase getUserUserCase) {
        this.getUserUserCase = getUserUserCase;
    }

    @Override
    public UserDetails loadByUsername(String username) {
        return getUserUserCase.getUser(username);
    }
}

