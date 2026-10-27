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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Bean configuration related to authentication and security.
 * <p>
 * Defines the components required for {@code UserDetailsService}-based
 * authentication, authentication management, and password encryption with BCrypt.
 */
@Configuration
public class BeansConfig {

    /**
     * Exposes a {@code UserDetailsService} based on the {@code JwtAuthenticationProvider}.
     * <p>
     * Delegates user loading to the JWT provider's {@code loadByUsername} method.
     *
     * @param jwtAuthenticationProvider JWT authentication provider used to load users
     * @return {@code UserDetailsService} implementation used by Spring Security
     */
    @Bean
    public UserDetailsService userDetailsService(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return jwtAuthenticationProvider::loadByUsername;
    }

    /**
     * Configures the application's main {@code AuthenticationProvider}.
     * <p>
     * Uses a {@code DaoAuthenticationProvider} that delegates to the configured
     * {@code UserDetailsService} and uses BCrypt as the password encryption algorithm.
     *
     * @param jwtAuthenticationProvider JWT authentication provider for the user service
     * @return authentication provider configured to validate credentials
     */
    @Bean
    public AuthenticationProvider authenticationProvider(JwtAuthenticationProvider jwtAuthenticationProvider) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService(jwtAuthenticationProvider));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the {@code AuthenticationManager} that coordinates the authentication process.
     * <p>
     * Obtained from Spring Security's automatic configuration.
     *
     * @param config authentication configuration provided by Spring
     * @return authentication manager used by the security context
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the password encoder used by the application.
     * <p>
     * Uses the {@code BCryptPasswordEncoder} algorithm to store passwords securely.
     *
     * @return BCrypt-based password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Reusable security expression service.
     * Allows checking whether the authenticated user is an administrator or the
     * same user referenced by email.
     */
    @Bean
    public SecurityPermissionEvaluator securityPermissionEvaluator() {
        return new SecurityPermissionEvaluator();
    }

    public static class SecurityPermissionEvaluator {
        public boolean isAdminOrSameUser(Authentication authentication, String email) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                return false;
            }
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            boolean isSameUser = email != null && email.equalsIgnoreCase(userDetails.getUsername());
            return isAdmin || isSameUser;
        }
    }
}
