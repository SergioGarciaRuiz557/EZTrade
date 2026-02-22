package com.trading.platform.eztrade.security.filter;

import com.trading.platform.eztrade.security.configuration.BeansConfig.SecurityPermissionEvaluator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autorización específico para el endpoint GET /api/user.
 * <p>
 * Aplica la regla:
 * <ul>
 *     <li>Permitir acceso si el usuario autenticado tiene rol ADMIN.</li>
 *     <li>Permitir acceso si el email solicitado coincide con el usuario autenticado.</li>
 *     <li>En caso contrario, devolver 403 FORBIDDEN antes de llegar al controlador.</li>
 * </ul>
 * De este modo, el módulo {@code user} no necesita depender de Spring Security.
 */
@Component
public class UserAccessFilter extends OncePerRequestFilter {

    private final SecurityPermissionEvaluator permissionEvaluator;

    public UserAccessFilter(SecurityPermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("/api/user".equals(path) && "GET".equalsIgnoreCase(method)) {
            String email = request.getParameter("email");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!permissionEvaluator.isAdminOrSameUser(authentication, email)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
