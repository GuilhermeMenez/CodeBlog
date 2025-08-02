package blog.code.codeblog.service;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import blog.code.codeblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @DisplayName("Configuração inicial dos mocks para AuthorizationServiceTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve registrar um usuário com sucesso")
    void registerSucessfully() {
        UserDTO userDTO = new UserDTO("Guilherme", "email@test.com", "123456", UserRoles.COSTUMER);
        String encodedPassword = "senhaCriptografada";
        User newUser = new User("Guilherme", "email@test.com", encodedPassword, UserRoles.COSTUMER);
        String token = "tokenDeTeste";

        when(userService.findByLogin(userDTO.email())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.password())).thenReturn(encodedPassword);
        doNothing().when(userService).saveUser(any(User.class));
        when(tokenService.generateToken(any(UserDTO.class))).thenReturn(token);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(newUser);

        String resultToken = authorizationService.register(userDTO);

        assertNotNull(resultToken);
        assertEquals(token, resultToken);

        verify(userService).findByLogin(userDTO.email());
        verify(passwordEncoder).encode(userDTO.password());
        verify(userService).saveUser(any(User.class));
        verify(tokenService).generateToken(any(UserDTO.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um usuário com e-mail já existente")
    void testRegisterWithExistingUserThrowsException() {
        // Arrange
        UserDTO userDTO = new UserDTO("Jane Doe", "janedoe@example.com", "password123", UserRoles.COSTUMER);
        User existingUser = new User("Jane Doe", "janedoe@example.com", "encodedPassword123", UserRoles.COSTUMER);

        when(userService.findByLogin(userDTO.email())).thenReturn(existingUser);

        assertThrows(UsernameNotFoundException.class, () -> authorizationService.register(userDTO));

        verify(userService, never()).saveUser(any(User.class));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, never()).generateToken(any(UserDTO.class));
    }
}