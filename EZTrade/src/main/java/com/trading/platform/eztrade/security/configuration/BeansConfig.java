package com.trading.platform.eztrade.security.configuration;

import com.trading.platform.eztrade.security.jwt.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration

public class BeansConfig {

    @Bean
    public UserDetailsService userDetailsService( JwtAuthenticationProvider  jwtAuthenticationProvider) {
        return jwtAuthenticationProvider::loadByUsername;
    }


    @Bean
    public AuthenticationProvider authenticationProvider(JwtAuthenticationProvider jwtAuthenticationProvider) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService(jwtAuthenticationProvider));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
