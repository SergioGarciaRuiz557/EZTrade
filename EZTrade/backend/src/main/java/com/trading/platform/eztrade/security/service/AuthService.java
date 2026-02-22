package com.trading.platform.eztrade.security.service;

import com.trading.platform.eztrade.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación de usuarios.
 * <p>
 * Se encarga de validar las credenciales recibidas y generar
 * un token JWT para el usuario autenticado.
 */
@Service
public class AuthService {

    /**
     * Componente de Spring Security encargado de realizar el proceso
     * de autenticación con las credenciales proporcionadas.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Servicio responsable de la generación de tokens JWT
     * para los usuarios autenticados.
     */
    private final JwtService jwtService;

    /**
     * Crea una nueva instancia del servicio de autenticación.
     *
     * @param authenticationManager gestor de autenticación de Spring Security
     * @param jwtService            servicio usado para generar tokens JWT
     */
    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Autentica a un usuario con su email y contraseña y genera un token JWT.
     *
     * @param email    correo electrónico del usuario
     * @param password contraseña en texto plano del usuario
     * @return token JWT asociado al usuario autenticado
     * @throws org.springframework.security.core.AuthenticationException
     *         si las credenciales no son válidas
     */
    public String login(String email, String password) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, password)
                );

        return jwtService.generateToken(
                (UserDetails) authentication.getPrincipal()
        );
    }
}


