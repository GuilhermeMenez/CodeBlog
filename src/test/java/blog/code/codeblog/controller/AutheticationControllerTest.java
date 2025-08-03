package blog.code.codeblog.controller;

import blog.code.codeblog.dto.AuthenticationDTO;
import blog.code.codeblog.dto.LoginResponseDTO;
import blog.code.codeblog.dto.UserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.AuthorizationService;
import blog.code.codeblog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutheticationControllerTest {

    @Spy
    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AutheticationController authenticationController;

    @BeforeEach
    @DisplayName("Configuração inicial do AutheticationControllerTest")
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }


    @Test
    void testRegisterSuccess() {
        UserDTO userDTO = new UserDTO("Test User", "test@example.com", "plainPassword", UserRoles.COSTUMER);

        when(userService.findByLogin("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encryptedPassword");
        doNothing().when(userService).saveUser(any(User.class));

        LoginResponseDTO token = new LoginResponseDTO("mocked-token");
        ResponseEntity<LoginResponseDTO> mockResponse = ResponseEntity.ok(token);
        doReturn(mockResponse).when(authorizationService).login(any(AuthenticationDTO.class));

        ResponseEntity<String> tokenResponse = authenticationController.register(userDTO);

        assertEquals("mocked-token", tokenResponse.getBody());
        verify(userService).saveUser(any(User.class));
    }

}