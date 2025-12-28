package blog.code.codeblog.config.security;

import blog.code.codeblog.config.handlers.CustomAuthenticationEntryPoint;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user when token is valid")
    void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");

        User mockUser = new User();
        mockUser.setLogin("test-user");
        mockUser.setRole(null);
        when(tokenService.recoverToken(request)).thenReturn("valid-token");
        when(tokenService.validateToken("valid-token")).thenReturn("test-user");
        when(userRepository.findByLogin("test-user")).thenReturn(mockUser);

        securityFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, times(1)).validateToken("valid-token");
        verify(userRepository, times(1)).findByLogin("test-user");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when token is missing")
    void shouldSkipAuthenticationWhenTokenIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tokenService.recoverToken(request)).thenReturn(null);

        securityFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, never()).validateToken(any());
        verify(userRepository, never()).findByLogin(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle JWTVerificationException and call entry point")
    void shouldHandleJwtVerificationException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        when(tokenService.recoverToken(request)).thenReturn("invalid-token");
        when(tokenService.validateToken("invalid-token")).thenThrow(new JWTVerificationException("Token invalid"));

        securityFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(customAuthenticationEntryPoint, times(1)).commence(eq(request), eq(response), any(org.springframework.security.authentication.BadCredentialsException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle AuthenticationException and call entry point")
    void shouldHandleAuthenticationException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.recoverToken(request)).thenReturn("valid-token");
        when(tokenService.validateToken("valid-token")).thenThrow(new AuthenticationException("Auth error") {});

        securityFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(customAuthenticationEntryPoint, times(1)).commence(eq(request), eq(response), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle user not found after token validation")
    void shouldHandleUserNotFound() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.recoverToken(request)).thenReturn("valid-token");
        when(tokenService.validateToken("valid-token")).thenReturn("test-user");
        when(userRepository.findByLogin("test-user")).thenReturn(null);

        securityFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, times(1)).validateToken("valid-token");
        verify(userRepository, times(1)).findByLogin("test-user");
        verify(filterChain, times(1)).doFilter(request, response);
    }

}