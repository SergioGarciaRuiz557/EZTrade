package com.trading.platform.eztrade.security.filter;

import com.trading.platform.eztrade.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
        @Autowired
        private JwtService jwtService;
        @Autowired
        private UserDetailsService userDetailsService;
        private final HandlerExceptionResolver handlerExceptionResolver;

        @Autowired
        public JwtAuthFilter(HandlerExceptionResolver handlerExceptionResolver) {
            this.handlerExceptionResolver = handlerExceptionResolver;
        }

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
                boolean canBeRenewed = jwtService.canTokenBeRenewed(jwt);

                if (!isTokenValid || (isTokenExpired && !canBeRenewed)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                if (isTokenExpired) {
                    String newToken = jwtService.renewToken(jwt, userDetails);
                    response.setHeader("Authorization", "Bearer " + newToken);
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
