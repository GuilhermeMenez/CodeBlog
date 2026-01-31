package blog.code.codeblog.service;


import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;

class AuthorizationServiceTest {

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
    @DisplayName("Initial mock setup for AuthorizationServiceTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should register a user successfully")
    void registerSuccessfully() {
        CreateUserDTO createUserDTO = new CreateUserDTO("Guilherme", "email@test.com", "123456", UserRoles.COSTUMER, null);
        String encodedPassword = "encryptedPassword";
        User newUser = new User("Guilherme", "email@test.com", encodedPassword, UserRoles.COSTUMER);
        String token = "testToken";

        when(userService.findByLogin(createUserDTO.email())).thenReturn(null);
        when(passwordEncoder.encode(createUserDTO.password())).thenReturn(encodedPassword);
        doNothing().when(userService).saveUser(any(User.class));
        when(tokenService.generateToken(any(User.class))).thenReturn(token);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(newUser);

        String resultToken = authorizationService.register(createUserDTO);

        assertNotNull(resultToken);
        assertEquals(token, resultToken);

        verify(userService).findByLogin(createUserDTO.email());
        verify(passwordEncoder).encode(createUserDTO.password());
        verify(userService).saveUser(any(User.class));
        verify(tokenService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering a user with an already registered email")
    void registerWithExistingUserThrowsException() {
        CreateUserDTO createUserDTO = new CreateUserDTO("Jane Doe", "janedoe@example.com", "password123", UserRoles.COSTUMER, null);
        User existingUser = new User("Jane Doe", "janedoe@example.com", "encodedPassword123", UserRoles.COSTUMER);

        when(userService.findByLogin(createUserDTO.email())).thenReturn(existingUser);

        assertThrows(IllegalArgumentException.class, () -> authorizationService.register(createUserDTO));

        verify(userService, never()).saveUser(any(User.class));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void loginSuccessfully() {
        User user = new User("Guilherme", "email@test.com", "encryptedPassword", UserRoles.COSTUMER);
        String token = "testToken";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn(token);

        var loginResponse = authorizationService.login(new blog.code.codeblog.dto.authentication.AuthenticationDTO("email@test.com", "123456"));
        assertNotNull(loginResponse);
        assertEquals(token, loginResponse.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).generateToken(user);
    }

    @Test
    @DisplayName("Should throw exception when login fails due to invalid credentials")
    void loginWithInvalidCredentialsThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});
        assertThrows(org.springframework.security.core.AuthenticationException.class, () ->
                authorizationService.login(new blog.code.codeblog.dto.authentication.AuthenticationDTO("email@test.com", "wrongpassword")));
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should logout successfully and blacklist token")
    void logoutSuccessfully() {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(tokenService.recoverToken(request)).thenReturn("tokenToBlacklist");
        doNothing().when(tokenService).blackListToken("tokenToBlacklist");
        when(request.getRemoteUser()).thenReturn("email@test.com");
        authorizationService.logout(request);
        verify(tokenService).recoverToken(request);
        verify(tokenService).blackListToken("tokenToBlacklist");
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsernameSuccessfully() {
        User user = new User("Guilherme", "email@test.com", "encryptedPassword", UserRoles.COSTUMER);
        when(userService.findByLogin("email@test.com")).thenReturn(user);
        var result = authorizationService.loadUserByUsername("email@test.com");
        assertNotNull(result);
        assertEquals(user, result);
        verify(userService).findByLogin("email@test.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found by username")
    void loadUserByUsernameThrowsException() {
        when(userService.findByLogin("notfound@test.com")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () ->
                authorizationService.loadUserByUsername("notfound@test.com"));
        verify(userService).findByLogin("notfound@test.com");
    }
}