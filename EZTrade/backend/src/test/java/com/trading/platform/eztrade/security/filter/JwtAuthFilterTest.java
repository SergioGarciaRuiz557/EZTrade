package com.trading.platform.eztrade.security.filter;

import com.trading.platform.eztrade.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;


    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("doFilterInternal sin cabecera Authorization delega al siguiente filtro y no autentica")
    void doFilterInternal_withoutAuthorizationHeader_delegatesFilterChain() throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        given(request.getHeader("Authorization")).willReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal con token válido carga el usuario y delega en la cadena")
    void doFilterInternal_withValidToken_loadsUserAndDelegates() throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        String token = "jwt-token";
        String email = "john.doe@test.com";
        UserDetails userDetails = new User(email, "pwd123", List.of());

        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtService.extractUsername(token)).willReturn(email);
        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
        given(jwtService.isTokenValid(token, userDetails)).willReturn(true);
        given(jwtService.isTokenExpired(token)).willReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verificamos la interacción con los colaboradores clave
        verify(jwtService).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(email);
        verify(jwtService).isTokenValid(token, userDetails);
        verify(jwtService).isTokenExpired(token);
        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(handlerExceptionResolver);
    }
}
