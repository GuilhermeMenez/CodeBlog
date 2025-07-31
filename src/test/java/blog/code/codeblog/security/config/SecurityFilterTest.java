package blog.code.codeblog.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

class SecurityFilterTest {

    private SecurityFilter securityFilter;
    private FilterChain filterChain;

    @BeforeEach
    @DisplayName("Configuração inicial do SecurityFilter")
    void setup() {
        securityFilter = new SecurityFilter();
        filterChain = Mockito.mock(FilterChain.class);
    }

    @Test
    @DisplayName("Executa o filtro com uma requisição válida e deve prosseguir no fluxo")
    void doFilterInternal_withValidRequest_shouldProceed() throws ServletException, IOException {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}