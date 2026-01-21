package com.trading.platform.eztrade.security.jwt;

import com.trading.platform.eztrade.user.api.LoadUserForSecurityPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider {

    private final LoadUserForSecurityPort userPort;

    public JwtAuthenticationProvider(LoadUserForSecurityPort userPort) {
        this.userPort = userPort;
    }


    public UserDetails loadByUsername(String s) {
        return userPort.loadByUsername(s);
    }
}
