package com.trading.platform.eztrade.security.filter;

import com.trading.platform.eztrade.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;

/**
 * Security filter responsible for processing and validating the JWT token in each request.
 * <p>
 * Extracts the token from the <strong>Authorization</strong> header, validates
 * its state, renews it when appropriate, and sets the authentication in the
 * <strong>SecurityContext</strong> when it is valid.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Service responsible for generating, validating, and renewing JWT tokens.
     */
    private final JwtService jwtService;

    /**
     * Service used to load the user details associated with the token.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Centralized exception resolver used to delegate errors produced during filtering.
     */
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Creates a new JWT filter instance.
     *
     * @param jwtService            JWT token management service (extraction and validation)
     * @param userDetailsService    service used to load the user details associated with the token
     * @param handlerExceptionResolver component used to resolve exceptions during filter execution
     */
    @Autowired
    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsService userDetailsService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * Applies filtering logic to authenticate JWT-based requests.
     * <p>
     * Main steps:
     * <ul>
     *   <li>Extract the {@code Authorization} header.</li>
     *   <li>Obtain the JWT token and user email.</li>
     *   <li>Validate the token and, if it is expired but renewable, generate a new one.</li>
     *   <li>Set authentication in the security context when the token is valid.</li>
     *   <li>Delegate exception handling to the {@code HandlerExceptionResolver}.</li>
     * </ul>
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain Spring Security filter chain
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            jwt = authHeader.substring(7);

            userEmail = jwtService.extractUsername(jwt);

            if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            boolean isTokenValid = jwtService.isTokenValid(jwt, userDetails);
            boolean isTokenExpired = jwtService.isTokenExpired(jwt);

            if (!isTokenValid || isTokenExpired) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }


            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

        filterChain.doFilter(request, response);

    }
}

