package com.trading.platform.eztrade.security.configuration;

import com.trading.platform.eztrade.security.filter.JwtAuthFilter;
import com.trading.platform.eztrade.security.filter.UserAccessFilter;
import com.trading.platform.eztrade.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central application security configuration.
 * <p>
 * Defines the security filter chain, registers the JWT filter, and configures
 * authentication, authorization, and session management policies.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthenticationConfig {

    /**
     * Authentication provider used by Spring Security to validate user credentials.
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * HandlerExceptionResolver injected to delegate exceptions produced during
     * the security filtering process.
     */
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final UserAccessFilter userAccessFilter;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final List<String> allowedOrigins;

    /**
     * Creates a new authentication configuration instance.
     *
     * @param authenticationProvider authentication provider configured in the context
     * @param handlerExceptionResolver exception resolver for the JWT filter
     */
    public AuthenticationConfig(AuthenticationProvider authenticationProvider,
                                HandlerExceptionResolver handlerExceptionResolver,
                                UserAccessFilter userAccessFilter,
                                JwtService jwtService,
                                UserDetailsService userDetailsService,
                                @Value("${app.cors.allowed-origins:http://localhost:3001}") String allowedOrigins) {
        this.authenticationProvider = authenticationProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.userAccessFilter = userAccessFilter;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Defines the JWT authentication filter that runs before
     * {@link UsernamePasswordAuthenticationFilter}.
     * <p>
     * The filter uses {@link JwtService} to extract and validate the token and
     * {@link UserDetailsService} to load the associated user details.
     *
     * @return configured {@link JwtAuthFilter} instance
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtService, userDetailsService, handlerExceptionResolver);
    }

    /**
     * Configures the Spring Security filter chain.
     * <p>
     *     <ul>
     *         <li>Disables CSRF to work with JWT and stateless sessions.</li>
     *         <li>Allows public access to registration and login endpoints.</li>
     *         <li>Requires authentication for the remaining requests.</li>
     *         <li>Sets the session policy to STATELESS.</li>
     *         <li>Registers the authentication provider and JWT filter.</li>
     *     </ul>
     * @param http {@code HttpSecurity} object provided by Spring to configure HTTP security
     * @return security filter chain built with the defined configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/register",
                                "/auth/login",
                                "/ws",
                                "/ws/**"
                        ).permitAll()
                        // Protected endpoints that require an authenticated user
                        .requestMatchers(
                                "/api/user",
                                "/api/v1/market/**",
                                "/api/v1/trading/**",
                                "/api/v1/wallet/**",
                                "/api/portfolio"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userAccessFilter, JwtAuthFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
