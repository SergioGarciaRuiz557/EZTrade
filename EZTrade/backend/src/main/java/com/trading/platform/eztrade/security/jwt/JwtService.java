package com.trading.platform.eztrade.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de la generación, validación y renovación de tokens JWT.
 * <p>
 * Proporciona operaciones para extraer información del token, comprobar su estado
 * y crear nuevos tokens firmados con una clave secreta simétrica.
 */
@Service
public class JwtService {

    /**
     * Clave secreta utilizada para firmar y validar los tokens JWT.
     * <p>
     * Debe mantenerse segura y no exponerse públicamente.
     */
    @Value("${security.jwt.secret}")
    private String secretKey;

    /**
     * Tiempo de validez del token de acceso en milisegundos.
     * <p>
     * En este caso, 24 horas.
     */
    @Value("${security.jwt.token-expiration-ms:86400000}")
    private long tokenExpirationMs;

    /**
     * Ventana de renovación del token en milisegundos tras su expiración.
     * <p>
     * En este caso, 7 días adicionales.
     */
    @Value("${security.jwt.refresh-window-ms:604800000}")
    private long refreshWindowMs;

    /**
     * Extrae el nombre de usuario (subject) contenido en el token JWT.
     *
     * @param token token JWT del que se quiere obtener el subject
     * @return nombre de usuario asociado al token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim concreto del token usando una función resolutora.
     *
     * @param token          token JWT del que se quieren extraer los claims
     * @param claimsResolver función que recibe los claims y devuelve el valor deseado
     * @param <T>            tipo del valor devuelto
     * @return valor del claim solicitado
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un nuevo token JWT para el usuario indicado sin claims adicionales.
     *
     * @param userDetails detalles del usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un nuevo token JWT para el usuario indicado, incluyendo claims adicionales.
     *
     * @param extraClaims claims extra que se desean incluir en el token
     * @param userDetails detalles del usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Comprueba si un token es válido para un usuario dado.
     * <p>
     * En esta implementación se valida que el subject del token coincida con
     * el nombre de usuario proporcionado.
     *
     * @param token       token JWT a validar
     * @param userDetails detalles del usuario contra el que se valida el token
     * @return <strong>true</strong> si el token pertenece al usuario, <strong>false</strong> en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()));
    }

    /**
     * Indica si el token JWT se encuentra expirado.
     *
     * @param token token JWT a comprobar
     * @return <strong>true</strong> si la fecha de expiración es anterior al momento actual
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Obtiene la fecha de expiración del token.
     *
     * @param token token JWT
     * @return fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims contenidos en el token.
     * <p>
     * Si el token está expirado, devuelve igualmente los claims asociados.
     *
     * @param token token JWT
     * @return claims del token
     * @throws RuntimeException si el token es inválido o está mal formado
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith((SecretKey) getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (UnsupportedJwtException | MalformedJwtException | io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token or mal formed", e);
        }
    }

    /**
     * Obtiene la clave de firma HMAC a partir de la clave secreta codificada.
     *
     * @return clave criptográfica usada para firmar y verificar tokens
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Determina si un token expirado puede renovarse dentro de la ventana de refresco.
     *
     * @param token token JWT a comprobar
     * @return <strong>true</strong> si el token está expirado pero dentro del periodo de renovación
     */
    public boolean canTokenBeRenewed(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            long currentTime = System.currentTimeMillis();
            return expiration.before(new Date(currentTime)) &&
                    expiration.getTime() + refreshWindowMs > currentTime;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Renueva un token JWT generando uno nuevo para el usuario indicado.
     * <p>
     * Solo se permite la renovación si el token original puede ser renovado
     * según la ventana de refresco configurada.
     *
     * @param token       token JWT original
     * @param userDetails detalles del usuario para el nuevo token
     * @return nuevo token JWT firmado
     * @throws IllegalArgumentException si el token no puede ser renovado
     */
    public String renewToken(String token, UserDetails userDetails) {
        if (!canTokenBeRenewed(token)) {
            throw new IllegalArgumentException("The JWT couldn't be renewed");
        }
        return generateToken(userDetails);
    }
}
