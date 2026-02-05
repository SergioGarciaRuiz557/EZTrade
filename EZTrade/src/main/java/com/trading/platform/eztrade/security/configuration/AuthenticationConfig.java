package com.trading.platform.eztrade.security.configuration;

import com.trading.platform.eztrade.security.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Configuración central de seguridad de la aplicación.
 * <p>
 * Define la cadena de filtros de seguridad, registra el filtro JWT y configura
 * las políticas de autenticación, autorización y gestión de sesiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthenticationConfig {

    /**
     * Proveedor de autenticación utilizado por Spring Security
     * para validar las credenciales de los usuarios.
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * HandlerExceptionResolver inyectado para delegar el manejo de
     * excepciones producidas durante el proceso de filtrado de seguridad.
     */
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Crea una nueva instancia de la configuración de autenticación.
     *
     * @param authenticationProvider proveedor de autenticación configurado en el contexto
     * @param handlerExceptionResolver resolvedor de excepciones para el filtro JWT
     */
    public AuthenticationConfig(AuthenticationProvider authenticationProvider,
                                HandlerExceptionResolver handlerExceptionResolver) {
        this.authenticationProvider = authenticationProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * Define el filtro de autenticación JWT que se ejecutará antes del
     * filtro UsernamePasswordAuthenticationFilter.
     *
     * @return instancia configurada de JwtAuthFilter
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(handlerExceptionResolver);
    }

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     * <p>
     *     <ul>
     *         <li>Deshabilita CSRF para trabajar con JWT y sesiones sin estado.</li>
     *         <li>Permite el acceso público a los endpoints de registro y login.</li>
     *         <li>Exige autenticación para el resto de peticiones.</li>
     *         <li>Establece la política de sesión como STATELESS.</li>
     *         <li>Registra el proveedor de autenticación y el filtro JWT.</li>
     *
     *
     *     </ul>
     * @param http objeto HttpSecurity proporcionado por Spring para configurar la seguridad HTTP
     * @return la cadena de filtros de seguridad construida con la configuración definida
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/register",
                                "/auth/login"
                        ).permitAll()
                        .requestMatchers(
                                "/api/user"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}


