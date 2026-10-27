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
 * Service responsible for generating, validating, and renewing JWT tokens.
 * <p>
 * Provides operations to extract token information, check token state, and
 * create new tokens signed with a symmetric secret key.
 */
@Service
public class JwtService {

    /**
     * Secret key used to sign and validate JWT tokens.
     * <p>
     * Must be kept secure and never exposed publicly.
     */
    @Value("${security.jwt.secret}")
    private String secretKey;

    /**
     * Access token validity time in milliseconds.
     * <p>
     * In this case, 24 hours.
     */
    @Value("${security.jwt.token-expiration-ms:86400000}")
    private long tokenExpirationMs;

    /**
     * Token renewal window in milliseconds after expiration.
     * <p>
     * In this case, 7 additional days.
     */
    @Value("${security.jwt.refresh-window-ms:604800000}")
    private long refreshWindowMs;

    /**
     * Extracts the username (subject) contained in the JWT token.
     *
     * @param token JWT token whose subject should be obtained
     * @return username associated with the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token          JWT token whose claims should be extracted
     * @param claimsResolver function that receives the claims and returns the desired value
     * @param <T>            returned value type
     * @return requested claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a new JWT token for the given user without additional claims.
     *
     * @param userDetails authenticated user details
     * @return signed JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a new JWT token for the given user, including additional claims.
     *
     * @param extraClaims extra claims to include in the token
     * @param userDetails authenticated user details
     * @return signed JWT token
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
     * Checks whether a token is valid for a given user.
     * <p>
     * This implementation validates that the token subject matches the provided
     * username.
     *
     * @param token       JWT token to validate
     * @param userDetails user details used as the validation target
     * @return <strong>true</strong> if the token belongs to the user, <strong>false</strong> otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()));
    }

    /**
     * Indicates whether the JWT token is expired.
     *
     * @param token JWT token to check
     * @return <strong>true</strong> if the expiration date is before the current time
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Gets the token expiration date.
     *
     * @param token JWT token
     * @return token expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims contained in the token.
     * <p>
     * If the token is expired, its associated claims are still returned.
     *
     * @param token JWT token
     * @return token claims
     * @throws RuntimeException if the token is invalid or malformed
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
     * Obtains the HMAC signing key from the encoded secret key.
     *
     * @return cryptographic key used to sign and verify tokens
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Determines whether an expired token can be renewed within the refresh window.
     *
     * @param token JWT token to check
     * @return <strong>true</strong> if the token is expired but within the renewal period
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
     * Renews a JWT token by generating a new one for the given user.
     * <p>
     * Renewal is only allowed if the original token can be renewed according to
     * the configured refresh window.
     *
     * @param token       original JWT token
     * @param userDetails user details for the new token
     * @return new signed JWT token
     * @throws IllegalArgumentException if the token cannot be renewed
     */
    public String renewToken(String token, UserDetails userDetails) {
        if (!canTokenBeRenewed(token)) {
            throw new IllegalArgumentException("The JWT couldn't be renewed");
        }
        return generateToken(userDetails);
    }
}
