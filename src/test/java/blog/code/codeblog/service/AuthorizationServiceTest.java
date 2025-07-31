package blog.code.codeblog.service;

import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Teste de registro - sucesso no registro")
    public void testRegisterSuccessfulRegistration() {
        UserDTO userDTO = new UserDTO("Test User", "test@example.com", "password", UserRoles.COSTUMER);
        when(userService.findByLogin(userDTO.email())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.password())).thenReturn("encryptedPassword");
        when(tokenService.generateToken(any())).thenReturn("token");

        User user = new User(userDTO.name(), userDTO.email(), "encryptedPassword", userDTO.role());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
                new UsernamePasswordAuthenticationToken(user, null, null)
        );
        String token = authorizationService.register(userDTO);

        assertNotNull(token);
        assertEquals("token", token);
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Teste de registro - email já registrado")
    public void testRegisterEmailAlreadyRegistered() {
        UserDTO userDTO = new UserDTO("Test User", "test@example.com", "password", UserRoles.COSTUMER);
        User existingUser = new User(userDTO.name(), userDTO.email(), "encryptedPassword", userDTO.role());
        when(userService.findByLogin(userDTO.email())).thenReturn(existingUser);

        assertThrows(UsernameNotFoundException.class, () -> authorizationService.register(userDTO));
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Teste de registro - falha na autenticação")
    public void testRegisterAuthenticationFails() {
        UserDTO userDTO = new UserDTO("Test User", "test@example.com", "password", UserRoles.COSTUMER);
        when(userService.findByLogin(userDTO.email())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.password())).thenReturn("encryptedPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authorizationService.register(userDTO));
        verify(userService, times(1)).saveUser(any(User.class));
    }
}