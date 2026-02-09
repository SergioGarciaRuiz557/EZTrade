package com.trading.platform.eztrade.security.filter;

import com.trading.platform.eztrade.security.configuration.BeansConfig.SecurityPermissionEvaluator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccessFilterTest {

    @Mock
    private SecurityPermissionEvaluator permissionEvaluator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserAccessFilter userAccessFilter;

    @Test
    @DisplayName("GET /api/user con permiso permitido continua la cadena de filtros")
    void doFilterInternal_allowsWhenEvaluatorReturnsTrue() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john.doe@test.com", "pwd")
        );

        given(request.getRequestURI()).willReturn("/api/user");
        given(request.getMethod()).willReturn("GET");
        given(request.getParameter("email")).willReturn("john.doe@test.com");
        given(permissionEvaluator.isAdminOrSameUser(any(Authentication.class), any(String.class)))
                .willReturn(true);

        userAccessFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/user sin permiso devuelve 403 y no continua la cadena")
    void doFilterInternal_forbidsWhenEvaluatorReturnsFalse() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john.doe@test.com", "pwd")
        );

        given(request.getRequestURI()).willReturn("/api/user");
        given(request.getMethod()).willReturn("GET");
        given(request.getParameter("email")).willReturn("other@test.com");
        given(permissionEvaluator.isAdminOrSameUser(any(Authentication.class), any(String.class)))
                .willReturn(false);

        userAccessFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }
}
