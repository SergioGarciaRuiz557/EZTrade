package com.trading.platform.eztrade.security.controller;

import com.trading.platform.eztrade.security.dto.JwtResponse;
import com.trading.platform.eztrade.security.dto.LoginRequest;
import com.trading.platform.eztrade.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST responsable de gestionar las operaciones de autenticación.
 * <p>
 * Expone el endpoint de inicio de sesión que valida las credenciales del usuario
 * y devuelve un token JWT en caso de autenticación exitosa.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Servicio de autenticación encargado de la lógica de login
     * y generación del token JWT.
     */
    private final AuthService authService;

    /**
     * Crea una nueva instancia del controlador de autenticación.
     *
     * @param authService servicio de autenticación utilizado para validar credenciales
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint de inicio de sesión.
     * <p>
     * Recibe las credenciales del usuario, delega la autenticación en
     * el {@code AuthService} y, si es correcta, devuelve un token JWT.
     *
     * @param request objeto con el identificador (email o username) y la contraseña del usuario
     * @return respuesta HTTP 200 con el token JWT en el cuerpo
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request) {

        String token = authService.login(
                request.getIdentifier(),
                request.getPassword()
        );

        return ResponseEntity.ok(new JwtResponse(token));
    }
}

