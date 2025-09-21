package blog.code.codeblog.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {



    @Mock
    private AuthenticationConfiguration authenticationConfiguration;


    @InjectMocks
    private SecurityConfiguration securityConfiguration;

    @Test
   @DisplayName("Deve retornar uma instancia de BCrypt")
    void passwordEncoderReturnsBCryptInstance() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    @DisplayName("deve retornar um AuthenticationManager")
    void ReturnauthenticationManager() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager manager = securityConfiguration.authenticationManager(authenticationConfiguration);
        assertNotNull(manager);
        assertEquals(mockManager, manager);
    }
//    @Test
//    @DisplayName("Deve permitir os metodos configurados no CORS")
//    void corsConfigurershouldApplyCorrectSettings() {
//        WebMvcConfigurer configurer = securityConfiguration.corsConfigurer();
//
//        CorsRegistry registry = mock(CorsRegistry.class);
//        CorsRegistration registration = mock(CorsRegistration.class);
//
//        when(registry.addMapping("/**")).thenReturn(registration);
//        when(registration.allowedOrigins("http://localhost:5173")).thenReturn(registration);
//        when(registration.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")).thenReturn(registration);
//        when(registration.allowedHeaders("*")).thenReturn(registration);
//        when(registration.allowCredentials(true)).thenReturn(registration);
//
//        configurer.addCorsMappings(registry);
//
//        verify(registry).addMapping("/**");
//        verify(registration).allowedOrigins("http://localhost:5173");
//        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
//        verify(registration).allowedHeaders("*");
//        verify(registration).allowCredentials(true);
//    }
}