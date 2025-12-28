package blog.code.codeblog.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigurationTest {
    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private SecurityConfiguration securityConfiguration;

    public SecurityConfigurationTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return a BCryptPasswordEncoder instance")
    void passwordEncoderReturnsBCryptInstance() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    @DisplayName("Should return an AuthenticationManager instance")
    void authenticationManagerReturnsInstance() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);
        AuthenticationManager manager = securityConfiguration.authenticationManager(authenticationConfiguration);
        assertNotNull(manager);
        assertEquals(mockManager, manager);
    }

}