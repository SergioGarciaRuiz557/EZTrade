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

/**
 * Filtro de seguridad encargado de procesar y validar el token JWT en cada petición.
 * <p>
 * Extrae el token de la cabecera <strong>Authorization</strong>, valida su estado,
 * renueva el token cuando procede y, en caso de ser válido, establece
 * la autenticación en el <strong>SecurityContext</strong>.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Servicio responsable de la generación, validación y renovación de tokens JWT.
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Servicio usado para cargar los detalles del usuario asociado al token.
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Resolver centralizado de excepciones para delegar el manejo de errores
     * producidos durante el filtrado.
     */
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Crea una nueva instancia del filtro JWT.
     *
     * @param handlerExceptionResolver componente usado para resolver excepciones
     *                                 durante la ejecución del filtro
     */
    @Autowired
    public JwtAuthFilter(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * Aplica la lógica de filtrado para autenticar peticiones basadas en JWT.
     * <p>
     * Pasos principales:
     * <ul>
     *   <li>Extraer la cabecera {@code Authorization}.</li>
     *   <li>Obtener el token JWT y el email de usuario.</li>
     *   <li>Validar el token y, si está caducado pero puede renovarse, generar uno nuevo.</li>
     *   <li>Establecer la autenticación en el contexto de seguridad cuando el token es válido.</li>
     *   <li>Delegar el manejo de excepciones en el {@code HandlerExceptionResolver}.</li>
     * </ul>
     *
     * @param request     petición HTTP entrante
     * @param response    respuesta HTTP saliente
     * @param filterChain cadena de filtros de Spring Security
     * @throws ServletException si ocurre un error a nivel de servlet
     * @throws IOException      si se produce un error de E/S durante el filtrado
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

