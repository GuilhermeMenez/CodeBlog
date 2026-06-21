package blog.code.codeblog.service;


import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;

class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserService userService;

    @BeforeEach
    @DisplayName("Initial mock setup for AuthorizationServiceTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);
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