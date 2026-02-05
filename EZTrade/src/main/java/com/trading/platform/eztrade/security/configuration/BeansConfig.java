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

/**
 * Configuración de beans relacionados con la autenticación y la seguridad.
 * <p>
 * Define los componentes necesarios para la autenticación basada en
 * {@code UserDetailsService}, la gestión de autenticación y el cifrado
 * de contraseñas mediante BCrypt.
 */
@Configuration
public class BeansConfig {

    /**
     * Expone un {@code UserDetailsService} basado en el {@code JwtAuthenticationProvider}.
     * <p>
     * Delegará la carga de usuarios en el método {@code loadByUsername} del
     * proveedor JWT.
     *
     * @param jwtAuthenticationProvider proveedor de autenticación JWT usado para cargar usuarios
     * @return implementación de {@code UserDetailsService} utilizada por Spring Security
     */
    @Bean
    public UserDetailsService userDetailsService(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return jwtAuthenticationProvider::loadByUsername;
    }

    /**
     * Configura el {@code AuthenticationProvider} principal de la aplicación.
     * <p>
     * Utiliza un {@code DaoAuthenticationProvider} que delega en el
     * {@code UserDetailsService} definido y emplea BCrypt como algoritmo
     * de cifrado de contraseñas.
     *
     * @param jwtAuthenticationProvider proveedor de autenticación JWT para el servicio de usuarios
     * @return proveedor de autenticación configurado para validar credenciales
     */
    @Bean
    public AuthenticationProvider authenticationProvider(JwtAuthenticationProvider jwtAuthenticationProvider) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService(jwtAuthenticationProvider));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el {@code AuthenticationManager} que coordina el proceso de autenticación.
     * <p>
     * Se obtiene a partir de la configuración automática de Spring Security.
     *
     * @param config configuración de autenticación proporcionada por Spring
     * @return gestor de autenticación utilizado por el contexto de seguridad
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Define el codificador de contraseñas utilizado por la aplicación.
     * <p>
     * Emplea el algoritmo {@code BCryptPasswordEncoder} para almacenar
     * las contraseñas de forma segura.
     *
     * @return codificador de contraseñas basado en BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

