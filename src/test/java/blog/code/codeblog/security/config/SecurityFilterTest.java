package blog.code.codeblog.security.config;

import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import blog.code.codeblog.service.TokenService;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar corretamente o usuário quando o token é válido")
    void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer valid-token");

        User mockUser = new User();
        mockUser.setLogin("test-user");
        mockUser.setRole(null);

        when(tokenService.validateToken("valid-token")).thenReturn("test-user");
        when(userRepository.findByLogin("test-user")).thenReturn(mockUser);


        securityFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, times(1)).validateToken("valid-token");
        verify(userRepository, times(1)).findByLogin("test-user");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve ignorar autenticação quando o token está ausente")
    void shouldSkipAuthenticationWhenTokenIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenService, never()).validateToken(any());
        verify(userRepository, never()).findByLogin(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

}