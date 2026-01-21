package com.trading.platform.eztrade.user.api;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetails;

@NamedInterface
public interface LoadUserForSecurityPort {
    UserDetails loadByUsername(String username);
}

