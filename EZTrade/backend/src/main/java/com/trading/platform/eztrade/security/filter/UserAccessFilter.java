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
 * Authorization filter specific to the GET /api/user endpoint.
 * <p>
 * Applies the rule:
 * <ul>
 *     <li>Allow access if the authenticated user has the ADMIN role.</li>
 *     <li>Allow access if the requested email matches the authenticated user.</li>
 *     <li>Otherwise, return 403 FORBIDDEN before reaching the controller.</li>
 * </ul>
 * This way, the {@code user} module does not need to depend on Spring Security.
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
